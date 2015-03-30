package cethric.xge.editor;

import cethric.xge.engine.Window;
import cethric.xge.engine.scene.Scene;
import cethric.xge.engine.scene.SceneManager;
import cethric.xge.engine.scene.object.Object;
import cethric.xge.engine.scene.object.camera.Camera;
import cethric.xge.engine.scene.object.mesh.Cube;
import cethric.xge.engine.scene.object.mesh.MeshManager;
import cethric.xge.engine.scene.object.mesh.loader.JassimpLoader;
import cethric.xge.util.XGEUtil;
import com.hackoeur.jglm.Vec3;
import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.errorCallbackDescriptionString;
import static org.lwjgl.glfw.Callbacks.glfwSetCallback;
import static org.lwjgl.glfw.GLFW.glfwInit;

/**
 * Created by blakerogan on 22/02/15.
 */
public class Editor {
    private static final Logger LOGGER = LogManager.getLogger(Editor.class);

    public Editor() {
    }

    public static GLFWErrorCallback glfwErrorCallback() {
        return new GLFWErrorCallback() {
            private final Map<Integer, String> ERROR_CODES = LWJGLUtil.getClassTokens(new LWJGLUtil.TokenFilter() {
                @Override
                public boolean accept(Field field, int value) {
                    return 0x10000 < value && value < 0x20000;
                }
            }, null, GLFW.class);

            @Override
            public void invoke(int error, long description) {
                String msg = errorCallbackDescriptionString(description);
                LOGGER.error(String.format("[LWJGL] %s error",ERROR_CODES.get(error)));
                LOGGER.error("Description: " + msg);
                LOGGER.error("Stacktrace:");
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 4; i < stack.length; i++) {
                    LOGGER.error("\t\t" + stack[i].toString());
                }
            }
        };
    }


    public void startGLFW() {
        GLFWErrorCallback errorCallback;
        glfwSetCallback(errorCallback = glfwErrorCallback());

        LOGGER.debug("Initializing GLFW");
        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        LOGGER.debug("GLFW Initialized");

        LOGGER.debug("Running XGE Editor Version: " + XGEUtil.getVersionString());
        LOGGER.debug("Running XGE Engine Version: " + XGEUtil.getEditorVersionString());
        LOGGER.info("Starting XGE Editor");

        Window window = new Window();

        SceneManager sceneManager = new SceneManager();
        Scene scene = new Scene();
        sceneManager.addScene(scene);
        scene.active(true);
        Quat4f quat4f = new Quat4f();
//        QuaternionUtil.setEuler(quat4f, 10, 10, 20);
//        quat4f.normalize();

        float n = 120f;

        Object object1 = new Object(new Vector3f(n, 50, 0), quat4f);
        object1.setMeshManager(JassimpLoader.loadMesh("../tuna/m_tuna.fbx"));

        Object object2 = new Object(new Vector3f(n, 50, n), quat4f);
        MeshManager meshManager2 = new MeshManager();
        meshManager2.addMesh(new Cube());
        object2.setMeshManager(meshManager2);

        Object object3 = new Object(new Vector3f(-n, 50, n), quat4f);
        MeshManager meshManager3 = new MeshManager();
        meshManager3.addMesh(new Cube());
        object3.setMeshManager(meshManager3);

        Object object4 = new Object(new Vector3f(n, 50, -n), quat4f);
        MeshManager meshManager4 = new MeshManager();
        meshManager4.addMesh(new Cube());
        object4.setMeshManager(meshManager4);

        Object object5 = new Object(new Vector3f(n, 100, n), quat4f);
        MeshManager meshManager5 = new MeshManager();
        meshManager5.addMesh(new Cube());
        object5.setMeshManager(meshManager5);

        scene.addObject(object1);
        scene.addObject(object2);
        scene.addObject(object3);
        scene.addObject(object4);
        scene.addObject(object5);

        Camera camera = new Camera(new Vec3(0, 180, 0), new Vec3(0, 1, 0), 0, 0);
        camera.setActive(true);
        scene.addCamera(camera);

        LOGGER.debug(String.format("Camera is: %s", camera.getActive()));

        XStream xStream = new XStream();
        xStream.alias("SceneManager", SceneManager.class);

        String sceneManagerXML = xStream.toXML(sceneManager);

        File file = new File("../Scene.xml");
        try {
            boolean b = file.createNewFile();
            if (b) {
                OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(file));
                outputStream.write(sceneManagerXML);
                outputStream.close();
                LOGGER.info(String.format("Scene File saved to: %s", file.getAbsolutePath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        window.setSceneManager(sceneManager);
        window.loop();
    }


    public static void main(String[] args) {
        final Editor editor = new Editor();
        editor.startGLFW();
    }
}
