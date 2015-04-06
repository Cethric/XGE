package cethric.xge.engine.scene.object.camera;

import cethric.xge.engine.scene.object.mesh.Cube;
import cethric.xge.engine.scene.object.mesh.Mesh;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.hackoeur.jglm.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by blakerogan on 5/04/15.
 */
public class Light implements ICamera {
    private transient Logger LOGGER = LogManager.getLogger(Light.class);

    private boolean active = false;
    private Vec3 position;
    private Vec3 front;
    private Vec3 up;
    private Vec3 right;
    private Vec3 worldUp;
    private float yaw;
    private float pitch;

    private Mesh cameraMesh;

    public Light(Vec3 position, Vec3 up, float yaw, float pitch) {
        front = new Vec3(0, 0, -1);
        this.position = position;
        this.worldUp = up;
        this.yaw = yaw;
        this.pitch = pitch;
        update(0l);
        cameraMesh = new Cube();
    }
    /**
     * called on every frame update
     *
     * @param delta long; the time since last update
     */
    @Override
    public void update(long delta) {
        this.front = new Vec3(
                (float)(Math.cos(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch))),
                (float)(Math.sin(Math.toRadians(this.pitch))),
                (float)(Math.sin(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch)))
        );
        this.right = this.front.cross(this.worldUp);
        this.up = this.right.cross(this.front);
    }

    /**
     * Get the current view matrix
     *
     * @return Mat4; the view matrix
     */
    @Override
    public Mat4 getView() {
        return Matrices.lookAt(this.position, this.position.add(this.front), this.up);
    }

    /**
     * Sets if this camera is currently active or not.
     *
     * @param active boolean; the active state of this camera
     */
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * return is this camera is currently active
     *
     * @return boolean; Is the camera active.
     */
    @Override
    public boolean getActive() {
        return this.active;
    }

    /**
     * Called when any mouse button is pressed
     *
     * @param x      double; the mouse x position
     * @param y      double; the mouse y position
     * @param button int; the mouse button pressed
     * @param mods   int; any modifier keys that are down
     */
    @Override
    public void mousePress(double x, double y, int button, int mods) {

    }

    /**
     * Called when any mouse button is released
     *
     * @param x      double; the mouse x position
     * @param y      double; the mouse y position
     * @param button int; the mouse button pressed
     * @param mods   int; any modifier keys that are down
     */
    @Override
    public void mouseRelease(double x, double y, int button, int mods) {

    }

    /**
     * Called when ever the mouse moves
     *
     * @param x  double; the x position of the mouse
     * @param y  double; the y position of the mouse
     * @param dx double; the distance of the x position of the mouse moved since last change
     * @param dy double; the distance of the y position of the mouse moved since last change
     */
    @Override
    public void mouseMove(double x, double y, double dx, double dy) {

    }

    /**
     * called when the user moves the scroll wheel
     *
     * @param x  double; mouse x position
     * @param y  double; mouse y position
     * @param sx double; the x axis offset
     * @param sy double; the y axis offset
     */
    @Override
    public void mouseScroll(double x, double y, double sx, double sy) {

    }

    /**
     * called when any key is pressed
     *
     * @param key  int; the key pressed
     * @param mods int; the modifier bit mask
     */
    @Override
    public void keyPress(int key, int mods) {

    }

    /**
     * called when any key is released
     *
     * @param key  int; the key released
     * @param mods int; the modifier bit mask
     */
    @Override
    public void keyRelease(int key, int mods) {

    }
}
