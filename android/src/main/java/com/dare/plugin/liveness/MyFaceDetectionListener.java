package com.dare.plugin.liveness;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class MyFaceDetectionListener implements Camera.FaceDetectionListener {

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length > 0) {
            CameraActivity.CameraPreviewListener eventListener;
            final int quality = 10;
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    try {
                        Preview mPreview = CameraActivity.mPreview;
                        Camera.Parameters parameters = camera.getParameters();
                        Camera.Size size = parameters.getPreviewSize();
                        int orientation = mPreview.getDisplayOrientation();
                        if (mPreview.getCameraFacing() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            bytes = CameraActivity.rotateNV21(bytes, size.width, size.height, (360 - orientation) % 360);
                        } else {
                            bytes = CameraActivity.rotateNV21(bytes, size.width, size.height, orientation);
                        }
                        // switch width/height when rotating 90/270 deg
                        Rect rect = orientation == 90 || orientation == 270 ?
                                new Rect(0, 0, size.height, size.width) :
                                new Rect(0, 0, size.width, size.height);
                        YuvImage yuvImage = new YuvImage(bytes, parameters.getPreviewFormat(), rect.width(), rect.height(), null);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        yuvImage.compressToJpeg(rect, quality, byteArrayOutputStream);
                        byte[] data = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.close();
//                        eventListener.onSnapshotTaken(Base64.encodeToString(data, Base64.NO_WRAP));
                    } catch (IOException e) {
//                        eventListener.onSnapshotTakenError("IO Error");
                    } finally {

                        camera.setPreviewCallback(null);
                    }
                }
            });
            Log.d("FaceDetection", "face detected: " + faces.length + " Face 1 Location X: "
                    + faces[0].rect.centerX() + "Y: " + faces[0].rect.centerY());

        }
    }
}
