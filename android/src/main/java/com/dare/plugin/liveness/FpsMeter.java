/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simple utility to calculate FPS.
 */
public final class FpsMeter {

    private static final int  STEP         = 5;
    private static final long PERIOD       = 500L;
    private static final long CACHE_WINDOW = 5_000L;

    /**
     * Cache each frame timestamp to do stabilization.
     */
    private final ArrayList<Long> frameTs       = new ArrayList<>();
    private       boolean         isInitialized = false;
    private       int             counter       = 0;
    private       int             prevCounter   = 0;
    private       long            prevTimestamp = 0;
    private       float           fps           = 0;

    private void init() {
        frameTs.clear();
        counter       = 0;
        prevCounter   = 0;
        prevTimestamp = SystemClock.elapsedRealtime();
        fps           = 0;
    }

    /**
     * Stamp current timestamp and measure accumulative frame rate.
     */
    public void measure() {
        if (isInitialized) {
            counter++;
            long now = SystemClock.elapsedRealtime();
            cacheTimestamp(now);

            int  diff    = counter - prevCounter;
            long elapsed = now - prevTimestamp;

            if (diff == STEP || elapsed >= PERIOD) {
                // fps = 1000F * diff / elapsed;
                fps         = estimateFps();
                prevCounter = counter;
                prevTimestamp = now;
            }
        } else {
            init();
            isInitialized = true;
        }
    }

    private void cacheTimestamp(long timestamp) {
        synchronized (frameTs) {
            frameTs.add(timestamp);

            Iterator<Long> it = frameTs.iterator();
            while (it.hasNext()) {
                long t = it.next();
                if (timestamp - t <= CACHE_WINDOW) break;

                it.remove();
            }
        }
    }

    private float estimateFps() {
        int size = frameTs.size();
        if (size == 1) return 0;

        return 1000F * size / (frameTs.get(size - 1) - frameTs.get(0));
    }

    /**
     * Reset accumulative frame rate.
     */
    public void reset() {
        isInitialized = false;
    }

    /**
     * Get accumulative frame rate.
     */
    public float fps() {
        return fps;
    }
}
