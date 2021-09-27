package com.dare.plugin;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

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
        String licenseKey = call.getString("licenseKey");
        JSObject ret = new JSObject();
        ret.put("value", implementation.initialize(this.getContext(), licenseKey));
        call.resolve(ret);
    }

    @PluginMethod
    public void detectBitmap(PluginCall call) {
        long presentationMs = Integer.parseInt(call.getString("presentationMs"));
        //here Bitmap
        JSObject ret = new JSObject();
        ret.put("value", implementation.detectBitmap(presentationMs, bitmap);
        call.resolve(ret);
    }
    
}
