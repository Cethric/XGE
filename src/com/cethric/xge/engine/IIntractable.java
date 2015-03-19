package com.cethric.xge.engine;

/**
 * Created by blakerogan on 22/02/15.
 */
public interface IIntractable {
    // Mouse
    /**
     * Called when any mouse button is pressed
     * @param x double; the mouse x position
     * @param y double; the mouse y position
     * @param button int; the mouse button pressed
     * @param mods int; any modifier keys that are down
     */
    public void mousePress(double x, double y, int button, int mods);

    /**
     * Called when any mouse button is released
     * @param x double; the mouse x position
     * @param y double; the mouse y position
     * @param button int; the mouse button pressed
     * @param mods int; any modifier keys that are down
     */
    public void mouseRelease(double x, double y, int button, int mods);

    /**
     * Called when ever the mouse moves
     * @param x double; the x position of the mouse
     * @param y double; the y position of the mouse
     * @param dx double; the distance of the x position of the mouse moved since last change
     * @param dy double; the distance of the y position of the mouse moved since last change
     */
    public void mouseMove(double x, double y, double dx, double dy);

    /**
     * called when the user moves the scroll wheel
     * @param x double; mouse x position
     * @param y double; mouse y position
     * @param sx double; the x axis offset
     * @param sy double; the y axis offset
     */
    public void mouseScroll(double x, double y, double sx, double sy);

    // Key
    /**
     * called when any key is pressed
     * @param key int; the key pressed
     * @param mods int; the modifier bit mask
     */
    public void keyPress(int key, int mods);
    /**
     * called when any key is released
     * @param key int; the key released
     * @param mods int; the modifier bit mask
     */
    public void keyRelease(int key, int mods);
}
