/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.cyberlink.faceme.AsyncEnginePreference;
import com.cyberlink.faceme.EnginePreference;
import com.cyberlink.faceme.FaceMeSdk;
import com.cyberlink.faceme.LicenseManager;
import com.cyberlink.faceme.LicenseOption;
import com.cyberlink.faceme.LivenessSingleFaceInfraredMode;
import com.cyberlink.faceme.widget.AntiSpoofingConfig;

import com.cyberlink.facemedemo.extension.R;

import com.dare.plugin.liveness.script.BuildConfig;

import java.util.ArrayList;

public class SettingsFragment<B extends SettingsFragment.Broker> extends BaseDialogFragment<B> {
    private static final String TAG = "SettingsFragment";

    public static final int UI_FULL_SCREEN          = 1 << 1;
    public static final int UI_ALPHA_BACKGROUND     = 1 << 2;

    public static final int UI_MIN_FACE             = 1 << 3;
    public static final int UI_VIDEO_MODE           = 1 << 4;
    public static final int UI_MASK_DETECTION       = 1 << 5;
    public static final int UI_EXTRACTION_MODEL     = 1 << 6;
    public static final int UI_PRECISION_LEVEL      = 1 << 7;
    public static final int UI_DETECT_SPEED_LEVEL   = 1 << 8;
    public static final int UI_ENGINE_PREFERENCE    = 1 << 9;
    public static final int UI_FACE_LANDMARKS       = 1 << 10;
    public static final int UI_FACE_FEATURES        = 1 << 11;

    public static final int UI_2D_ANTI_SPOOFING     = 1 << 14;
    public static final int UI_CAMERA_CONFIG        = 1 << 15;
    public static final int UI_RS_CAMERA_CONFIG     = 1 << 16;

    public static final int UI_VISIT_COUNT          = 1 << 21;

    public static final int UI_3D_ANTI_SPOOFING_OPTION      = 1 << 25;
    public static final int UI_3D_ANTI_SPOOFING_LASER_POWER = 1 << 26;

    public static final int UI_BATCH_SIZE               = 1 << 28;
    public static final int UI_HW_ACCELERATION          = 1 << 29;
    public static final int UI_API_MODE                 = 1 << 30;

    public static final int UI_ALL                  = Integer.MAX_VALUE; // 31.

    private static final String ARG_FLAG = "ui.flag";

    private static final int MIN_MIN_FACE_WIDTH_RATIO = 3;
    private static final int MAX_MIN_FACE_WIDTH_RATIO = 50;
    private static final float RELATED_MIN_FACE_WIDTH_RATIO = 100f;

    private static final int MIN_LASER_POWER = 0;
    private static final int MAX_LASER_POWER = 360;

    private static final int MIN_2DAS_ACTION_COUNT = 2;
    private static final int MAX_2DAS_ACTION_COUNT = 3;

    private static final int MAX_BATCH_SIZE = 7;

    public interface Broker extends IBroker {
        default void onSettingsChanged(boolean needRebuild) {}

        default void onCameraConfigChanged() {}
    }

    private int uiFlag = UI_ALL;
    private UiSettings uiSettings = null;

    private SeekBar seekBarMinFaceWidthRatio;
    private View btnAddMinFaceWidthRatio;
    private View btnRemoveMinFaceWidthRatio;
    private TextView txtMinFaceWidthRatio;

    private SeekBar seekBarEngineThreads;
    private TextView txtEngineThreads;

    private SeekBar seekbarDetectBatchSize;
    private TextView txtDetectBatchSize;
    private SeekBar seekbarExtractBatchSize;
    private TextView txtExtractBatchSize;

    private RadioGroup radioExtractModel;
    private RadioGroup radioPrecisionLevel;
    private RadioGroup radio3DasSpeedLevel;
    private Switch btnRsInfrared;
    private Button btnRsCalibration;
    private RadioGroup radioAPIMode;
    private Spinner spinnerHWAccMode;

    private SeekBar seekBarLaserPower;
    private View btnAddLaserPower;
    private View btnRemoveLaserPower;
    private TextView txtLaserPower;

    private Switch btnLandmark;

    private Switch btnFeatures;
    private Switch btnAge;
    private Switch btnAgeRange;
    private View layoutAgeRange;
    private Switch btnGender;
    private Switch btnEmotion;
    private Switch btnPose;
    private Switch btnMask;

    private RadioGroup radioCameraMirror;
    private RadioGroup radioCameraDisplayMirror;
    private RadioGroup radioCameraOrientation;
    private RadioGroup radioCameraRotate;
    private RadioGroup radioCameraDisplay;
    private RadioGroup radioRSCameraMirror;

    private RadioGroup radio2Das2ndStage;
    private RadioGroup radio2DasPrecisionMode;
    private RadioGroup radio2DasVoiceLangMode;
    private View actionOption2Das2ndStage;
    private View option2DasVoiceLang;
    private SeekBar seekBar2DasActionCount;
    private TextView txt2DasActionCount;
    private Switch btn2DasNodEnable;
    private Switch btn2DasSmileEnable;
    private Switch btn2DasVoiceEnable;
    private Switch btn2DasVibrateEnable;

    private Switch btnAutoVisitor;

