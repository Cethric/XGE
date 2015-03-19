package com.cethric.xge.engine.scene.object;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.cethric.xge.engine.scene.shader.ShaderProgram;
import com.hackoeur.jglm.Mat4;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Created by blakerogan on 14/03/15.
 */
public class Object implements IRenderable {
    Logger LOGGER = LogManager.getLogger(Object.class);
    private static int objectCount = 0;
    private String modelName = String.format("Model.%03d", objectCount);

    // Physics Stuff
    private RigidBody rigidBody;
    private CollisionShape collisionShape;

    private float[] modelMatrix = new float[16];
    FloatBuffer Mvp = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();

    // Tests Stuff Change Content is Setup and render with this...
    int vertexbuffer;
    int colorbuffer;

    public Object(Vector3f position, Quat4f rotation) {
        collisionShape = new BoxShape(new Vector3f(1, 1, 1));

        DefaultMotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(rotation, position, 1)));
        Vector3f inertia = new Vector3f(0, 0, 0);
        collisionShape.calculateLocalInertia(10f, inertia);
        RigidBodyConstructionInfo rigidBodyCI = new RigidBodyConstructionInfo(10f, motionState, collisionShape);
        rigidBody = new RigidBody(rigidBodyCI);
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
    }

    /**
     * Called every time the frame needs to be rendered.
     * @param V Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param P Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param shaderProgram ShaderProgram; the attached shader program that renders this cube.
     */
    @Override
    public void render(Mat4 V, Mat4 P, ShaderProgram shaderProgram) {
        Mat4 MVP = P.multiply(V).multiply(new Mat4(modelMatrix));
        Mvp.put(MVP.getBuffer());
        Mvp.rewind();
        shaderProgram.usetM4F("MVP", false, Mvp);
        drawCube();
    }

    public void drawCube() {
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, vertexbuffer);
        glVertexAttribPointer(
                0,                  // attribute 0. No particular reason for 0, but must match the layout in the shader.
                3,                  // size
                GL_FLOAT,           // type
                false,           // normalized?
                0,                  // stride
                0            // array buffer offset
        );

        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, colorbuffer);
        glVertexAttribPointer(
                1,                                // attribute. No particular reason for 1, but must match the layout in the shader.
                3,                                // size
                GL_FLOAT,                         // type
                false,                         // normalized?
                0,                                // stride
                0                         // array buffer offset
        );

        // Draw the triangle !
        glDrawArrays(GL_TRIANGLES, 0, 12*3); // 3 indices starting at 0 -> 1 triangle

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

    }

    /**
     * Setup the OpenGL and Bullet content as well as any other content that needs to be setup after creation
     */
    @Override
    public void setup() {
        int VertexArrayID = glGenVertexArrays();
        glBindVertexArray(VertexArrayID);

        FloatBuffer g_vertex_buffer_data = ByteBuffer.allocateDirect(3 * 36 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        g_vertex_buffer_data.put(new float[] {
                -1.0f,-1.0f,-1.0f,
                -1.0f,-1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f,-1.0f,
                -1.0f,-1.0f,-1.0f,
                -1.0f, 1.0f,-1.0f,
                1.0f,-1.0f, 1.0f,
                -1.0f,-1.0f,-1.0f,
                1.0f,-1.0f,-1.0f,
                1.0f, 1.0f,-1.0f,
                1.0f,-1.0f,-1.0f,
                -1.0f,-1.0f,-1.0f,
                -1.0f,-1.0f,-1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f,-1.0f,
                1.0f,-1.0f, 1.0f,
                -1.0f,-1.0f, 1.0f,
                -1.0f,-1.0f,-1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f,-1.0f, 1.0f,
                1.0f,-1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f,-1.0f,-1.0f,
                1.0f, 1.0f,-1.0f,
                1.0f,-1.0f,-1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f,-1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f,-1.0f,
                -1.0f, 1.0f,-1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f,-1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f,-1.0f, 1.0f
        });
        g_vertex_buffer_data.rewind();

        FloatBuffer g_color_buffer_data = ByteBuffer.allocateDirect(3 * 36 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        g_color_buffer_data.put(new float[] {
                0.583f,  0.771f,  0.014f,
                0.609f,  0.115f,  0.436f,
                0.327f,  0.483f,  0.844f,
                0.822f,  0.569f,  0.201f,
                0.435f,  0.602f,  0.223f,
                0.310f,  0.747f,  0.185f,
                0.597f,  0.770f,  0.761f,
                0.559f,  0.436f,  0.730f,
                0.359f,  0.583f,  0.152f,
                0.483f,  0.596f,  0.789f,
                0.559f,  0.861f,  0.639f,
                0.195f,  0.548f,  0.859f,
                0.014f,  0.184f,  0.576f,
                0.771f,  0.328f,  0.970f,
                0.406f,  0.615f,  0.116f,
                0.676f,  0.977f,  0.133f,
                0.971f,  0.572f,  0.833f,
                0.140f,  0.616f,  0.489f,
                0.997f,  0.513f,  0.064f,
                0.945f,  0.719f,  0.592f,
                0.543f,  0.021f,  0.978f,
                0.279f,  0.317f,  0.505f,
                0.167f,  0.620f,  0.077f,
                0.347f,  0.857f,  0.137f,
                0.055f,  0.953f,  0.042f,
                0.714f,  0.505f,  0.345f,
                0.783f,  0.290f,  0.734f,
                0.722f,  0.645f,  0.174f,
                0.302f,  0.455f,  0.848f,
                0.225f,  0.587f,  0.040f,
                0.517f,  0.713f,  0.338f,
                0.053f,  0.959f,  0.120f,
                0.393f,  0.621f,  0.362f,
                0.673f,  0.211f,  0.457f,
                0.820f,  0.883f,  0.371f,
                0.982f,  0.099f,  0.879f
        });
        g_color_buffer_data.rewind();

        vertexbuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexbuffer);
        glBufferData(GL_ARRAY_BUFFER, g_vertex_buffer_data, GL_STATIC_DRAW);

        colorbuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, colorbuffer);
        glBufferData(GL_ARRAY_BUFFER, g_color_buffer_data, GL_STATIC_DRAW);

    }

    /**
     * Remove any content that this object no longer needs as it will be deleted.
     */
    @Override
    public void teardown() {
        LOGGER.debug(String.format("Deleting Object: %s", modelName));
        glDeleteBuffers(vertexbuffer);
        glDeleteBuffers(colorbuffer);
    }
}
