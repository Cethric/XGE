package cethric.xge.engine.scene.object.texture;

import de.matthiasmann.twl.utils.PNGDecoder;
import jassimp.AiTextureType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Created by blakerogan on 19/03/15.
 */
public class Texture {
    private transient Logger LOGGER = LogManager.getLogger(Texture.class);

    private static int textureCount = 0;
    private String textureName = String.format("Texture.%03d", textureCount);

    private static HashMap<String, Texture> loadedTextures = new HashMap<String, Texture>();

    private int textureTarget = GL11.GL_TEXTURE_2D;
    private int textureID;
    public int activeTexture = GL13.GL_TEXTURE0;
    private int textureUnit = 0;
    private File source;
    private AiTextureType textureType;

    public static Texture LoadTexture(File source, AiTextureType textureType) throws IOException {
        String name = source.getName();
        if (loadedTextures.containsKey(name)) {
            return loadedTextures.get(name);
        }
        return new Texture(source, textureType);
    }

    public Texture(File source, AiTextureType textureType) throws IOException {
        textureCount++;
        textureName = source.getName();
        this.source = source;
        this.textureType = textureType;
        init();
    }

    public void init() throws IOException {
        activeTexture++;
//        activeTexture%=GL13.GL_MAX_TEXTURE_UNITS;

        textureID = glGenTextures();
        InputStream inputStream = new FileInputStream("shapes/textures/" + source.getName());
        PNGDecoder decoder = new PNGDecoder(inputStream);
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight()); //.order(ByteOrder.nativeOrder());
        decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
        buffer.flip();

        glEnable(GL_TEXTURE_2D);
        if (textureType == AiTextureType.DIFFUSE)
            glActiveTexture(GL13.GL_TEXTURE0);
        else if (textureType == AiTextureType.NORMALS)
            glActiveTexture(GL13.GL_TEXTURE1);
        else if (textureType == AiTextureType.SPECULAR)
            glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
        if (textureType == AiTextureType.DIFFUSE)
            textureUnit = 0;
        else if (textureType == AiTextureType.NORMALS)
            textureUnit = 1;
        else if (textureType == AiTextureType.SPECULAR)
            textureUnit = 2;
        LOGGER.debug(String.format("Texture: %s Loaded. Texture Unit: %d", textureName, textureUnit));
        loadedTextures.put(getTextureName(), this);
    }

    public int getTextureID() {
        return this.textureID;
    }

    public String getTextureName() {
        return this.textureName;
    }

    public int getTextureUnit() {
        return this.textureUnit;
    }

    public void bind() {
        if (textureType == AiTextureType.DIFFUSE)
            glActiveTexture(GL13.GL_TEXTURE0);
        else if (textureType == AiTextureType.NORMALS)
            glActiveTexture(GL13.GL_TEXTURE1);
        else if (textureType == AiTextureType.SPECULAR)
            glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
//        glDisable(GL_TEXTURE_2D);
    }

}
