package cethric.xge.engine.scene;

import cethric.xge.engine.scene.shader.FragmentShader;
import cethric.xge.engine.scene.shader.VertexShader;
import cethric.xge.engine.scene.object.Object;
import cethric.xge.engine.scene.object.camera.Camera;
import cethric.xge.engine.scene.shader.ShaderProgram;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.hackoeur.jglm.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by blakerogan on 14/03/15.
 */
public class Scene implements IScene {
    private Logger LOGGER = LogManager.getLogger(Scene.class);
    private static int sceneCount = 0;
    private boolean active = false;
    private boolean registered = false;
    private SceneManager sceneManager = new SceneManager();
    private String sceneName = String.format("Scene.%03d", sceneCount);

    // Physics Stuff
    private DbvtBroadphase broadphase;
    private DefaultCollisionConfiguration collisionConfiguration;
    private CollisionDispatcher dispatcher;
    private SequentialImpulseConstraintSolver solver;
    private DiscreteDynamicsWorld dynamicsWorld;

    private RigidBody ground;

    // Scene Contents
    private List<Object> objects = new ArrayList<Object>();
    private List<Camera> cameras = new ArrayList<Camera>();
    private Mat4 Projection = Matrices.perspective(45.0f, 3.0f / 3.0f, 0.1f, 100.0f),
            View = Matrices.lookAt(
                    new Vec3(1, 50, 1), // Camera is at (4,3,3), in World Space
                    new Vec3(0, 0,0), // and looks at the origin
                    new Vec3(0,1,0)  // Head is up (set to 0,-1,0 to look upside-down)
                    ),
            VP = Projection.multiply(View);

    // Shaders
    private ShaderProgram shaderProgram;
    FloatBuffer Mvp = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();


    public Scene() {
        sceneCount++;
    }

    /**
     * When called this function runs the scene update process
     */
    @Override
    public void render() {
        shaderProgram.install();

        sceneManager.render();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        for (Object object : objects) {
            object.render(View, Projection, shaderProgram);
        }

        Mat4 MVP = Projection.multiply(View).multiply(Mat4.MAT4_IDENTITY);
        Mvp.put(MVP.getBuffer());
        Mvp.rewind();
        shaderProgram.usetM4F("MVP", false, Mvp);

        dynamicsWorld.debugDrawWorld();

        shaderProgram.uninstall();
    }

    /**
     * When called this function updates the scene
     *
     * @param delta long; the time since the last update
     */
    @Override
    public void update(long delta) {
        sceneManager.update(delta);
        dynamicsWorld.stepSimulation((float)delta / 1000);
        for (Object object : objects) {
            object.update(delta);
        }

        for (Camera camera : cameras) {
            camera.update(delta);
            if (camera.getActive()) {
                View = camera.getView();
            }
        }
    }

    /**
     * This function is used to set the scenes width and height
     *
     * @param width  int; the new width
     * @param height int; the new height
     */
    @Override
    public void resize(int width, int height) {
        sceneManager.resize(width, height);
    }

