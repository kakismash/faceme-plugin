/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * A {@link SurfaceView} that can be adjusted to a specified aspect ratio.
 *
 * @see <a href="https://github.com/googlesamples/android-Camera2Video/blob/master/Application/src/main/java/com/example/android/camera2video/AutoFitTextureView.java">AutoFitTextureView</a>
 *      in googlesamples/android-Camera2Video
 */
public class AutoFitSurfaceView extends SurfaceView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutoFitSurfaceView(Context context) {
        this(context, null);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width == mRatioWidth && height == mRatioHeight) return;
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }

        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
