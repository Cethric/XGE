package com.cethric.xge.engine.scene.shader;

import org.lwjgl.opengl.ARBGeometryShader4;

/**
 * Created by blakerogan on 18/03/15.
 */
public class GeometryShader extends IShaderSource {

    public GeometryShader(String name, CharSequence source) {
        super(name, source);
    }

    @Override
    public int shaderType() {
        return ARBGeometryShader4.GL_GEOMETRY_SHADER_ARB;
    }
}
