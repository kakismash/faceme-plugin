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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Size;

import com.cyberlink.faceme.AsyncEnginePreference;
import com.cyberlink.faceme.DetectionMode;
import com.cyberlink.faceme.EnginePreference;
import com.cyberlink.faceme.ExtractionModelSpeedLevel;
import com.cyberlink.faceme.LivenessSingleFaceInfraredMode;
import com.cyberlink.faceme.PrecisionLevel;
import com.cyberlink.faceme.LivenessSingleFaceMotionTrackingSpeedLevel;
import com.cyberlink.faceme.widget.AntiSpoofingConfig;

import java.io.File;
import java.util.Locale;

/**
 * Persist configurations across different scenario pages to use the same
 * attributes in Demo App.
 */
public class UiSettings {

    private static final String PREFERENCE_NAME = "com.cyberlink.FaceMe.Settings";

    static final float DEFAULT_MIN_FACE_WIDTH_RATIO = 0.15f;
    static final int DEFAULT_2DAS_ACTION_CONUT = 2;

    public static final int API_SYNC_MODE_VALUE = 0;
    public static final int API_ASYNC_MODE_VALUE = 1;

    // XXX: v? represents history of changing preference meanings.
    //         It has no any relationship with SDK version.
    private static final String benchmarkFolder = "benchmarkFolder";
    private static final String benchmarkUri = "benchmarkUri";
    private static final String historyListFolder = "historyListFolder";
    private static final String historyListUri = "historyListUri";
    private static final String validationFile = "validationFile";
    private static final String validationFileUri = "validationFileUri";
    private static final String validationFolderUri = "validationFolderUri";
    private static final String asMeasureSetFolder = "asMeasureSetFolder";
    private static final String asMeasureSetUri = "asMeasureSetUri";
    private static final String idcardMeasureSetFolder = "idcardMeasureSetFolder";
    private static final String idcardMeasureSetUri = "idcardMeasureSetUri";

    private static final String showInfo = "showInfo";
    private static final String width = "width";
    private static final String height = "height";

    private static final String enginePreference = "enginePreference_v3";
    private static final String engineThreads = "engineThreads";
    private static final String extractModel = "extractModel_v3";

    private static final String minFaceWidthRatio = "minFaceWidthRatio";
    private static final String detectionMode = "detectionMode";
    private static final String precisionLevel = "precisionLevel_v3";

    private static final String showLandmark = "showLandmark";
    private static final String faceRecognition = "faceRecognition";
    private static final String showFeatures = "showFeatures";
    private static final String showAge = "showAge";
    private static final String ageInRange = "ageInRange";
    private static final String showGender = "showGender";
    private static final String showEmotion = "showEmotion";
    private static final String showPose = "showPose";
    private static final String showMaskDetection = "showMaskDetection";

    private static final String asyncPreference = "asyncPreference";
    private static final String hwAccMode = "hwAccMode";
    private static final String asyncHwAccMode = "asyncHwAccMode";
    private static final String APIMode = "APIMode";
    private static final String detectBatchSize = "detectBatchSize";
    private static final String extractBatchSize = "extractBatchSize";

    // Widgets features.
    private static final String precisionMode = "2DasPrecisionMode_v1";
    private static final String use2ndStage = "2DasUse2ndStage_v2";
    private static final String actionCount = "actionCount";
    private static final String actionNodEnable = "actionNodEnable";
    private static final String actionSmileEnable = "actionSmileEnable";
    private static final String voiceEnable = "voiceEnable";
    private static final String voiceLangCode = "voiceLangCode";
    private static final String vibrateEnable = "vibrateEnable";

    // UI features, non-SDK configurations.
    private static final String showVisitCount = "showVisitCount";

    // Configurations for custom ROM.
    private static final String flipCameraOutput = "flipCameraOutput";
    private static final String flipCameraDisplay = "flipCameraDisplay";
    private static final String cameraOrientation = "cameraOrientation";
    private static final String rotateCameraOutput = "rotateCameraOutput";
    private static final String cameraDisplayOrientation = "cameraDisplayOrientation";

    // Configurations for sort in face management.
    private static final String sortForFaceManagement = "sortForFaceManagement";

    private static final String AsSpeedLevel = "3DasSpeedLevel";
    private static final String AsInfraredMode = "3DasInfraredMode";
    private static final String AsLaserPower = "InfraredAsLaserPower";

