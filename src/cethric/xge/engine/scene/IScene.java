package cethric.xge.engine.scene;

import cethric.xge.engine.IIntractable;

/**
 * Created by blakerogan on 14/03/15.
 */
public interface IScene extends IIntractable {
    /**
     * When called this function runs the scene update process
     */
    public void render(int width, int height);

    /**
     * When called this function updates the scene
     * @param delta long; the time since the last update
     */
    public void update(long delta);

    /**
     * This function is used to set the scenes width and height
     * @param width int; the new width
     * @param height int; the new height
     */
    public void resize(int width, int height);

    /**
     * Registers the scene so it can be rendered. Allows for pre-game rendering and initialization
     */
    public void register();

    /**
     * Unregister the scene and deallocate the memory that was used in this scene.
     */
    public void unregister();

    /**
     * return is the scene is registered see <code>register</code> and <code>unregister</code>
     * @return boolean;
     */
    public boolean registered();

    /**
     * Set weather the scene is active (true) or not (false) is the scene is not active, mouse and key input is ignored.
     * @param active boolean; set scene state
     */
    public void active(boolean active);

    /**
     * return is the scene is currently active.
     * @return boolean;
     */
    public boolean active();
}
