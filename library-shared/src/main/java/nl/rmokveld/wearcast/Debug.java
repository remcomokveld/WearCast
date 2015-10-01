package nl.rmokveld.wearcast;

import android.util.Log;

import nl.rmokveld.wearcast.shared.BuildConfig;
import nl.rmokveld.wearcast.shared.C;

@SuppressWarnings("unused")
public class Debug {

    private static boolean sLoggingEnabled = BuildConfig.DEBUG;

    public static void setLoggingEnabled(boolean loggingEnabled) {
        sLoggingEnabled = loggingEnabled;
    }

    public static void logd(String message) {
        if (sLoggingEnabled) Log.d(C.TAG, message);
    }

    public static void logw(String message) {
        if (sLoggingEnabled) Log.w(C.TAG, message);
    }

    public static void logw(String message, Throwable tr) {
        if (sLoggingEnabled) Log.w(C.TAG, message, tr);
    }

    public static void loge(String message) {
        if (sLoggingEnabled) Log.e(C.TAG, message);
    }

    public static void loge(String message, Throwable tr) {
        if (sLoggingEnabled) Log.e(C.TAG, message, tr);
    }
}
