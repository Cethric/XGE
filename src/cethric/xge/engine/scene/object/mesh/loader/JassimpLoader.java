package cethric.xge.engine.scene.object.mesh.loader;

import cethric.xge.engine.scene.object.mesh.Mesh;
import cethric.xge.engine.scene.object.mesh.MeshManager;
import cethric.xge.engine.scene.object.texture.Texture;
import cethric.xge.engine.scene.shader.ShaderProgram;
import com.hackoeur.jglm.Mat4;
import jassimp.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Created by blakerogan on 21/03/15.
 */
public class JassimpLoader {
    public JassimpLoader() {

    }

    public MeshManager loadMesh(String filePath) {
        MeshManager meshManager = new MeshManager();
        try {
            AiScene aiScene = Jassimp.importFile(filePath);
            List<AiMaterial> materials = aiScene.getMaterials();
            System.out.println(aiScene.getNumMeshes());
            for (AiMesh mesh : aiScene.getMeshes()) {
                if (mesh.isPureTriangle()) {
                    final int size = mesh.getNumVertices() * 3;
                    final FloatBuffer g_vertex_buffer_data = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                    final FloatBuffer g_color_buffer_data = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                    final FloatBuffer g_uv_buffer_data = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                    List<Float> uv_data = new ArrayList<Float>();
                    System.out.println("mesh can be used");
                    System.out.println(mesh.toString());
                    IntBuffer index = mesh.getIndexBuffer();
                    int length = index.limit();
                    for (int i = 0; i < length; i++) {
                        AiWrapperProvider wrapperProvider = Jassimp.getWrapperProvider();
                        AiVector vector = (AiVector) mesh.getWrappedPosition(i, wrapperProvider);
                        AiVector uvw = (AiVector) mesh.getWrappedTexCoords(i, 0, wrapperProvider);

                        g_vertex_buffer_data.put(vector.getX());
                        g_vertex_buffer_data.put(vector.getY());
                        g_vertex_buffer_data.put(vector.getZ());

                        g_color_buffer_data.put(new Random().nextFloat());
                        g_color_buffer_data.put(new Random().nextFloat());
                        g_color_buffer_data.put(new Random().nextFloat());

                        if (uvw.getNumComponents() >= 2){
                            try {
                                uv_data.add(uvw.getX());
                                uv_data.add(uvw.getY());
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    System.out.println(uv_data.size());
                    g_vertex_buffer_data.rewind();
                    g_color_buffer_data.rewind();
                    g_uv_buffer_data.rewind();

                    AiMaterial material = materials.get(mesh.getMaterialIndex());

                    System.out.println("Loading texture");
                    String name = material.getTextureFile(AiTextureType.DIFFUSE, 0);
                    File source = new File(name);
                    final Texture texture = new Texture(source);

                    Mesh fMesh = new Mesh() {
                        int vertexbuffer;
                        int colorbuffer;
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
                            System.out.println("Render");
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
                            texture.bind();

                            glDrawArrays(GL_TRIANGLES, 0, size / 3); // 3 indices starting at 0 -> 1 triangle

                            glDisableVertexAttribArray(0);
                            glDisableVertexAttribArray(1);

                            texture.unbind();
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
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return meshManager;
    }
}
