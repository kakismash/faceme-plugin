/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.annotation.NonNull;

/**
 * For CyberLink used only to customize different configurations for different hardware devices.
 *
 * This handling is not for general Android devices. Even the same parameters would
 * work on one device, and might be useless, even harmful to other devices.
 */
@SuppressWarnings("deprecation")
public abstract class CustomHandler {

    public void applyAppOrientation(Activity activity) {}

    public void applyParameters(Camera.Parameters parameters) {}

    public Integer getUiLogicalCameraNum() { return null; }

    public int getFrameBufferSize() { return 3; }

    public void setDistanceCallback(DepthDistanceCallback distanceCallback) {}

    public void startCamera(@NonNull Camera.Parameters parameters) {}
    public void stopCamera() {}

    public Bitmap createFrameBitmap(Bitmap src) { return Bitmap.createBitmap(src); }

    public static class Factory {
        public static CustomHandler create(AutoFitSurfaceView subSurfaceView, StatListener statListener) {
            CustomDevice device  = CustomDevice.get();

            switch (device) {
                case ALTEK_AQ360:   return new AltekAq360Handler(subSurfaceView, statListener);
                default:            return new CustomHandler() {};
            }
        }
    }
}
