package cethric.xge.engine;

import cethric.xge.util.XGEDefaults;
import cethric.xge.engine.scene.SceneManager;
import cethric.xge.util.XGEUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Sys;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.util.concurrent.TimeUnit;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by blakerogan on 22/02/15.
 * This class defines a basic window, with a render/update loop.
 */
public class Window implements IWindow {
    // Misc Vars
    private static Logger LOGGER = LogManager.getLogger(Window.class);
    private long windowID;

    private int windowWidth = 0;
    private int windowHeight = 0;

    // Window Callbacks
    private GLFWWindowSizeCallback windowSizeCallback;
    private GLFWWindowCloseCallback windowCloseCallback;
    private GLFWWindowFocusCallback windowFocusCallback;
    private GLFWWindowIconifyCallback windowIconifyCallback;
    private GLFWWindowPosCallback windowPosCallback;
    private GLFWWindowRefreshCallback windowRefreshCallback;

    // Mouse Cursor Callbacks
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWCursorEnterCallback cursorEnterCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWScrollCallback scrollCallback;

    public final double[] old_x = new double[1];
    public final double[] old_y = new double[1];
    public final double[] cur_x = new double[1];
    public final double[] cur_y = new double[1];

    // Key Callbacks
    private GLFWKeyCallback keyCallback;

    // Scene Manager
    private SceneManager sceneManager;

    public Window() {
        init(XGEDefaults.INIT_WINDOW_WIDTH, XGEDefaults.INIT_WINDOW_HEIGHT, "Hello World", new WindowHints());
    }

