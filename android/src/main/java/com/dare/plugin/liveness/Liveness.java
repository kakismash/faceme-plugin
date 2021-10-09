package com.dare.plugin.liveness;

import android.view.TextureView;
import android.util.Size;

public class Liveness {

    private TextureView        mainCamPreviewView;
    private AutoFitSurfaceView subCamPreviewView;
    private StatListener       statListener;
    private CameraController   cameraController;

    public String initCamera() {
        mainCamPreviewView = findViewById(R.id.mainCamPreviewView);
        subCamPreviewView  = findViewById(R.id.subCamPreviewView);

        statListener = new StatListener();
        cameraController = CameraFactory.create(this, mainCamPreviewView, subCamPreviewView, this, statListener, true);
        cameraController.setUiSettings(uiSettings);
        Size previewSize = uiSettings.getPreviewSize();
        cameraController.setResolution(previewSize.getWidth(), previewSize.getHeight());

        return "init";
    }

}