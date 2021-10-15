package com.dare.plugin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.nio.charset.StandardCharsets;
import java.lang.Long;

import com.dare.plugin.liveness.LivenessDetector;

@CapacitorPlugin(name = "FaceMe")
public class FaceMePlugin extends Plugin {

    private FaceMe implementation = new FaceMe();

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
        System.out.println("FaceMe init camera");

        LivenessDetector liveness = new LivenessDetector();
        JSObject         ret      = new JSObject();
        int              cameraId = Integer.parseInt(call.getString("collectionId"));
        if (liveness.initCamera(cameraId) == null) {
            ret.put("value",
                    "Error opening camera");
        } else {
            ret.put("value",
                    "Camera inizilized");
        }
        call.resolve(ret);
    }
}
