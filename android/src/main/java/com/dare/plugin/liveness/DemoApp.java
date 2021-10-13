/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.multidex.MultiDexApplication;
import android.util.Log;

import java.util.Date;

public class DemoApp extends MultiDexApplication {
    private static final String TAG = "FaceMe.DemoApp";

    /**
     * A lazy implementation to use {@link Context} globally.
     */
    private static DemoApp singleton;

    
    public void onCreate() {
        Log.v(TAG, "onCreate: " +  new Date());
        super.onCreate();
        singleton = this;

        LicenseInfoHandler.init(this);

        DevTool.touch();
    }

    
    public static Context getContext() {
        return singleton.getApplicationContext();
    }

    public static String getResString(@StringRes int resId) {
        return singleton.getString(resId);
    }

    public static String getResString(@StringRes int resId, Object... formatArgs) {
        return singleton.getString(resId, formatArgs);
    }

    public static String getAppPackageName() {
        return singleton.getPackageName();
    }
}
