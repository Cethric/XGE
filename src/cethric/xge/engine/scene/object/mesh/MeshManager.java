package cethric.xge.engine.scene.object.mesh;

import cethric.xge.engine.scene.shader.ShaderProgram;
import com.hackoeur.jglm.Mat4;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blakerogan on 21/03/15.
 */
public class MeshManager implements IMeshManager {
    private Logger LOGGER = LogManager.getLogger(MeshManager.class);

    private List<Mesh> meshs = new ArrayList<Mesh>();

    private boolean animated = false;

    /**
     * Adds a new mesh to the rendering list of the manager.
     *
     * @param mesh Mesh; the new mesh to add
     */
    @Override
    public void addMesh(Mesh mesh) {
        meshs.add(mesh);
    }

    /**
     * Remove a mesh from the render list of this manager.
     *
     * @param mesh Mesh; the mesh to remove
     */
    @Override
    public void removeMesh(Mesh mesh) {
        meshs.remove(mesh);
    }

    /**
     * Return the mesh at id 'x'
     *
     * @param id int; the id/index of the mesh to return
     * @return Mesh; the mesh that was at the id/index 'x'
     */
    @Override
    public Mesh getMesh(int id) {
        return meshs.get(id);
    }

    /**
     * @return ArrayList&lt;Mesh&gt;; the array containing all the meshs
     */
    @Override
    public List<Mesh> getMeshs() {
        return this.meshs;
    }

    /**
     * Return if this MeshManger is animated and so should render each mesh on a new frame.
     *
     * @return boolean; if the MeshManager is animated
     */
    @Override
    public boolean isAnimated() {
        return animated;
    }

    /**
     * Set the animation state of the manager
     *
     * @param animated boolean; the animation state.
     */
    @Override
    public void animated(boolean animated) {
        this.animated = animated;
    }

    /**
     * Called on every update tick
     *
     * @param delta long; time since last update
     */
    @Override
    public void update(long delta) {
        //TODO This will manage the animated mesh render. Which stills needs to be created.
    }

    /**
     * Called every time the frame needs to be rendered.
     *
     * @param V             Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param P             Mat4; Projection * View matrix. times the model matrix to get the MVP matrix.
     * @param shaderProgram ShaderProgram; the attached shader program that renders this cube.
     */
    @Override
    public void render(Mat4 V, Mat4 P, ShaderProgram shaderProgram) {
        if (isAnimated()) {
            //TODO Create an animated mesh render.
            return;
        } else {
            for (Mesh mesh : meshs) {
//                LOGGER.debug(String.format("Rendering: %s", mesh.getName()));
                mesh.render(V, P, shaderProgram);
            }
        }
    }

    /**
     * Setup the OpenGL and Bullet content as well as any other content that needs to be setup after creation
     */
    @Override
    public void setup() {
        for (Mesh mesh : meshs) {
            mesh.setup();
        }
    }

    /**
     * Remove any content that this object no longer needs as it will be deleted.
     */
    @Override
    public void teardown() {
        for (Mesh mesh : meshs) {
            mesh.teardown();
        }
    }
}
