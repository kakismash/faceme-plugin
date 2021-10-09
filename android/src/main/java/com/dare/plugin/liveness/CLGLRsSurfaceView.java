/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;

import com.intel.realsense.librealsense.Frame;
import com.intel.realsense.librealsense.FrameSet;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CLGLRsSurfaceView extends GLSurfaceView implements AutoCloseable{

    private final CLGLRenderer mRenderer;
    private float mPreviousX = 0;
    private float mPreviousY = 0;
    private final AtomicBoolean isMirror = new AtomicBoolean(true);

    public CLGLRsSurfaceView(Context context) {
        super(context);
        mRenderer = new CLGLRenderer();
        setRenderer(mRenderer);
    }

    public CLGLRsSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRenderer = new CLGLRenderer();
        setRenderer(mRenderer);
    }

    public Map<Integer, Pair<String,Rect>> getRectangles() {
        return mRenderer.getRectangles();
    }

    public void upload(FrameSet frames) {
        mRenderer.upload(frames);
    }

    public void upload(Frame frame) {
        mRenderer.upload(frame);
    }

    public void clear() {
        mRenderer.clear();
    }

    public void setMirror(boolean isMirror) {
        mRenderer.setMirror(isMirror);
    }

   
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                mRenderer.onTouchEvent(dx, dy);
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    public void showPointcloud(boolean showPoints) {
        mRenderer.showPointcloud(showPoints);
    }

    
    public void close() {
        if(mRenderer != null)
            mRenderer.close();
    }
}
