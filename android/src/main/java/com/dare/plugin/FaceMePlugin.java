package com.dare.plugin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dare.plugin.liveness.*;

import com.getcapacitor.FileUtils;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(
        name = "FaceMe",
        permissions = {
                @Permission(strings = { Manifest.permission.CAMERA }, alias = FaceMePlugin.CAMERA),
                @Permission(
                        strings = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        alias = FaceMePlugin.STORAGE
                )
        }
)
public class FaceMePlugin extends Plugin implements CameraActivity.CameraPreviewListener {

    private FaceMe implementation = new FaceMe();

    // Permission alias constants
    static final String CAMERA  = "camera";
    static final String STORAGE = "storage";

    // Message constants
    private static final String PERMISSION_DENIED_ERROR_CAMERA  = "User denied access to camera";
    private static final String PERMISSION_DENIED_ERROR_STORAGE = "User denied access to storage";
    private static final String NO_CAMERA_ERROR                 = "Device doesn't have a camera available";

    // keep track of previously specified orientation to support locking orientation:
    private int previousOrientationRequest = -1;

    private CameraActivity fragment;

    private int containerViewId = 20;

    @PluginMethod
    public void initialize(PluginCall call) {
        System.out.println("FaceMe initialize");

        JSObject ret     = new JSObject();
        String   license = call.getString("license");

        ret.put("version",
                implementation.initialize(this.getContext(),
                                          license));
        call.resolve(ret);
    }

    @PluginMethod
    public void enroll(PluginCall call) {
        System.out.println("FaceMe enroll");

        JSObject ret   = new JSObject();
        String   name  = call.getString("name");
        String   data  = call.getString("data");
        String   image = call.getString("imageBase64");

        ret.put("collectionId",
                implementation.enroll(name,
                                      image,
                                      data));
        call.resolve(ret);
    }

    @PluginMethod
    public void search(PluginCall call) {
        System.out.println("FaceMe search");

        JSObject ret;
        String   image = call.getString("imageBase64");

        if(image != null) {
            ret = implementation.recognize(image);
        } else {
            ret = new JSObject();
        }

        call.resolve(ret);
    }

    @PluginMethod
    public void setCollectionName(PluginCall call) {
        System.out.println("FaceMe set collection name");

        JSObject ret          = new JSObject();
        long     collectionId = Long.valueOf(call.getInt("collectionId"));
        String   name         = call.getString("name");

        ret.put("value",
                implementation.setCollectionName(collectionId, name));
        call.resolve(ret);
    }

    @PluginMethod
    public void getCollectionName(PluginCall call) {
        System.out.println("FaceMe get collection name");

        JSObject ret      = new JSObject();
        long collectionId = Long.valueOf(call.getInt("collectionId"));

        ret.put("name",
                implementation.getCollectionName(collectionId));
        call.resolve(ret);
    }
    
    @PluginMethod
    public void setCollectionData(PluginCall call) {
        System.out.println("FaceMe set collection data");

        JSObject ret          = new JSObject();
        long     collectionId = Long.valueOf(call.getInt("collectionId"));
        String   data         = call.getString("data");

        ret.put("value",
                implementation.setCollectionData(collectionId, data));
        call.resolve(ret);
    }

    @PluginMethod
    public void getCollectionData(PluginCall call) {
        System.out.println("FaceMe get collection data");

        JSObject ret          = new JSObject();
        long     collectionId = Long.valueOf(call.getInt("collectionId"));

        ret.put("data",
                implementation.getCollectionData(collectionId));
        call.resolve(ret);
    }

