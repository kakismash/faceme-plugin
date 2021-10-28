package com.dare.plugin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.activity.result.ActivityResult;
import androidx.core.content.FileProvider;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@CapacitorPlugin(
        name = "FaceMe",
        permissions = {
                @Permission(strings = { Manifest.permission.CAMERA }, alias = FaceMePlugin.CAMERA),
                @Permission(
                        strings = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        alias = FaceMePlugin.PHOTOS
                )
        }
)
public class FaceMePlugin extends Plugin {

    private FaceMe implementation = new FaceMe();

    // Permission alias constants
    static final String CAMERA = "camera";
    static final String PHOTOS = "photos";

    // Message constants
    private static final String PERMISSION_DENIED_ERROR_CAMERA = "User denied access to camera";
    private static final String PERMISSION_DENIED_ERROR_PHOTOS = "User denied access to photos";
    private static final String NO_CAMERA_ERROR = "Device doesn't have a camera available";
    private static final String NO_CAMERA_ACTIVITY_ERROR = "Unable to resolve camera activity";

    private CameraSettings settings = new CameraSettings();

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

        JSObject ret     = new JSObject();
        String   name    = call.getString("name");
        String   data    = call.getString("data");
        String   image   = call.getString("imageBase64");

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
        long     collectionId = Long.parseLong(call.getString("collectionId"));
        String   name         = call.getString("name");

        ret.put("value",
                implementation.setCollectionName(collectionId, name));
        call.resolve(ret);
    }

    @PluginMethod
    public void getCollectionName(PluginCall call) {
        System.out.println("FaceMe get collection name");

        JSObject ret      = new JSObject();
        long collectionId = Long.parseLong(call.getString("collectionId"));

        ret.put("name",
                implementation.getCollectionName(collectionId));
        call.resolve(ret);
    }
    
    @PluginMethod
    public void setCollectionData(PluginCall call) {
        System.out.println("FaceMe set collection data");

        JSObject ret          = new JSObject();
        long     collectionId = Long.parseLong(call.getString("collectionId"));
        String   data         = call.getString("data");

        ret.put("value",
                implementation.setCollectionData(collectionId, data));
        call.resolve(ret);
    }

    @PluginMethod
    public void getCollectionData(PluginCall call) {
        System.out.println("FaceMe get collection data");

        JSObject ret          = new JSObject();
        long     collectionId = Long.parseLong(call.getString("collectionId"));

        ret.put("data",
                implementation.getCollectionData(collectionId));
        call.resolve(ret);
    }

    @PluginMethod
    public void deleteCollection(PluginCall call) {
        System.out.println("FaceMe delete collection");

        JSObject ret          = new JSObject();
        long     collectionId = Long.parseLong(call.getString("collectionId"));

        ret.put("value",
                implementation.deleteCollection(collectionId));
        call.resolve(ret);
    }

    @PluginMethod
    public void initCamera(PluginCall call) {
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            call.reject(NO_CAMERA_ERROR);
            return;
        }
        settings = getSettings(call);
        openCamera(call);
    }

    private CameraSettings getSettings(PluginCall call) {
        CameraSettings settings = new CameraSettings();
        settings.setQuality(call.getInt("quality", CameraSettings.DEFAULT_QUALITY));
        settings.setWidth(call.getInt("width", 0));
        settings.setHeight(call.getInt("height", 0));
        settings.setShouldResize(settings.getWidth() > 0 || settings.getHeight() > 0);
        settings.setShouldCorrectOrientation(call.getBoolean("correctOrientation", CameraSettings.DEFAULT_CORRECT_ORIENTATION));
        try {
            settings.setSource(CameraSource.valueOf(call.getString("source", CameraSource.PROMPT.getSource())));
        } catch (IllegalArgumentException ex) {
            settings.setSource(CameraSource.PROMPT);
        }
        return settings;
    }

    private void openCamera(final PluginCall call) {
        if (checkCameraPermissions(call)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivityForResult(call, takePictureIntent, "processCameraImage");
            } else {
                call.reject(NO_CAMERA_ACTIVITY_ERROR);
            }
        }
    }

    private boolean checkCameraPermissions(PluginCall call) {
        // if the manifest does not contain the camera permissions key, we don't need to ask the user
        boolean needCameraPerms = isPermissionDeclared(CAMERA);
        boolean hasCameraPerms = !needCameraPerms || getPermissionState(CAMERA) == PermissionState.GRANTED;
        if (!hasCameraPerms) {
            requestPermissionForAlias(CAMERA, call, "cameraPermissionsCallback");
            return false;
        }
        return true;
    }

    @PermissionCallback
    private void cameraPermissionsCallback(PluginCall call) {
        if (settings.getSource() == CameraSource.CAMERA && getPermissionState(CAMERA) != PermissionState.GRANTED) {
            Logger.debug(getLogTag(), "User denied camera permission: " + getPermissionState(CAMERA).toString());
            call.reject(PERMISSION_DENIED_ERROR_CAMERA);
            return;
        } else if (settings.getSource() == CameraSource.PHOTOS && getPermissionState(PHOTOS) != PermissionState.GRANTED) {
            Logger.debug(getLogTag(), "User denied photos permission: " + getPermissionState(PHOTOS).toString());
            call.reject(PERMISSION_DENIED_ERROR_PHOTOS);
            return;
        }
    }

    @ActivityCallback
    public void processCameraImage(PluginCall call, ActivityResult result) {
        settings = getSettings(call);

    }



}
