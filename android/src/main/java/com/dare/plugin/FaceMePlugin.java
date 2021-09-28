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
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        String licenseKey = call.getString("value");
        JSObject ret = new JSObject();
        ret.put("value", implementation.initialize(this.getContext(), licenseKey));
        call.resolve(ret);
    }

    @PluginMethod
    public void enrollingFace(PluginCall call) {
        String collectionName = call.getString("collectionName");

        String imageBase64 = call.getString("imageBase64");
        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
        JSObject ret = new JSObject();
        ret.put("value", implementation.enrollingFace(decodedString));
        call.resolve(ret);
    }
    
}