    @PluginMethod
    public void deleteCollection(PluginCall call) {
        System.out.println("FaceMe delete collection");

        JSObject ret          = new JSObject();
        long     collectionId = Long.valueOf(call.getInt("collectionId"));

        ret.put("value",
                implementation.deleteCollection(collectionId));
        call.resolve(ret);
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    public void initCamera(PluginCall call) {
        saveCall(call);
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            call.reject(NO_CAMERA_ERROR);
            return;
        }
        final Integer x             = call.getInt("x");
        final Integer y             = call.getInt("y");
        final Integer width         = call.getInt("width");
        final Integer height        = call.getInt("height");
        final Integer paddingBottom = call.getInt("paddingBottom");
        final String  position      = call.getString("position");
        openCamera(call,
                   x,
                   y,
                   width,
                   height,
                   paddingBottom,
                   position);
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    public void closeCamera(PluginCall call) {
        try {
            bridge.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    FrameLayout containerView = getBridge().getActivity().findViewById(containerViewId);
                    containerView.destroyDrawingCache();
                    containerView.removeAllViews();

                    fragment.closeCamera();
                }
            });
        } catch (Exception e) {
            call.reject("Failed to close camera at " + e.getMessage());
        }
    }

    @PluginMethod(returnType = PluginMethod.RETURN_NONE)
    public void takePicture(PluginCall call) {
        if(!hasCamera(call)){
            call.error("Camera is not running");
            return;
        }
        saveCall(call);

        final Integer width   = call.getInt("width");
        final Integer height  = call.getInt("height");
        final Integer quality = call.getInt("quality");
        try {
            fragment.takePicture(width, height, quality);
        } catch (Exception e) {
            call.reject("Failed to take picture at " + e.getMessage());
        }
    }

    @SuppressLint("WrongConstant")
    private void openCamera(final PluginCall call,
                            final Integer    x,
                            final Integer    y,
                            final Integer    width,
                            final Integer    height,
                            final Integer    paddingBottom,
                            final String     position) {
        if (checkCameraPermissions(call)) {

            final Boolean toBack                     = call.getBoolean("toBack", false);
            final Boolean storeToFile                = call.getBoolean("storeToFile", false);
            final Boolean disableExifHeaderStripping = call.getBoolean("disableExifHeaderStripping", true);
            final Boolean lockOrientation            = call.getBoolean("lockAndroidOrientation", false);

            previousOrientationRequest               = getBridge().getActivity().getRequestedOrientation();

            fragment = new CameraActivity();
            fragment.setEventListener(this);

            fragment.defaultCamera              = position;
            fragment.tapToTakePicture           = false;
            fragment.dragEnabled                = false;
            fragment.tapToFocus                 = true;
            fragment.disableExifHeaderStripping = disableExifHeaderStripping;
            fragment.storeToFile                = storeToFile;
            fragment.toBack                     = toBack;

            bridge.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DisplayMetrics metrics = getBridge().getActivity().getResources().getDisplayMetrics();
                    // lock orientation if specified in options:
                    if (lockOrientation) {
                        getBridge().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    }

                    // offset
                    int computedX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, metrics);
                    int computedY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, y, metrics);

                    // size
                    int computedWidth;
                    int computedHeight;
                    int computedPaddingBottom;

                    if(paddingBottom != 0) {
                        computedPaddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingBottom, metrics);
                    } else {
                        computedPaddingBottom = 0;
                    }

                    if(width != 0) {
                        computedWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, metrics);
                    } else {
                        Display defaultDisplay = getBridge().getActivity().getWindowManager().getDefaultDisplay();
                        final Point size = new Point();
                        defaultDisplay.getSize(size);

                        computedWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size.x, metrics);
                    }

                    if(height != 0) {
                        computedHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, metrics) - computedPaddingBottom;
                    } else {
                        Display defaultDisplay = getBridge().getActivity().getWindowManager().getDefaultDisplay();
                        final Point size = new Point();
                        defaultDisplay.getSize(size);

                        computedHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size.y, metrics) - computedPaddingBottom;
                    }

                    fragment.setRect(computedX, computedY, computedWidth, computedHeight);

                    FrameLayout containerView = getBridge().getActivity().findViewById(containerViewId);
//                    if(containerView == null){
                        containerView = new FrameLayout(getActivity().getApplicationContext());
                        containerView.setId(containerViewId);

                        getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
                        ((ViewGroup)getBridge().getWebView().getParent()).addView(containerView);
                        if(toBack == true) {
                            getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView());
                        }

                        FragmentManager fragmentManager = getBridge().getActivity().getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(containerView.getId(), fragment);
                        fragmentTransaction.commit();
                        call.success();
//                    } else {
//                        call.reject("camera already started");
//                    }
                }
            });
        }

    }

    private boolean checkCameraPermissions(PluginCall call) {
        // if the manifest does not contain the camera permissions key, we don't need to ask the user
        boolean needCameraPerms = isPermissionDeclared(CAMERA);
        boolean hasCameraPerms  = !needCameraPerms || getPermissionState(CAMERA) == PermissionState.GRANTED;
        if (!hasCameraPerms) {
            requestPermissionForAlias(CAMERA, call, "cameraPermissionsCallback");
            return false;
        }
        return true;
    }

    @PermissionCallback
    private void cameraPermissionsCallback(PluginCall call) {
        if (getPermissionState(CAMERA) != PermissionState.GRANTED) {
            Logger.debug(getLogTag(), "User denied camera permission: " + getPermissionState(CAMERA).toString());
            call.reject(PERMISSION_DENIED_ERROR_CAMERA);
            return;
        } else if (getPermissionState(STORAGE) != PermissionState.GRANTED) {
            Logger.debug(getLogTag(), "User denied storage permission: " + getPermissionState(STORAGE).toString());
            call.reject(PERMISSION_DENIED_ERROR_STORAGE);
            return;
        }
    }

    private boolean hasCamera(PluginCall call) {
        if(this.hasView(call) == false){
            return false;
        }
        if(fragment.getCamera() == null) {
            return false;
        }
        return true;
    }

    private boolean hasView(PluginCall call) {
        if(fragment == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void handleOnResume() {
        super.handleOnResume();
    }

    @Override
    public void onPictureTaken(String originalPicture) {
        JSObject jsObject = new JSObject();
        jsObject.put("value", originalPicture);
        getSavedCall().success(jsObject);
    }

    @Override
    public void onPictureTakenError(String message) {
        getSavedCall().reject(message);
    }

    @Override
    public void onSnapshotTaken(String originalPicture) {
        JSObject jsObject = new JSObject();
        jsObject.put("value", originalPicture);
        getSavedCall().success(jsObject);
    }

    @Override
    public void onSnapshotTakenError(String message) {
        getSavedCall().reject(message);
    }

    @Override
    public void onFocusSet(int pointX, int pointY) {

    }

    @Override
    public void onFocusSetError(String message) {

    }

    @Override
    public void onBackButton() {

    }

    @Override
    public void onCameraStarted() {
        PluginCall pluginCall = getSavedCall();
        System.out.println("camera started");

        pluginCall.success();
    }

    @Override
    public void onStartRecordVideo() {

    }

    @Override
    public void onStartRecordVideoError(String message) {
        getSavedCall().reject(message);
    }

    @Override
    public void onStopRecordVideo(String file) {
        PluginCall pluginCall = getSavedCall();
        JSObject jsObject = new JSObject();
        jsObject.put("videoFilePath", file);
        pluginCall.success(jsObject);
    }

    @Override
    public void onStopRecordVideoError(String error) {
        getSavedCall().reject(error);
    }



}
