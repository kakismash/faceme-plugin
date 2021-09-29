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

@CapacitorPlugin(name = "FaceMe")
public class FaceMePlugin extends Plugin {

    private FaceMe implementation = new FaceMe();

    @PluginMethod
    public void echo(PluginCall call) {
        String   value = call.getString("value");
        JSObject ret   = new JSObject();

        ret.put("value",
                implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        JSObject ret     = new JSObject();
        String   license = call.getString("license");

        ret.put("version",
                implementation.initialize(this.getContext(),
                                          license));
        call.resolve(ret);
    }

    @PluginMethod
    public void enrollingFace(PluginCall call) {
        JSObject ret           = new JSObject();
        String   name          = call.getString("name");
        String   imageBase64   = call.getString("imageBase64");
        byte[]   decodedString = Base64.decode(imageBase64, Base64.DEFAULT);

        ret.put("collectionId",
                implementation.enroll(name,
                                      decodedString));
        call.resolve(ret);
    }

    @PluginMethod
    public void searchFace(PluginCall call) {
        JSObject ret         = new JSObject();
        String imageBase64   = call.getString("imageBase64");
        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);

        ret.put("collectionId",
                implementation.recognize(decodedString));
        call.resolve(ret);
    }
}
