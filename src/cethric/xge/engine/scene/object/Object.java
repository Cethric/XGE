package cethric.xge.engine.scene.object;

import cethric.xge.engine.scene.object.mesh.Mesh;
import cethric.xge.engine.scene.object.mesh.MeshManager;
import cethric.xge.engine.scene.shader.ShaderProgram;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.hackoeur.jglm.Mat4;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by blakerogan on 14/03/15.
 */
public class Object implements IRenderable {
    private transient Logger LOGGER = LogManager.getLogger(Object.class);
    private static int objectCount = 0;
    private String modelName = String.format("Model.%03d", objectCount);

    // Physics Stuff
    private RigidBody rigidBody;
    private CompoundShape collisionShape;
    private DefaultMotionState initialMotionState;

    private float[] modelMatrix = new float[16];
    FloatBuffer Mvp = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
    FloatBuffer m = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
    FloatBuffer v = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();

    // Mesh Manager and mesh rendering
    private MeshManager meshManager;

    public Object(Vector3f position, Quat4f rotation) {
        collisionShape = new CompoundShape(); //(new Vector3f(50, 50, 50));
        initialMotionState = new DefaultMotionState(new Transform(new Matrix4f(rotation, position, 1)));
        objectCount++;

        meshManager = new MeshManager();
    }

    public RigidBody getRigidBody() {
        return rigidBody;
    }

    /**
     * Called on every update tick
     *
     * @param delta long; time since last update
     */
    @Override
    public void update(long delta) {
        Transform transform = new Transform();
        rigidBody.getWorldTransform(transform);
        transform.getOpenGLMatrix(modelMatrix);

        meshManager.update(delta);
    }

    /**
     * Called every time the frame needs to be rendered.
     * @param V Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param P Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param shaderProgram ShaderProgram; the attached shader program that renders this cube.
     */
    @Override
    public void render(Mat4 V, Mat4 P, ShaderProgram shaderProgram) {
        Mat4 M = new Mat4(modelMatrix);
        m.put(M.getBuffer());
        m.rewind();
        v.put(V.getBuffer());
        v.rewind();
        Mat4 MVP = P.multiply(V).multiply(M);
        Mvp.put(MVP.getBuffer());
        Mvp.rewind();
        shaderProgram.usetM4F("MVP", false, Mvp);
        shaderProgram.usetM4F("M", false, m);
        shaderProgram.usetM4F("V", false, v);
        meshManager.render(V, P, shaderProgram);
    }

    /**
     * Setup the OpenGL and Bullet content as well as any other content that needs to be setup after creation
     */
    @Override
    public void setup() {
        meshManager.setup();
        for (Mesh mesh : meshManager.getMeshs()) {
            LOGGER.debug(String.format("Added %s's Collision Shape: %s to: %s", mesh.getName(), mesh.getCollisionShape(), this.modelName));
            collisionShape.addChildShape(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 0), 1.0f)), mesh.getCollisionShape());
        }

        Vector3f inertia = new Vector3f();
        collisionShape.calculateLocalInertia(10f, inertia);
        RigidBodyConstructionInfo rigidBodyCI = new RigidBodyConstructionInfo(10f, initialMotionState, collisionShape, inertia);
        rigidBody = new RigidBody(rigidBodyCI);
    }

    /**
     * Remove any content that this object no longer needs as it will be deleted.
     */
    @Override
    public void teardown() {
        LOGGER.debug(String.format("Deleting Object: %s", modelName));
        meshManager.teardown();
    }

    public MeshManager getMeshManager() {
        return meshManager;
    }

    public void setMeshManager(MeshManager meshManager) {
        this.meshManager = meshManager;
    }
}
