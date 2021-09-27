/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2021 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.cyberlink.facemedemo.pages.ampleinfrareddas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.IntRange;
import android.util.Log;

import com.cyberlink.faceme.FaceFeature;
import com.cyberlink.faceme.FaceLandmark;
import com.cyberlink.faceme.FaceLivenessResult;
import com.cyberlink.faceme.LicenseManager;
import com.cyberlink.faceme.LicenseOption;
import com.cyberlink.faceme.LivenessDetectConfig;
import com.cyberlink.faceme.LivenessDetectionOption;
import com.cyberlink.faceme.LivenessDetector;
import com.cyberlink.faceme.LivenessDetectorConfig;
import com.cyberlink.faceme.LivenessSingleFaceInfraredMode;
import com.cyberlink.faceme.LivenessTrackingMode;
import com.cyberlink.faceme.Pose;
import com.cyberlink.facemedemo.sdk.FaceHolder;
import com.cyberlink.facemedemo.sdk.PresentFacesHolder;
import com.cyberlink.facemedemo.ui.CLToast;
import com.cyberlink.facemedemo.ui.UiSettings;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class LivenessIRDetectionHandler {
    private static final String TAG = "FaceMe.Liveness";

    private static final long CACHE_PERIOD_MS = 5000L;

    public interface OnDetectedListener {
        void onDetected(int width, int height, PresentFacesHolder presentFaces);
    }

    public interface DumpInfraredListener {
        void dumpInfrared(PresentFacesHolder presentFaces, int width, int height, byte[] data);
    }

    private final Context context;
    private final OnDetectedListener detectListener;
    private final DumpInfraredListener dumpInfraredListener;

    private final Handler detectionHandler;
    private final Handler mainHandler;
    private final UiSettings uiSettings;

    private final AtomicBoolean isReleased = new AtomicBoolean(false);
    private final AtomicBoolean isDetecting = new AtomicBoolean(false);
    private final ArrayList<PresentInfraredData> presentInfraredDatasCache = new ArrayList<>();

    private LivenessDetector detector = null;

    private class PresentInfraredData {
        public final long presentationMs;
        public final int width;
        public final int height;
        public final byte[] data;

        public PresentInfraredData(long presentationMs, int width, int height, byte[] data) {
            this.presentationMs = presentationMs;
            this.width = width;
            this.height = height;
            this.data = data;
        }
    }

    public LivenessIRDetectionHandler(Context context, UiSettings uiSettings, OnDetectedListener detectListener) {
        this(context, uiSettings, detectListener, null);
    }

    public LivenessIRDetectionHandler(Context context, UiSettings uiSettings, OnDetectedListener detectListener, DumpInfraredListener dumpInfraredListener) {
        this.context = context.getApplicationContext();
        this.detectListener = detectListener;
        this.dumpInfraredListener = dumpInfraredListener;

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.detectionHandler = new Handler(thread.getLooper());
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.uiSettings = uiSettings;

        initialize();
    }

    private void initialize() {
        detectionHandler.post(() -> initDetector());
    }

    private void initDetector() {
        if (isReleased.get()) return;
        try {
            releaseDetector();

            int result;
            long start = System.currentTimeMillis();

            detector = new LivenessDetector();
            LivenessDetectorConfig config = new LivenessDetectorConfig();
            config.infraredCameraId = "ample_infrared";
            result = detector.initialize(config, "");
            if (result < 0) throw new IllegalStateException("Initialize liveness detector failed: " + result);

            result = detector.setDetectionOption(LivenessDetectionOption.TRACKING_MODE, LivenessTrackingMode.SINGLE_FACE_MOTION);
            if (result < 0) throw new IllegalStateException("Enable motion tracking but failed: " + result);

            result = detector.setDetectionOption(LivenessDetectionOption.SINGLE_FACE_MOTION_TRACKING_SPEED_LEVEL, uiSettings.get3DasSpeedLevel());
            if (result < 0) throw new IllegalStateException("Set single face tracking speed level but failed: " + result);

            result = detector.setDetectionOption(LivenessDetectionOption.SINGLE_FACE_INFRARED_MODE, LivenessSingleFaceInfraredMode.INFRARED);
            if (result < 0) throw new IllegalStateException("Set single face infrared mode but failed: " + result);

            Log.v(TAG, " > initDetector took " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            Log.e(TAG, "Cannot setup FaceMe LivenessDetector", e);
            CLToast.showLong("Cannot setup FaceMe LivenessDetector\n" + e.getMessage());
            releaseDetector();
            checkLicenseOption();
        }
    }

    private void checkLicenseOption() {
        LicenseManager licenseManager = null;
        try {
            licenseManager = new LicenseManager();
            int result = licenseManager.initializeEx();
            if (result < 0) throw new IllegalStateException("Initialize license manager failed: " + result);

            Object value = licenseManager.getProperty(LicenseOption.LIVENESS_DETECTOR);
            if (!(value instanceof Boolean) || !((Boolean) value)) {
                CLToast.showLong("LivenessDetector is not authorized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot get LicenseOption", e);
        } finally {
            if (licenseManager != null) licenseManager.release();
        }
    }

    private void releaseDetector() {
        if (detector != null) {
            Log.v(TAG, " > releaseDetector");
            detector.release();
            detector = null;
        }
    }

    public void release() {
        detectionHandler.removeCallbacksAndMessages(null);
        detectionHandler.post(() -> {
            releaseDetector();
            detectionHandler.getLooper().quitSafely();
        });

        mainHandler.removeCallbacksAndMessages(null);
    }

    public void onFrame(float confidenceThreshold, Bitmap image, PresentFacesHolder presentFaces, boolean isDiscontinuity) {
        if (!uiSettings.isShowPose()) {
            CLToast.showLong("Enable face angle extraction to do liveness detection.");
            return;
        }

        if (isDetecting.getAndSet(true)) return;

        detectionHandler.post(() -> {
            if (detector == null) {
                isDetecting.set(false);
                return;
            }

            PresentInfraredData irData = chooseSuitableIrData(presentFaces.presentationMs);
            if (irData == null) {
                isDetecting.set(false);
                return;
            }

            int numOfFaces = presentFaces.faces.size();
            if (numOfFaces == 1) {
                FaceHolder face;
                LivenessDetectConfig livenessDetectConfig = new LivenessDetectConfig();

                ArrayList<Rect> boundingBoxes = new ArrayList<>(numOfFaces);
                ArrayList<FaceFeature> faceFeatures = new ArrayList<>(numOfFaces);
                ArrayList<Pose> poses = new ArrayList<>(numOfFaces);
                ArrayList<FaceLandmark> landmarks = new ArrayList<>(numOfFaces);
                for (int index = 0; index < numOfFaces; index++) {
                    face = presentFaces.faces.get(index);
                    boundingBoxes.add(face.faceInfo.boundingBox);
                    faceFeatures.add(face.faceFeature);
                    poses.add(face.faceAttribute.pose);
                    landmarks.add(face.faceLandmark);
                }

                livenessDetectConfig.confidenceThreshold = confidenceThreshold;
                livenessDetectConfig.faceFeatures = faceFeatures;
                livenessDetectConfig.landmarks = landmarks;
                livenessDetectConfig.poses = poses;
                livenessDetectConfig.discontinuity = isDiscontinuity;
                livenessDetectConfig.infraredData = irData.data;
                livenessDetectConfig.infraredDataWidth = irData.width;
                livenessDetectConfig.infraredDataHeight = irData.height;

                long start = System.currentTimeMillis();
                FaceLivenessResult[] result = detector.detect(livenessDetectConfig, image,
                        boundingBoxes, numOfFaces);
                if (result != null) {
                    long duration = System.currentTimeMillis() - start;
                    if (duration > 50) Log.v(TAG, " > detection took " + duration + "ms");

                    for (int index = 0; index < numOfFaces; index++) {
                        face = presentFaces.faces.get(index);
                        face.liveness.status = result[index].status;
                        face.liveness.probability = result[index].probability;
                    }
                }
            }

            if (dumpInfraredListener != null) {
                mainHandler.post(() -> dumpInfraredListener.dumpInfrared(presentFaces, irData.width, irData.height, irData.data));
            }

            mainHandler.post(() -> detectListener.onDetected(irData.width, irData.height, presentFaces));
            isDetecting.set(false);
        });
    }

    public void onIrFrame(long presentationMs, int width, int height, byte[] data) {
        detectionHandler.post(() -> {
            if (detector == null) return;

            int position = presentInfraredDatasCache.size() - 1;
            PresentInfraredData cacheItem;

            for (; position >= 0; position--) {
                cacheItem = presentInfraredDatasCache.get(position);
                if (presentationMs > cacheItem.presentationMs) break;
            }

            presentInfraredDatasCache.add(position + 1, new PresentInfraredData(presentationMs, width, height, data));

            for (; position >= 0; position--) {
                cacheItem = presentInfraredDatasCache.get(position);
                if ((presentationMs - cacheItem.presentationMs) > CACHE_PERIOD_MS)
                    presentInfraredDatasCache.remove(position);
            }
        });
    }

    private PresentInfraredData chooseSuitableIrData(long presentationMs) {
        long minPresentationMs = CACHE_PERIOD_MS;
        int nearestPosition = -1;
        for (int position = presentInfraredDatasCache.size() - 1; position >= 0; position--) {
            PresentInfraredData cacheItem = presentInfraredDatasCache.get(position);
            long diffPresentationMs = Math.abs(presentationMs - cacheItem.presentationMs);
            if (diffPresentationMs <= minPresentationMs) {
                minPresentationMs = diffPresentationMs;
                nearestPosition = position;
            } else {
                break;
            }
        }

        if (nearestPosition < 0) return null;
        return presentInfraredDatasCache.get(nearestPosition);
    }

    public void setDetectionOption(@LivenessDetectionOption.ELivenessDetectionOption int option, @IntRange(from = 0) int value) {
        int result = detector.setDetectionOption(option, value);

        if (result < 0) throw new IllegalStateException("setDetectionOption but failed: " + result);
    }

    public boolean isDetecting() {
        return isDetecting.get();
    }
}
