package cethric.xge.engine.scene.object.mesh;

import cethric.xge.engine.scene.object.texture.Texture;
import cethric.xge.engine.scene.shader.ShaderProgram;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.hackoeur.jglm.Mat4;

import javax.vecmath.Vector3f;

/**
 * Created by blakerogan on 21/03/15.
 */
public abstract class Mesh implements IMesh {
    static int meshCount = 0;
    private String meshName = String.format("Mesh.%03d", meshCount);

    //Physics
    private CollisionShape collisionShape;

    //Textures
    public Texture textureSpec;
    public Texture textureNorm;
    public Texture textureDiff;

    public Mesh() {
        meshCount++;
        collisionShape = new BoxShape(new Vector3f(50, 50, 50));
    }

    public CollisionShape getCollisionShape() {
        return this.collisionShape;
    }

    /**
     * Get the name of the mesh
     *
     * @return String; the name of the mesh
     */
    @Override
    public String getName() {
        return meshName;
    }

    /**
     * Set the name of the mesh
     *
     * @param name String; the new name for the mesh
     */
    @Override
    public void setName(String name) {
        this.meshName = name;
    }

    /**
     * Called on every update tick
     *
     * @param delta long; time since last update
     */
    @Override
    public abstract void update(long delta);

    /**
     * Called every time the frame needs to be rendered.
     *
     * @param V             Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param P             Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param shaderProgram ShaderProgram; the attached shader program that renders this cube.
     */
    @Override
    public abstract void render(Mat4 V, Mat4 P, ShaderProgram shaderProgram);

    /**
     * Setup the OpenGL and Bullet content as well as any other content that needs to be setup after creation
     */
    @Override
    public abstract void setup();

    /**
     * Remove any content that this object no longer needs as it will be deleted.
     */
    @Override
    public abstract void teardown();
}
