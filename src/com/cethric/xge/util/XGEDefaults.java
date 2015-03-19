package com.cethric.xge.util;

/**
 * Created by blakerogan on 22/02/15.
 */
public class XGEDefaults {
    // Version Data
    public static final int XGE_VERSION_MAJOR = 0;
    public static final int XGE_VERSION_MINOR = 0;
    public static final int XGE_VERSION_PATCH = 0;
    public static final String XGE_VERSION_RELEASE = "a";

    public static final int XGE_EDITOR_VERSION_MAJOR = 0;
    public static final int XGE_EDITOR_VERSION_MINOR = 0;
    public static final int XGE_EDITOR_VERSION_PATCH = 1;
    public static final String XGE_EDITOR_VERSION_RELEASE = "a";

    // Window Data;
    public static final int INIT_WINDOW_WIDTH = 600;
    public static final int INIT_WINDOW_HEIGHT = 600;

    // Game Loop Data;
    public static final int TICKS_PER_SECOND = 25;
    public static final float SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    public static final int MAX_FRAME_SKIP = 5;

    public static final int FRAMES_PER_SECOND = 120;
    public static final float SKIP_FRAMES = 1000 / FRAMES_PER_SECOND;
    public static final int MAX_TICK_SKIP = 1;
}