    /**
     * Registers the scene so it can be rendered. Allows for pre-game rendering and initialization
     */
    @Override
    public void register() {
        registered = true;
        broadphase = new DbvtBroadphase();
        collisionConfiguration = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(collisionConfiguration);
        solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, -10f, 0));

        StaticPlaneShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 1);
        DefaultMotionState groundMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, -1, 0), 1f)));
        RigidBodyConstructionInfo groundRigidCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0, 0, 0));
        ground = new RigidBody(groundRigidCI);
        dynamicsWorld.addRigidBody(ground);

        dynamicsWorld.setDebugDrawer(new BulletDebugDraw());

        CharSequence vertex_shader = "" +
                "#version 330 core\n" +
                "\n" +
                "// Input vertex data, different for all executions of this shader.\n" +
                "layout(location = 0) in vec3 vertexPosition_modelspace;\n" +
                "layout(location = 1) in vec3 vertexColor;\n" +
                "\n" +
                "// Output data ; will be interpolated for each fragment.\n" +
                "out vec3 fragmentColor;\n" +
                "// Values that stay constant for the whole mesh.\n" +
                "uniform mat4 MVP;\n" +
                "\n" +
                "void main(){\t\n" +
                "\n" +
                "\t// Output position of the vertex, in clip space : MVP * position\n" +
                "\tgl_Position =  MVP * vec4(vertexPosition_modelspace,1);\n" +
                "\n" +
                "\t// The color of each vertex will be interpolated\n" +
                "\t// to produce the color of each fragment\n" +
                "\tfragmentColor = vertexColor;\n" +
                "}\n" +
                "\n";


        CharSequence fragment_shader = "" +
                "#version 330 core\n" +
                "\n" +
                "// Interpolated values from the vertex shaders\n" +
                "in vec3 fragmentColor;\n" +
                "\n" +
                "// Ouput data\n" +
                "out vec3 color;\n" +
                "\n" +
                "void main(){\n" +
                "\n" +
                "\t// Output color = color specified in the vertex shader, \n" +
                "\t// interpolated between all 3 surrounding vertices\n" +
                "\tcolor = fragmentColor;\n" +
                "\n" +
                "}";

        VertexShader vertexShader = new VertexShader("vs", vertex_shader);
        FragmentShader fragmentShader = new FragmentShader("fs", fragment_shader);
        this.shaderProgram = new ShaderProgram(vertexShader, fragmentShader, null);
        this.shaderProgram.link();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glClearColor(0f, 0f, 0.4f, 1f);
    }

    /**
     * Unregister the scene and deallocate the memory that was used in this scene.
     */
    @Override
    public void unregister() {
        registered = false;
        sceneManager.close();
        broadphase = null;
        collisionConfiguration = null;
        dispatcher = null;
        solver = null;
        dynamicsWorld.destroy();
        ground.destroy();

        shaderProgram.destroy();

        for (Object object : objects) {
            object.teardown();
        }
    }

    /**
     * return is the scene is registered see <code>register</code> and <code>unregister</code>
     *
     * @return boolean;
     */
    @Override
    public boolean registered() {
        return registered;
    }

    /**
     * Set weather the scene is active (true) or not (false) is the scene is not active, mouse and key input is ignored.
     *
     * @param active boolean; set scene state
     */
    @Override
    public void active(boolean active) {
        this.active = active;
    }

    /**
     * return is the scene is currently active.
     *
     * @return boolean;
     */
    @Override
    public boolean active() {
        return active;
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
        sceneManager.mousePress(x, y, button, mods);
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
        sceneManager.mouseRelease(x, y, button, mods);
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
        sceneManager.mouseMove(x, y, dx, dy);
        for (Camera camera : cameras) {
            if (camera.getActive()) {
                camera.mouseMove(x, y, dx, dy);
            }
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
        sceneManager.mouseScroll(x, y, sx, sy);
    }

    /**
     * called when any key is pressed
     *
     * @param key  int; the key pressed
     * @param mods int; the modifier bit mask
     */
    @Override
    public void keyPress(int key, int mods) {
        sceneManager.keyPress(key, mods);
        for (Camera camera : cameras) {
            if (camera.getActive()) {
                camera.keyPress(key, mods);
            }
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
        sceneManager.keyRelease(key, mods);
        for (Camera camera : cameras) {
            if (camera.getActive()) {
                camera.keyRelease(key, mods);
            }
        }
    }

    /**
     * Set the scene manager for this window
     * @param sceneManager SceneManager; The set manager for this window.
     */
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public void addObject(Object object) {
        objects.add(object);
        dynamicsWorld.addRigidBody(object.getRigidBody());
        object.setup();
    }

    public void addCamera(Camera camera) {
        cameras.add(camera);
        dynamicsWorld.addRigidBody(camera.getRigidBody());
    }
}
