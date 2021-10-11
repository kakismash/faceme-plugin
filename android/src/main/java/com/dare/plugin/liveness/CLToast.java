/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

/**
 * A straightforward utility class to show a {@link Toast}.
 * <p/>
 * <b>NOTE</b>:
 * <ul>
 *     <li>Previous {@link Toast} shown via this class will be cancelled.</li>
 *     <li>It doesn't determine if UI is visible. Be careful to consider if it's suitable
 *         to show Toast after callback, such as app is in background, previous Activity
 *         has been destroyed, etc.</li>
 * </ul>
 */
public class CLToast {
    private static final String TAG = "CLToast";
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static Toast toast = null;

    private static void showInternal(String msg, int duration) {
        mainHandler.removeCallbacksAndMessages(null); // Clear and cancel all pending messages.
        mainHandler.post(() -> {
            if (toast != null) {
                toast.cancel();
            }

            Log.v(TAG, msg);
            toast = Toast.makeText(DemoApp.getContext(), msg, duration);
            toast.show();
        });
    }

    public static void show(@StringRes int msgResId, Object... args) {
        showShort(msgResId, args);
    }

    public static void show(String msg) {
        showShort(msg);
    }

    public static void showShort(@StringRes int msgResId, Object... args) {
        showShort(DemoApp.getResString(msgResId, args));
    }

    /**
     * Show a Toast with short duration explicitly.
     */
    public static void showShort(String msg) {
        showInternal(msg, Toast.LENGTH_SHORT);
    }

    public static void showLong(@StringRes int msgResId) {
        showLong(DemoApp.getContext().getString(msgResId));
    }

    /**
     * Show a Toast with long duration explicitly.
     */
    public static void showLong(String msg) {
        showInternal(msg, Toast.LENGTH_LONG);
    }
}
