package cethric.xge.engine.scene;

import cethric.xge.engine.IIntractable;

/**
 * Created by blakerogan on 14/03/15.
 */
public interface ISceneManager extends IIntractable {
    /**
     * When called this function runs the scene update process
     */
    public void render();

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
}
