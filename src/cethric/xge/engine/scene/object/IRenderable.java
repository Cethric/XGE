package cethric.xge.engine.scene.object;

import cethric.xge.engine.scene.shader.ShaderProgram;
import com.hackoeur.jglm.Mat4;

/**
 * Created by blakerogan on 14/03/15.
 */
public interface IRenderable {
    /**
     * Called on every update tick
     * @param delta long; time since last update
     */
    public void update(long delta);

    /**
     * Called every time the frame needs to be rendered.
     * @param V Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param P Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param shaderProgram ShaderProgram; the attached shader program that renders this cube.
     */
    public void render(Mat4 V, Mat4 P, ShaderProgram shaderProgram);

    /**
     * Setup the OpenGL and Bullet content as well as any other content that needs to be setup after creation
     */
    public void setup();

    /**
     * Remove any content that this object no longer needs as it will be deleted.
     */
    public void teardown();
}
