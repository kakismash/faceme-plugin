/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.WorkerThread;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;

/**
 * Use Camera1 for better preview performance.
 */
@SuppressWarnings("deprecation")
class InfraredCameraV17 extends BaseCameraController {
    private static final String TAG = "FaceMe.CameraV17";

    private final Camera.PreviewCallback onReceivedPreviewFrame;
    private final Camera.PreviewCallback onReceivedInfraredFrame;
    private final Handler colorCameraHandler;
    private final Handler infraredCameraHandler;
    private final FrameV17 frameHandler;

    private int colorCameraId;
    private int infraredCameraId;
    private Camera colorCamera;
    private Camera infraredCamera;
    private final byte[][] mFrameBuffer;
    private final byte[][] mInfraredFrameBuffer;

    InfraredCameraV17(Activity activity, TextureView mainTextureView, AutoFitSurfaceView subSurfaceView, Callback callback, StatListener listener) {
        super(activity, mainTextureView, subSurfaceView, callback, listener);

        this.onReceivedPreviewFrame = newPreviewCallback();
        this.onReceivedInfraredFrame = newInfraredCallback();
        this.frameHandler = new FrameV17(this, callback, listener);

        this.mFrameBuffer = new byte[customHandler.getFrameBufferSize()][];
        this.mInfraredFrameBuffer = new byte[customHandler.getFrameBufferSize()][];
        customHandler.setDistanceCallback(this.frameHandler);
        colorCameraId = 0;
        infraredCameraId = 1;

        HandlerThread cameraThread = new HandlerThread(TAG);
        cameraThread.start();
        this.colorCameraHandler = new Handler(cameraThread.getLooper());
        HandlerThread InfraredCameraThread = new HandlerThread(TAG);
        InfraredCameraThread.start();
        this.infraredCameraHandler = new Handler(InfraredCameraThread.getLooper());
    }

    private Camera.PreviewCallback newPreviewCallback() {
        return (data, camera) -> {
            long presentationMs = System.currentTimeMillis();

            if (statListener != null) statListener.onFrameCaptured();
            frameHandler.onFrame(presentationMs, data, isCameraFacingBack, () -> camera.addCallbackBuffer(data));
        };
    }

    private Camera.PreviewCallback newInfraredCallback() {
        return (data, camera) -> {
            long presentationMs = System.currentTimeMillis();

            frameHandler.onInfraredData(presentationMs, data, () -> camera.addCallbackBuffer(data));
        };
    }

    @Override
    public void setCameraCallback(Callback callback) {
        super.setCameraCallback(callback);
        frameHandler.setCameraCallback(callback);
    }

    @Override
    public int getUiLogicalCameraNum() {
        Integer num = customHandler.getUiLogicalCameraNum();
        if (num != null) return num;

        return Camera.getNumberOfCameras();
    }

    @Override
    void startCamera(boolean nextCameraId) {
        super.startCamera(nextCameraId);

        if (!isTextureAvailable.get() || !mainTextureView.isAvailable()) {
            Log.w(TAG, " > texture is unavailable yet");
            return;
        }
        if (noCameraPermission()) {
            CLToast.show(R.string.ext_permission_fail, "Camera");
            return;
        }

        configureTransform(); // startCamera

        colorCameraHandler.post(() -> startCameraInternal(nextCameraId));
    }

