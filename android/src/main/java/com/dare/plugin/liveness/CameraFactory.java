/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.app.Activity;
import android.view.TextureView;

public class CameraFactory {

    public static CameraController create(Activity activity,
                                          TextureView mainTextureView, AutoFitSurfaceView subSurfaceView,
                                          CameraController.Callback callback, StatListener listener) {
        return create(activity, mainTextureView, subSurfaceView, callback, listener, false);
    }
    public static CameraController create(Activity activity,
                                          TextureView mainTextureView, AutoFitSurfaceView subSurfaceView,
                                          CameraController.Callback callback, StatListener listener, boolean hasIRCamera) {
        if (hasIRCamera)
            return new InfraredCameraV17(activity, mainTextureView, subSurfaceView, callback, listener);
        else
            return new CameraV17(activity, mainTextureView, subSurfaceView, callback, listener);
    }
    public static CameraController create(Activity activity,
                                          TextureView mainTextureView, AutoFitSurfaceView subSurfaceView,
                                          CameraController.Callback callback, StatListener listener,
                                          CLGLRsSurfaceView mGLRsSurfaceView) {
        return new RealSenseCamera(activity, mainTextureView, subSurfaceView, callback, listener, mGLRsSurfaceView);
    }
}
