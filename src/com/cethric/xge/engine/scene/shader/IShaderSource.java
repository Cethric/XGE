package com.cethric.xge.engine.scene.shader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by blakerogan on 18/03/15.
 */
public abstract class IShaderSource {
    private Logger LOGGER = LogManager.getLogger(IShaderSource.class);
    private String name;
    private CharSequence source;
    private int shaderID;
    private boolean compiling = false;
    private int tag = -1;
    private List<IShaderSource> dependencies = new ArrayList<IShaderSource>();

    public IShaderSource(String name, CharSequence source) {
        this.name = name;
        this.source = source;
    }

    private void _compile() throws RuntimeException {
        if (this.shaderID > 0)
            return;
        if (this.compiling)
            return;

        this.compiling = true;
        LOGGER.debug(shaderType());
        this.shaderID = glCreateShader(shaderType());
        if (this.shaderID == 0) {
            throw new RuntimeException(String.format("Shader: %s Could not be create", this.name));
        }

        glShaderSource(this.shaderID, this.source);
        glCompileShader(this.shaderID);
        this.compiling = false;

        String log = glGetShaderInfoLog(this.shaderID);
        if (log.length() > 0) {
            throw new RuntimeException(String.format("Could not compile shader: %s\n%s", this.name, log));
        }
    }

    private void _attachTo(int program) {
        if (isCompiled())
                glAttachShader(program, this.shaderID);
    }

    public void compile() {
        this._compile();
    }

    public void attachTo(int program) {
        this._attachTo(program);
    }

    public int shaderType() {
        throw new NotImplementedException();
    }

    public boolean isCompiled() {
        return this.shaderID != 0;
    }
}
