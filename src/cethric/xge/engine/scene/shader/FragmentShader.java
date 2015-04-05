package cethric.xge.engine.scene.shader;

import org.lwjgl.opengl.ARBFragmentShader;

import java.io.File;
import java.io.IOException;

/**
 * Created by blakerogan on 18/03/15.
 */
public class FragmentShader extends IShaderSource {

    public FragmentShader(String name, CharSequence source) {
        super(name, source);
    }

    public FragmentShader(String name, File file_source) throws IOException {
        super(name, file_source);
    }

    @Override
    public int shaderType() {
        return ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
    }
}