package com.dare.plugin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraAccessException;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.core.content.FileProvider;

import com.dare.plugin.liveness.CameraDetector;
import com.dare.plugin.liveness.CameraSettings;
import com.dare.plugin.liveness.*;
import com.dare.plugin.liveness.CameraUtils;
import com.dare.plugin.liveness.ExifWrapper;
import com.dare.plugin.liveness.ImageUtils;
import com.dare.plugin.liveness.LivenessDetector;
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

import java.io.File;

@CapacitorPlugin(
        name = "Camera"
)
public class FaceMePlugin extends Plugin {

    private FaceMe implementation = new FaceMe();
    private CameraDetector cameraDetector = new CameraDetector();
/*
    // Permission alias constants
    static final String CAMERA = "camera";
    static final String PHOTOS = "photos";

    // Message constants
    private static final String INVALID_RESULT_TYPE_ERROR = "Invalid resultType option";
    private static final String PERMISSION_DENIED_ERROR_CAMERA = "User denied access to camera";
    private static final String PERMISSION_DENIED_ERROR_PHOTOS = "User denied access to photos";
    private static final String NO_CAMERA_ERROR = "Device doesn't have a camera available";
    private static final String NO_CAMERA_ACTIVITY_ERROR = "Unable to resolve camera activity";
    private static final String IMAGE_FILE_SAVE_ERROR = "Unable to create photo on disk";
    private static final String IMAGE_PROCESS_NO_FILE_ERROR = "Unable to process image, file not found on disk";
    private static final String UNABLE_TO_PROCESS_IMAGE = "Unable to process image";
    private static final String IMAGE_EDIT_ERROR = "Unable to edit image";
    private static final String IMAGE_GALLERY_SAVE_ERROR = "Unable to save the image in the gallery";

    private String imageFileSavePath;
    private String imageEditedFileSavePath;
    private Uri imageFileUri;
    private Uri imagePickedContentUri;
    private boolean isEdited = false;
    private boolean isFirstRequest = true;
    private boolean isSaved = false;

    private CameraSettings settings = new CameraSettings();
*/
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
        /*if (this.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            call.reject(NO_CAMERA_ERROR);
            return;
        }*/
        cameraDetector.openCamera(call, this.getContext());

        //livenessDetector.dispatchTakePictureIntent();
        //call.resolve();

        /*isEdited = false;
        settings = getSettings(call);
        showCamera(call);*/
    }

    /*private CameraSettings getSettings(PluginCall call) {
        CameraSettings settings = new CameraSettings();
        settings.setResultType(getResultType(call.getString("resultType")));
        settings.setSaveToGallery(call.getBoolean("saveToGallery", CameraSettings.DEFAULT_SAVE_IMAGE_TO_GALLERY));
        settings.setAllowEditing(call.getBoolean("allowEditing", false));
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

    private void showCamera(final PluginCall call) {
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            call.reject(NO_CAMERA_ERROR);
            return;
        }
        openCamera(call);
    }

    private void openCamera(final PluginCall call) {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                // If we will be saving the photo, send the target file along
                try {
                    String appId = getAppId();
                    File photoFile = CameraUtils.createImageFile(getActivity());
                    imageFileSavePath = photoFile.getAbsolutePath();
                    // TODO: Verify provider config exists
                    imageFileUri = FileProvider.getUriForFile(getActivity(), appId + ".fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
                } catch (Exception ex) {
                    call.reject(IMAGE_FILE_SAVE_ERROR, ex);
                    return;
                }

                startActivityForResult(call, takePictureIntent, "processCameraImage");
            } else {
                call.reject(NO_CAMERA_ACTIVITY_ERROR);
            }

    }

    private CameraResultType getResultType(String resultType) {
        if (resultType == null) {
            return null;
        }
        try {
            return CameraResultType.valueOf(resultType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            Logger.debug(getLogTag(), "Invalid result type \"" + resultType + "\", defaulting to base64");
            return CameraResultType.BASE64;
        }
    }*/




}
