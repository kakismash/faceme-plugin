/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import androidx.annotation.IntRange;

import java.util.Arrays;

public class StatListener {

    private static final int ACCUMULATED_NUMBER = 3;

    /**
     * A stat class to average numbers of values to smooth average curve.
     */
    private static class MovingAverage {

        private final int windowSize;

        private transient boolean firstValue;
        private transient double sum;
        private transient final double[] circularBuffer;
        private transient int circularBufferIndex;

        static MovingAverage create() {
            return new MovingAverage(ACCUMULATED_NUMBER);
        }

        MovingAverage(@IntRange(from=1) int size) {
            this.windowSize = size;
            this.circularBuffer = new double[size];
            this.firstValue = true;
        }

        void reset() {
            Arrays.fill(circularBuffer, 0);
            circularBufferIndex = 0;
            sum = 0;
            firstValue = true;
        }

        boolean notFirstValue(double value) {
            if (firstValue) {
                Arrays.fill(circularBuffer, value);
                sum = value * windowSize;
                firstValue = false;
                return false;
            } else {
                return true;
            }
        }

        void addValue(double value) {
            if (notFirstValue(value)) {
                sum -= circularBuffer[circularBufferIndex];
                circularBuffer[circularBufferIndex++] = value;
                sum += value;
                if (circularBufferIndex >= windowSize) {
                    circularBufferIndex = 0;
                }
            }
        }

        double getAverage() {
            return sum / windowSize;
        }
    }

    private final FpsMeter      fpsFramesCaptured = new FpsMeter();
    private final FpsMeter      fpsImagesCaptured = new FpsMeter();

    private final FpsMeter      fpsBitmapsCreated = new FpsMeter();
    private final MovingAverage avgTimeBitmapsRotated = MovingAverage.create(); // time
    private final MovingAverage avgTimeBitmapsTook = MovingAverage.create(); // time

    private final FpsMeter      fpsFaceExtracted = new FpsMeter();
    private final MovingAverage avgTimeFacesTotal = MovingAverage.create(); // time
    private final MovingAverage avgTimeFacesDetected = MovingAverage.create(); // time
    private final MovingAverage avgTimeFacesExtracted = MovingAverage.create(); // time
    private final MovingAverage avgTimeFacesRecognized = MovingAverage.create(); // time

    private final FpsMeter      fpsDisparityCallback = new FpsMeter();
    private final FpsMeter      fpsDistanceCallback = new FpsMeter();
    private final MovingAverage avgTimeDistanceCallback = MovingAverage.create(); // time

    /** Reset all average values */
    public void reset() {
        fpsFramesCaptured.reset();
        fpsImagesCaptured.reset();

        fpsBitmapsCreated.reset();
        avgTimeBitmapsRotated.reset();
        avgTimeBitmapsTook.reset();

        fpsFaceExtracted.reset();
        avgTimeFacesTotal.reset();
        avgTimeFacesDetected.reset();
        avgTimeFacesExtracted.reset();
        avgTimeFacesRecognized.reset();

        fpsDisparityCallback.reset();
        fpsDistanceCallback.reset();
        avgTimeDistanceCallback.reset();
    }

    void onFrameCaptured() {
        fpsFramesCaptured.measure();
    }

    void onImageCaptured() {
        fpsImagesCaptured.measure();
    }

    void onBitmapRotated(long duration) {
        avgTimeBitmapsRotated.addValue(duration);
    }

    void onBitmapCreated(long duration) {
        fpsBitmapsCreated.measure();
        avgTimeBitmapsTook.addValue(duration);
    }

    public void onFacesExtracted() {
        fpsFaceExtracted.measure();
    }

    public void onFacesRecognized(int count, long total, long detect, long extract, long recognize) {
        avgTimeFacesTotal.addValue(total);

        if (detect > 0) {
            avgTimeFacesDetected.addValue(detect);
        }
        if (extract > 0) {
            avgTimeFacesExtracted.addValue(extract);
        }
        if (count > 0) { // recognize time is too small, so we depend on count instead.
            avgTimeFacesRecognized.addValue(recognize);
        }
    }

    public void onDisparityCallback() {
        fpsDisparityCallback.measure();
    }

    public void onDistanceCallback(long ms) {
        fpsDistanceCallback.measure();
        avgTimeDistanceCallback.addValue(ms);
    }

    public float getFrameCapturedFps() {
        return fpsFramesCaptured.fps();
    }

    public float getImageCapturedFps() {
        return fpsImagesCaptured.fps();
    }

    public float getBitmapCreatedFps() {
        return fpsBitmapsCreated.fps();
    }

    public double getAverageBitmapRotatedTime() {
        return avgTimeBitmapsRotated.getAverage();
    }

    public double getAverageBitmapCreatedTime() {
        return avgTimeBitmapsTook.getAverage();
    }

    public float getFaceExtractedFps() {
        return fpsFaceExtracted.fps();
    }

    public double getAverageFaceTotalTime() {
        return avgTimeFacesTotal.getAverage();
    }

    public double getAverageFaceDetectedTime() {
        return avgTimeFacesDetected.getAverage();
    }

    public double getAverageFaceExtractedTime() {
        return avgTimeFacesExtracted.getAverage();
    }

    public double getAverageFaceRecognizedTime() {
        return avgTimeFacesRecognized.getAverage();
    }

    public float getDisparityCallbackFps() {
        return fpsDisparityCallback.fps();
    }

    public float getDistanceCallbackFps() {
        return fpsDistanceCallback.fps();
    }

    public double getAverageDistanceCallbackTime() {
        return avgTimeDistanceCallback.getAverage();
    }
}
