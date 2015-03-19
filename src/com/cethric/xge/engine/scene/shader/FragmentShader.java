package com.cethric.xge.engine.scene.shader;

import org.lwjgl.opengl.ARBFragmentShader;

/**
 * Created by blakerogan on 18/03/15.
 */
public class FragmentShader extends IShaderSource {

    public FragmentShader(String name, CharSequence source) {
        super(name, source);
    }

    @Override
    public int shaderType() {
        return ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
    }
}