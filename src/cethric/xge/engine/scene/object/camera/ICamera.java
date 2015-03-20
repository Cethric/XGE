package cethric.xge.engine.scene.object.camera;

import cethric.xge.engine.IIntractable;
import com.hackoeur.jglm.Mat4;

/**
 * Created by blakerogan on 15/03/15.
 */
public interface ICamera extends IIntractable {
    /**
     * called on every frame update
     * @param delta long; the time since last update
     */
    public void update(long delta);

    /**
     * Get the current view matrix
     * @return Mat4; the view matrix
     */
    public Mat4 getView();

    /**
     * Sets if this camera is currently active or not.
     * @param active boolean; the active state of this camera
     */
    public void setActive(boolean active);

    /**
     * return is this camera is currently active
     * @return boolean; Is the camera active.
     */
    public boolean getActive();
}
