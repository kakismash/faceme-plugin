package com.dare.plugin;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

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
    public void inizialize(PluginCall call) {
        String licenseKey = call.getString("licenseKey");
        FaceMeSdk.initialize(context.getApplicationContext(), licenseKey);
    }
}