    @WorkerThread
    private void startCameraInternal(boolean nextCameraId) {
        try {
            int num = Camera.getNumberOfCameras();
            if (num == 0) throw new IllegalStateException("Hardware camera is unavailable");

            if (nextCameraId) colorCameraId = getNextCameraId();
            colorCamera = Camera.open(colorCameraId);
            infraredCamera = Camera.open(infraredCameraId);

            int facing = getCameraInfo(colorCameraId).facing;
            boolean facingBack = facing == Camera.CameraInfo.CAMERA_FACING_BACK;
            if (isCameraFacingBack != facingBack) {
                Log.w(TAG, " > request " + (isCameraFacingBack ? "back" : "front") + " but got another");
                isCameraFacingBack = facingBack;
            }

            Camera.Parameters params = setupCameraParameters();
            colorCamera.startPreview();
            customHandler.startCamera(params);
            colorCamera.setErrorCallback((i, camera) -> {
                Log.e(TAG, " > Camera Error : " + i);
                CLToast.showLong("Camera Error : " + i);
            });
            infraredCamera.startPreview();
            infraredCamera.setErrorCallback((i, camera) -> {
                Log.e(TAG, " > Camera Error : " + i);
                CLToast.showLong("Camera Error : " + i);
            });

        } catch (Exception e) {
            Log.e(TAG, " > cannot open camera", e);
            if (colorCamera != null) {
                colorCamera.release();
                colorCamera = null;
            }
            if (infraredCamera != null) {
                infraredCamera.release();
                infraredCamera = null;
            }

            String msg = appContext.getResources().getString(R.string.ext_launcher_no_camera_available);
            if (cameraCallback != null) {
                cameraCallback.onErrorMessage(msg, e.getMessage());
            } else {
                msg += "\n" + e.getMessage();
                CLToast.show(msg);
            }
        }
    }

