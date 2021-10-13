/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.cyberlink.facemedemo.extension.R;

public abstract class BaseActivity extends AppCompatActivity implements SettingsFragment.Broker {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int APP_SETTING_REQUEST_CODE = 200;

    private boolean isActive = false;

    protected UiSettings uiSettings;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    
    protected final void onCreate(Bundle savedInstanceState) {
        long start = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        requestDefaultDisplaySize();
        setContentView(getContentLayout());

        uiSettings = new UiSettings(this);
        // Request necessary permission.
        requestPermissionsIfNeeded();
        initialize();

        Log.d(getTagId(), "onCreate took " + (System.currentTimeMillis() - start) + "ms");
    }

    @CallSuper
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @CallSuper
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    protected abstract String getTagId();

    @LayoutRes
    protected abstract int getContentLayout();

    protected abstract void initialize();

    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return; // Lower Android OS, no need.
        if (hasRequiredPermissions()) return; // Has granted, no need.

        requestPermissions(getRequiredPermissions(), PERMISSION_REQUEST_CODE);
    }

    protected final boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true; // Lower Android OS, always true.

        boolean allGranted = true;
        for (String permission : getRequiredPermissions()) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        return allGranted;
    }

    // For fix the UI layout is wrong when user enlarge the display size of devices.
    // reference: https://stackoverflow.com/questions/50252424/disabling-an-app-or-activity-zoom-if-setting-display-display-size-changed/50306032#50306032
    private void requestDefaultDisplaySize() {
        Configuration configuration = getResources().getConfiguration();
        configuration.fontScale = (float) 1; // 0.85 small size, 1 normal size, 1,15 big etc
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Set system default density if system provide it.
            configuration.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE;
        } else {
            configuration.densityDpi = (int) getResources().getDisplayMetrics().xdpi;
        }
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @CallSuper
    protected void onStart() {
        super.onStart();
        DevTool.touch();
    }

    protected static boolean isDevMode() {
        return DevTool.isDevMode();
    }

    @CallSuper
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != APP_SETTING_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (hasRequiredPermissions()) {
            onPermissionsGranted();
        } else {
            requestPermissionsIfNeeded();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            onPermissionsGranted();
            return;
        }

        showDialogForPermission();
    }

    private void showDialogForPermission() {
        String msg = getString(R.string.ext_permission_fail, getPermissionString());
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("Go to App Settings", (dialog, which) -> openAppSettingPage())
                .setNegativeButton("Close", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void openAppSettingPage() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", getPackageName(), null));
        try {
            startActivityForResult(intent, APP_SETTING_REQUEST_CODE);
        } catch (ActivityNotFoundException t) {
            finish();
        }
    }

    protected void requestFullscreenMode() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    protected abstract String[] getRequiredPermissions();

    protected abstract String getPermissionString();

    protected void onPermissionsGranted() {}

    protected boolean isActive() {
        return isActive;
    }
}
