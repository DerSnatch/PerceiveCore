package com.perceivedev.perceivecore.nbt;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.perceivedev.perceivecore.reflection.ReflectionUtil;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.MethodPredicate;

import static com.perceivedev.perceivecore.reflection.ReflectionUtil.NameSpace.NMS;
import static com.perceivedev.perceivecore.reflection.ReflectionUtil.NameSpace.OBC;


/**
 * Allows modification of TileEntity data
 * <p>
 * The methods must only be called when at least one world is loaded, as it
 * needs to spawn a sample entity (ArmorStand). <br>
 * It will be enforced by throwing an {@link IllegalStateException}.
 * <p>
 * <br>
 * <i><b>DISCLAIMER: </b></i> <br>
 * Doesn't allow for the addition of new tags. You can modify the tags of the
 * TileEntity, but not add new ones. This is a limitation of minecraft.
 */
@SuppressWarnings("unused")
public class TileEntityNBTUtil {

    private static final Logger LOGGER = Logger.getLogger("TileEntityNBTUtil");

    private static Method loadFromNBT, saveToNBT, getTileEntity;
    private static boolean error = false;

    private static final Class<?> CRAFT_BLOCK_STATE_CLASS;

    static {
        CRAFT_BLOCK_STATE_CLASS = ReflectionUtil.getClass(OBC, "block.CraftBlockState").orElse(null);

        if (CRAFT_BLOCK_STATE_CLASS == null) {
            LOGGER.warning("CraftBlockState not found. Version: "
                    + Bukkit.getBukkitVersion() + " " + Bukkit.getVersion());
            error = true;
        }

        ReflectionUtil.ReflectResponse<Method> tileEntityMethod = ReflectionUtil.getMethod(CRAFT_BLOCK_STATE_CLASS,
                new MethodPredicate()
                        .withName("getTileEntity").withParameters());

        if (!tileEntityMethod.isValuePresent()) {
            LOGGER.warning("getTileEntity not found. Version: "
                    + Bukkit.getBukkitVersion() + " " + Bukkit.getVersion());
            error = true;
        }
        else {
            getTileEntity = tileEntityMethod.getValue();
            initializeMethods();
        }
    }

    /**
     * Gets the NMS handle of a bukkit entity
     *
     * @param blockState The Bukkit {@link BlockState}
     *
     * @return The NMS tile entity
     *
     * @throws IllegalStateException if {@link #ensureNoError()} throws it
     */
    private static Object toTileEntity(BlockState blockState) {
        ensureNoError();
        return ReflectionUtil.invokeMethod(getTileEntity, blockState).getValue();
    }

    /**
     * Returns the {@link NBTWrappers.NBTTagCompound} of a {@link BlockState}
     *
     * @param blockState The Bukkit {@link BlockState} to get an
     * {@link NBTWrappers.NBTTagCompound} for.
     *
     * @return The {@link NBTWrappers.NBTTagCompound} of the {@link BlockState}
     */
    @SuppressWarnings("WeakerAccess")   // others may want to call that...
    public static NBTWrappers.NBTTagCompound getNbtTag(BlockState blockState) {
        Objects.requireNonNull(blockState, "blockState can not be null");
        ensureCorrectClass(blockState);
        ensureNoError();

        Object tileEntity = toTileEntity(blockState);

        Object nbtTag = new NBTWrappers.NBTTagCompound().toNBT();

        // populate it
        ReflectionUtil.invokeMethod(saveToNBT, tileEntity, nbtTag);

        return (NBTWrappers.NBTTagCompound) NBTWrappers.INBTBase.fromNBT(nbtTag);
    }

    /**
     * Sets the {@link NBTWrappers.NBTTagCompound} of a {@link BlockState}.
     * <p>
     * Changes will appear.
     *
     * @param blockState The Bukkit {@link BlockState} to get an
     * {@link NBTWrappers.NBTTagCompound} for.
     * @param compound The {@link NBTWrappers.NBTTagCompound} to set it to
     *
     * @throws NullPointerException     If blockState or compound is null
     * @throws IllegalArgumentException If {@link #isValidClass(BlockState)}
     *                                  returns false
     * @throws IllegalStateException    If an unrepairable error occurred earlier
     *                                  (probably version incompatibility).
     */
    @SuppressWarnings("WeakerAccess")   // others may want to call that...
    public static void setNbtTag(BlockState blockState, NBTWrappers.NBTTagCompound compound) {
        Objects.requireNonNull(blockState, "blockState can not be null");
        Objects.requireNonNull(compound, "compound can not be null");
        ensureCorrectClass(blockState);
        ensureNoError();

        Object tileEntity = toTileEntity(blockState);

        ReflectionUtil.invokeMethod(loadFromNBT, tileEntity, compound.toNBT());

        // maybe unneeded
        blockState.update();
    }

    /**
     * Appends the {@link NBTWrappers.NBTTagCompound} to the Nbt tag of a
     * {@link BlockState}
     * .
     * <p>
     * Changes will appear.
     *
     * @param blockState The Bukkit {@link BlockState} to get an
     * {@link NBTWrappers.NBTTagCompound} for.
     * @param compound The {@link NBTWrappers.NBTTagCompound} to set it to
     *
     * @throws NullPointerException     If blockState or compound is null
     * @throws IllegalArgumentException If {@link #isValidClass(BlockState)}
     *                                  returns false
     * @throws IllegalStateException    If an unrepairable error occurred earlier
     *                                  (probably version incompatibility).
     */
    public static void appendNbtTag(BlockState blockState, NBTWrappers.NBTTagCompound compound) {
        Objects.requireNonNull(blockState, "blockState can not be null");
        Objects.requireNonNull(compound, "compound can not be null");
        ensureCorrectClass(blockState);
        ensureNoError();

        Object tileEntity = toTileEntity(blockState);

        NBTWrappers.NBTTagCompound tileNBT = getNbtTag(blockState);
        for (Entry<String, NBTWrappers.INBTBase> entry : compound.getAllEntries().entrySet()) {
            tileNBT.set(entry.getKey(), entry.getValue());
        }

        ReflectionUtil.invokeMethod(loadFromNBT, tileEntity, tileNBT.toNBT());

        // maybe unneeded
        blockState.update();
    }

