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
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;

import com.cyberlink.faceme.LivenessSingleFaceInfraredMode;

import com.intel.realsense.librealsense.Align;
import com.intel.realsense.librealsense.Config;
import com.intel.realsense.librealsense.DepthFrame;
import com.intel.realsense.librealsense.Device;
import com.intel.realsense.librealsense.DeviceList;
import com.intel.realsense.librealsense.DeviceListener;
import com.intel.realsense.librealsense.Extension;
import com.intel.realsense.librealsense.Frame;
import com.intel.realsense.librealsense.FrameReleaser;
import com.intel.realsense.librealsense.FrameSet;
import com.intel.realsense.librealsense.Option;
import com.intel.realsense.librealsense.Pipeline;
import com.intel.realsense.librealsense.RsContext;
import com.intel.realsense.librealsense.Sensor;
import com.intel.realsense.librealsense.StreamFormat;
import com.intel.realsense.librealsense.StreamType;
import com.intel.realsense.librealsense.VideoFrame;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


class RealSenseCamera extends BaseCameraController {
    private static final String TAG = "FaceMe.RealSense";
    private final CLGLRsSurfaceView mGLSurfaceView;
    private RsContext mRsContext;
    private final CameraController.Callback cameraCallback;

    private final int previewWidth = 640;
    private final int previewHeight = 480;

    private final Handler mStreamingHandler = new Handler();
    private final Handler frameHandler;
    private final Handler createBitmapHandler;
    private Pipeline mPipeline;
    private boolean mIsStreaming = false;

    private final Align mAlignToImageHelper = new Align(StreamType.COLOR);

    private final Runnable mStreaming;

    private float mLaserPower;

    private final AtomicBoolean isHandlingFrame = new AtomicBoolean(false);

    private final DepthUiSettings depthUiSettings;

    RealSenseCamera(Activity activity, TextureView mainTextureView, AutoFitSurfaceView subSurfaceView, Callback callback, StatListener listener, CLGLRsSurfaceView glSurfaceView) {
        super(activity, mainTextureView, subSurfaceView, callback, listener);

        mGLSurfaceView = glSurfaceView;
        cameraCallback = callback;

        RsContext.init(DemoApp.getContext());

        depthUiSettings = new DepthUiSettings(DemoApp.getContext());

        HandlerThread frameHandlerThread = new HandlerThread(TAG);
        frameHandlerThread.start();
        this.frameHandler = new Handler(frameHandlerThread.getLooper());

        HandlerThread bitmapCreationThread = new HandlerThread(TAG);
        bitmapCreationThread.start();
        this.createBitmapHandler = new Handler(bitmapCreationThread.getLooper());

        mStreaming = new Runnable() {
            @Override
            public void run() {
                if (!mIsStreaming)
                    return;

                try (FrameReleaser fr = new FrameReleaser()) {
                    try (FrameSet frames = mPipeline.waitForFrames()) {
                        Frame frameForPreview = frames.first(StreamType.COLOR, StreamFormat.RGB8).releaseWith(fr);
                        mGLSurfaceView.upload(frameForPreview);
                        if (statListener != null) {
                            statListener.onFrameCaptured();
                        }
                        if (!isHandlingFrame.getAndSet(true)) {
                            FrameSet mFrames = frames.clone();
                            frameHandler.post(() -> {
                                onFrame(mFrames);
                                isHandlingFrame.set(false);
                            });
                        }
                    }
                    mStreamingHandler.post(this);
                } catch (Exception e) {
                    Log.d(TAG, "streaming, error: " + e.getMessage());
                }
            }
        };
    }

    @Override
    public int getCameraOrientation() {
        return 0;
    }

