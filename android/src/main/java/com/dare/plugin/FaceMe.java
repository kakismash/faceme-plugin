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

    private FaceMeRecognizer fmRecognizer;

    public String echo(String value) {
        return "Return From ECHO";
    }

    public void initialize(Context context, String licenseKey) {
        try {
            FaceMeSdk.initialize(context.getApplicationContext(), licenseKey);
            return FaceMeSdk.version();
        } catch (Exception e) {
            return "Error: " + e;
        }
    }

    private enrollingFace(FaceHolder faceHolder) {
        initRecognizer(faceHolder);

        // Initializing configuration settings of DataManager
        FaceFeatureScheme featureScheme     = faceMeRecognizer.getFeatureScheme();
        FaceMeDataManager faceMeDataManager = new FaceMeDataManager();
        int result                          = faceMeDataManager.initializeEx(faceMeRecognizer.getFeatureScheme());

        // Enrolling a face into database
        long startIndex   = 0;
        int count         = 1;
        long collectionId = 0;
        String name = "<!-- collection name -->";
        QueryResult queryCollectionResult = dataManager.queryFaceCollectionByName(name, startIndex, count);
        if (queryCollectionResult == null || queryCollectionResult.resultIds.isEmpty()) {
            collectionId = dataManager.createFaceCollection(name);
        } else {
            collectionId = queryCollectionResult.resultIds.get(0);
        } 
        FaceFeature faceFeature = faceMeRecognizer.getFaceFeature(0, 0);
        long faceId             = dataManager.addFace(collectionId, faceFeature);
    }

    private void initRecognizer(FaceHolder faceHolder) {
        try {
            // Initializing configuration settings of Recognizer
            FaceMeRecognizer faceMeRecognizer  = new FaceMeRecognizer();
            RecognizerConfig config            = new RecognizerConfig();
            config.preference                  = EnginePreference.PREFER_NONE;
            config.detectionModelSpeedLevel    = DetectionModelSpeedLevel.DEFAULT;
            config.maxDetectionThreads         = 2;
            config.extractionModelSpeedLevel   = ExtractionModelSpeedLevel.VERY_HIGH;
            config.maxExtractionThreads        = 2;
            config.mode                        = RecognizerMode.IMAGE;
            recognizerConfig.maxFrameHeight    = 1280;
            recognizerConfig.maxFrameWidth     = 720;
            recognizerConfig.minFaceWidthRatio = 0.15f;

            int result                         = faceMeRecognizer.initializeEx(config);
            if (result < 0) {
                throw new IllegalStateException("Initialize recognizer failed: " + result);
            }
            // Always profiling in demo app.
            boolean success = faceMeRecognizer.setProperty("Profiling", true);
            if (!success) {
                throw new IllegalStateException("Profiling recognizer failed");
            }

            // Setting extraction options and configurations
            faceMeRecognizer.setExtractionOption(ExtractionOption.DETECTION_SPEED_LEVEL, 
                                                    DetectionSpeedLevel.PRECISE);
            faceMeRecognizer.setExtractionOption(ExtractionOption.DETECTION_OUTPUT_ORDER,
                                                    DetectionOutputOrder.CONFIDENCE);
            faceMeRecognizer.setExtractionOption(ExtractionOption.DETECTION_MODE, 
                                                    DetectionMode.NORMAL);

            ExtractConfig extractConfig      = new ExtractConfig();
            extractConfig.extractBoundingBox = true;
            extractConfig.extractFeature     = false;
            extractConfig.extractAge         = false;
            extractConfig.extractGender      = false;
            extractConfig.extractEmotion     = false;
            extractConfig.extractPose        = false;
            extractConfig.extractOcclusion   = true;

        } catch (Exception e) {
            if (faceMeRecognizer != null) recognizer.release();
            throw e;
        }
    }

    /*private void detectBitmap(long presentationMs, Bitmap bitmap) {
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
*/
    
}
