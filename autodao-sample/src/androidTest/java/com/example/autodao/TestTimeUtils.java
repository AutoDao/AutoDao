package com.example.autodao;

import android.util.Log;

/**
 * Created by bill_lv on 2016/7/11.
 */
public class TestTimeUtils {

    private static String TAG = "TestTimeUtils";
    private static long startTime = 0;
    private static String methodName = null;

    public static void start(String methodName) {
        start(TAG, methodName);
    }

    public static void start(String tag, String methodName) {
        TestTimeUtils.methodName = methodName;
        startTime = System.currentTimeMillis();
        TAG = tag;
    }

    public static void stop() {
        long time = System.currentTimeMillis() - startTime;
        Log.d(TAG, methodName + " cost time is " + time + "ms");
    }
}
