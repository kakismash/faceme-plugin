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

@CapacitorPlugin(name = "FaceMe")
public class FaceMePlugin extends Plugin {

    private FaceMe implementation = new FaceMe();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");
        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        String licenseKey = call.getString("value");
        JSObject ret      = new JSObject();
        ret.put("value", implementation.initialize(this.getContext(), licenseKey));
        call.resolve(ret);
    }

    @PluginMethod
    public void enrollingFace(PluginCall call) {
        String collectionName = call.getString("collectionName");
        String imageBase64    = call.getString("imageBase64");
        byte[] decodedString  = Base64.decode(imageBase64, Base64.DEFAULT);
        JSObject ret          = new JSObject();
        ret.put("faceId", implementation.enrollingFace(decodedString));
        call.resolve(ret);
    }

    @PluginMethod
    public void searchFace(PluginCall call) {
        String imageBase64   = call.getString("imageBase64");
        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
        JSObject ret         = new JSObject();
        ret.put("faceId", implementation.recognizingPeople(decodedString));
        call.resolve(ret);
    }

    @PluginMethod
    public void changeCollectionName(PluginCall call) {
        long collectionId = Long.parseLong(call.getString("collectionId"));
        String name        = call.getString("name");
        JSObject ret      = new JSObject();
        ret.put("value", implementation.changeCollectionName(collectionId, name));
        call.resolve(ret);
    }

    @PluginMethod
    public void getCollectionName(PluginCall call) {
        long collectionId = Long.parseLong(call.getString("collectionId"));
        JSObject ret      = new JSObject();
        ret.put("name", implementation.getCollectionName(collectionId));
        call.resolve(ret);
    }
    
    @PluginMethod
    public void deleteFace(PluginCall call) {
        long faceId       = Long.parseLong(call.getString("faceId"));
        JSObject ret      = new JSObject();
        ret.put("value", implementation.deleteFace(faceId));
        call.resolve(ret);
    }
}
