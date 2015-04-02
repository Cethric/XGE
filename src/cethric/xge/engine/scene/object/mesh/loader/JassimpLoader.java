package cethric.xge.engine.scene.object.mesh.loader;

import cethric.xge.engine.scene.object.mesh.Mesh;
import cethric.xge.engine.scene.object.mesh.MeshManager;
import cethric.xge.engine.scene.object.texture.Texture;
import cethric.xge.engine.scene.shader.ShaderProgram;
import com.hackoeur.jglm.Mat4;
import jassimp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Created by blakerogan on 21/03/15.
 */
public class JassimpLoader {
    private static Logger LOGGER = LogManager.getLogger(JassimpLoader.class);
    public JassimpLoader() {

    }

    public static MeshManager loadMesh(String filePath) {
        MeshManager meshManager = new MeshManager();
        try {
            AiScene aiScene = Jassimp.importFile(filePath);
            List<AiMesh> meshList = aiScene.getMeshes();
            List<AiMaterial> materialList = aiScene.getMaterials();
            for (AiMesh mesh : meshList) {
                final int size = mesh.getNumVertices() * 3;
                final FloatBuffer g_vertex_buffer_data = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                final FloatBuffer g_color_buffer_data = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                List<Float> uv_data = new ArrayList<Float>();

                int faces = mesh.getNumFaces();
                AiMaterial material = materialList.get(mesh.getMaterialIndex());
                String matFile = material.getTextureFile(AiTextureType.DIFFUSE, 0);
                final Texture texture = new Texture(new File(matFile));
                for (int face = 0; face < faces; face++) {
                    int faceIndexSize = mesh.getFaceNumIndices(face);
                    for (int index = 0; index < faceIndexSize; index++) {
                        int vertIndex = mesh.getFaceVertex(face, index);
                        AiWrapperProvider wrapperProvider = Jassimp.getWrapperProvider();
                        AiVector vector = (AiVector) mesh.getWrappedPosition(vertIndex, wrapperProvider);
                        float x, y, z;
                        x = vector.getX();
                        y = vector.getY();
                        z = vector.getZ();

                        g_vertex_buffer_data.put(x);
                        g_vertex_buffer_data.put(y);
                        g_vertex_buffer_data.put(z);

                        LOGGER.debug(String.format("X: %s Y: %s Z: %s", x, y, z));
                        try {
                            AiVector uvCoords = (AiVector) mesh.getWrappedTexCoords(index, material.getTextureUVIndex(AiTextureType.DIFFUSE, 0), wrapperProvider);
                            LOGGER.debug("Index: " + index + " VertIndex: " + vertIndex);
                            float u, v, w;
                            try {
                                u = mesh.getTexCoordU(vertIndex, material.getTextureUVIndex(AiTextureType.DIFFUSE, 0)); //uvCoords.getX();
                            } catch (IndexOutOfBoundsException e) {
                                u = 0.1f;
                            }
                            try {
                                v = mesh.getTexCoordV(vertIndex, material.getTextureUVIndex(AiTextureType.DIFFUSE, 0)); //uvCoords.getY();
                            } catch (IndexOutOfBoundsException e) {
                                v = 0.1f;
                            }
                            //try {
                            //    w = mesh.getTexCoordW(vertIndex, material.getTextureUVIndex(AiTextureType.DIFFUSE, 0)); //uvCoords.getZ();
                            //} catch (IndexOutOfBoundsException e) {
                            //    w = 0.1f;
                            //} catch (UnsupportedOperationException e1) {
                            //    w = 0.1f;
                            //}
                            uv_data.add(u);
                            uv_data.add(v);
//                            uv_data.add(w);
                            LOGGER.debug(String.format("U: %f V: %f", u, v));
                        } catch (IndexOutOfBoundsException e) {
                            LOGGER.debug(String.format("Error Occurred at Index: %d VertIndex: %d", index, vertIndex));
                            LOGGER.error(e.getLocalizedMessage(), e);
                        }

                        g_color_buffer_data.put(new Random().nextFloat());
                        g_color_buffer_data.put(new Random().nextFloat());
                        g_color_buffer_data.put(new Random().nextFloat());
                    }
                }

                g_vertex_buffer_data.rewind();
                g_color_buffer_data.rewind();
                final FloatBuffer g_uv_buffer_data = ByteBuffer.allocateDirect(uv_data.size() * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                for (Float f : uv_data) {
                    g_uv_buffer_data.put(f);
                }
                g_uv_buffer_data.rewind();

                LOGGER.debug(g_vertex_buffer_data);

                Mesh fMesh = new Mesh() {
                    int vertexbuffer;
                    int colorbuffer;
                    int uvbuffer;
                    /**
                     * Called on every update tick
                     *
                     * @param delta long; time since last update
                     */
                    @Override
                    public void update(long delta) {

                    }

                    /**
                     * Called every time the frame needs to be rendered.
                     *
                     * @param V             Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
                     * @param P             Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
                     * @param shaderProgram ShaderProgram; the attached shader program that renders this cube.
                     */
                    @Override
                    public void render(Mat4 V, Mat4 P, ShaderProgram shaderProgram) {
                        texture.bind();
                        shaderProgram.uset1I("myTextureSampler", 1);
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

                        glEnableVertexAttribArray(2);
                        glBindBuffer(GL_ARRAY_BUFFER, uvbuffer);
                        glVertexAttribPointer(
                                2,                                // attribute. No particular reason for 1, but must match the layout in the shader.
                                2,                                // size
                                GL11.GL_FLOAT,                         // type
                                false,                         // normalized?
                                0,                                // stride
                                0                         // array buffer offset
                        );

                        // Draw the triangle !

                        glDrawArrays(GL11.GL_TRIANGLES, 0, size / 3); // 3 indices starting at 0 -> 1 triangle

                        glDisableVertexAttribArray(0);
                        glDisableVertexAttribArray(1);
                        glDisableVertexAttribArray(2);

//                        texture.unbind();
                    }

                    /**
                     * Setup the OpenGL and Bullet content as well as any other content that needs to be setup after creation
                     */
                    @Override
                    public void setup() {
                        int VertexArrayID = glGenVertexArrays();
                        glBindVertexArray(VertexArrayID);

                        vertexbuffer = glGenBuffers();
                        glBindBuffer(GL_ARRAY_BUFFER, vertexbuffer);
                        glBufferData(GL_ARRAY_BUFFER, g_vertex_buffer_data, GL_STATIC_DRAW);

                        colorbuffer = glGenBuffers();
                        glBindBuffer(GL_ARRAY_BUFFER, colorbuffer);
                        glBufferData(GL_ARRAY_BUFFER, g_color_buffer_data, GL_STATIC_DRAW);

                        uvbuffer = glGenBuffers();
                        glBindBuffer(GL_ARRAY_BUFFER, uvbuffer);
                        glBufferData(GL_ARRAY_BUFFER, g_uv_buffer_data, GL_STATIC_DRAW);
                    }

                    /**
                     * Remove any content that this object no longer needs as it will be deleted.
                     */
                    @Override
                    public void teardown() {

                    }
                };
                meshManager.addMesh(fMesh);

            }

        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return meshManager;
    }
}
