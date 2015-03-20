package cethric.xge.engine.scene.shader;

import org.lwjgl.opengl.ARBVertexShader;

/**
 * Created by blakerogan on 18/03/15.
 */
public class VertexShader extends IShaderSource {

    public VertexShader(String name, CharSequence source) {
        super(name, source);
    }

    @Override
    public int shaderType() {
        return ARBVertexShader.GL_VERTEX_SHADER_ARB;
    }
}