package com.ordyx.plugins.faceme;

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


@CapacitorPlugin(name = "Faceme")
public class FacemePlugin extends Plugin {

    private Faceme implementation = new Faceme();
    private Context context;

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void inizialize(String licenseKey) {
        if (licenseKey != null) {
            try {
                FaceMeSdk.initialize(context.getApplicationContext(), licenseKey);
                callback.onCallback(licenseKey);
            } catch (Exception e) {
                callback.error("Something went wrong. " + e);
            }
        } else {
            callback.error("Please don't pass null values.");
        }
    }
}
