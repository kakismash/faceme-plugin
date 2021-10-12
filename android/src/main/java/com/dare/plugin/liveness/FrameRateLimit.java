/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.os.SystemClock;

/**
 * A simple implementation to limit maximum framerate via
 * blocking current thread.
 */
public final class FrameRateLimit {

    private long interval;
    private long previous = 0;

    public FrameRateLimit(long interval) {
        this.interval = interval;
    }

    public FrameRateLimit(double fps) {
        this.interval = (fps > 0) ? (long) (1000 / fps) : 0;
    }

    public void setFPS(double fps) {
        this.interval = (fps > 0) ? (long) (1000 / fps) : 0;
        this.previous = 0;
    }

    public void await() {
        long diff = previous + interval - System.currentTimeMillis();
        if (diff > 0) {
            SystemClock.sleep(diff);
        }
        previous = System.currentTimeMillis();
    }
}
