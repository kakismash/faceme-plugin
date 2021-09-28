package com.dare.plugin;

import android.content.Context;

import com.cyberlink.faceme.FaceMeSdk;
import com.cyberlink.faceme.FeatureData;
import com.cyberlink.faceme.FeatureType;
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
import com.cyberlink.faceme.FaceFeature;
import com.cyberlink.faceme.FaceMeDataManager;
import com.cyberlink.faceme.SimilarFaceResult;
import com.cyberlink.faceme.PrecisionLevel;

import java.nio.ByteBuffer;
import java.util.List;

public class FaceMe {

    private FaceMeRecognizer faceMeRecognizer   = null;
    private FaceMeDataManager faceMeDataManager = null;
    private long collectionCount                = 0;

    public String initialize(Context context, String licenseKey) {
        try {
            FaceMeSdk.initialize(context.getApplicationContext(), licenseKey);
            faceMeRecognizer  = initRecognizer();
            faceMeDataManager = new FaceMeDataManager();
            int result        = faceMeDataManager.initializeEx(faceMeRecognizer.getFeatureScheme());
            if (result < 0) {
                throw new IllegalStateException("Initialize FaceMeDataManager failed: " + result);
            }
            return FaceMeSdk.version();
        } catch (Exception e) {
            return "Error: " + e;
        }
    }

    private FaceMeRecognizer initRecognizer() {
        FaceMeRecognizer faceMeRecognizer = null;
        try {
            // Initializing configuration settings of Recognizer
            faceMeRecognizer                   = new FaceMeRecognizer();
            RecognizerConfig config            = new RecognizerConfig();
            config.preference                  = EnginePreference.PREFER_NONE;
            config.detectionModelSpeedLevel    = DetectionModelSpeedLevel.DEFAULT;
            config.maxDetectionThreads         = 2;
            config.extractionModelSpeedLevel   = ExtractionModelSpeedLevel.VERY_HIGH;
            config.maxExtractionThreads        = 2;
            config.mode                        = RecognizerMode.IMAGE;

            int result                         = faceMeRecognizer.initializeEx(config);
            if (result < 0) {
                throw new IllegalStateException("Initialize recognizer failed: " + result);
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
            throw e;
        }
        return faceMeRecognizer;
    }

    public String echo(String value) {
        return "Return From ECHO";
    }

    public long enrollingFace(byte[] bytes) {
        FaceFeature faceFeature = buildFaceFeature(bytes);
        collectionCount         = collectionCount + 1;
        long faceId             = faceMeDataManager.addFace(collectionCount, faceFeature);
        return faceId;
    }

    public long recognizingPeople(byte[] bytes) {
        // Get confidence threshold from precision level
        int precisionLevel        = PrecisionLevel.LEVEL_1E6;
        float confidenceThreshold = faceMeDataManager.getPrecisionThreshold(precisionLevel);

        // Get face template from the target face
        FaceFeature faceFeature = buildFaceFeature(bytes);

        // Get search results from database
        long faceId;
        List<SimilarFaceResult> searchResult = faceMeDataManager.searchSimilarFace(confidenceThreshold, -1, faceFeature, 1);
        if (searchResult != null && !searchResult.isEmpty()) {
            SimilarFaceResult result = searchResult.get(0);
            return result.faceId;
        } else {
            return -1;
        }
    }

    private FaceFeature buildFaceFeature(byte[] bytes) {
        FaceFeature faceFeature = new FaceFeature();
        FeatureData fData       = new FeatureData();
        fData.data              = bytesToFloats(bytes);
        faceFeature.featureData = fData;
        faceFeature.featureType = FeatureType.STANDARD_PRECISION;
        return faceFeature;
    }

    // Just in case the other doesn't work
    private float[] bytesToFloats(byte[] bytes) {
        if (bytes.length % Float.BYTES != 0)
            throw new RuntimeException("Illegal length");
        float floats[] = new float[bytes.length / Float.BYTES];
        ByteBuffer.wrap(bytes).asFloatBuffer().get(floats);
        return floats;
    }
    
}
