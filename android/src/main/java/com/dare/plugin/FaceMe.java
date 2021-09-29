package com.dare.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.cyberlink.faceme.FMLicenseManager;
import com.cyberlink.faceme.LicenseManager;
import com.cyberlink.faceme.FaceMeSdk;
import com.cyberlink.faceme.FaceFeature;
import com.cyberlink.faceme.FaceMeDataManager;
import com.cyberlink.faceme.FaceMeRecognizer;
import com.cyberlink.faceme.RecognizerConfig;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.ExtractConfig;
import com.cyberlink.faceme.ExtractionOption;
import com.cyberlink.faceme.FeatureData;
import com.cyberlink.faceme.FeatureType;
import com.cyberlink.faceme.DetectionMode;
import com.cyberlink.faceme.DetectionOutputOrder;
import com.cyberlink.faceme.DetectionModelSpeedLevel;
import com.cyberlink.faceme.DetectionSpeedLevel;
import com.cyberlink.faceme.PrecisionLevel;
import com.cyberlink.faceme.EnginePreference;
import com.cyberlink.faceme.SimilarFaceResult;
import com.cyberlink.faceme.ExtractionModelSpeedLevel;
import com.cyberlink.faceme.QueryResult;

public class FaceMe {

    private FaceMeRecognizer  recognizer  = null;
    private FaceMeDataManager dataManager = null;
    private static final Map<Integer, String> errors = errorMap();
    private long collectionCount                = 0;

    private static Map<Integer, String> errorMap() {
        Map<Integer, String> map = new HashMap<>();

        map.put(-1, "Failed");
        map.put(-2, "Out of memory");
        map.put(-3, "Not implemented");
        map.put(-4, "Invalid argument");
        map.put(-5, "Index out of range");
        map.put(-7, "Inconsistent data structure version");
        map.put(-8, "Not initialized");
        map.put(-9, "Configuration error");
        map.put(-10, "Not found");
        map.put(-11, "Not supported");
        map.put(-30, "Feature size mismatch");
        map.put(-31, "Database inaccesible");
        map.put(-32, "Feature format incomplete");
        map.put(-33, "Count of collections in database is exceeded");
        map.put(-34, "Database password does not match certain rules");
        map.put(-35, "Database password is incorrect");
        map.put(-36, "Database cannot change to enable/disable encryption setting when any record exists");
        map.put(-40, "Unknown license server");
        map.put(-41, "Network issue while communicate with license server");
        map.put(-42, "License failed");
        map.put(-43, "License is expired");
        map.put(-44, "License activation was exceeded");
        map.put(-45, "License feature is not supported");
        map.put(-46, "License hardware is constrained");
        map.put(-47, "License server error");
        map.put(-48, "License is restricted with specific package name for Android, or bundleId for iOS");
        map.put(-49, "License key is incorrect");

        return Collections.unmodifiableMap(map);
    }

    public String echo(String value) {
        return "Echoing " + value;
    }

    public String initialize(Context context,
                             String license) {
        try {
            initSdk(context, license);
            verifyLicense();
            recognizer  = initRecognizer();
            dataManager = initDataManager(recognizer);

            return FaceMeSdk.version();
        } catch (Exception e) {
            return "Error: " + e;
        }
    }

    public long enroll(String name,
                       byte[] bytes) {
        long        collectionId = -1;
        Bitmap      bitmap       = BitmapFactory.decodeByteArray(bytes,
                                                                 0,
                                                                bytes.length);
        FaceFeature face         = extractFaceFeature(bitmap);
        QueryResult result       = dataManager.queryFaceCollectionByName(name, 0, 1);

        if (result == null ||
            result.resultIds.isEmpty()){
            collectionId = dataManager.createFaceCollection(name);
        } else {
            collectionId = result.resultIds.get(0);
        }

        long faceId = dataManager.addFace(collectionId,
                                                face);

        return collectionId;
    }

    public long recognize(byte[] bytes) {
        long                    collectionId = -1;
        Bitmap                  bitmap       = BitmapFactory.decodeByteArray(bytes,
                                                                             0,
                                                                             bytes.length);
        FaceFeature             face         = extractFaceFeature(bitmap);
        float                   confidence   = dataManager.getPrecisionThreshold(PrecisionLevel.LEVEL_1E6);
        List<SimilarFaceResult> result       = dataManager.searchSimilarFace(confidence,
                                                                             -1,
                                                                             face,
                                                                             1);

        if (result != null &&
            !result.isEmpty()) {
            SimilarFaceResult faceResult = result.get(0);

            collectionId = faceResult.collectionId;
        }

        return collectionId;
    }

