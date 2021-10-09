/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class BaseCameraController implements CameraController {
    private static final String TAG = "FaceMe.CameraBase";

    final Context appContext;
    private final WindowManager windowManager;

    final TextureView mainTextureView;
    private final TextureView.SurfaceTextureListener textureCallback;
    final AtomicBoolean isTextureAvailable = new AtomicBoolean(false);

    Callback cameraCallback = new Callback() {};
    final StatListener statListener;
    final CustomHandler customHandler;
    UiSettings uiSettings;

    /**
     * Current camera is facing_back or facing_front.
     */
    boolean isCameraFacingBack = false;

    private int viewWidth;
    private int viewHeight;
    int preferWidth = 1280; // preferSize, bufferSize.
    int preferHeight = 720;
    int previewWidth = preferWidth;
    int previewHeight = preferHeight;

    boolean isOutputMirror = false;
    boolean isDisplayMirror = false;
    Integer forceRotateDegrees = null;

    boolean concurrentBitmapEnabled = false;
    float limitFps = 60;

    BaseCameraController(Activity activity,
                         TextureView mainTextureView, AutoFitSurfaceView subSurfaceView,
                         Callback callback, StatListener listener) {
        this.appContext = activity.getApplicationContext();
        this.windowManager = activity.getWindowManager();
        this.mainTextureView = mainTextureView;
        this.textureCallback = newTextureCallback();

        if (callback != null) this.cameraCallback = callback;
        this.statListener = listener;
        this.customHandler = CustomHandler.Factory.create(subSurfaceView, listener);
        this.uiSettings = new UiSettings(appContext);

        initComponents(activity);
    }

    private TextureView.SurfaceTextureListener newTextureCallback() {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                Log.i(TAG, "onSurfaceTextureAvailable: " + width + "x" + height);
                isTextureAvailable.set(true);
                viewWidth = width;
                viewHeight = height;
                startCamera(false);
            }

            
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                Log.i(TAG, "onSurfaceTextureSizeChanged: " + width + "x" + height);
                viewWidth = width;
                viewHeight = height;
                configureTransform(); // onSurfaceTextureSizeChanged
            }

            
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                Log.v(TAG, "onSurfaceTextureDestroyed");
                isTextureAvailable.set(false);
                return true;
            }

            
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        };
    }

    private void initComponents(Activity activity) {
        customHandler.applyAppOrientation(activity);
        isOutputMirror = uiSettings.isFlipCameraOutput();
        isDisplayMirror = uiSettings.isFlipCameraDisplay();
        forceRotateDegrees = uiSettings.getCameraOutputRotation();

        mainTextureView.setSurfaceTextureListener(textureCallback);
    }

    boolean noCameraPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                appContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     */
    void configureTransform() {
        Log.v(TAG, " > configureTransform");
        Matrix matrix = new Matrix();

        float centerX = viewWidth * 0.5F;
        float centerY = viewHeight * 0.5F;

        int deviceOrientation = getDeviceRotationDegree();
        int cameraOrientation = getCameraOrientation();
        boolean theSameWidthHeightDirection = (deviceOrientation - cameraOrientation) % 180 == 0;
        // height divided by width; smaller == wider
        float ratioPreview = 1F * previewHeight / previewWidth;
        float ratioView    = 1F * viewHeight / viewWidth;

        float newWidth, newHeight;
        // Fit content in View.
        if (viewWidth >= viewHeight) { // Landscape view.
            if (theSameWidthHeightDirection) {
                if (ratioView <= ratioPreview) {
                    // Landscape Camera in Wider Landscape View
                    newHeight = viewHeight;
                    newWidth = viewHeight / ratioPreview;
                } else {
                    // Landscape Camera in Landscape View
                    newWidth = viewWidth;
                    newHeight = viewWidth * ratioPreview;
                }
            } else {
                if (ratioView <= (1F / ratioPreview)) {
                    // Portrait Camera in Landscape View
                    newHeight = viewHeight;
                    newWidth = viewHeight * ratioPreview;
                } else {
                    // Should not happen since Aspect Ratio of Portrait Camera should always larger than View.
                    newHeight = viewHeight;
                    newWidth = viewHeight * ratioPreview;
                }
            }
        } else {
            if (theSameWidthHeightDirection) {
                if (ratioView <= ratioPreview) {
                    // Should not happen since Aspect Ratio of Landscape Camera should always smaller than View.
                    newWidth = viewWidth;
                    newHeight = viewHeight;
                } else {
                    // Landscape Camera in Portrait View.
                    newWidth = viewWidth;
                    newHeight = viewWidth * ratioPreview;
                }
            } else {
                if (ratioView <= (1F / ratioPreview)) {
                    // Portrait Camera in Portrait View.
                    newHeight = viewHeight;
                    newWidth = viewHeight * ratioPreview;
                } else {
                    // Portrait Camera in Taller Portrait View.
                    newWidth = viewWidth;
                    newHeight = viewWidth / ratioPreview;
                }
            }
        }

        if ((deviceOrientation % 180) == 90) {
            matrix.postScale(newHeight / viewWidth, newWidth / viewHeight, centerX, centerY);
        } else if ((deviceOrientation % 180) == 0) {
            matrix.postScale(newWidth / viewWidth, newHeight / viewHeight, centerX, centerY);
        }

        int rotationDegree = 0;
        if (deviceOrientation != 0) {
            rotationDegree = 360 - deviceOrientation;
        }

        Log.v(TAG, "orientation: camera:" + cameraOrientation + ", device:" + deviceOrientation + ", matrix:" + rotationDegree);
        if (rotationDegree != 0) {
            matrix.postRotate(rotationDegree, centerX, centerY);
        }

        if (isDisplayMirror) {
            matrix.postScale(-1.0f, 1.0f, centerX, centerY);
        }

        // align: horizontal center; vertical top.
        float offsetY = (viewHeight - newHeight) * 0.5F;
        matrix.postTranslate(0, -offsetY);

        mainTextureView.setTransform(matrix);
        Log.v(TAG, "   " + newWidth + "x" + newHeight + ", cx:" + centerX + ", cy:" + (centerY - offsetY));
        cameraCallback.onPreviewSizeChanged((int)newWidth, (int)newHeight, centerX, centerY - offsetY);
    }

    public abstract int getCameraOrientation();

    /**
     * A data structure to transfer device information
     * intermediately.
     */
    static class DeviceRotation {
        boolean isPortrait;
        int degree;
        DeviceRotation(boolean isPortrait, int degree) {
            this. isPortrait = isPortrait;
            this.degree = degree;
        }
    }

    DeviceRotation getDeviceRotationInfo() {
        int rotation = getDeviceRotationDegree();
        Point screenSize = new Point();
        windowManager.getDefaultDisplay().getSize(screenSize);
        return new DeviceRotation(screenSize.x < screenSize.y, rotation);
    }

    int getDeviceRotationDegree() {
        int rotation = (windowManager == null) ? Surface.ROTATION_0 : windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_90:   return 90;
            case Surface.ROTATION_180:  return 180;
            case Surface.ROTATION_270:  return 270;
            case Surface.ROTATION_0:
            default:
                return 0;
        }
    }

    static void logE(Exception e) {
        Log.e(TAG, "Something went wrong", e);
    }

    static void logE(String msg) {
        try {
            // Throw an Exception to print calling stack.
            throw new Exception(msg);
        } catch (Exception e) {
            logE(e);
        }
    }

    @CallSuper
    @Override
    public void setCameraCallback(Callback callback) {
        if (callback != null)
            this.cameraCallback = callback;
        else
            this.cameraCallback = new Callback() {};
    }

    @Override
    public Size getCurrentResolution() {
        if (viewWidth >= viewHeight) { // Landscape view.
            return new Size(previewWidth, previewHeight);
        } else {
            return new Size(previewHeight, previewWidth);
        }
    }

    @Override
    public void setResolution(int preferWidth, int preferHeight) {
        this.previewWidth = this.preferWidth = preferWidth;
        this.previewHeight = this.preferHeight = preferHeight;

        restartCamera(false);
    }

    @CallSuper
    void startCamera(boolean nextCameraId) {
        Log.i(TAG, "startCamera");
        isOutputMirror = uiSettings.isFlipCameraOutput();
        isDisplayMirror = uiSettings.isFlipCameraDisplay();
        forceRotateDegrees = uiSettings.getCameraOutputRotation();
        if (statListener != null) statListener.reset();
    }

    abstract void stopCamera();

    @Override
    public void restart() {
        Log.d(TAG, "restart");

        restartCamera(false);
    }

    private void restartCamera(boolean nextCameraId) {
        Log.i(TAG, "restartCamera");
        stopCamera();
        startCamera(nextCameraId);
    }

    @Override
    public void pause() {
        Log.d(TAG, "pause");

        stopCamera();
    }

    @Override
    public void resume() {
        Log.d(TAG, "resume");

        if (mainTextureView.isAvailable()) startCamera(false);
    }

    @CallSuper
    @Override
    public void release() {
        Log.d(TAG, "release");

        stopCamera();

        mainTextureView.setSurfaceTextureListener(null);
    }

    @Override
    public boolean switchCamera() {
        isCameraFacingBack = !isCameraFacingBack;
        restartCamera(true);
        return isCameraFacingBack;
    }

    @Override
    public void setCameraId(int cameraId) {
        Log.i(TAG, "Set camera ID to " + cameraId);
    }

    @Override
    public void setConcurrentBitmap(boolean enable) {
        this.concurrentBitmapEnabled = enable;
    }

    @CallSuper
    @Override
    public void setLimitFps(float fps) {
        this.limitFps = fps;
    }

    static int getBufferLength(int width, int height) {
        int yStride = (int) Math.ceil(width / 16.0) * 16;
        int uvStride = (int) Math.ceil((yStride / 2) / 16.0) * 16;
        int ySize = yStride * height;
        int uvSize = uvStride * height;
        return ySize + uvSize;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            logE("Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    @Override
    public void setLaserPower(float newValue) { }

    @Override
    public void setUiSettings(UiSettings uiSettings) {
        this.uiSettings = uiSettings;
    }
}
