/**
 * 
 */
package com.perceivedev.perceivecore.gui.component;

import java.util.function.Consumer;

import com.perceivedev.perceivecore.gui.DisplayColor;

/**
 * @author Rayzr
 *
 */
public class Button extends Label {

    /**
     * The code to run when the button is clicked
     */
    protected Consumer<ClickEvent> clickHandler;

    /**
     * Whether or not to close the inventory when the button is clicked
     */
    protected boolean              closeOnClick = true;

    public Button(int x, int y, int width, int height, String name, Consumer<ClickEvent> clickHandler) {
        super(x, y, width, height, name, new String[0]);
        this.clickHandler = clickHandler;
        setColor(DisplayColor.LIME);
    }

    public Button(int x, int y, String name, Consumer<ClickEvent> clickHandler) {
        this(x, y, 1, 1, name, clickHandler);
    }

    public Button(int x, int y, String name) {
        this(x, y, name, null);
    }

    public Button(String name) {
        this(0, 0, name);
    }

    public Button(int x, int y, Consumer<ClickEvent> clickHandler) {
        this(x, y, "Button", clickHandler);
    }

    public Button(Consumer<ClickEvent> clickHandler) {
        this(0, 0, clickHandler);
    }

    public Button(String name, Consumer<ClickEvent> clickHandler) {
        this(0, 0, "Button", clickHandler);
    }

    /**
     * @return the clickHandler
     */
    public Consumer<ClickEvent> getClickHandler() {
        return clickHandler;
    }

    /**
     * @param clickHandler the clickHandler to set
     */
    public void setClickHandler(Consumer<ClickEvent> clickHandler) {
        this.clickHandler = clickHandler;
    }

    /**
     * This calls the {@link #clickHandler} if it is present, and if
     * {@link #closeOnClick} is true then it closes the inventory.
     * 
     * @param e the ClickEvent
     * @see Component#onClick(ClickEvent)
     */
    @Override
    protected void onClick(ClickEvent e) {
        if (clickHandler != null) {
            clickHandler.accept(e);
        }
        if (closeOnClick) {
            e.getPlayer().closeInventory();
        }
    }

    /**
     * @return Whether or not to {@link #closeOnClick}
     */
    public boolean closeOnClick() {
        return closeOnClick;
    }

    /**
     * @param set if it this button should {@link #closeOnClick}
     */
    public void setCloseOnClick(boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
    }

}