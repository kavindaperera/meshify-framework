package com.codewizards.meshify.logs;


import com.codewizards.meshify.api.Meshify;

public class Log {
    public static void d(String str, String str2) {
        if (Meshify.debug) {
            android.util.Log.d(str, str2);
        }
    }

    public static void e(String str, String str2) {
        if (Meshify.debug) {
            android.util.Log.e(str, str2);
        }
    }

    public static void i(String str, String str2) {
        if (Meshify.debug) {
            android.util.Log.i(str, str2);
        }
    }

    public static void v(String str, String str2) {
        if (Meshify.debug) {
            android.util.Log.v(str, str2);
        }
    }

    public static void w(String str, String str2) {
        if (Meshify.debug) {
            android.util.Log.w(str, str2);
        }
    }

    public static void e(String str, String str2, Throwable th) {
        if (Meshify.debug) {
            android.util.Log.e(str, str2, th);
        }
    }
}
