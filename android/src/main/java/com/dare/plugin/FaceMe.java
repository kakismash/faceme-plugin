package com.dare.plugin;

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

public class FaceMe {

    public String echo(String value) {
        return "Return From ECHO";
    }

    public String initialize(Context context, String licenseKey) {
        try {
//            FaceMeSdk.isInitialized();
            FaceMeSdk.initialize(context.getApplicationContext(), licenseKey);
            return FaceMeSdk.version();
        } catch (Exception e) {
            return "Error: " + e;
        }
    }
}
