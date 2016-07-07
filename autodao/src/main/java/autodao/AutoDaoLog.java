package autodao;

import android.util.Log;

/**
 * Created by tubingbing on 16/7/6.
 */
public class AutoDaoLog {

    private final static String TAG = "autodao";
    private static boolean debug = false;

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean flag) {
        debug = flag;
    }

    public static boolean isLoggable(int level) {
        return Log.isLoggable(TAG, level);
    }

    public static String getStackTraceString(Throwable th) {
        return Log.getStackTraceString(th);
    }

    public static int println(int level, String msg) {
        return Log.println(level, TAG, msg);
    }

    public static int v(String msg) {
        return Log.v(TAG, msg);
    }

    public static int v(String msg, Throwable th) {
        return Log.v(TAG, msg, th);
    }

    public static int d(String msg) {
        return Log.d(TAG, msg);
    }

    public static int d(String msg, Throwable th) {
        return Log.d(TAG, msg, th);
    }

    public static int i(String msg) {
        return Log.i(TAG, msg);
    }

    public static int i(String msg, Throwable th) {
        return Log.i(TAG, msg, th);
    }

    public static int w(String msg) {
        return Log.w(TAG, msg);
    }

    public static int w(String msg, Throwable th) {
        return Log.w(TAG, msg, th);
    }

    public static int w(Throwable th) {
        return Log.w(TAG, th);
    }

    public static int e(String msg) {
        return Log.w(TAG, msg);
    }

    public static int e(String msg, Throwable th) {
        return Log.e(TAG, msg, th);
    }

}
