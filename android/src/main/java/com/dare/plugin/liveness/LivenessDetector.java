package com.dare.plugin.liveness;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.util.Log;
import android.hardware.Camera;
import android.app.Activity;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;


public class LivenessDetector {

    public String initCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (intent == null) {
            return "Error opening camera";
        }
        return "Camera inizialized";
    }

    //private static final int CAMERA_PERMISSIONS_REQUEST = 100;
    //private static final String TAG = "FaceMe Plugin";

    //private final Activity context = new Activity();

    /*private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        context.startActivity(intent);
     }*/
    
    /*public String initCamera() {
        try {
            CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
            
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length == 0) {
                return "not found";
            }
        
            String cameraId = cameraIds[0];
        
            if (ActivityCompat.checkSelfPermission(this,
              Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
              ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST);
            }
        
            cameraManager.openCamera(cameraId, CameraDevice.StateCallback, null);
          } catch (CameraAccessException e) {
            Log.d(TAG, "No access to the camera.", e);
          }
    }*/

    /** Check if this device has a camera 
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }*/
    
    
    /** A safe way to get an instance of the Camera object. 
    public static Camera initCamera(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }*/

    

    /*private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
    
            Log.e(TAG, "Permission not granted WRITE_EXTERNAL_STORAGE.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
    
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        225);
            }
        } 
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permission not granted CAMERA.");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
    
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        226);
            }
        }
    
    }*/

}