    private static final String databaseEncryption = "databaseEncryption";

    private final SharedPreferences pref;

    private final String asVoiceLangCode;

    public UiSettings(Context context) {
        this.pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        String lang = Locale.getDefault().getDisplayLanguage();
        if (Locale.TRADITIONAL_CHINESE.getDisplayLanguage().equals(lang)) {
            asVoiceLangCode = "zho";
        } else {
            asVoiceLangCode = "eng";
        }
    }

    private void setString(String key, String value) {
        pref.edit().putString(key, value).apply();
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
    private void remove(String key) {
        pref.edit().remove(key).apply();
    }

    public void setBenchmarkFolder(@NonNull File folder) {
        setString(benchmarkFolder, folder.getAbsolutePath());
    }
    public File getBenchmarkFolder() {
        return new File(pref.getString(benchmarkFolder, ""));
    }

    public void setBenchmarkUri(@NonNull Uri uri) {
        setString(benchmarkUri, uri.toString());
    }
    public Uri getBenchmarkUri() {
        return Uri.parse(pref.getString(benchmarkUri, ""));
    }

    public void setHistoryListFolder(@NonNull File folder) {
        setString(historyListFolder, folder.getAbsolutePath());
    }
    public File getHistoryListFolder() {
        return new File(pref.getString(historyListFolder, ""));
    }

    public void setHistoryListUri(@NonNull Uri uri) {
        setString(historyListUri, uri.toString());
    }
    public Uri getHistoryListUri() {
        return Uri.parse(pref.getString(historyListUri, ""));
    }

    public void setValidationFile(@NonNull File file) {
        setString(validationFile, file.getAbsolutePath());
    }
    public File getValidationFile() {
        return new File(pref.getString(validationFile, ""));
    }

    public void setValidationFileUri(@NonNull Uri uri) {
        setString(validationFileUri, uri.toString());
    }
    public Uri getValidationFileUri() {
        return Uri.parse(pref.getString(validationFileUri, ""));
    }

    public void setValidationFolderUri(@NonNull Uri uri) {
        setString(validationFolderUri, uri.toString());
    }
    public Uri getValidationFolderUri() {
        return Uri.parse(pref.getString(validationFolderUri, ""));
    }

    public void setAsMeasureSetFolder(@NonNull File folder) {
        setString(asMeasureSetFolder, folder.getAbsolutePath());
    }
    public File getAsMeasureSetFolder() {
        if (pref.contains(asMeasureSetFolder)) {
            return new File(pref.getString(asMeasureSetFolder, ""));
        } else {
            return ExternalStorageUtils.getExternalStorageDir("FaceMe");
        }
    }

    public void setAsMeasureSetUri(@NonNull Uri uri) {
        setString(asMeasureSetUri, uri.toString());
    }
    public Uri getAsMeasureSetUri() {
        return Uri.parse(pref.getString(asMeasureSetUri, ""));
    }

    public void setIdcardMeasureSetFolder(File folder) {
        if (folder == null)
            remove(idcardMeasureSetFolder);
        else
            setString(idcardMeasureSetFolder, folder.getAbsolutePath());
    }
    public File getIdcardMeasureSetFolder() {
        if (pref.contains(idcardMeasureSetFolder)) {
            return new File(pref.getString(idcardMeasureSetFolder, ""));
        } else {
            return ExternalStorageUtils.getExternalStorageDir("FaceMe");
        }
    }
    public void setIdcardMeasureSetUri(Uri uri) {
        if (uri == null)
            remove(idcardMeasureSetUri);
        else
            setString(idcardMeasureSetUri, uri.toString());
    }
    public Uri getIdcardMeasureSetUri() {
        return Uri.parse(pref.getString(idcardMeasureSetUri, ""));
    }

    public void setShowInfo(boolean enable) {
        setBoolean(showInfo, enable);
    }
    public boolean isShowInfo() {
        return pref.getBoolean(showInfo, false);
    }

    public void setPreviewSize(int width, int height) {
        setInteger(UiSettings.width, width);
        setInteger(UiSettings.height, height);
    }
    public Size getPreviewSize() {
        return new Size(pref.getInt(width, 1280), pref.getInt(height, 720));
    }

    void setEnginePreference(int value) {
        setInteger(enginePreference, value);
    }
    public int getEnginePreference() {
        return pref.getInt(enginePreference, EnginePreference.PREFER_NONE);
    }

    void setAsyncPreference(int value) {
        setInteger(asyncPreference, value);
    }
    public int getAsyncPreference() {
        return pref.getInt(asyncPreference, AsyncEnginePreference.PREFER_NONE);
    }

    public void setHwAccMode(int value) {
        setInteger(hwAccMode, value);
    }
    public int getHwAccMode() {
        return pref.getInt(hwAccMode, EnginePreference.PREFER_NONE);
    }

    public void setAsyncHwAccMode(int value) {
        setInteger(asyncHwAccMode, value);
    }
    public int getAsyncHwAccMode() {
        return pref.getInt(asyncHwAccMode, AsyncEnginePreference.PREFER_NONE);
    }

    void setAPIMode(int value) {
        setInteger(APIMode, value);
    }
    public int getAPIMode() {
        return pref.getInt(APIMode, API_SYNC_MODE_VALUE);
    }

    void setDetectBatchSize(int value) {
        setInteger(detectBatchSize, value);
    }
    public int getDetectBatchSize() {
        return pref.getInt(detectBatchSize, 1);
    }

    void setExtractBatchSize(int value) {
        setInteger(extractBatchSize, value);
    }
    public int getExtractBatchSize() {
        return pref.getInt(extractBatchSize, 1);
    }

    void setMinFaceWidthRatio(float value) {
        setFloat(minFaceWidthRatio, value);
    }
    public float getMinFaceWidthRatio() {
        return pref.getFloat(minFaceWidthRatio, DEFAULT_MIN_FACE_WIDTH_RATIO);
    }

    void setEngineThreads(int value) {
        setInteger(engineThreads, value);
    }
    public int getEngineThreads() {
        int cpuCounts = Runtime.getRuntime().availableProcessors();
        int defaultValue = Math.min(cpuCounts, 4);
        return pref.getInt(engineThreads, defaultValue);
    }

    void setPrecisionLevel(int value) {
        setInteger(precisionLevel, value);
    }
    @PrecisionLevel.EPrecisionLevel
    public int getPrecisionLevel() {
        return pref.getInt(precisionLevel, PrecisionLevel.LEVEL_1E6);
    }

    void setExtractModel(@ExtractionModelSpeedLevel.EExtractionModelSpeedLevel int value) {
        setInteger(extractModel, value);
    }
    @ExtractionModelSpeedLevel.EExtractionModelSpeedLevel
    public int getExtractModel() {
        return pref.getInt(extractModel, ExtractionModelSpeedLevel.VH6_M);
    }

    void setDetectionMode(@DetectionMode.EDetectionMode int value) {
        setInteger(detectionMode, value);
    }
    @DetectionMode.EDetectionMode
    public int getDetectionMode() {
        return pref.getInt(detectionMode, DetectionMode.FAST);
    }

    void setShowLandmark(boolean enable) {
        setBoolean(showLandmark, enable);
    }
    public boolean isShowLandmark() {
        return pref.getBoolean(showLandmark, false);
    }

    void setFaceRecognition(boolean enable) {
        setBoolean(faceRecognition, enable);
    }
    public boolean isFaceRecognition() {
        return pref.getBoolean(faceRecognition, false);
    }

    void setShowFeatures(boolean enable) {
        setBoolean(showFeatures, enable);
    }
    public boolean isShowFeatures() {
        return pref.getBoolean(showFeatures, true);
    }

    void setShowAge(boolean enable) {
        setBoolean(showAge, enable);
    }
    public boolean isShowAge() {
        return pref.getBoolean(showAge, false);
    }

    void setAgeInRange(boolean enable) {
        setBoolean(ageInRange, enable);
    }
    public boolean isAgeInRange() {
        return pref.getBoolean(ageInRange, true);
    }

    void setShowGender(boolean enable) {
        setBoolean(showGender, enable);
    }
    public boolean isShowGender() {
        return pref.getBoolean(showGender, true);
    }

    void setShowEmotion(boolean enable) {
        setBoolean(showEmotion, enable);
    }
    public boolean isShowEmotion() {
        return pref.getBoolean(showEmotion, true);
    }

    void setShowPose(boolean enable) {
        setBoolean(showPose, enable);
    }
    public boolean isShowPose() {
        return pref.getBoolean(showPose, true);
    }

    void setShowMaskDetection(boolean enable) {
        setBoolean(showMaskDetection, enable);
    }
    public boolean isShowMaskDetection() {
        return pref.getBoolean(showMaskDetection, true);
    }

    void set2DasPrecisionMode(int mode) {
        setInteger(precisionMode, mode);
    }
    public int get2DasPrecisionMode() {
        return pref.getInt(precisionMode, 2); // STANDARD as default.
    }

    void set2DasUse2ndStage(int mode) {
        setInteger(use2ndStage, mode);
    }
    public int get2DasUse2ndStage() {
        return pref.getInt(use2ndStage, AntiSpoofingConfig.INTERACTION_RANDOM);
    }

    void set2DasActionCount(int value) {
        setInteger(actionCount, value);
    }
    public int get2DasActionCount() {
        return pref.getInt(actionCount, DEFAULT_2DAS_ACTION_CONUT);
    }

    void set2DasNodActionEnable(boolean enable) {
        setBoolean(actionNodEnable, enable);
    }
    public boolean is2DasNodActionEnable() {
        return pref.getBoolean(actionNodEnable, true);
    }

    void set2DasSmileActionEnable(boolean enable) {
        setBoolean(actionSmileEnable, enable);
    }
    public boolean is2DasSmileActionEnable() {
        return pref.getBoolean(actionSmileEnable, true);
    }

    void set2DasVibrateEnable(boolean enable) {
        setBoolean(vibrateEnable, enable);
    }
    public boolean is2DasVibrateEnable() {
        return pref.getBoolean(vibrateEnable, true);
    }

    void set2DasVoiceEnable(boolean enable) {
        setBoolean(voiceEnable, enable);
    }
    public boolean is2DasVoiceEnable() {
        return pref.getBoolean(voiceEnable, true);
    }

    void setVoiceLangCode(String langCode) {
        setString(voiceLangCode, langCode);
    }
    public String getVoiceLangCode() {
        return pref.getString(voiceLangCode, asVoiceLangCode);
    }

    void setShowVisitCount(boolean enable) {
        setBoolean(showVisitCount, enable);
    }
    public boolean isShowVisitCount() {
        return pref.getBoolean(showVisitCount, false);
    }

    public boolean isFlipCameraOutput() {
        return pref.getBoolean(flipCameraOutput, CustomDevice.get().needFlipCameraOutput());
    }
    void setFlipCameraOutput(boolean enable) {
        setBoolean(flipCameraOutput, enable);
    }

    public boolean isFlipCameraDisplay() {
        return pref.getBoolean(flipCameraDisplay, CustomDevice.get().needFlipCameraDisplay());
    }
    void setFlipCameraDisplay(boolean enable) {
        setBoolean(flipCameraDisplay, enable);
    }

    public Integer getCameraOrientation() {
        if (pref.contains(cameraOrientation))
            return pref.getInt(cameraOrientation, 0);
        else
            return CustomDevice.get().forceCameraOrientation();
    }
    void setCameraOrientation(Integer degrees) {
        if (degrees == null)
            remove(cameraOrientation);
        else
            setInteger(cameraOrientation, degrees);
    }

    public Integer getCameraOutputRotation() {
        if (pref.contains(rotateCameraOutput))
            return pref.getInt(rotateCameraOutput, 0);
        else
            return CustomDevice.get().forceCameraOutputRotation();
    }
    void setCameraOutputRotation(Integer degrees) {
        if (degrees == null)
            remove(rotateCameraOutput);
        else
            setInteger(rotateCameraOutput, degrees);
    }

    public Integer getCameraDisplayOrientation() {
        if (pref.contains(cameraDisplayOrientation))
            return pref.getInt(cameraDisplayOrientation, 0);
        else
            return CustomDevice.get().forceCameraDisplayOrientation();
    }
    void setCameraDisplayOrientation(Integer degrees) {
        if (degrees == null)
            remove(cameraDisplayOrientation);
        else
            setInteger(cameraDisplayOrientation, degrees);
    }

    public void setSortForFaceManagement(int sort) {
        setInteger(sortForFaceManagement, sort);
    }
    public int getSortForFaceManagement() {
        return pref.getInt(sortForFaceManagement, 1); // Sort by create date as default.
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

    public boolean isDatabaseEncryption() {
        return pref.getBoolean(databaseEncryption, false);
    }
    public void setDatabaseEncryption(boolean enable) {
        setBoolean(databaseEncryption, enable);
    }
}
