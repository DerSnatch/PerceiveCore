package com.perceivedev.perceivecore.gui.components.base.pane;

import java.util.Collection;
import java.util.Optional;

import com.perceivedev.perceivecore.gui.components.base.component.Component;

/** The base class for Panes. */
public interface Pane extends Component {

    /**
     * Adds a {@link Component}
     *
     * @param component The {@link Component} to add
     *
     * @return <code>true</code> if the component could be added
     */
    boolean addComponent(Component component);

    /**
     * Removes a {@link Component}
     *
     * @param component The {@link Component} to remove
     *
     * @return <code>true</code> if the {@link Component} was removed
     */
    boolean removeComponent(Component component);

    /**
     * Checks if the Pane contains the {@link Component}.
     *
     * @param component The {@link Component}
     *
     * @return True if it contains the component
     */
    boolean containsComponent(Component component);

    /**
     * Returns all children of this pane
     *
     * @return The children of this Pane
     */
    Collection<Component> getChildren();

    /**
     * Gets a component in a given slot
     *
     * @param x The x coordinate of the slot
     * @param y The y coordinate of the slot
     *
     * @return The component at this slot
     */
    Optional<Component> getComponentAtPoint(int x, int y);

    /**
     * Re-renders this pane
     *
     * @return <code>true</code> if it was re-rendered
     */
    boolean requestReRender();

    @Override
    Pane deepClone();
}