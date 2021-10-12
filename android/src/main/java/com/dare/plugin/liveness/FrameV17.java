/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.util.concurrent.atomic.AtomicBoolean;

class FrameV17 implements DepthDistanceCallback {
    private static final String TAG = "FaceMe.CameraF17";

    private final BaseCameraController cameraController;
    private CameraController.Callback cameraCallback = new CameraController.Callback() {};
    private final StatListener statListener;
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB scriptYuv2Rgb;
    private ScriptC_rotator scriptRotator;
    private final Handler workerHandler;
    private final Handler infraredWorkerHandler;
    private final AtomicBoolean isHandlingFrame = new AtomicBoolean(false);
    private final AtomicBoolean isHandlingInfraredFrame = new AtomicBoolean(false);
    final FrameRateLimit frameRateLimit = new FrameRateLimit(10F);

    private int previewWidth;
    private int previewHeight;
    private Allocation srcAllocation;
    private Allocation dstAllocation;
    private Allocation landscapeAllocation;
    private Allocation portraitAllocation;

    FrameV17(BaseCameraController cameraController, CameraController.Callback callback, StatListener listener) {
        this.cameraController = cameraController;
        this.frameRateLimit.setFPS(cameraController.limitFps);
        if (callback != null) this.cameraCallback = callback;
        this.statListener = listener;
        this.rs = RenderScript.create(cameraController.appContext);
        this.scriptYuv2Rgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        this.scriptRotator = new ScriptC_rotator(rs);
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.workerHandler = new Handler(thread.getLooper());
        HandlerThread infraredThread = new HandlerThread(TAG);
        infraredThread.start();
        this.infraredWorkerHandler = new Handler(infraredThread.getLooper());
    }

    void setCameraCallback(CameraController.Callback callback) {
        if (callback != null)
            this.cameraCallback = callback;
        else
            this.cameraCallback = new CameraController.Callback() {};
    }

    void setPreviewSize(int bufferLength, int previewWidth, int previewHeight) {
        workerHandler.removeCallbacksAndMessages(null);
        workerHandler.post(() -> {
            FrameV17.this.previewWidth = previewWidth;
            FrameV17.this.previewHeight = previewHeight;

            setupAllocations(bufferLength);
        });
    }

