package cethric.xge.engine.scene.object.texture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
            LOGGER.debug("Loading Texture: " + source.getAbsolutePath());
            LOGGER.debug("File status (true exists else not there: " + source.exists());
            BufferedImage image = ImageIO.read(source);
            LOGGER.debug("Image loaded adding to memory");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] imagearray = byteArrayOutputStream.toByteArray();
            ByteBuffer buffer = ByteBuffer.allocateDirect(imagearray.length * Byte.BYTES).order(ByteOrder.nativeOrder());
            buffer.put(imagearray);
            buffer.rewind();
            LOGGER.debug("texture loaded into memory");

            glEnable(textureTarget);
            glActiveTexture(activeTexture);
            glBindTexture(textureTarget, textureID);

            glTexParameteri(textureTarget, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(textureTarget, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
            glTexImage2D(textureTarget, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            LOGGER.debug("Texture Loaded");

        } catch (IOException e) {
            e.printStackTrace();
        }

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
