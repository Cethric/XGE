package cethric.xge.engine.scene.shader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by blakerogan on 18/03/15.
 */
public abstract class IShaderSource {
    private transient Logger LOGGER = LogManager.getLogger(IShaderSource.class);
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

    public IShaderSource(String name, File file_source) throws IOException {
        this.name = name;
        FileInputStream fis = null;
        InputStreamReader isr = null;

        char[] inputBuffer = null;
        String file_content = null;

        try {

            if (file_source.exists()) {
                // Probably you will get an exception if its
                // a huge content file..
                // I suggest you to handle content here
                // itself, instead of
                // returning it as return value..
                inputBuffer = new char[(int) file_source.length()];

                fis = new FileInputStream(file_source);

                isr = new InputStreamReader(fis);
                isr.read(inputBuffer);

                file_content = new String(inputBuffer);

                try {
                    isr.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            file_content = null;
        }

        this.source = file_content;
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
