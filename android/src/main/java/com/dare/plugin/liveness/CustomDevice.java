/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public enum CustomDevice {
    ACER_ABS_3G1() {
        
        public boolean matches() {
            return "acer".equals(Build.BRAND) &&
                    "ABS-3G1".equals(Build.MODEL) &&
                    "abs3g1_ww_gen1s".equals(Build.PRODUCT);
        }

        
        public boolean needFlipCameraOutput() { return true; }

        
        public boolean needFlipCameraDisplay() { return true; }

        
        public Integer forceCameraOutputRotation() { return 90; }

        
        public Integer forceCameraDisplayOrientation() { return 90; }
    },

    ALTEK_AQ360() {
        
        public boolean matches() {
            return "AQ360".equals(Build.MODEL) &&
                    "msm8953_64".equals(Build.DEVICE) &&
                    "ACSQBuild-01".equals(Build.HOST);
        }

        
        public String getDepthCameraId() { return "kara"; }

        
        public Integer forceCameraDisplayOrientation() { return 90; }
    },

    UNITECH_PA726() {
        
        public boolean matches() {
            return "PA726".equals(Build.MODEL); // TODO: Need more clues.
        }

       
        public Integer forceCameraOutputRotation() { return 180; }
    },

    VIA_VT6093() {
        
        public boolean matches() {
            return "SOM-9X20_VT6093IO".equals(Build.MODEL);
        }

        
        public boolean needFlipCameraOutput() { return true; }

        
        public boolean needFlipCameraDisplay() { return true; }

       
        public Integer forceCameraOutputRotation() { return 180; }

        
        public Integer forceCameraDisplayOrientation() { return 270; }
    },

    // Put GENERIC to the last position.
    GENERIC() {
        
        public boolean matches() {
            return true;
        }
    };

    public abstract boolean matches();

    public boolean hasDepthCamera() { return !TextUtils.isEmpty(getDepthCameraId()); }
    public String getDepthCameraId() { return null; }

    public boolean needFlipCameraOutput() { return false; }
    public boolean needFlipCameraDisplay() { return false; }
    public Integer forceCameraOrientation() { return null; }
    public Integer forceCameraOutputRotation() { return null; }
    public Integer forceCameraDisplayOrientation() { return null; }

    private static final String TAG = "FaceMe.Custom";
    private static final CustomDevice thisDevice;

    public static CustomDevice get() {
        return thisDevice;
    }

    static {
        Log.v(TAG, "board: " + Build.BOARD);
        Log.v(TAG, "bootloader: " + Build.BOOTLOADER);
        Log.v(TAG, "brand: " + Build.BRAND);
        Log.v(TAG, "device: " + Build.DEVICE);
        Log.v(TAG, "display: " + Build.DISPLAY);
        Log.v(TAG, "fingerprint: " + Build.FINGERPRINT);
        Log.v(TAG, "hardware: " + Build.HARDWARE);
        Log.v(TAG, "host: " + Build.HOST);
        Log.v(TAG, "id: " + Build.ID);
        Log.v(TAG, "manufacturer: " + Build.MANUFACTURER);
        Log.v(TAG, "model: " + Build.MODEL);
        Log.v(TAG, "product: " + Build.PRODUCT);
        Log.v(TAG, "tags: " + Build.TAGS);
        Log.v(TAG, "type: " + Build.TYPE);
        Log.v(TAG, "user: " + Build.USER);
        Log.v(TAG, "version.release: " + Build.VERSION.RELEASE);
        Log.v(TAG, "version.sdk_int: " + Build.VERSION.SDK_INT);

        CustomDevice aDevice = GENERIC;
        for (CustomDevice device : CustomDevice.values()) {
            if (device.matches()) {
                aDevice = device;
                Log.i(TAG, "Custom device found: " + aDevice);
                break;
            }
        }
        thisDevice = aDevice;
    }
}