    private static Camera.CameraInfo getCameraInfo(int cameraId) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        return cameraInfo;
    }

    @Override
    public int getCameraOrientation() {
        Integer cameraOrientation = uiSettings.getCameraOrientation();
        if (cameraOrientation != null)
            return cameraOrientation;
        return getCameraInfo(colorCameraId).orientation;
    }

    @Override
    public List<Camera.Size> getResolutions() {
        try {
            return colorCamera == null ? null : colorCamera.getParameters().getSupportedPreviewSizes();
        } catch (Exception e) {
            CLToast.showLong("Cannot list all resolutions");
            return null;
        }
    }

    private int getNextCameraId() {
        int num = Camera.getNumberOfCameras();
        if (num == 0) throw new IllegalStateException("Hardware camera is unavailable");

        colorCameraId += 1;
        if (colorCameraId >= num) colorCameraId = 0;
        return colorCameraId;

//        int facing = isCameraFacingBack ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        for (int cameraId = 0; cameraId < num; cameraId++) {
//            Camera.getCameraInfo(cameraId, cameraInfo);
//            if (cameraInfo.facing == facing) {
//                Log.d(TAG, " > getCameraId: " + cameraId + ", #" + num);
//                return cameraId;
//            }
//        }
//        return 0;
    }

    private Camera.Parameters setupCameraParameters() throws IOException {
        Camera.Parameters params = colorCamera.getParameters();
        params.setPreviewFormat(ImageFormat.NV21);
        params.setPreviewSize(previewWidth, previewHeight);

        setupOrientation();
        setAutoFocusModeIfPossible(params);

        customHandler.applyParameters(params);
        colorCamera.setParameters(params);

        Camera.Parameters infraredParams = infraredCamera.getParameters();
        infraredParams.setPreviewFormat(ImageFormat.NV21);
        infraredParams.setPreviewSize(previewWidth, previewHeight);

        setAutoFocusModeIfPossible(infraredParams);
        infraredCamera.setParameters(infraredParams);

        SurfaceTexture texture = mainTextureView.getSurfaceTexture();
        assert texture != null;
        texture.setDefaultBufferSize(previewWidth, previewHeight);
        colorCamera.setPreviewTexture(texture);

        setPreviewCallbackAndBuffer(previewWidth, previewHeight);

        return params;
    }

    private void setAutoFocusModeIfPossible(Camera.Parameters parameters) {
        List<String> cameraFocusModes = parameters.getSupportedFocusModes();
        if (cameraFocusModes != null) {
            // Check if device can continuous focus video first.
            for (String focusMode : cameraFocusModes) {
                if (Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(focusMode)) {
                    Log.d(TAG, " > FOCUS_MODE_CONTINUOUS_VIDEO");
                    parameters.setFocusMode(focusMode);
                    return;
                }
            }
            // If not, check continuous focus picture then, because it would perform more
            // aggressive than FOCUS_MODE_CONTINUOUS_VIDEO.
            for (String focusMode : cameraFocusModes) {
                if (Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(focusMode)) {
                    Log.d(TAG, " > FOCUS_MODE_CONTINUOUS_PICTURE");
                    parameters.setFocusMode(focusMode);
                    return;
                }
            }
            // If not, application should use FOCUS_MODE_AUTO with autoFocus function manually.
        }
    }

    private void setPreviewCallbackAndBuffer(int width, int height) {
        int bufferLength = getBufferLength(width, height);
        initPreviewCallbackWithBuffer(bufferLength);
        frameHandler.setPreviewSize(bufferLength, width, height);

        colorCamera.setPreviewCallbackWithBuffer(null);
        Log.v(TAG, " > add frame buffers #" + mFrameBuffer.length);
        for (byte[] aFrameBuffer : mFrameBuffer) {
            colorCamera.addCallbackBuffer(aFrameBuffer);
        }
        colorCamera.setPreviewCallbackWithBuffer(onReceivedPreviewFrame);

        infraredCamera.setPreviewCallbackWithBuffer(null);
        Log.v(TAG, " > add frame buffers #" + mInfraredFrameBuffer.length);
        for (byte[] aFrameBuffer : mInfraredFrameBuffer) {
            infraredCamera.addCallbackBuffer(aFrameBuffer);
        }
        infraredCamera.setPreviewCallbackWithBuffer(onReceivedInfraredFrame);
    }

    private void initPreviewCallbackWithBuffer(int bufferLength) {
        Log.v(TAG, " > init #" + mFrameBuffer.length + " buffer size: " + bufferLength);
        for (int i = 0; i < mFrameBuffer.length; i++) {
            mFrameBuffer[i] = new byte[bufferLength];
        }
        for (int i = 0; i < mInfraredFrameBuffer.length; i++) {
            mInfraredFrameBuffer[i] = new byte[bufferLength];
        }
    }

    private void setupOrientation() {
        int displayOrientation;

        // https://developer.android.com/reference/android/hardware/Camera#setDisplayOrientation(int)
        Integer forceDisplayOrientation = uiSettings.getCameraDisplayOrientation();
        if (forceDisplayOrientation == null) {
            int facing = getCameraInfo(colorCameraId).facing;
            int cameraOrientation = getCameraOrientation();

            if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                displayOrientation = 360 - cameraOrientation;
            } else {  // back-facing
                displayOrientation = cameraOrientation;
            }
        } else {
            displayOrientation = forceDisplayOrientation;
        }

        try {
            displayOrientation %= 360; // Valid value range between 0 ~ 360.

            colorCamera.setDisplayOrientation(displayOrientation);
            infraredCamera.setDisplayOrientation(displayOrientation);
            Log.v(TAG, "setDisplayOrientation: " + displayOrientation);
        } catch (Exception e) {
            // Some devices might throw exception with unknown reason.
            // Try-catch such exception before we have devices to investigate root cause.
            Log.e(TAG, "setDisplayOrientation failed", e);
        }
    }

    @Override
    void stopCamera() {
        Log.d(TAG, "stopCamera");

        colorCameraHandler.post(this::stopColorCameraInternal);
        infraredCameraHandler.post(this::stopInfraredCameraInternal);
    }

    private void stopColorCameraInternal() {
        if (colorCamera == null) return;

        try {
            customHandler.stopCamera();

            colorCamera.stopPreview();
            colorCamera.setPreviewCallback(null);
            colorCamera.release();
        } catch (Exception e) {
            Log.e(TAG, " > stopColorCamera but failed", e);
        } finally {
            colorCamera = null;
        }
    }

    private void stopInfraredCameraInternal() {
        if (infraredCamera == null) return;

        try {
            customHandler.stopCamera();

            infraredCamera.stopPreview();
            infraredCamera.setPreviewCallback(null);
            infraredCamera.release();
        } catch (Exception e) {
            Log.e(TAG, " > stopInfraredCamera but failed", e);
        } finally {
            infraredCamera = null;
        }
    }

    @Override
    public void release() {
        super.release();

        colorCameraHandler.getLooper().quitSafely();
        infraredCameraHandler.getLooper().quitSafely();
        frameHandler.release();
    }

    @Override
    public void setLimitFps(float fps) {
        super.setLimitFps(fps);
        frameHandler.frameRateLimit.setFPS(fps);
    }

    @Override
    public void setCameraId(int cameraId) {
        int num = Camera.getNumberOfCameras();
        if (num == 0) throw new IllegalStateException("Hardware camera is unavailable");

        if (cameraId < num) this.colorCameraId = cameraId;
    }
}
