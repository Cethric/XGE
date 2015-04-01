package cethric.xge.engine.scene.object.texture;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Created by blakerogan on 19/03/15.
 */
public class Texture {
    private Logger LOGGER = LogManager.getLogger(Texture.class);

    private static int textureCount = 0;
    private String textureName = String.format("Texture.%03d", textureCount);

    private int textureTarget = GL11.GL_TEXTURE_2D;
    private int textureID;
    private int activeTexture = GL13.GL_TEXTURE0;
    private File source;

    public Texture(File source) {
        textureCount++;
        textureName = source.getName();
        this.source = source;
        init();
    }

    public void init() {
        textureID = glGenTextures();
        try {
            InputStream inputStream = new FileInputStream("../tuna/" + source.getName());
            PNGDecoder decoder = new PNGDecoder(inputStream);
            ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight()); //.order(ByteOrder.nativeOrder());
            decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            buffer.flip();

            glEnable(textureTarget);
            glActiveTexture(activeTexture);
            glBindTexture(textureTarget, textureID);

            glTexParameteri(textureTarget, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(textureTarget, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            glTexParameteri(textureTarget, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameteri(textureTarget, GL_TEXTURE_WRAP_T, GL_CLAMP);

            glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
            glTexImage2D(textureTarget, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            glBindTexture(textureTarget, 0);
            LOGGER.debug("Texture Loaded");

        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

    }

    public int getTextureID() {
        return this.textureID;
    }

    public void bind() {
        glEnable(textureTarget);
        glActiveTexture(activeTexture);
        glBindTexture(textureTarget, textureID);
    }

    public void unbind() {
        glBindTexture(textureTarget, 0);
        glDisable(textureTarget);
    }

}
