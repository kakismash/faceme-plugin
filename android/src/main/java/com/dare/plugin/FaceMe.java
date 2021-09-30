package com.dare.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.cyberlink.faceme.LicenseManager;
import com.cyberlink.faceme.FaceMeSdk;
import com.cyberlink.faceme.FaceInfo;
import com.cyberlink.faceme.FaceFeature;
import com.cyberlink.faceme.FaceAttribute;
import com.cyberlink.faceme.FaceLandmark;
import com.cyberlink.faceme.FaceMeDataManager;
import com.cyberlink.faceme.FaceMeRecognizer;
import com.cyberlink.faceme.FeatureData;
import com.cyberlink.faceme.FeatureType;
import com.cyberlink.faceme.RecognizerConfig;
import com.cyberlink.faceme.RecognizerMode;
import com.cyberlink.faceme.ExtractConfig;
import com.cyberlink.faceme.ExtractionOption;
import com.cyberlink.faceme.DetectionMode;
import com.cyberlink.faceme.DetectionOutputOrder;
import com.cyberlink.faceme.DetectionModelSpeedLevel;
import com.cyberlink.faceme.DetectionSpeedLevel;
import com.cyberlink.faceme.PrecisionLevel;
import com.cyberlink.faceme.EnginePreference;
import com.cyberlink.faceme.SimilarFaceResult;
import com.cyberlink.faceme.ExtractionModelSpeedLevel;
import com.cyberlink.faceme.QueryResult;

import com.getcapacitor.JSObject;

import org.json.JSONObject;

public class FaceMe {

