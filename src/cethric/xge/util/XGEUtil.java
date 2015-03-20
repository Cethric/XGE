package cethric.xge.util;

/**
 * Created by blakerogan on 23/02/15.
 */
public class XGEUtil {
    public static String getVersionString() {
        return XGEDefaults.XGE_VERSION_MAJOR + "." + XGEDefaults.XGE_VERSION_MINOR + "." + XGEDefaults.XGE_VERSION_PATCH + XGEDefaults.XGE_VERSION_RELEASE;
    }

    public static String getEditorVersionString() {
        return XGEDefaults.XGE_EDITOR_VERSION_MAJOR + "." + XGEDefaults.XGE_EDITOR_VERSION_MINOR + "." + XGEDefaults.XGE_EDITOR_VERSION_PATCH + XGEDefaults.XGE_EDITOR_VERSION_RELEASE;
    }

    public static String getFullOSString() {
        return System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
    }

    public static String getJavaVersionString() {
        return System.getProperty("java.version") + " " + System.getProperty("java.vendor");
    }
}
