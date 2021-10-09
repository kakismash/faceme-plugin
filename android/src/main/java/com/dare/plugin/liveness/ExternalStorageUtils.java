/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.os.Build;
import android.os.Environment;

import java.io.File;

public class ExternalStorageUtils {
    public static File getExternalStorageDir(String fileName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            return new File(Environment.getExternalStorageDirectory() + File.separator + fileName);
        else
            return new File(DemoApp.getContext().getExternalFilesDir(""), fileName);
    }
}
