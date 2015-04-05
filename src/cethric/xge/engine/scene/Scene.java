package cethric.xge.engine.scene;

import cethric.xge.engine.scene.object.Object;
import cethric.xge.engine.scene.object.camera.Camera;
import cethric.xge.engine.scene.object.camera.Light;
import cethric.xge.engine.scene.shader.FragmentShader;
import cethric.xge.engine.scene.shader.ShaderProgram;
import cethric.xge.engine.scene.shader.VertexShader;
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
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE31;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

/**
 * Created by blakerogan on 14/03/15.
 */
public class Scene implements IScene {
    private transient Logger LOGGER = LogManager.getLogger(Scene.class);
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
    private Mat4 Projection = Matrices.perspective(45.0f, 3.0f / 3.0f, 0.1f, 10000000.0f),
            View = Matrices.lookAt(
                    new Vec3(1, 50, 1), // Camera is at (4,3,3), in World Space
                    new Vec3(0, 0, 0), // and looks at the origin
                    new Vec3(0, 1, 0)  // Head is up (set to 0,-1,0 to look upside-down)
            ),
            VP = Projection.multiply(View);

    // Shaders
    private ShaderProgram shaderProgram;
    private ShaderProgram depthShader;
    FloatBuffer Mvp = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private int framebuffer;
    private int depthTextureID;


    public Scene() {
        sceneCount++;
    }

    /**
     * When called this function runs the scene update process
     */
    @Override
    public void render(int width, int height) {
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);
        glViewport(0, 0, 1024, 1024);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        this.depthShader.install();
//        Mat4 depthProjectionMatrix = Matrices.ortho(-10, 10, -10, 10, -10, 20);
        Mat4 depthProjectionMatrix = Matrices.perspective(45.0f, 1.0f, 0.01f, 10000f);
//        Mat4 depthViewMatrix = Matrices.lookAt(new Vec3(0.5f, 2.0f, 2.0f), new Vec3(0, 0, 0), new Vec3(0, 1, 0));
        Light test = new Light(new Vec3(10, 700, 0), new Vec3(0, 1, 0), 0, -82f);
        test.update(0l);
        Mat4 depthViewMatrix = test.getView();
        Mat4 depthModelMatrix = Mat4.MAT4_IDENTITY;
        Mat4 DMVP = depthProjectionMatrix.multiply(depthViewMatrix).multiply(depthModelMatrix);
        Mvp.put(DMVP.getBuffer());
        Mvp.rewind();
        depthShader.usetM4F("MVP", false, Mvp);

        for (Object object : objects) {
            object.render(depthViewMatrix, depthProjectionMatrix, depthShader);
        }

//        depthShader.uninstall();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        shaderProgram.install();
        Mat4 biasMatrix = new Mat4(new float[] {
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f});

        Mat4 depthBiasMVP = biasMatrix.multiply(DMVP);
        Mvp.put(depthBiasMVP.getBuffer());
        Mvp.rewind();
        shaderProgram.usetM4F("DepthBiasMVP", false, Mvp);

        glActiveTexture(GL_TEXTURE31);
        glBindTexture(GL_TEXTURE_2D, depthTextureID);
        shaderProgram.uset1I("shadowMap", 31);

        sceneManager.render(width, height);
        for (Object object : objects) {
            object.render(View, Projection, shaderProgram);
//            object.render(depthViewMatrix, depthProjectionMatrix, shaderProgram);
        }

        Mat4 MVP = Projection.multiply(View).multiply(Mat4.MAT4_IDENTITY);
        Mvp.put(MVP.getBuffer());
//        Mvp.put(DMVP.getBuffer());
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
        dynamicsWorld.stepSimulation((float)delta, 10);
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
        dynamicsWorld.setGravity(new Vector3f(0, -9.8f, 0));

        StaticPlaneShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 1);
        DefaultMotionState groundMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, -1, 0), 1f)));
        RigidBodyConstructionInfo groundRigidCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0, 0, 0));
        ground = new RigidBody(groundRigidCI);
        dynamicsWorld.addRigidBody(ground);

        dynamicsWorld.setDebugDrawer(new BulletDebugDraw());

        // Setup Frame Buffer
        VertexShader depthVertexShader = null;
        FragmentShader depthFragmentShader = null;
        try {
            depthVertexShader = new VertexShader("DepthRTTVS", new File("shaders/OpenGL_Tutorial/DepthRTT.vertexshader"));
            depthFragmentShader = new FragmentShader("DepthRTTFS", new File("shaders/OpenGL_Tutorial/DepthRTT.fragmentshader"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.depthShader = new ShaderProgram(depthVertexShader, depthFragmentShader, null);
        this.depthShader.link();
        
        this.framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);

        // Setup the depth texture
        this.depthTextureID = glGenTextures();
        glActiveTexture(GL13.GL_TEXTURE31);
        glBindTexture(GL_TEXTURE_2D, this.depthTextureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16, 1024, 1024, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glTexParameteri(GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, this.depthTextureID, 0);

        glDrawBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer failed to initiate");
        }

        // Color Rendering

        VertexShader vertexShader = null;
        FragmentShader fragmentShader = null;
        try {
            vertexShader = new VertexShader("vs", new File("shaders/shader_basic.vertexshader"));
            fragmentShader = new FragmentShader("fs", new File("shaders/shader_basic.fragmentshader"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.shaderProgram = new ShaderProgram(vertexShader, fragmentShader, null);
        this.shaderProgram.link();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glClearColor(0f, 0f, 0.4f, 1f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);

        glEnable(GL_TEXTURE_2D);
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
        object.setup();
        dynamicsWorld.addRigidBody(object.getRigidBody());
    }

    public void addCamera(Camera camera) {
        cameras.add(camera);
        dynamicsWorld.addRigidBody(camera.getRigidBody());
    }
}
