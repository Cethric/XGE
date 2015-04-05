package cethric.xge.engine.scene.shader;

import org.lwjgl.opengl.ARBGeometryShader4;

import java.io.File;
import java.io.IOException;

/**
 * Created by blakerogan on 18/03/15.
 */
public class GeometryShader extends IShaderSource {

    public GeometryShader(String name, CharSequence source) {
        super(name, source);
    }

    public GeometryShader(String name, File file_source) throws IOException {
        super(name, file_source);
    }

    @Override
    public int shaderType() {
        return ARBGeometryShader4.GL_GEOMETRY_SHADER_ARB;
    }
}