    private void initCamera() {
        //Register to notifications regarding RealSense devices attach/detach events via the DeviceListener.
        mRsContext = new RsContext();
        DeviceList deviceList = mRsContext.queryDevices();
        Device device = deviceList.createDevice(0);
        if (device == null) {
            CLToast.show("Cannot find D415 camera.");
            return;
        }
        List<Sensor> sensors = device.querySensors();

        // load Intel RealSense D415 preset
        try {
            InputStream is = appContext.getAssets().open("faceme_model/model/das/d415/RealSenseD415_preset.json");
            int size = is.available();
            byte[] presetData = new byte[size];
            int totalByte = is.read(presetData);
            is.close();
            if (totalByte > 0) {
                device.loadPresetFromJson(presetData);
            } else {
                CLToast.show("Error: cannot load D415 preset.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            for (Sensor s : sensors) {
                if (s.supports(Option.LASER_POWER)) {
                    float desiredLaserPower = 270f;
                    if (depthUiSettings.get3DasInfraredMode() == LivenessSingleFaceInfraredMode.INFRARED) {
                        desiredLaserPower = depthUiSettings.get3DasInfraredLaserPower() == 0 ? 270f : depthUiSettings.get3DasInfraredLaserPower();
                    }
                    mLaserPower = desiredLaserPower;
                    s.setValue(Option.LASER_POWER, mLaserPower);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        mRsContext.setDevicesChangedCallback(new DeviceListener() {
            @Override
            public void onDeviceAttach() {
                Log.d(TAG, "onDeviceAttach");
                restart();
            }
            @Override
            public void onDeviceDetach() {
                Log.d(TAG, "onDeviceDetach");
                stopCamera();
            }
        });
        mPipeline = new Pipeline();
    }

    private void startCamera() {
        if (!checkRealSenseCamera() || mIsStreaming)
            return;
        initCamera();
        mGLSurfaceView.clear();

        mGLSurfaceView.setMirror(depthUiSettings.isFlipCameraDisplay());

//        Default: 640, 480
//        Intel RealSense D415
//        RGB resolution: 424x240, 1280x720, 640x480
//        Depth resolution: 480x270, 1280x720, 640x480
        try (Config config = new Config()) {
            config.enableStream(StreamType.COLOR, -1, previewWidth, previewHeight, StreamFormat.ANY, 15);
            config.enableStream(StreamType.DEPTH, -1, previewWidth, previewHeight, StreamFormat.ANY, 15);
            config.enableStream(StreamType.INFRARED, -1, previewWidth, previewHeight, StreamFormat.Y8, 15);
            mPipeline.start(config);
        } catch (Exception e) {
            Log.e(TAG, "Cannot start pipeline", e);
        }

        mIsStreaming = true;
        mStreamingHandler.post(mStreaming);
    }

    @Override
    void stopCamera() {
        if (!mIsStreaming)
            return;

        mIsStreaming = false;
        mRsContext.close();
        mStreamingHandler.removeCallbacks(mStreaming);
        frameHandler.removeCallbacksAndMessages(null);
        createBitmapHandler.removeCallbacksAndMessages(null);
        mPipeline.stop();
        mGLSurfaceView.clear();
    }

    private boolean checkRealSenseCamera() {
        ExternalDevice.update();
        return ExternalDevice.get() == ExternalDevice.INTEL_REALSENSE_D415;
    }

    private void onFrame(FrameSet mFrames) {
        try (FrameReleaser fr = new FrameReleaser()) {
            mFrames.releaseWith(fr);
            FrameSet mAlignedFrame = mFrames.applyFilter(mAlignToImageHelper).releaseWith(fr);
            if (statListener != null) {
                statListener.onImageCaptured();
            }
            mAlignedFrame.foreach((Frame frame) -> {
                long mFrameTimestamp = System.currentTimeMillis();
                if (frame.getProfile().getType().equals(StreamType.COLOR) && frame.getProfile().getFormat().equals(StreamFormat.RGB8)) {
                    CameraController.CheckCallback callback = () -> {
                        try (VideoFrame mVideoFrame = frame.as(Extension.VIDEO_FRAME)) {
                            byte[] imageByteArray = new byte[mVideoFrame.getDataSize()];
                            mVideoFrame.getData(imageByteArray);
                            int videoWidth = mVideoFrame.getWidth();
                            int videoHeight = mVideoFrame.getHeight();
                            createBitmapHandler.post(() -> {
                                Bitmap mImageBitmap = convertRGB24ToBitmap(imageByteArray, videoWidth, videoHeight);
                                if (statListener != null) {
                                        statListener.onBitmapCreated(System.currentTimeMillis() - mFrameTimestamp);
                                }
                                cameraCallback.onBitmap(mFrameTimestamp, mImageBitmap);
                            });
                        }
                    };
                    cameraCallback.checkTask(callback);
                } else if (frame.getProfile().getType().equals(StreamType.DEPTH) && frame.getProfile().getFormat().equals(StreamFormat.Z16)) {
                    CameraController.CheckCallback callback = () -> {
                        try (DepthFrame mDepthFrame = frame.as(Extension.DEPTH_FRAME)) {
                            byte[] depthByteArray = new byte[mDepthFrame.getDataSize()];
                            mDepthFrame.getData(depthByteArray);
                            if (statListener != null) {
                                statListener.onDistanceCallback(System.currentTimeMillis() - mFrameTimestamp);
                            }
                            cameraCallback.onDistanceMap(mFrameTimestamp, mDepthFrame.getWidth(), mDepthFrame.getHeight(), depthByteArray);
                        }
                    };
                    cameraCallback.checkTask(callback);
                } else if (frame.getProfile().getType().equals(StreamType.INFRARED) && frame.getProfile().getFormat().equals(StreamFormat.Y8)) {
                    try (VideoFrame mVideoFrame = frame.as(Extension.VIDEO_FRAME)) {
                        byte[] imageByteArray = new byte[mVideoFrame.getDataSize()];
                        mVideoFrame.getData(imageByteArray);
                        int videoWidth = mVideoFrame.getWidth();
                        int videoHeight = mVideoFrame.getHeight();

                        createBitmapHandler.post(() -> {
                            cameraCallback.onInfraredData(mFrameTimestamp, videoWidth, videoHeight, imageByteArray);
                        });
                    }
                }
            });
        }
    }

    // Convert a RGB byte array to Bitmap and each channel of RGB has 8 bits.
    private Bitmap convertRGB24ToBitmap(byte[] byteArray, int width, int height) {
        Bitmap colorBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int numPixels = width * height;
        int[] colors = new int[numPixels];
        for (int idx = 0; idx < numPixels; idx++) {
            int r = (byteArray[3*idx] & 0xFF);
            int g = (byteArray[3*idx+1] & 0xFF);
            int b = (byteArray[3*idx+2] & 0xFF);
            colors[idx] = Color.rgb(r, g, b);
        }
        colorBitmap.setPixels(colors, 0, width, 0, 0, width, height);
        return colorBitmap;
    }

    @Override
    public int getUiLogicalCameraNum() {
        return mRsContext.queryDevices().getDeviceCount();
    }

    @Override
    public void restart() {
        stopCamera();
        startCamera();
    }

    @Override
    public void pause() {
        stopCamera();
    }

    @Override
    public void resume() {
        startCamera();
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    public boolean switchCamera() {
        return false;
    }

    @Override
    public <S> List<S> getResolutions() {
        return null;
    }

    @Override
    public Size getCurrentResolution() {
        return new Size(previewWidth, previewHeight);
    }

    @Override
    public void setLaserPower(float newValue) {
        if (newValue > 360F || newValue < 0F || newValue == mLaserPower || !mIsStreaming) {
            return;
        }
        DeviceList deviceList = mRsContext.queryDevices();
        if (deviceList.getDeviceCount() < 1) {
            CLToast.show("Cannot find D415 camera.");
            return;
        }
        Device device = deviceList.createDevice(0);

        List<Sensor> sensors = device.querySensors();
        try {
            for (Sensor s : sensors) {
                if (s.supports(Option.LASER_POWER)) {
                    s.setValue(Option.LASER_POWER, newValue);
                    mLaserPower = s.getValue(Option.LASER_POWER);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
