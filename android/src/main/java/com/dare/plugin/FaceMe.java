package com.dare.plugin;

import android.content.Context;

import com.cyberlink.faceme.FaceMeSdk;
import com.cyberlink.faceme.LicenseManager;
import com.cyberlink.faceme.FaceMeRecognizer;
import com.cyberlink.faceme.RecognizerConfig;
import com.cyberlink.faceme.DetectionModelSpeedLevel;
import com.cyberlink.faceme.ExtractConfig;
import com.cyberlink.faceme.ExtractionOption;
import com.cyberlink.faceme.EnginePreference;
import com.cyberlink.faceme.ExtractionModelSpeedLevel;
import com.cyberlink.faceme.DetectionOutputOrder;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.DetectionSpeedLevel;
import com.cyberlink.faceme.DetectionMode;
import com.cyberlink.faceme.FaceAttribute;
import com.cyberlink.faceme.FaceFeature;
import com.cyberlink.faceme.FaceInfo;
import com.cyberlink.faceme.FaceLandmark;
import com.cyberlink.faceme.FaceLivenessStatus;
import com.cyberlink.faceme.FaceMeDataManager;
import com.cyberlink.faceme.LicenseManager;
import com.cyberlink.faceme.QueryResult;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.SimilarFaceResult;

import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class FaceMe {

    private static final String TAG = "FaceMe.AmpleIRASAct";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final long AUTO_HIDE_INTERVAL = 2_000L;
    private static final long MINIMUM_BITMAP_CACHE_TIME = 500L;
    private static final long FAIL_RUN_TIME_INTERVAL = 3_000L;
    private static final long DARKEN_BACKLIGHT_INTERVAL = 10_000L;
    private static final double BOUNDING_BOX_OVERLAP_ALLOWED_RATIO = 0.8;

    private InfraredDumpHandler infraredDumpHandler;
    private LivenessIRDetectionHandler livenessIRDetectionHandler;
    private StatUiHandler statUiHandler;

    private final ArrayList<View> viewsAutoHide = new ArrayList<>();
    private final ArrayList<View> viewsDevTool = new ArrayList<>();
    private final AtomicBoolean dumpImageFlag = new AtomicBoolean(false);

    private int previousFacesCount = 0;

    private final AtomicBoolean isRecognizing = new AtomicBoolean(false);
    private final AtomicBoolean hasRecognitionFeature = new AtomicBoolean(false);
    private final AtomicBoolean isShowingResult = new AtomicBoolean(false);
    private final AtomicBoolean isNewFace = new AtomicBoolean(true);
    private final AtomicReference<Bitmap> lastDetectBitmapQueue = new AtomicReference<>(null);

    private Handler recognizerHandler;
    private FaceMeRecognizer fmRecognizer;
    private ExtractConfig extractConfig;
    private float confidenceThreshold;
    private FaceMeDataManager fmDataManager;
    private float defaultScreenBrightness;

    private FaceHolder visitorFace = null;

    private final Runnable darkenScreenLighterRunnable = () -> {
        setScreenLighter(0.0f);
    };

    public String echo(String value) {
        return "Return From ECHO";
    }

    public String initialize(Context context, String licenseKey) {
        try {
//            FaceMeSdk.isInitialized();
            FaceMeSdk.initialize(context.getApplicationContext(), licenseKey);
            return FaceMeSdk.version();
        } catch (Exception e) {
            return "Error: " + e;
        }
    }

    private void detectBitmap(long presentationMs, Bitmap bitmap) {
        if (fmRecognizer == null || (livenessIRDetectionHandler!= null && livenessIRDetectionHandler.isDetecting())) {
            isRecognizing.set(false);
            return;
        }
        Bitmap oldBitmap = lastDetectBitmapQueue.getAndSet(bitmap);
        if (oldBitmap != null) {
            oldBitmap.recycle();
        }

        int facesCount = fmRecognizer.extractFace(extractConfig, Collections.singletonList(bitmap));
        ArrayList<FaceHolder> faces = new ArrayList<>();
        if (facesCount > 0) {
            for (int faceIndex = 0; faceIndex < facesCount; faceIndex++) {
                FaceInfo faceInfo = fmRecognizer.getFaceInfo(0, faceIndex);
                FaceLandmark faceLandmark = fmRecognizer.getFaceLandmark(0, faceIndex);
                FaceAttribute faceAttr = fmRecognizer.getFaceAttribute(0, faceIndex);
                FaceFeature faceFeature = fmRecognizer.getFaceFeature(0, faceIndex);

                Bitmap faceBitmap = getCropFaceBitmap(bitmap, faceInfo.boundingBox);

                FaceHolder holder = new FaceHolder(faceInfo, faceLandmark, faceAttr, faceFeature, faceBitmap);

                faces.add(holder);
            }
        }
        faceCountCheck(previousFacesCount, facesCount);
        previousFacesCount = facesCount;
        PresentFacesHolder presentFaces = biggestFaceChoose(presentationMs, faces);

        if (hasRecognitionFeature.get()) {
            if (visitorFace != null) {
                if (faces.size() == 0 || !isFaceExist(visitorFace, faces)) { // If visitor leave, start the new process to do anti-spoofing detect.
                    visitorFace = null;
                    extractConfig.extractFeature = false;
                    mainHandler.post(() -> {
                        livenessResult.setVisibility(View.GONE);
                        Log.v(TAG, "Liveness Result GONE.");
                    });
                    Log.v(TAG, "Visitor disappear.");
                    isNewFace.set(true);
                    hasRecognitionFeature.set(false);
                }
            }
        }

        onExtracted(bitmap, presentFaces);

        int total = 0, detect = 0, extract = 0, recognize = 0;
        boolean gotProfiling = false;

        PerformanceStatus perfStatus = PerformanceStatus.make(fmRecognizer.getProperty("Performance"));
        if (perfStatus != null) {
            gotProfiling = true;
            if (perfStatus.total != null) total = perfStatus.total;
            if (perfStatus.detect != null) detect = perfStatus.detect;
            if (perfStatus.extract != null) extract = perfStatus.extract;
        }
        if (statListener != null) {
            statListener.onFacesExtracted();
            if (gotProfiling) statListener.onFacesRecognized(facesCount, total, detect, extract, recognize);
        }
        isRecognizing.set(false);
    }

    private void faceCountCheck(int previousFacesCount, int facesCount) {
        if (previousFacesCount == 0 && facesCount == 0) return;
        if (previousFacesCount != 0 && facesCount != 0) return;

        if (previousFacesCount == 0) { // If faces appear in camera, Brighten the screen backlight.
            mainHandler.removeCallbacks(darkenScreenLighterRunnable);
            mainHandler.post(() -> {
                setScreenLighter(defaultScreenBrightness);
            });
        } else { // If faces don't exist, darken the screen backlight.
            mainHandler.postDelayed(darkenScreenLighterRunnable, DARKEN_BACKLIGHT_INTERVAL);
        }
    }

    private void getDefaultScreenBrightness() {
        Window window = this.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        defaultScreenBrightness = params.screenBrightness;
    }

    private void setScreenLighter(float brightness) {
        Window window = this.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = brightness;
        window.setAttributes(params);
    }

    private PresentFacesHolder biggestFaceChoose(long presentationMs, ArrayList<FaceHolder> faces) {
        if (faces.size() <= 1) {
            return new PresentFacesHolder(presentationMs, faces);
        }

        // If there are more than 1 faces, choose the biggest face bigger than the others 20%.
        faces.size();
        int biggestFace = 0;
        int biggestFaceArea = 0;
        int secondBiggestFaceArea = 0;
        for (int index = 0 ; index < faces.size() ; index++) {
            FaceHolder face = faces.get(index);
            int faceArea = face.faceInfo.boundingBox.height() * face.faceInfo.boundingBox.width();
            if (faceArea > biggestFaceArea) {
                if (biggestFaceArea != 0) {
                    secondBiggestFaceArea = biggestFaceArea;
                }
                biggestFaceArea = faceArea;
                biggestFace = index;
            } else {
                if (faceArea > secondBiggestFaceArea) {
                    secondBiggestFaceArea = faceArea;
                }
            }
        }

        ArrayList<FaceHolder> targetFace = new ArrayList<>();
        if (biggestFaceArea > (int) (secondBiggestFaceArea * 1.2)) {
            targetFace.add(faces.get(biggestFace));
            return new PresentFacesHolder(presentationMs, targetFace);
        } else {
            return new PresentFacesHolder(presentationMs, faces);
        }
    }

    public void notifyDumpInfraredIfNeeded(PresentFacesHolder presentFaces, int width, int height, byte[] data) {
        InfraredDumpHandler infraredDumpHandler = this.infraredDumpHandler;
        if (infraredDumpHandler != null) {
            infraredDumpHandler.dumpInfrared(presentFaces, width, height, data);
        }
    }
}
