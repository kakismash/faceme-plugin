/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public enum ExternalDevice {
    INTEL_REALSENSE_D415() {
        UsbDevice thisUsbDevice;
        
        public boolean matches() {
            UsbManager usbManager = (UsbManager) DemoApp.getContext().getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
            for (UsbDevice usbDevice : usbDevices.values()) {
                if ("Intel(R) RealSense(TM) Depth Camera 415 ".equals(usbDevice.getProductName())) {
                    thisUsbDevice = usbDevice;
                    return true;
                }
            }
            return false;
        }

        public UsbDevice getUsbDevice() { return thisUsbDevice; }

        public String getDepthCameraId() { return "d415"; }
    },

    // Put GENERIC to the last position.
    GENERIC() {
        
        public boolean matches() {
            return true;
        }
    };

    public abstract boolean matches();

    public UsbDevice getUsbDevice() { return null; }
    public boolean hasDepthCamera() { return !TextUtils.isEmpty(getDepthCameraId()); }
    public String getDepthCameraId() { return null; }

    private static final String TAG = "FaceMe.External";
    private static ExternalDevice thisDevice;

    public static ExternalDevice get() {
        return thisDevice;
    }

    public static void update() {
        ExternalDevice aDevice = GENERIC;
        for (ExternalDevice device : ExternalDevice.values()) {
            if (device.matches()) {
                aDevice = device;
                Log.i(TAG, "External device found: " + aDevice);
                break;
            }
        }
        thisDevice = aDevice;
    }

    static {
        update();
    }
}
