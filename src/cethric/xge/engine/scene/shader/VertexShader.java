package cethric.xge.engine.scene.shader;

import org.lwjgl.opengl.ARBVertexShader;

import java.io.File;
import java.io.IOException;

/**
 * Created by blakerogan on 18/03/15.
 */
public class VertexShader extends IShaderSource {

    public VertexShader(String name, CharSequence source) {
        super(name, source);
    }

    public VertexShader(String name, File file_source) throws IOException {
        super(name, file_source);
    }

    @Override
    public int shaderType() {
        return ARBVertexShader.GL_VERTEX_SHADER_ARB;
    }
}