    public static SettingsFragment newInstance(int flag) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FLAG, flag);
        args.putBoolean(ARG_FULLSCREEN, (flag & UI_FULL_SCREEN) == UI_FULL_SCREEN);
        fragment.setArguments(args);
        return fragment;
    }

    public void setUiSettings(UiSettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            uiFlag = arguments.getInt(ARG_FLAG);
        }
        if (uiSettings == null) {
            uiSettings = new UiSettings(DemoApp.getContext());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        updateUiSettings();
    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        if (window != null) {
            int height = getResources().getDimensionPixelSize(R.dimen.dialog_max_height);
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, height);
        }
    }

    @Override
    protected String getTagId() {
        return TAG;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings;
    }

    private boolean enableFlag(int flag) {
        return (uiFlag & flag) == flag;
    }

    @Override
    protected void initUiComponents(@NonNull LayoutInflater inflater, View rootView) {
        if (!enableFlag(UI_ALPHA_BACKGROUND)) {
            rootView.setBackgroundResource(R.drawable.bg_rounded_grey);
        }

        disableUnsupportedLicenseOption(rootView);

        initSdkVersion(rootView);
        initMinFaceWidth(rootView);
        initEnginePreference(rootView);
        initMaskDetection(rootView);
        initExtractModel(rootView);
        initPrecisionLevel(rootView);
        initFastDetection(rootView);
        initLandmark(rootView);

        initHWAccMode(rootView);
        initAPIMode(rootView);
        initBatchSize(rootView);

        initFeatureSet(rootView);
        init2DasOptions(rootView);
        initCameraConfig(rootView);
        initRSCameraConfig(rootView);

        initAutoVisitor(rootView);

        init3DasOption(rootView);
        initLaserPower(rootView);

        hideMostLayouts((ViewGroup) rootView);
    }

    private void disableUnsupportedLicenseOption(View rootView) {
        LicenseManager licMgr = null;
        try {
            licMgr = new LicenseManager();
            int result = licMgr.initializeEx();
            if (result < 0) throw new IllegalStateException("Initialize license manager failed: " + result);


            // Face extraction model.
            if (allow(licMgr, LicenseOption.EXTRACTION)) {
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_VH6M)) {
                    rootView.findViewById(R.id.radioExtractModelVH6_M).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_VH6)) {
                    rootView.findViewById(R.id.radioExtractModelVH6).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_H6)) {
                    rootView.findViewById(R.id.radioExtractModelH6).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_VH5M)) {
                    rootView.findViewById(R.id.radioExtractModelVH5_M).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_VH5)) {
                    rootView.findViewById(R.id.radioExtractModelVH5).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_H5)) {
                    rootView.findViewById(R.id.radioExtractModelH5).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_VHM)) {
                    rootView.findViewById(R.id.radioExtractModelVH_M).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_VERY_HIGH_PRECISION)) {
                    rootView.findViewById(R.id.radioExtractModelVH).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_H3)) {
                    rootView.findViewById(R.id.radioExtractModelH3).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_HIGH_PRECISION)) {
                    rootView.findViewById(R.id.radioExtractModelH1).setEnabled(false);
                }
                if (notAllow(licMgr, LicenseOption.EXTRACTION_MODEL_HIGH_PRECISION_ASIAN)) {
                    rootView.findViewById(R.id.radioExtractModelH2).setEnabled(false);
                }
            }
            // Face feature.
            if (notAllow(licMgr, LicenseOption.EXTRACTION)) {
                rootView.findViewById(R.id.btnFeatures).setEnabled(false);
            }
            // Face attributes extraction.
            if (notAllow(licMgr, LicenseOption.EXTRACTION_FACE_ATTRIBUTES)) {
                rootView.findViewById(R.id.btnAge).setEnabled(false);
                rootView.findViewById(R.id.btnAgeRange).setEnabled(false);
                rootView.findViewById(R.id.btnGender).setEnabled(false);
                rootView.findViewById(R.id.btnEmotion).setEnabled(false);
            }
            // Face Pose
            if (notAllow(licMgr, LicenseOption.EXTRACTION_FACE_POSE)) {
                rootView.findViewById(R.id.btnPose).setEnabled(false);
            }
            // Mask Detection
            if (notAllow(licMgr, LicenseOption.EXTRACTION_FACE_OCCLUSION)) {
                rootView.findViewById(R.id.btnMask).setEnabled(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot checkLicenseOption", e);
        } finally {
            if (licMgr != null) licMgr.release();
        }
    }

    private static boolean allow(LicenseManager licenseManager , String option) {
        Object value = licenseManager.getProperty(option);
        return (value instanceof Boolean) && ((Boolean) value);
    }
    private static boolean notAllow(LicenseManager licenseManager , String option) {
        return !allow(licenseManager, option);
    }

    private static boolean theSameValue(boolean a, boolean b) {
        return a == b;
    }
    private static boolean theSameValue(int a, int b) {
        return a == b;
    }

    private static boolean hasOption(int a, int b) {
        return (a & b) == b;
    }

    private void initSdkVersion(View rootView) {
        TextView txtAppTitle = rootView.findViewById(R.id.txtAppTitle);
        String extensionMode = " (" + BuildConfig.EXTENSION_MODE + ")";
        txtAppTitle.append(Strings.stringSpan(extensionMode, Color.GRAY));

        TextView txtVersion = rootView.findViewById(R.id.txtVersion);
        txtVersion.setText(Strings.stringSpan("APP: ", Color.LTGRAY));
        try {
            String packageName = rootView.getContext().getPackageName();
            String appVer = rootView.getContext().getPackageManager()
                    .getPackageInfo(packageName, 0).versionName;
            txtVersion.append(Strings.stringSpan(appVer, Color.GRAY));
        } catch (PackageManager.NameNotFoundException e) {
            txtVersion.append(Strings.stringSpan("Unknown", Color.GRAY));
        }

        txtVersion.append("        ");
        txtVersion.append(Strings.stringSpan("SDK: ", Color.LTGRAY));
        txtVersion.append(Strings.stringSpan(FaceMeSdk.version(), Color.GRAY));
    }

    private void initMinFaceWidth(View rootView) {
        seekBarMinFaceWidthRatio = rootView.findViewById(R.id.seekbarMinFaceWidthRatio);
        btnAddMinFaceWidthRatio = rootView.findViewById(R.id.btnAddMinFaceWidthRatio);
        btnRemoveMinFaceWidthRatio = rootView.findViewById(R.id.btnRemoveMinFaceWidthRatio);
        txtMinFaceWidthRatio = rootView.findViewById(R.id.txtMinFaceWidthRatio);

        seekBarMinFaceWidthRatio.setMax(MAX_MIN_FACE_WIDTH_RATIO - MIN_MIN_FACE_WIDTH_RATIO);
        btnAddMinFaceWidthRatio.setOnClickListener((v) -> seekBarMinFaceWidthRatio.setProgress(seekBarMinFaceWidthRatio.getProgress() + 1));
        btnRemoveMinFaceWidthRatio.setOnClickListener((v) -> seekBarMinFaceWidthRatio.setProgress(seekBarMinFaceWidthRatio.getProgress() - 1));

        seekBarMinFaceWidthRatio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateMinFaceWidthRatioValue(progress);

                if (!fromUser) {
                    float newMinFaceWidthRatio = (float) (progress + MIN_MIN_FACE_WIDTH_RATIO) / RELATED_MIN_FACE_WIDTH_RATIO;
                    if (newMinFaceWidthRatio == uiSettings.getMinFaceWidthRatio()) return;

                    uiSettings.setMinFaceWidthRatio(newMinFaceWidthRatio);
                    broker.onSettingsChanged(true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                updateMinFaceWidthRatioValue(progress);

                float newMinFaceWidthRatio = (float) (progress + MIN_MIN_FACE_WIDTH_RATIO) / RELATED_MIN_FACE_WIDTH_RATIO;
                if (newMinFaceWidthRatio == uiSettings.getMinFaceWidthRatio()) return;

                uiSettings.setMinFaceWidthRatio(newMinFaceWidthRatio);
                broker.onSettingsChanged(true);
            }
        });

        txtMinFaceWidthRatio.setOnLongClickListener((v) -> {
            seekBarMinFaceWidthRatio.setProgress((int) (UiSettings.DEFAULT_MIN_FACE_WIDTH_RATIO * RELATED_MIN_FACE_WIDTH_RATIO) - MIN_MIN_FACE_WIDTH_RATIO);
            return true;
        });
    }

    private void updateMinFaceWidthRatioValue(int seekBarProgress) {
        txtMinFaceWidthRatio.setText((seekBarProgress + MIN_MIN_FACE_WIDTH_RATIO) + "%");
        btnAddMinFaceWidthRatio.setEnabled(seekBarProgress < seekBarMinFaceWidthRatio.getMax());
        btnRemoveMinFaceWidthRatio.setEnabled(seekBarProgress > 0);
    }

    private void initEnginePreference(View rootView) {
        seekBarEngineThreads = rootView.findViewById(R.id.seekbarEngineThreads);
        txtEngineThreads = rootView.findViewById(R.id.txtEngineThreads);

        int cpuCounts = Runtime.getRuntime().availableProcessors();
        if (cpuCounts == 1) seekBarEngineThreads.setEnabled(false);
        seekBarEngineThreads.setMax(cpuCounts - 1);
        seekBarEngineThreads.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateEngineThreadValue(progress);

                int newEngineThreads = progress + 1;
                if (theSameValue(newEngineThreads, uiSettings.getEngineThreads())) return;

                uiSettings.setEngineThreads(newEngineThreads);
                broker.onSettingsChanged(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateEngineThreadValue(int seekBarProgress) {
        txtEngineThreads.setText(String.valueOf(seekBarProgress + 1));
    }

    private void updateEnginePreference() {
        int progressValue = uiSettings.getEngineThreads() - 1;
        seekBarEngineThreads.setProgress(progressValue);
        updateEngineThreadValue(progressValue);
    }

    private void initBatchSize(View rootView) {
        seekbarDetectBatchSize = rootView.findViewById(R.id.seekbarDetectBatchSize);
        txtDetectBatchSize = rootView.findViewById(R.id.txtDetectBatchSize);

        if (uiSettings.getAPIMode() == UiSettings.API_SYNC_MODE_VALUE)
            seekbarDetectBatchSize.setEnabled(false);
        seekbarDetectBatchSize.setMax(MAX_BATCH_SIZE);
        seekbarDetectBatchSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateDetectBatchSizeValue(progress);

                int newDetectBatchSize = progress + 1;
                if (theSameValue(newDetectBatchSize, uiSettings.getDetectBatchSize())) return;

                uiSettings.setDetectBatchSize(newDetectBatchSize);
                broker.onSettingsChanged(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarExtractBatchSize = rootView.findViewById(R.id.seekbarExtractBatchSize);
        txtExtractBatchSize = rootView.findViewById(R.id.txtExtractBatchSize);

        if (uiSettings.getAPIMode() == UiSettings.API_SYNC_MODE_VALUE)
            seekbarExtractBatchSize.setEnabled(false);
        seekbarExtractBatchSize.setMax(MAX_BATCH_SIZE);
        seekbarExtractBatchSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateExtractBatchSizeValue(progress);

                int newExtractBatchSize = progress + 1;
                if (theSameValue(newExtractBatchSize, uiSettings.getExtractBatchSize())) return;

                uiSettings.setExtractBatchSize(newExtractBatchSize);
                broker.onSettingsChanged(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateDetectBatchSizeValue(int seekBarProgress) {
        txtDetectBatchSize.setText(String.valueOf(seekBarProgress + 1));
    }

    private void updateExtractBatchSizeValue(int seekBarProgress) {
        txtExtractBatchSize.setText(String.valueOf(seekBarProgress + 1));
    }

    private void updateBatchSize() {
        int detectBatchProgressValue = uiSettings.getDetectBatchSize() - 1;
        seekbarDetectBatchSize.setProgress(detectBatchProgressValue);
        updateDetectBatchSizeValue(detectBatchProgressValue);

        int extractBatchProgressValue = uiSettings.getExtractBatchSize() - 1;
        seekbarExtractBatchSize.setProgress(extractBatchProgressValue);
        updateExtractBatchSizeValue(extractBatchProgressValue);
    }

    private void initExtractModel(View rootView) {
        radioExtractModel = rootView.findViewById(R.id.radioExtractModel);
        radioExtractModel.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            try {
                int newLevel = Integer.parseInt((String) tag);
                if (theSameValue(newLevel, uiSettings.getExtractModel())) return;

                uiSettings.setExtractModel(newLevel);
                broker.onSettingsChanged(true);
            } catch (NumberFormatException ignored) {}
        });
    }

    private void updateExtractModel(int method) {
        int childCount = radioExtractModel.getChildCount();
        for (int idx = 0; idx < childCount; idx++) {
            View child = radioExtractModel.getChildAt(idx);
            if (!(child instanceof RadioButton)) continue;
            if (!(child.getTag() instanceof String)) continue;

            try {
                if (Integer.parseInt((String) child.getTag()) == method) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    /**
     * @see com.cyberlink.faceme.PrecisionLevel
     */
    private void initPrecisionLevel(View rootView) {
        radioPrecisionLevel = rootView.findViewById(R.id.radioPrecisionLevel);
        radioPrecisionLevel.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            try {
                int newLevel = Integer.parseInt((String) tag);
                if (theSameValue(newLevel, uiSettings.getPrecisionLevel())) return;

                uiSettings.setPrecisionLevel(newLevel);
                broker.onSettingsChanged(false);
            } catch (NumberFormatException ignored) {}
        });
    }

    private void updatePrecisionLevel(int level) {
        int childCount = radioPrecisionLevel.getChildCount();
        for (int idx = 0; idx < childCount; idx++) {
            View child = radioPrecisionLevel.getChildAt(idx);
            if (!(child instanceof RadioButton)) continue;
            if (!(child.getTag() instanceof String)) continue;

            try {
                if (Integer.parseInt((String) child.getTag()) == level) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void initFastDetection(View rootView) {
        rootView.findViewById(R.id.radioFastDetectionOff).setEnabled(false);
        rootView.findViewById(R.id.radioFastDetectionAuto).setEnabled(false);
    }

    private void updateHWAccMode(int mode) {
        int ttagMode = 0;
        switch (mode) {
            case EnginePreference.PREFER_NONE: //CPU
                ttagMode = 0;
                break;
            case EnginePreference.PREFER_MTKNP_DETECTION | EnginePreference.PREFER_MTKNP_EXTRACTION: //MTK
                ttagMode = 1;
                break;
            case EnginePreference.PREFER_SNPE_DETECTION | EnginePreference.PREFER_SNPE_EXTRACTION: //SNPE
                ttagMode = 2;
                break;
            case EnginePreference.PREFER_NXP_DETECTION | EnginePreference.PREFER_NXP_EXTRACTION: //NXP
                ttagMode = 3;
                break;
            default:
                break;
        }

        spinnerHWAccMode.setSelection(ttagMode, false);
    }

    private void updateAPIMode(int mode) {
        int childCount = radioAPIMode.getChildCount();
        for (int idx = 0; idx < childCount; idx++) {
            View child = radioAPIMode.getChildAt(idx);
            if (!(child instanceof RadioButton)) continue;
            if (!(child.getTag() instanceof String)) continue;

            try {
                if (Integer.parseInt((String) child.getTag()) == mode) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void initLandmark(View rootView) {
        btnLandmark = rootView.findViewById(R.id.btnLandmark);
        btnLandmark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isShowLandmark())) return;

            uiSettings.setShowLandmark(isChecked);
            broker.onSettingsChanged(false);
        });
    }

    private void initHWAccMode(View rootView) {
        spinnerHWAccMode = rootView.findViewById(R.id.spinnerHWMode);
        spinnerHWAccMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                int newMode = EnginePreference.PREFER_NONE;
                int asyncNewMode = AsyncEnginePreference.PREFER_NONE;
                boolean needUpdate = true;
                switch (position) {
                    case 0: //CPU
                        newMode = EnginePreference.PREFER_NONE;
                        asyncNewMode = AsyncEnginePreference.PREFER_NONE;
                        break;
                    case 1: //MTK
                        newMode = EnginePreference.PREFER_MTKNP_DETECTION | EnginePreference.PREFER_MTKNP_EXTRACTION;
                        asyncNewMode = AsyncEnginePreference.PREFER_MTKNP_DETECTION | AsyncEnginePreference.PREFER_MTKNP_EXTRACTION;
                        break;
                    case 2: //SNPE
                        newMode = EnginePreference.PREFER_SNPE_DETECTION | EnginePreference.PREFER_SNPE_EXTRACTION;
                        asyncNewMode = AsyncEnginePreference.PREFER_SNPE_DETECTION | AsyncEnginePreference.PREFER_SNPE_EXTRACTION;
                        break;
                    case 3: //NXP
                        needUpdate = false;
                        newMode = EnginePreference.PREFER_NXP_DETECTION | EnginePreference.PREFER_NXP_EXTRACTION;
                        asyncNewMode = AsyncEnginePreference.PREFER_NXP_DETECTION | AsyncEnginePreference.PREFER_NXP_EXTRACTION;
                        if (theSameValue(newMode, uiSettings.getHwAccMode()) && theSameValue(asyncNewMode, uiSettings.getAsyncHwAccMode())) return; // If last value is NXP enable, do not show alert dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("Notice")
                                .setMessage("This feature can only enabled on a platform with NXP NPU (like i.MX8M Plus). Continue?")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> {
                                    uiSettings.setHwAccMode(EnginePreference.PREFER_NXP_DETECTION | EnginePreference.PREFER_NXP_EXTRACTION);
                                    uiSettings.setAsyncHwAccMode(AsyncEnginePreference.PREFER_NXP_DETECTION | AsyncEnginePreference.PREFER_NXP_EXTRACTION);
                                    broker.onSettingsChanged(true);
                                })
                                .setNeutralButton("Cancel", null);
                        builder.show();
                        break;
                    default:
                        break;
                }
                if (theSameValue(newMode, uiSettings.getHwAccMode()) && theSameValue(asyncNewMode, uiSettings.getAsyncHwAccMode())) return;

                if (!needUpdate) return;
                uiSettings.setHwAccMode(newMode);
                uiSettings.setAsyncHwAccMode(asyncNewMode);
                broker.onSettingsChanged(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void initAPIMode(View rootView) {
        radioAPIMode = rootView.findViewById(R.id.radioAPIMode);
        radioAPIMode.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            try {
                int newMode = Integer.parseInt((String) tag);
                if (theSameValue(newMode, uiSettings.getAPIMode())) return;

                uiSettings.setAPIMode(newMode);
                if (newMode == UiSettings.API_SYNC_MODE_VALUE) {
                    seekbarDetectBatchSize.setEnabled(false);
                    seekbarExtractBatchSize.setEnabled(false);
                } else if (newMode == UiSettings.API_ASYNC_MODE_VALUE) {
                    seekbarDetectBatchSize.setEnabled(true);
                    seekbarExtractBatchSize.setEnabled(true);
                }
                broker.onSettingsChanged(true);
            } catch (NumberFormatException ignored) {}
        });
    }


    private void initMaskDetection(View rootView) {
        btnMask = rootView.findViewById(R.id.btnMask);
        btnMask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isShowMaskDetection())) return;

            uiSettings.setShowMaskDetection(isChecked);
            if (uiSettings.getAPIMode() == UiSettings.API_SYNC_MODE_VALUE) {
                broker.onSettingsChanged(false);
            } else if (uiSettings.getAPIMode() == UiSettings.API_ASYNC_MODE_VALUE) {
                broker.onSettingsChanged(true);
            }
        });
    }

    private void initFeatureSet(View rootView) {
        btnFeatures = rootView.findViewById(R.id.btnFeatures);
        btnFeatures.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isShowFeatures())) return;

            if (!isChecked && btnAutoVisitor != null) {
                btnAutoVisitor.setChecked(false);
            }

            uiSettings.setShowFeatures(isChecked);
            broker.onSettingsChanged(false);
        });

        btnAge = rootView.findViewById(R.id.btnAge);
        btnAgeRange = rootView.findViewById(R.id.btnAgeRange);
        layoutAgeRange = rootView.findViewById(R.id.layoutAgeRange);
        btnAge.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isShowAge())) return;

            uiSettings.setShowAge(isChecked);
            broker.onSettingsChanged(false);
            layoutAgeRange.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        btnAgeRange.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isAgeInRange())) return;

            uiSettings.setAgeInRange(isChecked);
            broker.onSettingsChanged(false);
        });

        btnGender = rootView.findViewById(R.id.btnGender);
        btnGender.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isShowGender())) return;

            uiSettings.setShowGender(isChecked);
            broker.onSettingsChanged(false);
        });

        btnEmotion = rootView.findViewById(R.id.btnEmotion);
        btnEmotion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isShowEmotion())) return;

            uiSettings.setShowEmotion(isChecked);
            broker.onSettingsChanged(false);
        });

        btnPose = rootView.findViewById(R.id.btnPose);
        btnPose.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isShowPose())) return;

            uiSettings.setShowPose(isChecked);
            broker.onSettingsChanged(false);
        });
    }

    private void updateFeatureSet() {
        btnFeatures.setChecked(uiSettings.isShowFeatures());
        btnAge.setChecked(uiSettings.isShowAge());
        if (!uiSettings.isShowAge()) {
            layoutAgeRange.setVisibility(View.GONE);
        }
        btnAgeRange.setChecked(uiSettings.isAgeInRange());
        btnGender.setChecked(uiSettings.isShowGender());
        btnEmotion.setChecked(uiSettings.isShowEmotion());
        btnPose.setChecked(uiSettings.isShowPose());
    }

    private void initCameraConfig(View rootView) {
        radioCameraMirror = rootView.findViewById(R.id.radioCameraMirror);
        radioCameraMirror.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            String tag = (String) radioGroup.findViewById(checkedId).getTag();
            boolean newFlag = Boolean.parseBoolean(tag);
            boolean oldFlag = uiSettings.isFlipCameraOutput();
            try {
                if (theSameValue(newFlag, oldFlag)) return;

                uiSettings.setFlipCameraOutput(newFlag);
                broker.onCameraConfigChanged();
            } catch (NumberFormatException ignored) {}
        });

        radioCameraDisplayMirror = rootView.findViewById(R.id.radioCameraDisplayMirror);
        radioCameraDisplayMirror.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            String tag = (String) radioGroup.findViewById(checkedId).getTag();
            boolean newFlag = Boolean.parseBoolean(tag);
            boolean oldFlag = uiSettings.isFlipCameraDisplay();
            try {
                if (theSameValue(newFlag, oldFlag)) return;

                uiSettings.setFlipCameraDisplay(newFlag);
                broker.onCameraConfigChanged();
            } catch (NumberFormatException ignored) {}
        });

        radioCameraOrientation = rootView.findViewById(R.id.radioCameraOrientation);
        radioCameraOrientation.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            Integer oldFlag = uiSettings.getCameraOrientation();
            try {
                int newFlag = Integer.parseInt((String) tag);
                if (oldFlag == null && newFlag == -1) return;
                if (oldFlag != null && oldFlag == newFlag) return;

                uiSettings.setCameraOrientation(newFlag == -1 ? null : newFlag);
                broker.onCameraConfigChanged();
            } catch (NumberFormatException ignored) {}
        });

        radioCameraRotate = rootView.findViewById(R.id.radioCameraRotate);
        radioCameraRotate.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            Integer oldFlag = uiSettings.getCameraOutputRotation();
            try {
                int newFlag = Integer.parseInt((String) tag);
                if (oldFlag == null && newFlag == -1) return;
                if (oldFlag != null && oldFlag == newFlag) return;

                uiSettings.setCameraOutputRotation(newFlag == -1 ? null : newFlag);
                broker.onCameraConfigChanged();
            } catch (NumberFormatException ignored) {}
        });

        radioCameraDisplay = rootView.findViewById(R.id.radioCameraDisplay);
        radioCameraDisplay.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            Integer oldFlag = uiSettings.getCameraDisplayOrientation();
            try {
                int newFlag = Integer.parseInt((String) tag);
                if (oldFlag == null && newFlag == -1) return;
                if (oldFlag != null && oldFlag == newFlag) return;

                uiSettings.setCameraDisplayOrientation(newFlag == -1 ? null : newFlag);
                broker.onCameraConfigChanged();
            } catch (NumberFormatException ignored) {}
        });
    }

    private void updateCameraConfig() {
        {
            int childCount = radioCameraMirror.getChildCount();
            boolean flipOutput = uiSettings.isFlipCameraOutput();
            for (int idx = 0; idx < childCount; idx++) {
                View child = radioCameraMirror.getChildAt(idx);
                if (!(child instanceof RadioButton)) continue;
                if (!(child.getTag() instanceof String)) continue;

                if (Boolean.parseBoolean((String) child.getTag()) == flipOutput) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            }
        }
        {
            int childCount = radioCameraDisplayMirror.getChildCount();
            boolean flipOutput = uiSettings.isFlipCameraDisplay();
            for (int idx = 0; idx < childCount; idx++) {
                View child = radioCameraDisplayMirror.getChildAt(idx);
                if (!(child instanceof RadioButton)) continue;
                if (!(child.getTag() instanceof String)) continue;

                if (Boolean.parseBoolean((String) child.getTag()) == flipOutput) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            }
        }
        {
            int childCount = radioCameraOrientation.getChildCount();
            Integer cameraOrientation = uiSettings.getCameraOrientation();
            for (int idx = 0; idx < childCount; idx++) {
                View child = radioCameraOrientation.getChildAt(idx);
                if (!(child instanceof RadioButton)) continue;
                if (!(child.getTag() instanceof String)) continue;

                try {
                    int tagValue = Integer.parseInt((String) child.getTag());
                    if (cameraOrientation == null && tagValue == -1 ||
                            cameraOrientation != null && cameraOrientation == tagValue) {
                        ((RadioButton) child).setChecked(true);
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        {
            int childCount = radioCameraRotate.getChildCount();
            Integer outputRotation = uiSettings.getCameraOutputRotation();
            for (int idx = 0; idx < childCount; idx++) {
                View child = radioCameraRotate.getChildAt(idx);
                if (!(child instanceof RadioButton)) continue;
                if (!(child.getTag() instanceof String)) continue;

                try {
                    int tagValue = Integer.parseInt((String) child.getTag());
                    if (outputRotation == null && tagValue == -1 ||
                            outputRotation != null && outputRotation == tagValue) {
                        ((RadioButton) child).setChecked(true);
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        {
            int childCount = radioCameraDisplay.getChildCount();
            Integer displayOrientation = uiSettings.getCameraDisplayOrientation();
            for (int idx = 0; idx < childCount; idx++) {
                View child = radioCameraDisplay.getChildAt(idx);
                if (!(child instanceof RadioButton)) continue;
                if (!(child.getTag() instanceof String)) continue;

                try {
                    int tagValue = Integer.parseInt((String) child.getTag());
                    if (displayOrientation == null && tagValue == -1 ||
                            displayOrientation != null && displayOrientation == tagValue) {
                        ((RadioButton) child).setChecked(true);
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private void initRSCameraConfig(View rootView) {
        radioRSCameraMirror = rootView.findViewById(R.id.radioRSCameraMirror);
        radioRSCameraMirror.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            String tag = (String) radioGroup.findViewById(checkedId).getTag();
            boolean newFlag = Boolean.parseBoolean(tag);
            boolean oldFlag = uiSettings.isFlipCameraDisplay();
            try {
                if (theSameValue(newFlag, oldFlag)) return;

                uiSettings.setFlipCameraDisplay(newFlag);
                broker.onCameraConfigChanged();
            } catch (NumberFormatException ignored) {}
        });
    }

    private void updateRSCameraConfig() {
        {
            int childCount = radioRSCameraMirror.getChildCount();
            boolean flipOutput = uiSettings.isFlipCameraDisplay();
            for (int idx = 0; idx < childCount; idx++) {
                View child = radioRSCameraMirror.getChildAt(idx);
                if (!(child instanceof RadioButton)) continue;
                if (!(child.getTag() instanceof String)) continue;

                if (Boolean.parseBoolean((String) child.getTag()) == flipOutput) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            }
        }
    }

    private void init2DasOptions(View rootView) {
        actionOption2Das2ndStage = rootView.findViewById(R.id.actionOption2Das2ndStage);
        radio2Das2ndStage = rootView.findViewById(R.id.radio2Das2ndStage);
        radio2Das2ndStage.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            try {
                int newMode = Integer.parseInt((String) tag);
                if (theSameValue(newMode, uiSettings.get2DasUse2ndStage())) return;

                if (newMode != AntiSpoofingConfig.INTERACTION_RANDOM) {
                    actionOption2Das2ndStage.setVisibility(View.GONE);
                } else {
                    actionOption2Das2ndStage.setVisibility(View.VISIBLE);
                }

                uiSettings.set2DasUse2ndStage(newMode);
                broker.onSettingsChanged(false);
            } catch (NumberFormatException ignored) {}
        });

        radio2DasPrecisionMode = rootView.findViewById(R.id.radio2DasPrecisionMode);
        radio2DasPrecisionMode.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            try {
                int newMode = Integer.parseInt((String) tag);
                if (theSameValue(newMode, uiSettings.get2DasPrecisionMode())) return;

                uiSettings.set2DasPrecisionMode(newMode);
                broker.onSettingsChanged(false);
            } catch (NumberFormatException ignored) {}
        });

        radio2DasVoiceLangMode = rootView.findViewById(R.id.radio2DasVoiceLangMode);
        radio2DasVoiceLangMode.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            try {
                int newMode = Integer.parseInt((String) tag);
                int oldMode = -1;
                if ("zho".equals(uiSettings.getVoiceLangCode())) oldMode = 1;
                else if ("eng".equals(uiSettings.getVoiceLangCode())) oldMode = 2;
                if (theSameValue(newMode, oldMode)) return;

                String newValue = "";
                if (newMode == 1) newValue = "zho";
                else if (newMode == 2) newValue = "eng";
                uiSettings.setVoiceLangCode(newValue);
                broker.onSettingsChanged(true);
            } catch (NumberFormatException ignored) {}
        });

        seekBar2DasActionCount = rootView.findViewById(R.id.seekbar2DasActionCount);
        txt2DasActionCount = rootView.findViewById(R.id.txt2DasActionCount);
        seekBar2DasActionCount.setMax(MAX_2DAS_ACTION_COUNT - MIN_2DAS_ACTION_COUNT);

        seekBar2DasActionCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                update2DasActionCount(progress);

                int new2DasActionCount = progress + MIN_2DAS_ACTION_COUNT;
                if (theSameValue(new2DasActionCount, uiSettings.get2DasActionCount())) return;

                uiSettings.set2DasActionCount(new2DasActionCount);
                broker.onSettingsChanged(false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btn2DasNodEnable = rootView.findViewById(R.id.btn2DasNodEnable);
        btn2DasNodEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.is2DasNodActionEnable())) return;

            uiSettings.set2DasNodActionEnable(isChecked);
            broker.onSettingsChanged(false);
        });

        btn2DasSmileEnable = rootView.findViewById(R.id.btn2DasSmileEnable);
        btn2DasSmileEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.is2DasSmileActionEnable())) return;

            uiSettings.set2DasSmileActionEnable(isChecked);
            broker.onSettingsChanged(false);
        });

        option2DasVoiceLang = rootView.findViewById(R.id.option2DasVoiceLang);
        btn2DasVoiceEnable = rootView.findViewById(R.id.btn2DasVoiceEnable);
        btn2DasVoiceEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.is2DasVoiceEnable())) return;

            uiSettings.set2DasVoiceEnable(isChecked);
            broker.onSettingsChanged(false);
            option2DasVoiceLang.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btn2DasVibrateEnable = rootView.findViewById(R.id.btn2DasVibrateEnable);
        btn2DasVibrateEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.is2DasVibrateEnable())) return;

            uiSettings.set2DasVibrateEnable(isChecked);
            broker.onSettingsChanged(false);
        });
    }

    private void update2DasOptions() {
        int childCount = radio2Das2ndStage.getChildCount();
        int TwoDasUse2ndStage = uiSettings.get2DasUse2ndStage();
        for (int idx = 0; idx < childCount; idx++) {
            View child = radio2Das2ndStage.getChildAt(idx);
            if (!(child instanceof RadioButton)) continue;
            if (!(child.getTag() instanceof String)) continue;

            try {
                if (Integer.parseInt((String) child.getTag()) == TwoDasUse2ndStage) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            } catch (NumberFormatException ignored) {}
        }

        if (uiSettings.get2DasUse2ndStage() != AntiSpoofingConfig.INTERACTION_RANDOM) {
            actionOption2Das2ndStage.setVisibility(View.GONE);
        } else {
            actionOption2Das2ndStage.setVisibility(View.VISIBLE);
        }

        childCount = radio2DasPrecisionMode.getChildCount();
        int precisionMode = uiSettings.get2DasPrecisionMode();
        for (int idx = 0; idx < childCount; idx++) {
            View child = radio2DasPrecisionMode.getChildAt(idx);
            if (!(child instanceof RadioButton)) continue;
            if (!(child.getTag() instanceof String)) continue;

            try {
                if (Integer.parseInt((String) child.getTag()) == precisionMode) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            } catch (NumberFormatException ignored) {}
        }

        if (!uiSettings.is2DasVoiceEnable()) {
            option2DasVoiceLang.setVisibility(View.GONE);
        } else {
            option2DasVoiceLang.setVisibility(View.VISIBLE);
        }

        childCount = radio2DasVoiceLangMode.getChildCount();
        int langCodeMode = -1;
        String langCode = uiSettings.getVoiceLangCode();
        if ("zho".equals(langCode)) langCodeMode = 1;
        if ("eng".equals(langCode)) langCodeMode = 2;
        for (int idx = 0; idx < childCount; idx++) {
            View child = radio2DasVoiceLangMode.getChildAt(idx);
            if (!(child instanceof RadioButton)) continue;
            if (!(child.getTag() instanceof String)) continue;

            try {
                if (Integer.parseInt((String) child.getTag()) == langCodeMode) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            } catch (NumberFormatException ignored) {}
        }

        btn2DasNodEnable.setChecked(uiSettings.is2DasNodActionEnable());
        btn2DasSmileEnable.setChecked(uiSettings.is2DasSmileActionEnable());
        btn2DasVoiceEnable.setChecked(uiSettings.is2DasVoiceEnable());
        btn2DasVibrateEnable.setChecked(uiSettings.is2DasVibrateEnable());
    }

    private void update2DasActionCount(int seekBarProgress) {
        txt2DasActionCount.setText(String.valueOf(seekBarProgress + MIN_2DAS_ACTION_COUNT));
    }

    private void initAutoVisitor(View rootView) {
        btnAutoVisitor = rootView.findViewById(R.id.btnAutoVisitor);
        btnAutoVisitor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.isShowVisitCount())) return;

            if (isChecked && (!uiSettings.isShowFeatures() || !uiSettings.isShowPose())) {
                CLToast.showLong("Enable face features extraction and Face Angle first.");
                buttonView.setChecked(false);
                return;
            }

            uiSettings.setShowVisitCount(isChecked);
            broker.onSettingsChanged(false);
        });
    }

    private void init3DasOption(View rootView) {
        btnRsInfrared = rootView.findViewById(R.id.btnRsInfrared);
        btnRsInfrared.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (theSameValue(isChecked, uiSettings.get3DasInfraredMode() == LivenessSingleFaceInfraredMode.INFRARED)) return;

            if (isChecked) {
                uiSettings.set3DasInfraredMode(LivenessSingleFaceInfraredMode.INFRARED);
                btnRsCalibration.setEnabled(true);
            } else {
                uiSettings.set3DasInfraredMode(LivenessSingleFaceInfraredMode.NONE);
                btnRsCalibration.setEnabled(false);
            }

            broker.onSettingsChanged(false);
        });

        btnRsCalibration = rootView.findViewById(R.id.btnRsCalibration);
        btnRsCalibration.setOnClickListener((v -> {
            uiSettings.set3DasInfraredLaserPower(0F);

            broker.onSettingsChanged(false);
        }));

        radio3DasSpeedLevel = rootView.findViewById(R.id.radio3dAntiSpoofingSpeedLevel);
        radio3DasSpeedLevel.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Object tag = radioGroup.findViewById(checkedId).getTag();
            try {
                int newLevel = Integer.parseInt((String) tag);
                if (theSameValue(newLevel, uiSettings.get3DasSpeedLevel())) return;

                uiSettings.set3DasSpeedLevel(newLevel);
                broker.onSettingsChanged(false);
            } catch (NumberFormatException ignored) {}
        });
    }

    private void update3DasOption(int level) {
        boolean isInfraredMode = uiSettings.get3DasInfraredMode() == LivenessSingleFaceInfraredMode.INFRARED;
        btnRsInfrared.setChecked(isInfraredMode);
        btnRsCalibration.setEnabled(isInfraredMode);

        int childCount = radio3DasSpeedLevel.getChildCount();
        for (int idx = 0; idx < childCount; idx++) {
            View child = radio3DasSpeedLevel.getChildAt(idx);
            if (!(child instanceof RadioButton)) continue;
            if (!(child.getTag() instanceof String)) continue;

            try {
                if (Integer.parseInt((String) child.getTag()) == level) {
                    ((RadioButton) child).setChecked(true);
                    break;
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void initLaserPower(View rootView) {
        seekBarLaserPower = rootView.findViewById(R.id.seekbarLaserPower);
        btnAddLaserPower = rootView.findViewById(R.id.btnAddLaserPower);
        btnRemoveLaserPower = rootView.findViewById(R.id.btnRemoveLaserPower);
        txtLaserPower = rootView.findViewById(R.id.txtLaserPower);

        seekBarLaserPower.setMax(MAX_LASER_POWER - MIN_LASER_POWER);
        btnAddLaserPower.setOnClickListener((v) -> seekBarLaserPower.setProgress(seekBarLaserPower.getProgress() + 1));
        btnRemoveLaserPower.setOnClickListener((v) -> seekBarLaserPower.setProgress(seekBarLaserPower.getProgress() - 1));

        seekBarLaserPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateLaserPowerValue(progress);

                int newLaserPower = progress + MIN_LASER_POWER;
                if (theSameValue(newLaserPower, (int) uiSettings.get3DasInfraredLaserPower())) return;

                uiSettings.set3DasInfraredLaserPower(newLaserPower);
                broker.onSettingsChanged(false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateLaserPowerValue(int seekBarProgress) {
        txtLaserPower.setText(String.valueOf(seekBarProgress + MIN_LASER_POWER));
        btnAddLaserPower.setEnabled(seekBarProgress < seekBarLaserPower.getMax());
        btnRemoveLaserPower.setEnabled(seekBarProgress > 0);
    }

    private void updateUiSettings() {
        int progressValue;

        progressValue = (int) (uiSettings.getMinFaceWidthRatio() * RELATED_MIN_FACE_WIDTH_RATIO) - MIN_MIN_FACE_WIDTH_RATIO;
        seekBarMinFaceWidthRatio.setProgress(progressValue);
        updateMinFaceWidthRatioValue(progressValue);

        updateEnginePreference();

        updateExtractModel(uiSettings.getExtractModel());
        updatePrecisionLevel(uiSettings.getPrecisionLevel());

        if (enableFlag(UI_API_MODE) && uiSettings.getAPIMode() == UiSettings.API_ASYNC_MODE_VALUE)
            updateHWAccMode(uiSettings.getAsyncHwAccMode());
        else
            updateHWAccMode(uiSettings.getHwAccMode());

        updateAPIMode(uiSettings.getAPIMode());
        updateBatchSize();

        btnLandmark.setChecked(uiSettings.isShowLandmark());

        btnMask.setChecked(uiSettings.isShowMaskDetection());

        updateFeatureSet();
        updateCameraConfig();
        updateRSCameraConfig();
        update2DasOptions();
        progressValue = uiSettings.get2DasActionCount() - MIN_2DAS_ACTION_COUNT;
        seekBar2DasActionCount.setProgress(progressValue);
        update2DasActionCount(progressValue);

        btnAutoVisitor.setChecked(uiSettings.isShowVisitCount());

        update3DasOption(uiSettings.get3DasSpeedLevel());
        progressValue = (int) (uiSettings.get3DasInfraredLaserPower() - MIN_LASER_POWER);
        seekBarLaserPower.setProgress(progressValue);
        updateLaserPowerValue(progressValue);
    }

    private void logWarningWithStack() {
        try {
            throw new IllegalStateException();
        } catch (Exception e) {
            Log.w(TAG, "Layout changed, need to review code logic here", e);
        }
    }

    private void hideMostLayouts(ViewGroup rootView) {
        // XXX: Root view of current layout implementation should be
        //      ScrollView -> LinearLayout.
        int childCount = rootView.getChildCount();
        if (childCount != 1) {
            logWarningWithStack();
            return;
        }

        // It should be a single layout View as child of ScrollView.
        ViewGroup layoutView = (ViewGroup) rootView.getChildAt(0);
        childCount = layoutView.getChildCount();
        // XXX: Should have version, separator, ... at least 3 Views.
        if (childCount < 3) {
            logWarningWithStack();
            return;
        }

        ArrayList<Integer> viewToBeShown = new ArrayList<>();
        viewToBeShown.add(R.id.layoutVersion);

        if (enableFlag(UI_MIN_FACE)) viewToBeShown.add(R.id.layoutMinFaceWidth);
        if (enableFlag(UI_VIDEO_MODE)) viewToBeShown.add(R.id.layoutFastDetection);
        if (enableFlag(UI_MASK_DETECTION)) viewToBeShown.add(R.id.layoutMaskDetection);
        if (enableFlag(UI_EXTRACTION_MODEL)) viewToBeShown.add(R.id.layoutFaceExtraction);
        if (enableFlag(UI_PRECISION_LEVEL)) viewToBeShown.add(R.id.layoutPrecisionLevel);
        if (enableFlag(UI_ENGINE_PREFERENCE)) viewToBeShown.add(R.id.layoutEnginePreference);
        if (enableFlag(UI_FACE_LANDMARKS)) viewToBeShown.add(R.id.layoutFaceLandmarks);
        if (enableFlag(UI_FACE_FEATURES)) viewToBeShown.add(R.id.layoutFaceFeatures);
        if (enableFlag(UI_2D_ANTI_SPOOFING)) viewToBeShown.add(R.id.layout2DAntiSpoofingOpt);
        if (enableFlag(UI_CAMERA_CONFIG)) viewToBeShown.add(R.id.layoutConfigCamera);
        if (enableFlag(UI_RS_CAMERA_CONFIG)) viewToBeShown.add(R.id.layoutConfigRSCamera);
        if (enableFlag(UI_VISIT_COUNT)) viewToBeShown.add(R.id.layoutVisitCount);
        if (enableFlag(UI_3D_ANTI_SPOOFING_OPTION)) viewToBeShown.add(R.id.layout3DAntiSpoofingOpt);
        if (enableFlag(UI_3D_ANTI_SPOOFING_LASER_POWER)) viewToBeShown.add(R.id.layout3DLaserPower);
        if (enableFlag(UI_HW_ACCELERATION)) viewToBeShown.add(R.id.layoutHWAcceleration);
        if (enableFlag(UI_API_MODE)) viewToBeShown.add(R.id.layoutAPIMode);
        if (enableFlag(UI_BATCH_SIZE)) viewToBeShown.add(R.id.layoutBatchSize);

        int visibility;
        View view;
        for (int idx = 0; idx < childCount; idx++) {
            view = layoutView.getChildAt(idx);

            visibility = viewToBeShown.contains(view.getId()) ? View.VISIBLE : View.GONE;

            view.setVisibility(visibility);
            if ((idx - 1) > 0) {
                view = layoutView.getChildAt(idx - 1);
                if (view.getId() == R.id.viewSeparator) {
                    view.setVisibility(visibility);
                }
            }
        }
    }
}