    /**
     * Checks whether you can pass the {@link BlockState} to the
     * {@link #getNbtTag(BlockState)}
     * or
     * {@link #setNbtTag(BlockState, NBTWrappers.NBTTagCompound)}
     * methods
     *
     * @param blockState The Bukkit {@link BlockState} to check
     *
     * @return True if the {@link BlockState} has a TileEntity
     *
     * @throws IllegalStateException If an unrepairable error occurred earlier
     *                               (probably version incompatibility).
     */
    @SuppressWarnings("WeakerAccess")   // others may want to call that...
    public static boolean isValidClass(BlockState blockState) {
        ensureNoError();
        // no NPE will be thrown.
        return !CRAFT_BLOCK_STATE_CLASS.equals(blockState.getClass());
    }

    /**
     * @param state The {@link BlockState} to check. Non null.
     *
     * @throws IllegalArgumentException If the state doesn't have a
     *                                  TileEntity
     */
    private static void ensureCorrectClass(BlockState state) {
        if (!isValidClass(state)) {
            throw new IllegalArgumentException("The state is not a TileEntity. Valid is e.g. a Chest or a Furnace.");
        }
    }

    /**
     * @throws IllegalStateException If {@link #error} is true
     */
    private static void ensureNoError() {
        if (error) {
            throw new IllegalStateException("A critical, non recoverable error occurred earlier.");
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void initializeMethods() {
        if (Bukkit.getWorlds().isEmpty()) {
            throw new IllegalStateException("Called me before at least one world was loaded...");
        }
        Block block = Bukkit.getWorlds().get(0).getBlockAt(Bukkit.getWorlds().get(0).getSpawnLocation());
        BlockState oldState = block.getState();
        block.setType(Material.CHEST);
        BlockState chestState = block.getState();

        Object tileEntity = ReflectionUtil.invokeMethod(getTileEntity, chestState).getValue();

        if (ReflectionUtil.getMajorVersion() > 2 || ReflectionUtil.getMinorVersion() > 9) {
            initializeMethodsAfter1_9(ReflectionUtil.getClass(NMS, "TileEntity").get(), chestState, tileEntity);
        }
        else {
            initializeMethodsBefore1_9(ReflectionUtil.getClass(NMS, "TileEntity").get(), chestState, tileEntity);
        }

        if (loadFromNBT == null || saveToNBT == null) {
            LOGGER.warning("Null: "
                    + " load " + (loadFromNBT == null)
                    + " save " + (saveToNBT == null)
                    + ". Version: " + Bukkit.getBukkitVersion() + " " + Bukkit.getVersion());
            error = true;
        }

        // restore old
        oldState.update(true);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void initializeMethodsAfter1_9(Class<?> tileEntityClass, BlockState blockState,
                                                  Object tileEntity) {

        // the load method is the same
        initializeMethodsBefore1_9(tileEntityClass, blockState, tileEntity);

        // search the save method
        for (Method method : tileEntityClass.getMethods()) {
            if (method.getReturnType().equals(ReflectionUtil.getClass(NMS, "NBTTagCompound").get())
                    && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].equals(ReflectionUtil.getClass(NMS, "NBTTagCompound").get())
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())) {

                Object testCompound = new NBTWrappers.NBTTagCompound().toNBT();
                ReflectionUtil.invokeMethod(method, tileEntity, testCompound);

                NBTWrappers.NBTTagCompound compound = (NBTWrappers.NBTTagCompound) NBTWrappers.INBTBase.fromNBT
                        (testCompound);
                if (compound == null || compound.isEmpty()) {
                    continue;
                }

                if (saveToNBT != null) {
                    LOGGER.warning("Found more than one save method. Version: "
                            + Bukkit.getBukkitVersion() + " " + Bukkit.getVersion());
                    error = true;
                    return;
                }
                saveToNBT = method;
            }
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void initializeMethodsBefore1_9(Class<?> tileEntityClass, BlockState blockState,
                                                   Object tileEntity) {
        for (Method method : tileEntityClass.getMethods()) {
            if (method.getReturnType().equals(Void.TYPE)
                    && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].equals(ReflectionUtil.getClass(NMS, "NBTTagCompound").get())
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())) {

                Object testCompound = new NBTWrappers.NBTTagCompound().toNBT();
                ReflectionUtil.invokeMethod(method, tileEntity, testCompound);

                NBTWrappers.NBTTagCompound compound = (NBTWrappers.NBTTagCompound) NBTWrappers.INBTBase.fromNBT
                        (testCompound);
                if (compound == null) {
                    continue;
                }

                if (compound.isEmpty()) {
                    // load method
                    if (loadFromNBT != null) {
                        LOGGER.warning("Found more than one load method. Version: "
                                + Bukkit.getBukkitVersion() + " " + Bukkit.getVersion());
                        error = true;
                        return;
                    }
                    loadFromNBT = method;
                }
                else {
                    // save method
                    if (saveToNBT != null) {
                        LOGGER.warning("Found more than one save method. Version: "
                                + Bukkit.getBukkitVersion() + " " + Bukkit.getVersion());
                        error = true;
                        return;
                    }
                    saveToNBT = method;
                }
            }
        }
    }
}
