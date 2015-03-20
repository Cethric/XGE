package cethric.xge.engine;

/**
 * Created by blakerogan on 22/02/15.
 */
public interface IWindow extends IIntractable {
    // Window
    /**
     * called when the window changes size
     * @param width int; the new window width
     * @param height int; the new window height
     */
    public void windowResize(int width, int height);

    /**
     * called when the window is about to close
     */
    public void windowClose();

    /**
     * called when the window comes into focus
     */
    public void windowFocus();

    /**
     * called when the window is no longer in focus
     */
    public void windowUnfocus();

    /**
     * called when the window is iconified
     */
    public void windowIconify();

    /**
     * called when the window is brought out of iconification
     */
    public void windowRestore();

    /**
     * called when the user moves the window
     * @param x int; the new window x position
     * @param y int; the new window y position
     */
    public void windowMove(int x, int y);

    /**
     * called when the window requires a refresh, such as when a portion of the window is revealed after being covered
     */
    public void windowRefresh();

    /**
     * called when the mouse enters the window
     */
    public void mouseEnter();

    /**
     * called when the mouse leaves the window
     */
    public void mouseLeave();

    // XGE Engine
    /**
     * called every time a new frame is required
     */
    public void render();

    /**
     * called every time the frame is required to be updated
     * @param delta long; time since last update
     */
    public void update(long delta);
}
