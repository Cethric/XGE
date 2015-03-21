package cethric.xge.engine.scene.object.texture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.io.File;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Created by blakerogan on 19/03/15.
 */
public class Texture {
    private static int textureCount = 0;
    private String textureName = String.format("Texture.%03d", textureCount);

    private int textureTarget = GL11.GL_TEXTURE_2D;
    private int textureID;
    private int activeTexture = GL13.GL_TEXTURE0;

    public Texture(File source) {
        textureCount++;
        textureName = source.getName();
        init();
    }

    public void init() {
        textureID = glGenTextures();
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
