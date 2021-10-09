/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.graphics.Bitmap;
import android.util.Size;

import java.util.List;

public interface CameraController {
    interface Callback {
        /**
         * Callback when retrieved a frame. It would be on arbitrary thread
         * depending on implementation. Client have to recycle {@link Bitmap} itself
         * or else {@link OutOfMemoryError} would occur.
         */
        default void onBitmap(long presentationMs, Bitmap bitmap) {}

        /**
         * Callback when retrieved a distance map. Value of each distance pixel
         * should be in millimeter (mm).
         */
        default void onDistanceMap(long presentationMs, int width, int height, byte[] data) {}

        /**
         * Callback when retrieved a frame. It would be on arbitrary thread
         * depending on implementation.
         */
        default void onInfraredData(long presentationMs, int width, int height, byte[] data) {}

        /**
         * Check if client is processing previous task or available.
         */
        default void checkTask(CheckCallback callback) { callback.rejected(); }

        default void onPreviewSizeChanged(int width, int height, float cx, float cy) {}

        default void onErrorMessage(String errTitle, String errMsg) { CLToast.show(errTitle + "\n" + errMsg); }
    }

    interface CheckCallback {
        void acquired();
        default void rejected() {}
    }

    void setCameraCallback(Callback callback);

    int getUiLogicalCameraNum();

    void restart();

    /**
     * Notify controller that Activity onPause event invoked.
     * This will trigger controller to stop and release camera instance.
     */
    void pause();

    /**
     * Notify controller that Activity onResume event invoked.
     * This will trigger controller to create and open camera instance.
     */
    void resume();

    /**
     * Notify controller that Activity onDestroy event invoked.
     * This will trigger controller to release all related resources.
     */
    void release();

    /**
     * Notify controller to switch camera between front and rear one.
     *
     * @return {@code true} means rear. Otherwise front camera.
     */
    boolean switchCamera();
    void setCameraId(int cameraId);

    <S> List<S> getResolutions();

    Size getCurrentResolution();

    void setResolution(int width, int height);

    void setConcurrentBitmap(boolean enable);
    void setLimitFps(float fps);
    void setLaserPower(float newValue);

    void setUiSettings(UiSettings uiSettings);

    int getCameraOrientation();
}