    private FaceFeature extractFaceFeature(Bitmap bitmap) {

        FaceFeature   feature = null;
        ExtractConfig config  = new ExtractConfig();

        config.extractBoundingBox = true;
        config.extractFeature     = false;
        config.extractAge         = false;
        config.extractGender      = false;
        config.extractEmotion     = false;
        config.extractPose        = false;
        config.extractOcclusion   = true;

        int facesCount = recognizer.extractFace(config,
                                                Collections.singletonList(bitmap));

        if (facesCount > 0) {

            if(facesCount > 1){
                throw new IllegalStateException("Too many faces in image (" + facesCount + ")");
            }

            feature = recognizer.getFaceFeature(0, 0);
        }

        return feature;
    }

    private void initSdk(Context context,
                         String license){
        FaceMeSdk.initialize(context.getApplicationContext(),
                             license);
    }

    private FaceMeDataManager initDataManager(FaceMeRecognizer recognizer) {

        FaceMeDataManager manager = new FaceMeDataManager();
        int               result  = manager.initializeEx(recognizer.getFeatureScheme());
        if (result < 0) {
            throw new IllegalStateException("Failed initializing FaceMe Data Manager: " + errorLabel(result));
        }

        return manager;
    }

    private FaceMeRecognizer initRecognizer() {

        FaceMeRecognizer recognizer = new FaceMeRecognizer();
        RecognizerConfig config     = new RecognizerConfig();

        config.preference                  = EnginePreference.PREFER_NONE;
        config.detectionModelSpeedLevel    = DetectionModelSpeedLevel.DEFAULT;
        config.maxDetectionThreads         = 2;
        config.extractionModelSpeedLevel   = ExtractionModelSpeedLevel.VERY_HIGH;
        config.maxExtractionThreads        = 2;
        config.mode                        = RecognizerMode.IMAGE;

        int result = recognizer.initializeEx(config);

        if (result < 0) {
            throw new IllegalStateException("Failed initializing FaceMe recognizer: " + errorLabel(result));
        }

        recognizer.setExtractionOption(ExtractionOption.DETECTION_SPEED_LEVEL,
                                       DetectionSpeedLevel.PRECISE);
        recognizer.setExtractionOption(ExtractionOption.DETECTION_OUTPUT_ORDER,
                                       DetectionOutputOrder.CONFIDENCE);
        recognizer.setExtractionOption(ExtractionOption.DETECTION_MODE,
                                       DetectionMode.NORMAL);

        return recognizer;
    }

    private int verifyLicense() {

        LicenseManager licenseManager = new LicenseManager();

        int result = licenseManager.initializeEx();

        if (result < 0) {
            throw new IllegalStateException("Failed initializing FaceMe license manager: " + errorLabel(result));
        }

        result = licenseManager.registerLicense();

        if (result < 0) {
            throw new IllegalStateException("Failed registering FaceMe license: " + errorLabel(result));
        }

        licenseManager.release();

        return result;
    }

    /*private FaceFeature buildFaceFeature(byte[] bytes) {

        FaceFeature faceFeature = new FaceFeature();
        FeatureData fData       = new FeatureData();
        fData.data              = bytesToFloats(bytes);
        faceFeature.featureData = fData;
        faceFeature.featureType = FeatureType.STANDARD_PRECISION;
        return faceFeature;

    }*/

    // Just in case the other doesn't work
    /*private float[] bytesToFloats(byte[] bytes) {

        if (bytes.length % Float.BYTES != 0){
            throw new RuntimeException("Illegal length");
        }

        float floats[] = new float[bytes.length / Float.BYTES];

        ByteBuffer.wrap(bytes).asFloatBuffer().get(floats);

        return floats;
    }*/


    private String errorLabel(int error) {

        String label = errors.get(error);

        if(label != null){
            label = "Error";
        }

        return label + " (" + error + ")";
    }
    
    public boolean changeCollectionName(Long collectionId, String name) {

        if (collectionId == null || collectionId < 0 || name == null || name == "") {
            throw new IllegalStateException("CollectionId or name can't be null");
        }

        return faceMeDataManager.setFaceCollectionName(collectionId, name);
    }

    public String getCollectionName(Long collectionId) {

        if (collectionId == null || collectionId < 0) {
            throw new IllegalStateException("CollectionId can't be null");
        } 

        String name       = "";
        name              = faceMeDataManager.getFaceCollectionName(collectionId);

        if (name == "") {
            return "Collection not found";
        } else {
            return name;
        }
    }

    public boolean deleteFace(Long faceId) {

        if (faceId == null || faceId < 0) {
            throw new IllegalStateException("FaceId can't be null");
        }
        
        return faceMeDataManager.deleteFace(faceId);
    }

    
}