    /**
     * @param windowWidth int; the initial window width
     * @param windowHeight int; the initial window height
     * @param title String; the windows title
     * @param windowHints WindowHint; the window/OpenGL context attributes
     */
    public void init(int windowWidth, int windowHeight, String title, WindowHints windowHints) {
        LOGGER.debug("Making Window: " + title);
        windowHints.setHints();
        windowID = glfwCreateWindow(windowWidth, windowHeight, title, 0, 0);
        LOGGER.debug("Window Created with id: " + windowID);
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        glfwSetWindowSizeCallback(windowID, windowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                windowResize(width, height);
            }
        });
        glfwSetWindowCloseCallback(windowID, windowCloseCallback = new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long window) {
                windowClose();
            }
        });
        glfwSetWindowFocusCallback(windowID, windowFocusCallback = new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, int focused) {
                if (focused == GL11.GL_TRUE) {
                    windowFocus();
                } else {
                    windowUnfocus();
                }
            }
        });
        glfwSetWindowIconifyCallback(windowID, windowIconifyCallback = new GLFWWindowIconifyCallback() {
            @Override
            public void invoke(long window, int iconified) {
                if (iconified == GL11.GL_TRUE) {
                    windowIconify();
                } else {
                    windowRestore();
                }
            }
        });
        glfwSetWindowPosCallback(windowID, windowPosCallback = new GLFWWindowPosCallback() {
            @Override
            public void invoke(long window, int xpos, int ypos) {
                windowMove(xpos, ypos);
            }
        });
        glfwSetWindowRefreshCallback(windowID, windowRefreshCallback = new GLFWWindowRefreshCallback() {
            @Override
            public void invoke(long window) {
                windowRefresh();
            }
        });

        glfwSetCursorPosCallback(windowID, cursorPosCallback = new GLFWCursorPosCallback() {

            @Override
            public void invoke(long window, double xpos, double ypos) {
                cur_x[0] = xpos;
                cur_y[0] = ypos;

                double dx = old_x[0] - cur_x[0];
                double dy = old_y[0] - cur_y[0];
                mouseMove(xpos, ypos, dx, dy);

                old_x[0] = cur_x[0];
                old_y[0] = cur_y[0];
            }
        });
        glfwSetCursorEnterCallback(windowID, cursorEnterCallback = new GLFWCursorEnterCallback() {
            @Override
            public void invoke(long window, int entered) {
                if (entered == GL11.GL_TRUE) {
                    mouseEnter();
                } else {
                    mouseLeave();
                }
            }
        });
        glfwSetMouseButtonCallback(windowID, mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW.GLFW_PRESS) {
                    mousePress(cur_x[0], cur_y[0], button, mods);
                } else if (action == GLFW.GLFW_RELEASE) {
                    mouseRelease(cur_x[0], cur_y[0], button, mods);
                }
            }
        });
        glfwSetScrollCallback(windowID, scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                mouseScroll(cur_x[0], cur_y[0], xoffset, yoffset);
            }
        });
        glfwSetKeyCallback(windowID, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW.GLFW_PRESS) {
                    keyPress(key, mods);
                } else if (action == GLFW.GLFW_RELEASE) {
                    keyRelease(key, mods);
                }
            }
        });

        LOGGER.debug("Making Context Current");
        glfwMakeContextCurrent(windowID);

        GLContext.createFromCurrent();
        LOGGER.debug("Created Context");

        LOGGER.info("XGE is running on: " + XGEUtil.getFullOSString());
        LOGGER.debug("Java Version: " + XGEUtil.getJavaVersionString());
        LOGGER.info("LWJGL Version: " + Sys.getVersion());
        LOGGER.info("GLFW Version: " + GLFW.glfwGetVersionString());
        LOGGER.info("OpenGL Version: " + GL11.glGetString(GL11.GL_VERSION));
        LOGGER.debug("OpenGL Vendor: " + GL11.glGetString(GL11.GL_VENDOR));
        LOGGER.debug("OpenGL Renderer: " + GL11.glGetString(GL11.GL_RENDERER));
        try {
            LOGGER.debug("OpenGL Extensions: " + GL11.glGetString(GL11.GL_EXTENSIONS));
        } catch (NullPointerException ignored) {
            LOGGER.debug("OpenGL Extensions: UNDETECTED");
        }

        sceneManager = new SceneManager();
    }


    /**
     * Set the scene manager for this window
     * @param sceneManager SceneManager; The set manager for this window.
     */
    public void setSceneManager(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    // Loop Data
    private final long start = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);

    private long renderDuration = 0;
    private long updateDuration = 0;

    private long getTime() {
        return TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) - start;
    }

    /**
     * The main game loop that handles most of the Update and render events.
     */
    public void loop() {
        glfwSetInputMode(windowID, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        try {
            long next_game_tick = getTime();
            long last_game_tick = getTime();
            long next_game_frame = getTime();
            int loops;

            while (glfwWindowShouldClose(windowID) == GL11.GL_FALSE) {
                loops = 0;
                while (getTime() > next_game_tick & loops < XGEDefaults.MAX_FRAME_SKIP) {
                    long start_update = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
                    long delta = getTime() - last_game_tick;
                    update(delta);
                    next_game_tick += XGEDefaults.SKIP_TICKS;
                    loops++;
                    long end_update = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
                    updateDuration = end_update - start_update;
                }

                loops = 0;
                while (getTime() > next_game_frame & loops < XGEDefaults.MAX_TICK_SKIP) {
                    long start_render = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
                    render();
                    next_game_frame += XGEDefaults.SKIP_FRAMES;
                    loops++;
                    long end_render = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
                    renderDuration = end_render - start_render;
                }

                glfwSwapBuffers(windowID);
                glfwPollEvents();
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            sceneManager.close();
            cleanup();
        }
    }

    /**
     * Called when the game loop dies
     * This process cleans up all of the callbacks and destroys most of the scene data
     */
    public void cleanup() {
        LOGGER.debug(String.format("destroying window %s", new Object[]{windowID}));

        windowCloseCallback.release();
        windowFocusCallback.release();
        windowIconifyCallback.release();
        windowPosCallback.release();
        windowRefreshCallback.release();
        windowSizeCallback.release();

        cursorPosCallback.release();
        cursorEnterCallback.release();
        mouseButtonCallback.release();
        scrollCallback.release();

        keyCallback.release();

        LOGGER.debug("Goodbye");
    }

    /**
     * This method when calls returns the <code>windowID</code> of this window
     * @return long; the window pointer id
     */
    public long getWindowID() {
        return windowID;
    }

    /**
     * called when the window changes size
     *
     * @param width  int; the new window width
     * @param height int; the new window height
     */
    @Override
    public void windowResize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
        sceneManager.resize(width, height);
    }

    /**
     * called when the window is about to close
     */
    @Override
    public void windowClose() {

    }

    /**
     * called when the window comes into focus
     */
    @Override
    public void windowFocus() {

    }

    /**
     * called when the window is no longer in focus
     */
    @Override
    public void windowUnfocus() {

    }

    /**
     * called when the window is iconified
     */
    @Override
    public void windowIconify() {

    }

    /**
     * called when the window is brought out of iconification
     */
    @Override
    public void windowRestore() {

    }

    /**
     * called when the user moves the window
     *
     * @param x int; the new window x position
     * @param y int; the new window y position
     */
    @Override
    public void windowMove(int x, int y) {

    }

    /**
     * called when the window requires a refresh, such as when a portion of the window is revealed after being covered
     */
    @Override
    public void windowRefresh() {

    }

    /**
     * called when the mouse enters the window
     */
    @Override
    public void mouseEnter() {

    }

    /**
     * called when the mouse leaves the window
     */
    @Override
    public void mouseLeave() {

    }

    /**
     * called every time a new frame is required
     */
    @Override
    public void render() {
//        LOGGER.debug("render");
        sceneManager.render();

    }

    /**
     * called every time the frame is required to be updated
     *
     * @param delta long; time since last update
     */
    @Override
    public void update(long delta) {
        sceneManager.update(delta);

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
        sceneManager.mousePress(x, y, button, mods);
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
        sceneManager.mouseRelease(x, y, button, mods);
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
        sceneManager.mouseMove(x, y, dx, dy);
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
        sceneManager.mouseScroll(x, y, sx, sy);
    }

    /**
     * called when any key is pressed
     *
     * @param key  int; the key pressed
     * @param mods int; the modifier bit mask
     */
    @Override
    public void keyPress(int key, int mods) {
        sceneManager.keyPress(key, mods);
        if (key == GLFW.GLFW_KEY_1 && mods == GLFW.GLFW_MOD_SHIFT) {
            LOGGER.debug(String.format("Render Time: %dms, Update Time: %dms", renderDuration, updateDuration));
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
        sceneManager.keyRelease(key, mods);
    }

    public class WindowHints {
        private int resizable = GL11.GL_TRUE;
        private int visible = GL11.GL_TRUE;
        private int decorated = GL11.GL_TRUE;
        private int auto_iconify = GL11.GL_TRUE;
        private int red_bits = 8;
        private int green_bits = 8;
        private int blue_bits = 8;
        private int alpha_bits = 8;
        private int depth_bits = 24;
        private int stencil_bits = 8;
        private int accum_red_bits = 0;
        private int accum_green_bits = 0;
        private int accum_blue_bits = 0;
        private int accum_alpha_bits = 0;
        private int aux_buffers = 0;
        private int samples = 4;
        private int refresh_rate = 0;
        private int stereo = GL11.GL_FALSE;
        private int srgb_capable = GL11.GL_FALSE;
        private int client_api = GLFW.GLFW_OPENGL_API;
        private int context_version_major = 3;
        private int context_version_minor = 2;
        private int context_robustness = GLFW.GLFW_NO_ROBUSTNESS;
        private int context_release_behavior = GLFW.GLFW_ANY_RELEASE_BEHAVIOR;
        private int opengl_forward_compat = GL11.GL_TRUE;
        private int opengl_debug_context = GL11.GL_FALSE;
        private int opengl_profile = GLFW.GLFW_OPENGL_CORE_PROFILE;

        public WindowHints() {

        }

        public WindowHints(int resizable, int visible, int decorated, int auto_iconify, int red_bits, int green_bits,
                           int blue_bits, int alpha_bits, int depth_bits, int stencil_bits, int accum_red_bits,
                           int accum_green_bits, int accum_blue_bits, int accum_alpha_bits, int aux_buffers,
                           int samples, int refresh_rate, int stereo, int srgb_capable, int client_api,
                           int context_version_major, int context_version_minor, int context_robustness,
                           int context_release_behavior, int opengl_forward_compat, int opengl_debug_context,
                           int opengl_profile) {
            this.resizable = resizable;
            this.visible = visible;
            this.decorated = decorated;
            this.auto_iconify = auto_iconify;
            this.red_bits = red_bits;
            this.green_bits = green_bits;
            this.blue_bits = blue_bits;
            this.alpha_bits = alpha_bits;
            this.depth_bits = depth_bits;
            this.stencil_bits = stencil_bits;
            this.accum_red_bits = accum_red_bits;
            this.accum_green_bits = accum_green_bits;
            this.accum_blue_bits = accum_blue_bits;
            this.accum_alpha_bits = accum_alpha_bits;
            this.aux_buffers = aux_buffers;
            this.samples = samples;
            this.refresh_rate = refresh_rate;
            this.stereo = stereo;
            this.srgb_capable = srgb_capable;
            this.client_api = client_api;
            this.context_version_major = context_version_major;
            this.context_version_minor = context_version_minor;
            this.context_robustness = context_robustness;
            this.context_release_behavior = context_release_behavior;
            this.opengl_forward_compat = opengl_forward_compat;
            this.opengl_debug_context = opengl_debug_context;
            this.opengl_profile = opengl_profile;

        }

        public void setHints() {
            glfwWindowHint(GLFW_RESIZABLE, resizable);
            glfwWindowHint(GLFW_VISIBLE, visible);
            glfwWindowHint(GLFW_DECORATED, decorated);
            glfwWindowHint(GLFW_AUTO_ICONIFY, auto_iconify);
            glfwWindowHint(GLFW_RED_BITS, red_bits);
            glfwWindowHint(GLFW_GREEN_BITS, green_bits);
            glfwWindowHint(GLFW_BLUE_BITS, blue_bits);
            glfwWindowHint(GLFW_ALPHA_BITS, alpha_bits);
            glfwWindowHint(GLFW_DEPTH_BITS, depth_bits);
            glfwWindowHint(GLFW_STENCIL_BITS, stencil_bits);
            glfwWindowHint(GLFW_ACCUM_RED_BITS, accum_red_bits);
            glfwWindowHint(GLFW_ACCUM_GREEN_BITS, accum_green_bits);
            glfwWindowHint(GLFW_ACCUM_BLUE_BITS, accum_blue_bits);
            glfwWindowHint(GLFW_ACCUM_ALPHA_BITS, accum_alpha_bits);
            glfwWindowHint(GLFW_AUX_BUFFERS, aux_buffers);
            glfwWindowHint(GLFW_SAMPLES, samples);
            glfwWindowHint(GLFW_REFRESH_RATE, refresh_rate);
            glfwWindowHint(GLFW_STEREO, stereo);
            glfwWindowHint(GLFW_SRGB_CAPABLE, srgb_capable);
            glfwWindowHint(GLFW_CLIENT_API, client_api);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, context_version_major);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, context_version_minor);
            glfwWindowHint(GLFW_CONTEXT_ROBUSTNESS, context_robustness);
            glfwWindowHint(GLFW_CONTEXT_RELEASE_BEHAVIOR, context_release_behavior);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, opengl_forward_compat);
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, opengl_debug_context);
            glfwWindowHint(GLFW_OPENGL_PROFILE, opengl_profile);
        }

        public int getResizable() {
            return resizable;
        }

        public void setResizable(int resizable) {
            this.resizable = resizable;
        }

        public int getVisible() {
            return visible;
        }

        public void setVisible(int visible) {
            this.visible = visible;
        }

        public int getDecorated() {
            return decorated;
        }

        public void setDecorated(int decorated) {
            this.decorated = decorated;
        }

        public int getAuto_iconify() {
            return auto_iconify;
        }

        public void setAuto_iconify(int auto_iconify) {
            this.auto_iconify = auto_iconify;
        }

        public int getRed_bits() {
            return red_bits;
        }

        public void setRed_bits(int red_bits) {
            this.red_bits = red_bits;
        }

        public int getGreen_bits() {
            return green_bits;
        }

        public void setGreen_bits(int green_bits) {
            this.green_bits = green_bits;
        }

        public int getBlue_bits() {
            return blue_bits;
        }

        public void setBlue_bits(int blue_bits) {
            this.blue_bits = blue_bits;
        }

        public int getAlpha_bits() {
            return alpha_bits;
        }

        public void setAlpha_bits(int alpha_bits) {
            this.alpha_bits = alpha_bits;
        }

        public int getDepth_bits() {
            return depth_bits;
        }

        public void setDepth_bits(int depth_bits) {
            this.depth_bits = depth_bits;
        }

        public int getStencil_bits() {
            return stencil_bits;
        }

        public void setStencil_bits(int stencil_bits) {
            this.stencil_bits = stencil_bits;
        }

        public int getAccum_red_bits() {
            return accum_red_bits;
        }

        public void setAccum_red_bits(int accum_red_bits) {
            this.accum_red_bits = accum_red_bits;
        }

        public int getAccum_green_bits() {
            return accum_green_bits;
        }

        public void setAccum_green_bits(int accum_green_bits) {
            this.accum_green_bits = accum_green_bits;
        }

        public int getAccum_blue_bits() {
            return accum_blue_bits;
        }

        public void setAccum_blue_bits(int accum_blue_bits) {
            this.accum_blue_bits = accum_blue_bits;
        }

        public int getAccum_alpha_bits() {
            return accum_alpha_bits;
        }

        public void setAccum_alpha_bits(int accum_alpha_bits) {
            this.accum_alpha_bits = accum_alpha_bits;
        }

        public int getAux_buffers() {
            return aux_buffers;
        }

        public void setAux_buffers(int aux_buffers) {
            this.aux_buffers = aux_buffers;
        }

        public int getSamples() {
            return samples;
        }

        public void setSamples(int samples) {
            this.samples = samples;
        }

        public int getRefresh_rate() {
            return refresh_rate;
        }

        public void setRefresh_rate(int refresh_rate) {
            this.refresh_rate = refresh_rate;
        }

        public int getStereo() {
            return stereo;
        }

        public void setStereo(int stereo) {
            this.stereo = stereo;
        }

        public int getSrgb_capable() {
            return srgb_capable;
        }

        public void setSrgb_capable(int srgb_capable) {
            this.srgb_capable = srgb_capable;
        }

        public int getClient_api() {
            return client_api;
        }

        public void setClient_api(int client_api) {
            this.client_api = client_api;
        }

        public int getContext_version_major() {
            return context_version_major;
        }

        public void setContext_version_major(int context_version_major) {
            this.context_version_major = context_version_major;
        }

        public int getContext_version_minor() {
            return context_version_minor;
        }

        public void setContext_version_minor(int context_version_minor) {
            this.context_version_minor = context_version_minor;
        }

        public int getContext_robustness() {
            return context_robustness;
        }

        public void setContext_robustness(int context_robustness) {
            this.context_robustness = context_robustness;
        }

        public int getContext_release_behavior() {
            return context_release_behavior;
        }

        public void setContext_release_behavior(int context_release_behavior) {
            this.context_release_behavior = context_release_behavior;
        }

        public int getOpengl_forward_compat() {
            return opengl_forward_compat;
        }

        public void setOpengl_forward_compat(int opengl_forward_compat) {
            this.opengl_forward_compat = opengl_forward_compat;
        }

        public int getOpengl_debug_context() {
            return opengl_debug_context;
        }

        public void setOpengl_debug_context(int opengl_debug_context) {
            this.opengl_debug_context = opengl_debug_context;
        }

        public int getOpengl_profile() {
            return opengl_profile;
        }

        public void setOpengl_profile(int opengl_profile) {
            this.opengl_profile = opengl_profile;
        }
    }
}
