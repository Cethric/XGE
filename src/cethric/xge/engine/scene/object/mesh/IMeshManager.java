package cethric.xge.engine.scene.object.mesh;

import cethric.xge.engine.scene.object.IRenderable;

import java.util.List;

/**
 * Created by blakerogan on 21/03/15.
 */
public interface IMeshManager extends IRenderable {
    /**
     * Adds a new mesh to the rendering list of the manager.
     * @param mesh Mesh; the new mesh to add
     */
    public void addMesh(Mesh mesh);

    /**
     * Remove a mesh from the render list of this manager.
     * @param mesh Mesh; the mesh to remove
     */
    public void removeMesh(Mesh mesh);

    /**
     * Return the mesh at id 'x'
     * @param id int; the id/index of the mesh to return
     * @return Mesh; the mesh that was at the id/index 'x'
     */
    public Mesh getMesh(int id);

    /**
     * @return ArrayList&lt;Mesh&gt;; the array containing all the meshs
     */
    public List<Mesh> getMeshs();

    /**
     * Return if this MeshManger is animated and so should render each mesh on a new frame.
     * @return boolean; if the MeshManager is animated
     */
    public boolean isAnimated();

    /**
     * Set the animation state of the manager
     * @param animated boolean; the animation state.
     */
    public void animated(boolean animated);
}
