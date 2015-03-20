package cethric.xge.engine.scene;

import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.IDebugDraw;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Created by blakerogan on 14/03/15.
 */
public class BulletDebugDraw extends IDebugDraw {
    private Logger LOGGER = LogManager.getLogger(BulletDebugDraw.class);
    private int debugMode = DebugDrawModes.DRAW_AABB | DebugDrawModes.DRAW_WIREFRAME | DebugDrawModes.DRAW_CONTACT_POINTS | DebugDrawModes.DRAW_TEXT | DebugDrawModes.DRAW_FEATURES_TEXT;
    @Override
    public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
        int VertexArrayID = glGenVertexArrays();
        glBindVertexArray(VertexArrayID);

        FloatBuffer g_vertex_buffer_data = ByteBuffer.allocateDirect(3 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        g_vertex_buffer_data.put(new float[] {
                from.x, from.y, from.z,
                to.x, to.y, to.z
        });
        g_vertex_buffer_data.rewind();

        FloatBuffer g_color_buffer_data = ByteBuffer.allocateDirect(3 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        g_color_buffer_data.put(new float[] {
                color.x, color.y, color.z,
                color.x, color.y, color.z
        });
        g_color_buffer_data.rewind();

        int vertexbuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexbuffer);
        glBufferData(GL_ARRAY_BUFFER, g_vertex_buffer_data, GL_STATIC_DRAW);

        int colorbuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, colorbuffer);
        glBufferData(GL_ARRAY_BUFFER, g_color_buffer_data, GL_STATIC_DRAW);

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
        glDrawArrays(GL_LINES, 0, 12*3); // 3 indices starting at 0 -> 1 triangle

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glDeleteBuffers(vertexbuffer);
        glDeleteBuffers(colorbuffer);
    }

    @Override
    public void drawContactPoint(Vector3f PointOnB, Vector3f normalOnB, float distance, int lifeTime, Vector3f color) {

    }

    @Override
    public void reportErrorWarning(String warningString) {
        LOGGER.debug(warningString);
    }

    @Override
    public void draw3dText(Vector3f location, String textString) {
        LOGGER.debug(textString);
    }

    @Override
    public void setDebugMode(int debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public int getDebugMode() {
        return debugMode;
    }
}
