/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

/**
 * A generic callback with depth distance map in millimeter (mm).
 * Any hardware that doesn't return such unit should convert to it before
 * callback to application layer.
 */
public interface DepthDistanceCallback {
    void onDistanceMapData(long presentationMs, int width, int height, byte[] data);
}
