package com.cethric.xge.engine.scene.shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by blakerogan on 16/03/15.
 */
public class ShaderProgram {
    private VertexShader vertexShader;
    private FragmentShader fragmentShader;
    private GeometryShader geometryShader;
    private int program;

    private HashMap<String, Integer> u_loc = new HashMap<String, Integer>();
    private HashMap<String, Integer> v_loc = new HashMap<String, Integer>();

    public ShaderProgram(VertexShader vertexShader, FragmentShader fragmentShader, GeometryShader geometryShader) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        this.geometryShader = geometryShader;
    }

    public void destroy() {
        if (this.program != 0) {
            glDeleteShader(this.program);
        }
    }

    public void setShader(Object shader) {
        if (shader instanceof VertexShader) {
            vertexShader = (VertexShader) shader;
        }
        if (shader instanceof FragmentShader) {
            fragmentShader = (FragmentShader) shader;
        }
        if (shader instanceof GeometryShader) {
            geometryShader = (GeometryShader) shader;
        }
    }

    public int link() throws RuntimeException {
        if (vertexShader != null) {
            vertexShader.compile();
        }
        if (fragmentShader != null) {
            fragmentShader.compile();
        }
        if (geometryShader != null) {
            geometryShader.compile();
        }

        program = glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Failed to create shader program object");
        }
        if (vertexShader != null) {
            vertexShader.attachTo(program);
        }
        if (fragmentShader != null) {
            fragmentShader.attachTo(program);
        }
        if (geometryShader != null) {
            geometryShader.attachTo(program);
        }
        glLinkProgram(program);

        String status = glGetShaderInfoLog(program);
        if (status.length() > 0) {
            throw new RuntimeException(String.format("Failed to link shader\n%s", status));
        }
        return this.program;
    }

    public int prog() {
        if (this.program!=0) return this.program;
        return this.link();
    }

    public void install() {
        if (program != 0) {
            glUseProgram(program);
        }
    }

    public void uninstall() {
        glUseProgram(0);
    }

    public int uniformLoc(String name) {
        if (u_loc.containsKey(name)) {
            return (int) u_loc.get(name);
        } else {
            if (program == 0) {
                this.link();
            }
            int v = glGetUniformLocation(program, name);
            u_loc.put(name, v);
            return v;
        }
    }

    /**
     * Loads a vec4 value into a uniform variable of the program object that is currently in use.
     *
     * @param name String; the uniform variable name
     * @param x float; the uniform x value
     */
    public void uset1F(String name, float x) {
        glUniform1f(uniformLoc(name), x);
    }

    /**
     * Loads a vec4 value into a uniform variable of the program object that is currently in use.
     *
     * @param name String; the uniform variable name
     * @param x float; the uniform x value
     * @param y float; the uniform y value
     */
    public void uset2F(String name, float x, float y) {
        glUniform2f(uniformLoc(name), x, y);
    }

    /**
     * Loads a vec4 value into a uniform variable of the program object that is currently in use.
     *
     * @param name String; the uniform variable name
     * @param x float; the uniform x value
     * @param y float; the uniform y value
     * @param z float; the uniform z value
     */
    public void uset3F(String name, float x, float y, float z) {
        glUniform3f(uniformLoc(name), x, y, z);
    }

    /**
     * Loads a vec4 value into a uniform variable of the program object that is currently in use.
     *
     * @param name String; the uniform variable name
     * @param x float; the uniform x value
     * @param y float; the uniform y value
     * @param z float; the uniform z value
     * @param w float; the uniform w value
     */
    public void uset4F(String name, float x, float y, float z, float w) {
        glUniform4f(uniformLoc(name), x, y, z, w);
    }

    /**
     * Loads a vec4 value into a uniform variable of the program object that is currently in use.
     *
     * @param name String; the uniform variable name
     * @param x int; the uniform x value
     */
    public void uset1I(String name, int x) {
        glUniform1i(uniformLoc(name), x);
    }

    /**
     * Loads a vec4 value into a uniform variable of the program object that is currently in use.
     *
     * @param name String; the uniform variable name
     * @param x int; the uniform x value
     * @param y int; the uniform y value
     * @param z int; the uniform z value
     */
    public void uset3I(String name, int x, int y, int z) {
        glUniform3i(uniformLoc(name), x, y, z);
    }

    /**
     * Loads a vec4 value into a uniform variable of the program object that is currently in use.
     *
     * @param name String; the uniform variable name
     * @param transpose boolean; (false) specifies column major order or, row major order row
     * @param matrix float[]; the matrix source to be applied
     */
    public void usetM4F(String name, boolean transpose, float[] matrix) {
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(matrix);
        floatBuffer.rewind();
        usetM4F(name, transpose, floatBuffer);
    }

    /**
     * Loads a vec4 value into a uniform variable of the program object that is currently in use.
     *
     * @param name String; the uniform variable name
     * @param transpose boolean; (false) specifies column major order or, row major order row
     * @param matrix FloatBuffer; the matrix source to be applied
     */
    public void usetM4F(String name, boolean transpose, FloatBuffer matrix) {
        glUniformMatrix4(uniformLoc(name), transpose, matrix);
    }
}
