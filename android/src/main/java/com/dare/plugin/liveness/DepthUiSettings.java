/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.content.Context;
import android.content.SharedPreferences;

import com.cyberlink.faceme.ExtractionModelSpeedLevel;
import com.cyberlink.faceme.LivenessSingleFaceInfraredMode;
import com.cyberlink.faceme.LivenessSingleFaceMotionTrackingSpeedLevel;

public class DepthUiSettings extends UiSettings {
    private static final String PREFERENCE_NAME = "com.cyberlink.FaceMe.DepthSettings";

    static final int DEFAULT_MIN_FACE_WIDTH = 80;
    static final float DEFAULT_MIN_FACE_WIDTH_RATIO = 0.12f;

    private static final String extractModel = "extractModel_v3";
    private static final String showFeatures = "showFeatures";
    private static final String showAge = "showAge";
    private static final String ageInRange = "ageInRange";
    private static final String showGender = "showGender";
    private static final String showEmotion = "showEmotion";
    private static final String minFaceWidthRatio = "minFaceWidthRatio";

    private static final String AsSpeedLevel = "3DasSpeedLevel";
    private static final String AsInfraredMode = "3DasInfraredMode";
    private static final String AsLaserPower = "InfraredAsLaserPower";

    private static final String flipCameraDisplay = "flipCameraDisplay";

    private final SharedPreferences pref;

    public DepthUiSettings(Context context) {
        super(context);
        this.pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private void setInteger(String key, int value) {
        pref.edit().putInt(key, value).apply();
    }
    private void setFloat(String key, float value) {
        pref.edit().putFloat(key, value).apply();
    }
    private void setBoolean(String key, boolean value) {
        pref.edit().putBoolean(key, value).apply();
    }

    void setExtractModel(@ExtractionModelSpeedLevel.EExtractionModelSpeedLevel int value) {
        setInteger(extractModel, value);
    }
    @ExtractionModelSpeedLevel.EExtractionModelSpeedLevel
    public int getExtractModel() {
        return pref.getInt(extractModel, ExtractionModelSpeedLevel.H6);
    }

    
    void setShowFeatures(boolean enable) {
        throw new IllegalStateException("3D Anti-spoofing do not need extract face feature!");
    }
    
    public boolean isShowFeatures() {
        return false;
    }

    
    void setShowAge(boolean enable) {
        throw new IllegalStateException("3D Anti-spoofing do not need extract age!");
    }
    
    public boolean isShowAge() {
        return false;
    }

    
    void setAgeInRange(boolean enable) {
        throw new IllegalStateException("3D Anti-spoofing do not need extract age range!");
    }
    
    public boolean isAgeInRange() {
        return false;
    }

    
    void setShowGender(boolean enable) {
        throw new IllegalStateException("3D Anti-spoofing do not need extract gender!");
    }
    
    public boolean isShowGender() {
        return false;
    }

    
    void setShowEmotion(boolean enable) {
        throw new IllegalStateException("3D Anti-spoofing do not need extract emotion!");
    }
    
    public boolean isShowEmotion() {
        return false;
    }

    
    void setShowPose(boolean enable) {
        throw new IllegalStateException("3D Anti-spoofing need extract pose!");
    }
    
    public boolean isShowPose() {
        return true;
    }

    
    void setMinFaceWidthRatio(float value) {
        setFloat(minFaceWidthRatio, value);
    }
    
    public float getMinFaceWidthRatio() {
        return pref.getFloat(minFaceWidthRatio, DEFAULT_MIN_FACE_WIDTH_RATIO);
    }

    void set3DasSpeedLevel(@LivenessSingleFaceMotionTrackingSpeedLevel.EThreeDASSpeedLevel int value) {
        setInteger(AsSpeedLevel, value);
    }
    @LivenessSingleFaceMotionTrackingSpeedLevel.EThreeDASSpeedLevel
    public int get3DasSpeedLevel() {
        return pref.getInt(AsSpeedLevel, LivenessSingleFaceMotionTrackingSpeedLevel.FAST);
    }

    void set3DasInfraredMode(@LivenessSingleFaceInfraredMode.EThreeDASInfraredMode int value) {
        setInteger(AsInfraredMode, value);
    }
    @LivenessSingleFaceInfraredMode.EThreeDASInfraredMode
    public int get3DasInfraredMode() {
        return pref.getInt(AsInfraredMode, LivenessSingleFaceInfraredMode.INFRARED);
    }

    public void set3DasInfraredLaserPower(float value) {
        setFloat(AsLaserPower, value);
    }
    public float get3DasInfraredLaserPower() {
        return pref.getFloat(AsLaserPower, 0);
    }

    
    public boolean isFlipCameraDisplay() {
        return pref.getBoolean(flipCameraDisplay, true);
    }
    
    void setFlipCameraDisplay(boolean enable) {
        setBoolean(flipCameraDisplay, enable);
    }

}