    private FaceMeRecognizer  recognizer  = null;
    private FaceMeDataManager dataManager = null;

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
                       String encoded,
                       String data) {

        verifyLicense();

        long        collectionId = -1;
        FaceFeature face         = extractFaceFeature(encoded);

        if(face != null) {
            QueryResult result = dataManager.queryFaceCollectionByName(name, 0, 1);

            if (result == null ||
                    result.resultIds.isEmpty()) {
                collectionId = dataManager.createFaceCollection(name);
            } else {
                collectionId = result.resultIds.get(0);
            }

            long faceId = dataManager.addFace(collectionId,
                                              face);

            if (data != null &&
                data.length() > 0) {
                dataManager.setFaceCollectionCustomData(collectionId,
                                                        data.getBytes());
            }
        }

        return collectionId;
    }

    public JSObject recognize(String encoded) {

        verifyLicense();

        JSObject    object = new JSObject();
        FaceFeature face   = extractFaceFeature(encoded);

        if(face != null){
            long                    collectionId = -1;
            float                   confidence   = (float).5; //dataManager.getPrecisionThreshold(PrecisionLevel.LEVEL_1E2);
            List<SimilarFaceResult> result       = dataManager.searchSimilarFace(confidence,
                                                                                -1,
                                                                                face,
                                                                                10);

            if (result != null &&
                !result.isEmpty()) {
                System.out.println("Found " + result.size() + " matches");
                SimilarFaceResult faceResult = result.get(0);
                byte[]            data       = dataManager.getFaceCollectionCustomData(faceResult.collectionId);

                object.put("collectionId", faceResult.collectionId);
                object.put("confidence", faceResult.confidence);
                object.put("name", dataManager.getFaceCollectionName(faceResult.collectionId));

                if(data != null){
                    object.put("data", new String(data));
                }
            }
        }

        return object;
    }

    public boolean setCollectionName(Long collectionId, String name) {

        verifyLicense();

        if (collectionId == null ||
            collectionId < 0 ||
            name == null ||
            name.isEmpty()) {
            throw new IllegalStateException("Failed setting collection name, id or name not specified");
        }

        return dataManager.setFaceCollectionName(collectionId, name);
    }

    public String getCollectionName(Long collectionId) {

        verifyLicense();

        if (collectionId == null ||
            collectionId < 0) {
            throw new IllegalStateException("Failed getting collection name, id not specified");
        } 

        return dataManager.getFaceCollectionName(collectionId);
    }

    public boolean setCollectionData(Long collectionId, String data) {

        verifyLicense();

        if (collectionId == null ||
            collectionId < 0 ||
            data == null ||
            data.isEmpty()) {
            throw new IllegalStateException("Failed setting collection data, id or data not specified");
        }

        return dataManager.setFaceCollectionCustomData(collectionId, data.getBytes());
    }

    public String getCollectionData(Long collectionId) {

        verifyLicense();

        if (collectionId == null ||
            collectionId < 0) {
            throw new IllegalStateException("Failed getting collection data, id not specified");
        }

        byte[] data = dataManager.getFaceCollectionCustomData(collectionId);

        return data==null?"":new String(data);
    }

    public boolean deleteCollection(Long collectionId) {

        verifyLicense();

        if (collectionId == null ||
            collectionId < 0) {
            throw new IllegalStateException("Failed deleting collection, id not specified");
        }

        return dataManager.deleteFaceCollection(collectionId);
    }

    public String echo(String value) {
        return "Echoing " + value;
    }

    private Bitmap bitmap(String encoded){
        BitmapFactory.Options options = new BitmapFactory.Options();

//        options.inJustDecodeBounds = true;

        byte[] bytes = Base64.decode(encoded,
                                     Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,
                                                0,
                                                bytes.length,
                                                options);

        return bitmap;
    }

    private FaceFeature extractFaceFeature(String encoded) {

        FaceFeature   feature = null;
        Bitmap        bitmap  = bitmap(encoded);
        ExtractConfig config  = new ExtractConfig();

        config.extractBoundingBox = true;
        config.extractFeature     = true;
        config.extractFullFeature = false;
        config.extractAge         = true;
        config.extractGender      = true;
        config.extractEmotion     = true;
        config.extractPose        = false;
        config.extractOcclusion   = false;

        int facesCount = recognizer.extractFace(config,
                                                Collections.singletonList(bitmap));

        if (facesCount > 0) {
            System.out.println("Found " + facesCount + " face(s)");

            if(facesCount > 1){
                throw new IllegalStateException("Too many faces in image (" + facesCount + ")");
            }
//            FaceInfo info = recognizer.getFaceInfo(0, 0);
  //          FaceLandmark landmark = recognizer.getFaceLandmark(0, 0);
    //        FaceAttribute attr = recognizer.getFaceAttribute(0, 0);
            feature = recognizer.getFaceFeature(0, 0);
        }

        return feature;
    }

    private void initSdk(Context context,
                         String license){
        FaceMeSdk.initialize(context.getApplicationContext(),
                             license);
    }

    private FaceMeDataManager initDataManager(FaceMeRecognizer recognizer){
        FaceMeDataManager manager = new FaceMeDataManager();
        int               result  = manager.initializeEx(recognizer.getFeatureScheme());

        if (result < 0) {
            throw new IllegalStateException("Failed initializing FaceMe Data Manager: " + resultLabel(result));
        }

        return manager;
    }

    private FaceMeRecognizer initRecognizer() {

        FaceMeRecognizer recognizer = new FaceMeRecognizer();
        RecognizerConfig config     = new RecognizerConfig();

        config.mode                        = RecognizerMode.IMAGE;
        config.preference                  = EnginePreference.PREFER_NONE;
        config.detectionModelSpeedLevel    = DetectionModelSpeedLevel.DEFAULT;
        config.extractionModelSpeedLevel   = ExtractionModelSpeedLevel.VERY_HIGH;
        config.maxDetectionThreads         = 2;
        config.maxExtractionThreads        = 2;
        config.maxFrameHeight              = 1280;
        config.maxFrameWidth               = 720;
        config.minFaceWidthRatio           = 0.10f;

        int result = recognizer.initializeEx(config);

        if (result < 0) {
            throw new IllegalStateException("Failed initializing FaceMe recognizer: " + resultLabel(result));
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
        // License verification to prevent local license expiration
        LicenseManager licenseManager = null;

        try {
            licenseManager = new LicenseManager();

            int result = licenseManager.initializeEx();

            if(result < 0){
                throw new IllegalStateException("Failed initializing FaceMe license manager: " + resultLabel(result));
            }

            result = licenseManager.registerLicense();

            if(result < 0){
                throw new IllegalStateException("Failed registering FaceMe license: " + resultLabel(result));
            }

            System.out.println("Verified license: " + resultLabel(result));

            return result;
        } finally {
            if (licenseManager != null) {
                licenseManager.release();
            }
        }
    }

    private FaceFeature buildFaceFeature(byte[] bytes) {

        verifyLicense();

        FaceFeature faceFeature = new FaceFeature();
        FeatureData fData       = new FeatureData();

        fData.data              = bytesToFloats(bytes);
        faceFeature.featureData = fData;
        faceFeature.featureType = FeatureType.STANDARD_PRECISION;

        return faceFeature;
    }

    // Just in case the other doesn't work
    private float[] bytesToFloats(byte[] bytes) {

        if (bytes.length % Float.BYTES != 0){
            throw new RuntimeException("Illegal length");
        }

        float floats[] = new float[bytes.length / Float.BYTES];

        ByteBuffer.wrap(bytes).asFloatBuffer().get(floats);

        return floats;
    }

    private String resultLabel(int result){
        String label;

        if(result == 0){
            label = "Success";
        } else {
            label = errors.get(result);

            if(label != null){
                label = "Error";
            }

            label = label +  " (" + result + ")";
        }

        return label;
    }

    private static final Map<Integer, String> errors = errorMap();

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
}
