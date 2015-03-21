package cethric.xge.engine.scene.object.mesh;

import cethric.xge.engine.scene.object.IRenderable;

/**
 * Created by blakerogan on 21/03/15.
 */
public interface IMesh extends IRenderable {

    /**
     * Get the name of the mesh
     * @return String; the name of the mesh
     */
    public String getName();

    /**
     * Set the name of the mesh
     * @param name String; the new name for the mesh
     */
    public void setName(String name);
}
