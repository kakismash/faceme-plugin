package com.dare.plugin.liveness;

import android.content.Context;

public class LivenessDetector {
    public String initCamera() {

        CameraViewImpl.Callback callback = ;

        Camera2 camera = new Camera2(null, null, Context.CAMERA_SERVICE);
        camera.start();
        return "initCamera";
    }
}