    private void setupAllocations(int bufferLength) {
        Type.Builder srcBuilder = new Type.Builder(rs, Element.U8(rs))
                .setX(bufferLength)
                .setMipmaps(false);
        if (srcAllocation != null) srcAllocation.destroy();
        srcAllocation = Allocation.createTyped(rs, srcBuilder.create(),
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Type.Builder dstBuilder = new Type.Builder(rs, Element.RGBA_8888(rs))
                .setX(previewWidth)
                .setY(previewHeight)
                .setMipmaps(false);
        if (dstAllocation != null) dstAllocation.destroy();
        dstAllocation = Allocation.createTyped(rs, dstBuilder.create(),
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Type.Builder portraitBuilder = new Type.Builder(rs, Element.RGBA_8888(rs))
                .setX(previewHeight)
                .setY(previewWidth)
                .setMipmaps(false);
        if (portraitAllocation != null) portraitAllocation.destroy();
        portraitAllocation = Allocation.createTyped(rs, portraitBuilder.create(),
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Type.Builder landscapeBuilder = new Type.Builder(rs, Element.RGBA_8888(rs))
                .setX(previewWidth)
                .setY(previewHeight)
                .setMipmaps(false);
        if (landscapeAllocation != null) landscapeAllocation.destroy();
        landscapeAllocation = Allocation.createTyped(rs, landscapeBuilder.create(),
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        scriptRotator.set_inWidth(previewWidth);
        scriptRotator.set_inHeight(previewHeight);
    }

    void release() {
        releaseRenderScript();
        workerHandler.getLooper().quitSafely();
        infraredWorkerHandler.getLooper().quitSafely();
    }

    private void releaseRenderScript() {
        if (rs != null) {
            rs.destroy();
            rs = null;
        }
        if (scriptYuv2Rgb != null) {
            scriptYuv2Rgb.destroy();
            scriptYuv2Rgb = null;
        }
        if (scriptRotator != null) {
            scriptRotator.destroy();
            scriptRotator = null;
        }
    }

    void onFrame(long presentationMs, byte[] data, boolean isCameraFacingBack, Runnable callback) {
        if (isHandlingFrame.getAndSet(true)) {
            callback.run();
            return;
        }

        workerHandler.post(() -> {
            // Do something from data.
            handleFrame(presentationMs, data, isCameraFacingBack);

            isHandlingFrame.set(false);
            callback.run();
        });
    }

    private void handleFrame(long presentationMs, byte[] data, boolean isCameraFacingBack) {
        long start = System.currentTimeMillis();
        srcAllocation.copy1DRangeFromUnchecked(0, data.length, data);
        scriptYuv2Rgb.setInput(srcAllocation);
        scriptYuv2Rgb.forEach(dstAllocation);
        if (statListener != null) statListener.onImageCaptured();

        CameraController.CheckCallback callback = () -> {
            Bitmap bitmap = rotateOrFlip(isCameraFacingBack);
            Bitmap output = cameraController.customHandler.createFrameBitmap(bitmap);
            if (statListener != null) statListener.onBitmapCreated(System.currentTimeMillis() - start);

            cameraCallback.onBitmap(presentationMs, output);
        };

        if (cameraController.limitFps > 0) {
            frameRateLimit.await();
        }

        if (cameraController.concurrentBitmapEnabled) {
            callback.acquired();
        } else {
            cameraCallback.checkTask(callback);
        }
    }

    private Bitmap rotateOrFlip(boolean isCameraFacingBack) {
        long start = System.currentTimeMillis();

        BaseCameraController.DeviceRotation deviceRotation = cameraController.getDeviceRotationInfo();
        if (cameraController.forceRotateDegrees != null) {
            if (cameraController.forceRotateDegrees == 90 || cameraController.forceRotateDegrees == 270) {
                deviceRotation.isPortrait = !deviceRotation.isPortrait;
            }
            deviceRotation.degree -= cameraController.forceRotateDegrees; // Force rotate clockwise.
        }

        // XXX: Sometimes degree is not the same when retrieved at this moment. So we use degree
        //      previous caught in deviceRotation argument but not retrieve the latest one.
        int deviceRotationDegree = deviceRotation.degree;

        // https://developer.android.com/reference/android/hardware/Camera.CameraInfo#orientation
        //  The value is the angle that the camera image needs to be rotated clockwise
        //   so it shows correctly on the display in its natural orientation (device rotation == 0).
        int rotationDegree = cameraController.getCameraOrientation();
        if (isCameraFacingBack)
            rotationDegree -= deviceRotationDegree;
        else
            rotationDegree += deviceRotationDegree;
        rotationDegree = (360 + rotationDegree) % 360;

        boolean needMirror = !isCameraFacingBack;
        if (cameraController.isOutputMirror) {
            needMirror = !needMirror;
        }

        Bitmap bitmap;
        Allocation allocation;

        // Assumed the orientation of the original image from camera is always landscape
        if (rotationDegree == 90 || rotationDegree == 270) {
            //noinspection SuspiciousNameCombination
            bitmap = Bitmap.createBitmap(previewHeight, previewWidth, Bitmap.Config.ARGB_8888);
            allocation = portraitAllocation;
        } else { // 0 or 180
            bitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
            allocation = landscapeAllocation;
        }

        performRenderScriptRotation(rotationDegree, needMirror, allocation);
        allocation.copyTo(bitmap);

        if (statListener != null) statListener.onBitmapRotated(System.currentTimeMillis() - start);

        return bitmap;
    }

    private void performRenderScriptRotation(int rotationDegree, boolean needMirror, Allocation allocation) {
        scriptRotator.set_inImage(dstAllocation);

        if (rotationDegree == 0) {
            if (needMirror)
                scriptRotator.forEach_flipH(allocation, allocation);
            else
                scriptYuv2Rgb.forEach(allocation); // Device natural orientation.
        } else if (rotationDegree == 90) {
            if (needMirror)
                scriptRotator.forEach_rotate90ccwFlipV(allocation, allocation);
            else
                scriptRotator.forEach_rotate90ccw(allocation, allocation);
        } else if (rotationDegree == 180) {
            if (needMirror)
                scriptRotator.forEach_flipV(allocation, allocation);
            else
                scriptRotator.forEach_rotate180cw(allocation, allocation);
        } else if (rotationDegree == 270) {
            if (needMirror)
                scriptRotator.forEach_rotate90ccwFlipH(allocation, allocation);
            else
                scriptRotator.forEach_rotate90cw(allocation, allocation);
        }
    }

    
    public void onDistanceMapData(long presentationMs, int width, int height, byte[] data) {
        cameraCallback.onDistanceMap(presentationMs, width, height, data);
    }

    void onInfraredData(long presentationMs, byte[] data, Runnable callback) {
        if (isHandlingInfraredFrame.getAndSet(true)) {
            callback.run();
            return;
        }

        infraredWorkerHandler.post(() -> {
            byte[] yData = getYValueFromYUVImage(previewWidth, previewHeight, data);
            cameraCallback.onInfraredData(presentationMs, previewWidth, previewHeight, yData);

            isHandlingInfraredFrame.set(false);
            callback.run();
        });
    }

    byte[] getYValueFromYUVImage(int width, int height, byte[] data) {
        byte[] yData = new byte[width * height];
        // YUV is a planar format. Only get Y value for infrared data. https://en.wikipedia.org/wiki/YUV
        System.arraycopy(data, 0, yData, 0, width * height);
        return yData;
    }
}
