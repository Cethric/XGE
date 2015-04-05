package cethric.xge.engine.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blakerogan on 14/03/15.
 */
public class SceneManager implements ISceneManager {
    private transient Logger LOGGER = LogManager.getLogger(SceneManager.class);

    private List<Scene> scenes = new ArrayList<Scene>();

    /**
     * Add a scene the scene list so that it can be interacted with and rendered
     * @param scene Scene; the scene to add
     */
    public void addScene(Scene scene) {
        scenes.add(scene);
        scene.register();
        LOGGER.debug(String.format("Added the Scene: %s to the manager", scene.getSceneName()));
    }

    /**
     * Remove a scene the scene list
     * @param scene Scene; the scene to remove
     */
    public void removeScene(Scene scene) {
        scenes.remove(scene);
        scene.unregister();
        LOGGER.debug(String.format("Removed the Scene: %s from the manager", scene));
    }

    /**
     * Unregister all the connected scenes.
     */
    public void close() {
        for (Scene scene : scenes) {
            scene.unregister();
        }
    }

    /**
     * When called this function runs the scene update process
     */
    @Override
    public void render() {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                scene.render();
            }
        }
    }

    /**
     * When called this function updates the scene
     *
     * @param delta long; the time since the last update
     */
    @Override
    public void update(long delta) {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                scene.update(delta);
            }
        }
    }

    /**
     * This function is used to set the scenes width and height
     *
     * @param width  int; the new width
     * @param height int; the new height
     */
    @Override
    public void resize(int width, int height) {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                scene.resize(width, height);
            }
        }
    }

    /**
     * Called when any mouse button is pressed
     *
     * @param x      double; the mouse x position
     * @param y      double; the mouse y position
     * @param button int; the mouse button pressed
     * @param mods   int; any modifier keys that are down
     */
    @Override
    public void mousePress(double x, double y, int button, int mods) {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                if (scene.active()) {
                    scene.mousePress(x, y, button, mods);
                }
            }
        }
    }

    /**
     * Called when any mouse button is released
     *
     * @param x      double; the mouse x position
     * @param y      double; the mouse y position
     * @param button int; the mouse button pressed
     * @param mods   int; any modifier keys that are down
     */
    @Override
    public void mouseRelease(double x, double y, int button, int mods) {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                if (scene.active()) {
                    scene.mouseRelease(x, y, button, mods);
                }
            }
        }
    }

    /**
     * Called when ever the mouse moves
     *
     * @param x  double; the x position of the mouse
     * @param y  double; the y position of the mouse
     * @param dx double; the distance of the x position of the mouse moved since last change
     * @param dy double; the distance of the y position of the mouse moved since last change
     */
    @Override
    public void mouseMove(double x, double y, double dx, double dy) {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                if (scene.active()) {
                    scene.mouseMove(x, y, dx, dy);
                }
            }
        }
    }

    /**
     * called when the user moves the scroll wheel
     *
     * @param x  double; mouse x position
     * @param y  double; mouse y position
     * @param sx double; the x axis offset
     * @param sy double; the y axis offset
     */
    @Override
    public void mouseScroll(double x, double y, double sx, double sy) {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                if (scene.active()) {
                    scene.mouseScroll(x, y, sx, sy);
                }
            }
        }
    }

    /**
     * called when any key is pressed
     *
     * @param key  int; the key pressed
     * @param mods int; the modifier bit mask
     */
    @Override
    public void keyPress(int key, int mods) {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                if (scene.active()) {
                    scene.keyPress(key, mods);
                }
            }
        }
    }

    /**
     * called when any key is released
     *
     * @param key  int; the key released
     * @param mods int; the modifier bit mask
     */
    @Override
    public void keyRelease(int key, int mods) {
        for (Scene scene : scenes) {
            if (scene.registered()) {
                if (scene.active()) {
                    scene.keyRelease(key, mods);
                }
            }
        }
    }
}
