package cethric.xge.engine.scene.object.mesh.loader;

import cethric.xge.engine.scene.object.mesh.Mesh;
import cethric.xge.engine.scene.object.mesh.MeshManager;
import cethric.xge.engine.scene.object.texture.Texture;
import cethric.xge.engine.scene.shader.ShaderProgram;
import cethric.xge.util.MeshUtil;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.util.ObjectArrayList;
import com.hackoeur.jglm.Mat4;
import com.sun.istack.internal.Nullable;
import jassimp.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
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
    private transient static Logger LOGGER = LogManager.getLogger(JassimpLoader.class);
    public JassimpLoader() {
        // Nothing to do here
    }

    public static MeshManager loadMesh(String filePath, @Nullable Texture diff, @Nullable Texture norm, @Nullable Texture spec) {
        MeshManager meshManager = new MeshManager();
        try {
            AiScene aiScene = Jassimp.importFile(filePath);
            List<AiMesh> meshList = aiScene.getMeshes();
            List<AiMaterial> materialList = aiScene.getMaterials();
            for (AiMesh mesh : meshList) {
                final int size = mesh.getNumVertices() * 3;
                final FloatBuffer g_vertex_buffer_data = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                final FloatBuffer g_normal_buffer_data = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                final FloatBuffer g_color_buffer_data = ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                List<Float> uv_data = new ArrayList<Float>();

                final ConvexHullShape convexHullShape = new ConvexHullShape(new ObjectArrayList<Vector3f>());

                int faces = mesh.getNumFaces();
                AiMaterial material = materialList.get(mesh.getMaterialIndex());
                for (int face = 0; face < faces; face++) {
                    int faceIndexSize = mesh.getFaceNumIndices(face);
                    for (int index = 0; index < faceIndexSize; index++) {
                        int vertIndex = mesh.getFaceVertex(face, index);

                        // Vertex
                        AiWrapperProvider wrapperProvider = Jassimp.getWrapperProvider();
                        AiVector vector = (AiVector) mesh.getWrappedPosition(vertIndex, wrapperProvider);
                        float x, y, z;
                        x = vector.getX();
                        y = vector.getY();
                        z = vector.getZ();

                        g_vertex_buffer_data.put(x);
                        g_vertex_buffer_data.put(y);
                        g_vertex_buffer_data.put(z);
//                        convexHullShape.addPoint(new Vector3f(x,y,z));
//                        LOGGER.debug(String.format("X: %s Y: %s Z: %s", x, y, z));

                        //Normal
                        AiVector normal = (AiVector) mesh.getWrappedNormal(vertIndex, wrapperProvider);
                        float nx, ny, nz;
                        nx = normal.getX();
                        ny = normal.getY();
                        nz = normal.getZ();

                        g_normal_buffer_data.put(nx);
                        g_normal_buffer_data.put(ny);
                        g_normal_buffer_data.put(nz);

//                        LOGGER.debug(String.format("NX: %s NY: %s NZ: %s", nx, ny, nz));

                        //Texture Coordinates
                        float u, v;
                        try {
                            u = mesh.getTexCoordU(vertIndex, material.getTextureUVIndex(AiTextureType.DIFFUSE, 0));
                        } catch (IndexOutOfBoundsException e) {
                            u = 0.1f;
                        } catch (IllegalStateException e) {
                            u = 0.1f;
                        }
                        try {
                            v = mesh.getTexCoordV(vertIndex, material.getTextureUVIndex(AiTextureType.DIFFUSE, 0));
                        } catch (IndexOutOfBoundsException e) {
                            v = 0.1f;
                        } catch (IllegalStateException e) {
                            v = 0.1f;
                        }
                        uv_data.add(u);
                        uv_data.add(1-v);
//                        LOGGER.debug(String.format("U: %f V: %f", u, 1-v));

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

                Object[] indexedVertexArray = MeshUtil.indexVertexArray(g_vertex_buffer_data);
                for (Vector3f vector3f : (List<Vector3f>)indexedVertexArray[0]) {
                    convexHullShape.addPoint(vector3f);
                }

                Mesh fMesh = new Mesh() {
                    int vertexbuffer;
                    int normalbuffer;
                    int colorbuffer;
                    int uvbuffer;

                    // Physics
                    private ConvexHullShape collisionShape;

                    @Override
                    public ConvexHullShape getCollisionShape() {
                        return this.collisionShape;
                    }
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
                        if (textureDiff != null) {
                            textureDiff.bind();
                            shaderProgram.uset1I("DiffuseTexture", textureDiff.getTextureUnit());
                        }
                        if (textureNorm != null) {
                            textureNorm.bind();
                            shaderProgram.uset1I("NormalTexture", textureNorm.getTextureUnit());
                            System.out.println("NormalTexture");
                        }
                        if (textureSpec != null) {
                            textureSpec.bind();
                            shaderProgram.uset1I("SpecTexture", textureSpec.getTextureUnit());
                            System.out.println("SpecTexture");
                        }

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
                        glBindBuffer(GL_ARRAY_BUFFER, normalbuffer);
                        glVertexAttribPointer(
                                3,                                // attribute. No particular reason for 1, but must match the layout in the shader.
                                3,                                // size
                                GL11.GL_FLOAT,                         // type
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

                        normalbuffer = glGenBuffers();
                        glBindBuffer(GL_ARRAY_BUFFER, normalbuffer);
                        glBufferData(GL_ARRAY_BUFFER, g_normal_buffer_data, GL_STATIC_DRAW);

                        colorbuffer = glGenBuffers();
                        glBindBuffer(GL_ARRAY_BUFFER, colorbuffer);
                        glBufferData(GL_ARRAY_BUFFER, g_color_buffer_data, GL_STATIC_DRAW);

                        uvbuffer = glGenBuffers();
                        glBindBuffer(GL_ARRAY_BUFFER, uvbuffer);
                        glBufferData(GL_ARRAY_BUFFER, g_uv_buffer_data, GL_STATIC_DRAW);

                        collisionShape = convexHullShape;

                    }

                    /**
                     * Remove any content that this object no longer needs as it will be deleted.
                     */
                    @Override
                    public void teardown() {

                    }
                };
                if (diff != null) {
                    fMesh.textureDiff = diff;
                } else {
                    if (material.getNumTextures(AiTextureType.DIFFUSE) > 0) {
                        String diffFile = material.getTextureFile(AiTextureType.DIFFUSE, 0);
                        try {
                            System.out.printf("Loading Diff Texture: %s%n", diffFile);
                            fMesh.textureDiff = Texture.LoadTexture(new File(diffFile), AiTextureType.DIFFUSE);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }
                if (material.getNumTextures(AiTextureType.SPECULAR) > 0) {
                    String specFile = material.getTextureFile(AiTextureType.SPECULAR, 0);
                    System.out.println(specFile);
                    try {
                        System.out.printf("Loading Spec Texture: %s%n", specFile);
                        fMesh.textureSpec = Texture.LoadTexture(new File(specFile), AiTextureType.SPECULAR);
                    } catch (Exception e) {
                        continue;
                    }
                }
                if (material.getNumTextures(AiTextureType.NORMALS) > 0) {
                    String normFile = material.getTextureFile(AiTextureType.NORMALS, 0);
                    System.out.println(normFile);
                    try {
                        System.out.printf("Loading Normal Texture: %s%n", normFile);
                        fMesh.textureNorm = Texture.LoadTexture(new File(normFile), AiTextureType.NORMALS);
                    } catch (Exception e) {
                        continue;
                    }
                }
                fMesh.setName('"' + new File(filePath).getName() + mesh.getName() + '"');
                meshManager.addMesh(fMesh);
                System.out.printf("Loaded Mesh: %s%n", fMesh.getName());

            }

        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return meshManager;
    }
}
