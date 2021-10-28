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
    private CameraDetector cameraDetector = new CameraDetector();

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
        //cameraDetector.openCamera(call, this.getContext());

        //livenessDetector.dispatchTakePictureIntent();
        //call.resolve();

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            call.reject(NO_CAMERA_ERROR);
            return;
        }
        settings = getSettings(call);
        showCamera(call);
    }

    private CameraSettings getSettings(PluginCall call) {
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
        if (checkCameraPermissions(call)) {
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
    }

    private boolean checkCameraPermissions(PluginCall call) {
        // if the manifest does not contain the camera permissions key, we don't need to ask the user
        boolean needCameraPerms = isPermissionDeclared(CAMERA);
        boolean hasCameraPerms = !needCameraPerms || getPermissionState(CAMERA) == PermissionState.GRANTED;
        boolean hasPhotoPerms = getPermissionState(PHOTOS) == PermissionState.GRANTED;

        // If we want to save to the gallery, we need two permissions
        if (settings.isSaveToGallery() && !(hasCameraPerms && hasPhotoPerms) && isFirstRequest) {
            isFirstRequest = false;
            String[] aliases;
            if (needCameraPerms) {
                aliases = new String[] { CAMERA, PHOTOS };
            } else {
                aliases = new String[] { PHOTOS };
            }
            requestPermissionForAliases(aliases, call, "cameraPermissionsCallback");
            return false;
        }
        // If we don't need to save to the gallery, we can just ask for camera permissions
        else if (!hasCameraPerms) {
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

        //doShow(call);
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
    }

    @ActivityCallback
    public void processCameraImage(PluginCall call, ActivityResult result) {
        settings = getSettings(call);
        if (imageFileSavePath == null) {
            call.reject(IMAGE_PROCESS_NO_FILE_ERROR);
            return;
        }
        // Load the image as a Bitmap
        File f = new File(imageFileSavePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Uri contentUri = Uri.fromFile(f);
        Bitmap bitmap = BitmapFactory.decodeFile(imageFileSavePath, bmOptions);

        if (bitmap == null) {
            call.reject("User cancelled photos app");
            return;
        }

        returnResult(call, bitmap, contentUri);
    }

    /**
     * After processing the image, return the final result back to the caller.
     * @param call
     * @param bitmap
     * @param u
     */
    private void returnResult(PluginCall call, Bitmap bitmap, Uri u) {
        ExifWrapper exif = ImageUtils.getExifData(getContext(), bitmap, u);
        try {
            bitmap = prepareBitmap(bitmap, u, exif);
        } catch (IOException e) {
            call.reject(UNABLE_TO_PROCESS_IMAGE);
            return;
        }
        // Compress the final image and prepare for output to client
        ByteArrayOutputStream bitmapOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, settings.getQuality(), bitmapOutputStream);

        if (settings.isAllowEditing() && !isEdited) {
            editImage(call, u, bitmapOutputStream);
            return;
        }

        boolean saveToGallery = call.getBoolean("saveToGallery", CameraSettings.DEFAULT_SAVE_IMAGE_TO_GALLERY);
        if (saveToGallery && (imageEditedFileSavePath != null || imageFileSavePath != null)) {
            isSaved = true;
            try {
                String fileToSavePath = imageEditedFileSavePath != null ? imageEditedFileSavePath : imageFileSavePath;
                File fileToSave = new File(fileToSavePath);
                String inserted = MediaStore.Images.Media.insertImage(
                        getContext().getContentResolver(),
                        fileToSavePath,
                        fileToSave.getName(),
                        ""
                );
                if (inserted == null) {
                    isSaved = false;
                }
            } catch (FileNotFoundException e) {
                isSaved = false;
                Logger.error(getLogTag(), IMAGE_GALLERY_SAVE_ERROR, e);
            }
        }

        if (settings.getResultType() == CameraResultType.BASE64) {
            returnBase64(call, exif, bitmapOutputStream);
        } else if (settings.getResultType() == CameraResultType.URI) {
            returnFileURI(call, exif, bitmap, u, bitmapOutputStream);
        } else if (settings.getResultType() == CameraResultType.DATAURL) {
            returnDataUrl(call, exif, bitmapOutputStream);
        } else {
            call.reject(INVALID_RESULT_TYPE_ERROR);
        }
        // Result returned, clear stored paths and images
        if (settings.getResultType() != CameraResultType.URI) {
            deleteImageFile();
        }
        imageFileSavePath = null;
        imageFileUri = null;
        imagePickedContentUri = null;
        imageEditedFileSavePath = null;
    }

    private void returnDataUrl(PluginCall call, ExifWrapper exif, ByteArrayOutputStream bitmapOutputStream) {
        byte[] byteArray = bitmapOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);

        JSObject data = new JSObject();
        data.put("format", "jpeg");
        data.put("dataUrl", "data:image/jpeg;base64," + encoded);
        data.put("exif", exif.toJson());
        call.resolve(data);
    }

    private void returnFileURI(PluginCall call, ExifWrapper exif, Bitmap bitmap, Uri u, ByteArrayOutputStream bitmapOutputStream) {
        Uri newUri = getTempImage(u, bitmapOutputStream);
        exif.copyExif(newUri.getPath());
        if (newUri != null) {
            JSObject ret = new JSObject();
            ret.put("format", "jpeg");
            ret.put("exif", exif.toJson());
            ret.put("path", newUri.toString());
            ret.put("webPath", FileUtils.getPortablePath(getContext(), bridge.getLocalUrl(), newUri));
            ret.put("saved", isSaved);
            call.resolve(ret);
        } else {
            call.reject(UNABLE_TO_PROCESS_IMAGE);
        }
    }

    private void editImage(PluginCall call, Uri uri, ByteArrayOutputStream bitmapOutputStream) {
        try {
            Uri tempImage = getTempImage(uri, bitmapOutputStream);
            Intent editIntent = createEditIntent(tempImage);
            if (editIntent != null) {
                startActivityForResult(call, editIntent, "processEditedImage");
            } else {
                call.reject(IMAGE_EDIT_ERROR);
            }
        } catch (Exception ex) {
            call.reject(IMAGE_EDIT_ERROR, ex);
        }
    }

    private Intent createEditIntent(Uri origPhotoUri) {
        try {
            File editFile = new File(origPhotoUri.getPath());
            Uri editUri = FileProvider.getUriForFile(getActivity(), getContext().getPackageName() + ".fileprovider", editFile);
            Intent editIntent = new Intent(Intent.ACTION_EDIT);
            editIntent.setDataAndType(editUri, "image/*");
            imageEditedFileSavePath = editFile.getAbsolutePath();
            int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            editIntent.addFlags(flags);
            editIntent.putExtra(MediaStore.EXTRA_OUTPUT, editUri);
            List<ResolveInfo> resInfoList = getContext()
                    .getPackageManager()
                    .queryIntentActivities(editIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, editUri, flags);
            }
            return editIntent;
        } catch (Exception ex) {
            return null;
        }
    }

    private Uri getTempImage(Uri u, ByteArrayOutputStream bitmapOutputStream) {
        ByteArrayInputStream bis = null;
        Uri newUri = null;
        try {
            bis = new ByteArrayInputStream(bitmapOutputStream.toByteArray());
            newUri = saveImage(u, bis);
        } catch (IOException ex) {} finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    Logger.error(getLogTag(), UNABLE_TO_PROCESS_IMAGE, e);
                }
            }
        }
        return newUri;
    }

    private Uri saveImage(Uri uri, InputStream is) throws IOException {
        File outFile = null;
        if (uri.getScheme().equals("content")) {
            String filename = Uri.parse(Uri.decode(uri.toString())).getLastPathSegment();
            if (!filename.contains(".jpg") && !filename.contains(".jpeg")) {
                filename += "." + (new java.util.Date()).getTime() + ".jpeg";
            }
            File cacheDir = getContext().getCacheDir();
            outFile = new File(cacheDir, filename);
        } else {
            outFile = new File(uri.getPath());
        }
        FileOutputStream fos = new FileOutputStream(outFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        return Uri.fromFile(outFile);
    }

    private void returnBase64(PluginCall call, ExifWrapper exif, ByteArrayOutputStream bitmapOutputStream) {
        byte[] byteArray = bitmapOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);

        JSObject data = new JSObject();
        data.put("format", "jpeg");
        data.put("base64String", encoded);
        data.put("exif", exif.toJson());
        call.resolve(data);
    }

    private void deleteImageFile() {
        if (imageFileSavePath != null && !settings.isSaveToGallery()) {
            File photoFile = new File(imageFileSavePath);
            if (photoFile.exists()) {
                photoFile.delete();
            }
        }
    }

    /**
     * Apply our standard processing of the bitmap, returning a new one and
     * recycling the old one in the process
     * @param bitmap
     * @param imageUri
     * @param exif
     * @return
     */
    private Bitmap prepareBitmap(Bitmap bitmap, Uri imageUri, ExifWrapper exif) throws IOException {
        if (settings.isShouldCorrectOrientation()) {
            final Bitmap newBitmap = ImageUtils.correctOrientation(getContext(), bitmap, imageUri, exif);
            bitmap = replaceBitmap(bitmap, newBitmap);
        }

        if (settings.isShouldResize()) {
            final Bitmap newBitmap = ImageUtils.resize(bitmap, settings.getWidth(), settings.getHeight());
            bitmap = replaceBitmap(bitmap, newBitmap);
        }

        return bitmap;
    }

    private Bitmap replaceBitmap(Bitmap bitmap, final Bitmap newBitmap) {
        if (bitmap != newBitmap) {
            bitmap.recycle();
        }
        bitmap = newBitmap;
        return bitmap;
    }



}
