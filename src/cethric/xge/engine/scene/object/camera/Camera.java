package cethric.xge.engine.scene.object.camera;

import cethric.xge.util.GameConfig;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.hackoeur.jglm.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Arrays;

/**
 * Created by blakerogan on 15/03/15.
 */
public class Camera implements ICamera {
    private Logger LOGGER = LogManager.getLogger(Camera.class);

    private boolean active = false;
    private Vec3 position;
    private Vec3 front;
    private Vec3 up;
    private Vec3 right;
    private Vec3 worldUp;
    private float yaw;
    private float pitch;
    private float movementSpeed = 0.01f;
    private float mouseSensitivity = 0.05f;
    private int[] strafe = new int[] {0, 0};

    // Physics Stuff
    private RigidBody rigidBody;
    private CapsuleShape collisionShape;


    public Camera(Vec3 position, Vec3 up, float yaw, float pitch) {
        float mass = 100f;
//        collisionShape = new BoxShape(new Vector3f(1, 1, 1));
        collisionShape = new CapsuleShape(0.5f, 2f);
        Quat4f rotation = new Quat4f();
        QuaternionUtil.setEuler(rotation, -(float)Math.toRadians(yaw), (float)Math.toRadians(pitch), 0);
        DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(rotation, new Vector3f(position.getX(), position.getY(), position.getZ()), 1)));
        Vector3f inertia = new Vector3f(0, 0, 0);
        collisionShape.calculateLocalInertia(mass, inertia);
        RigidBodyConstructionInfo rigidBodyCI = new RigidBodyConstructionInfo(mass, motionState, collisionShape, inertia);
        rigidBody = new RigidBody(rigidBodyCI);
        rigidBody.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

        front = new Vec3(0, 0, -1);
        this.position = position;
        this.worldUp = up;
        this.yaw = yaw;
        this.pitch = pitch;
        update(0l);
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
        this.yaw -= (float)(dx * mouseSensitivity);
        this.pitch += (float)(dy * mouseSensitivity);

        if (this.pitch > 89f) {
            this.pitch = 89f;
        }
        if (this.pitch < -89f) {
            this.pitch = -89f;
        }
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
        mods = 0;
        int[] keyPress = new int[] {key, mods};
        if (Arrays.equals(keyPress, GameConfig.FORWARD_KEY)) {
            LOGGER.debug("Walking Forward");
            strafe[0] += 1;
        } else if (Arrays.equals(keyPress, GameConfig.BACKWARD_KEY)) {
            LOGGER.debug("Walking Backward");
            strafe[0] -= 1;
        } else if (Arrays.equals(keyPress, GameConfig.LEFT_KEY)) {
            LOGGER.debug("Strafing Left");
            strafe[1] += 1;
        } else if (Arrays.equals(keyPress, GameConfig.RIGHT_KEY)) {
            LOGGER.debug("Strafing Right");
            strafe[1] -= 1;
        }
    }

    /**
     * called when any key is released
     *
     * @param key  int; the key released
     * @param mods int; the modifier bit mask
     */
    @Override
    public void keyRelease(int key, int mods) {
        mods = 0;
        int[] keyPress = new int[] {key, mods};
        if (Arrays.equals(keyPress, GameConfig.FORWARD_KEY)) {
            LOGGER.debug("No Longer Walking Forward");
            strafe[0] -= 1;
        }else if (Arrays.equals(keyPress, GameConfig.BACKWARD_KEY)) {
            LOGGER.debug("No Longer Walking Forward");
            strafe[0] += 1;
        } else if (Arrays.equals(keyPress, GameConfig.LEFT_KEY)) {
            LOGGER.debug("No Longer Strafing Left");
            strafe[1] -= 1;
        } else if (Arrays.equals(keyPress, GameConfig.RIGHT_KEY)) {
            LOGGER.debug("No Longer Strafing Right");
            strafe[1] += 1;
        }
    }

    /**
     * called on every frame update
     *
     * @param delta long; the time since last update
     */
    @Override
    public void update(long delta) {
        Transform nt = new Transform();
        rigidBody.getWorldTransform(nt);
        Vec3 npos = new Vec3(nt.origin.x, nt.origin.y+2, nt.origin.z);
        this.position = npos;

        float dt = delta / 1000f;
        float speed = dt * movementSpeed;
        if (strafe[0] != 0) {
            Vec3 start = this.position.add(this.front.multiply(speed * strafe[0]));
            Vec3 check = new Vec3(start.getX(), npos.getY(), start.getZ());
            this.position = check;
        }
        if (strafe[1] != 0) {
            Vec3 start = this.position.subtract(this.right.multiply(speed * strafe[1]));
            Vec3 check = new Vec3(start.getX(), npos.getY(), start.getZ());
            this.position = check;
        }

        nt.origin.x = this.position.getX();
//        nt.origin.y = nt.origin.y;
        nt.origin.z = this.position.getZ();
        Quat4f rot = new Quat4f();
        QuaternionUtil.setEuler(rot, -(float)Math.toRadians(this.yaw), 0, 0);
        nt.setRotation(rot);
        rigidBody.setWorldTransform(nt);

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
        return active;
    }

    public RigidBody getRigidBody() {
        return rigidBody;
    }
}
