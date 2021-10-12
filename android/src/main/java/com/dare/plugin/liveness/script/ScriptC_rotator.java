/*
 * Copyright (C) 2011-2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: D:\\MIS PROYECTOS\\FaceMe SDK - Android 6.2.0\\Doc\\Sample Code\\Demo App\\extension\\src\\shared\\rs\\rotator.rs
 */

package com.dare.plugin.liveness.script;

import android.os.Build;
import android.os.Process;

import java.lang.reflect.Field;

import android.renderscript.*;

/**
 * @hide
 */
public class ScriptC_rotator extends ScriptC {
    private static final String __rs_resource_name = "rotator";
    // Constructor
    public  ScriptC_rotator(RenderScript rs) {
        super(rs,
              __rs_resource_name,
              rotatorBitCode.getBitCode32(),
              rotatorBitCode.getBitCode64());
        __ALLOCATION = Element.ALLOCATION(rs);
        __I32 = Element.I32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __I32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_I32;
    private final static int mExportVarIdx_inImage = 0;
    private Allocation mExportVar_inImage;
    public synchronized void set_inImage(Allocation v) {
        setVar(mExportVarIdx_inImage, v);
        mExportVar_inImage = v;
    }

    public Allocation get_inImage() {
        return mExportVar_inImage;
    }

    public Script.FieldID getFieldID_inImage() {
        return createFieldID(mExportVarIdx_inImage, null);
    }

    private final static int mExportVarIdx_inWidth = 1;
    private int mExportVar_inWidth;
    public synchronized void set_inWidth(int v) {
        setVar(mExportVarIdx_inWidth, v);
        mExportVar_inWidth = v;
    }

    public int get_inWidth() {
        return mExportVar_inWidth;
    }

    public Script.FieldID getFieldID_inWidth() {
        return createFieldID(mExportVarIdx_inWidth, null);
    }

    private final static int mExportVarIdx_inHeight = 2;
    private int mExportVar_inHeight;
    public synchronized void set_inHeight(int v) {
        setVar(mExportVarIdx_inHeight, v);
        mExportVar_inHeight = v;
    }

    public int get_inHeight() {
        return mExportVar_inHeight;
    }

    public Script.FieldID getFieldID_inHeight() {
        return createFieldID(mExportVarIdx_inHeight, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_rotate90cw = 1;
    public Script.KernelID getKernelID_rotate90cw() {
        return createKernelID(mExportForEachIdx_rotate90cw, 59, null, null);
    }

    public void forEach_rotate90cw(Allocation ain, Allocation aout) {
        forEach_rotate90cw(ain, aout, null);
    }

    public void forEach_rotate90cw(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_rotate90cw, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_rotate90ccw = 2;
    public Script.KernelID getKernelID_rotate90ccw() {
        return createKernelID(mExportForEachIdx_rotate90ccw, 59, null, null);
    }

    public void forEach_rotate90ccw(Allocation ain, Allocation aout) {
        forEach_rotate90ccw(ain, aout, null);
    }

    public void forEach_rotate90ccw(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_rotate90ccw, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_rotate90ccwFlipH = 3;
    public Script.KernelID getKernelID_rotate90ccwFlipH() {
        return createKernelID(mExportForEachIdx_rotate90ccwFlipH, 59, null, null);
    }

    public void forEach_rotate90ccwFlipH(Allocation ain, Allocation aout) {
        forEach_rotate90ccwFlipH(ain, aout, null);
    }

    public void forEach_rotate90ccwFlipH(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_rotate90ccwFlipH, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_rotate90ccwFlipV = 4;
    public Script.KernelID getKernelID_rotate90ccwFlipV() {
        return createKernelID(mExportForEachIdx_rotate90ccwFlipV, 59, null, null);
    }

    public void forEach_rotate90ccwFlipV(Allocation ain, Allocation aout) {
        forEach_rotate90ccwFlipV(ain, aout, null);
    }

    public void forEach_rotate90ccwFlipV(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_rotate90ccwFlipV, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_rotate180cw = 5;
    public Script.KernelID getKernelID_rotate180cw() {
        return createKernelID(mExportForEachIdx_rotate180cw, 59, null, null);
    }

    public void forEach_rotate180cw(Allocation ain, Allocation aout) {
        forEach_rotate180cw(ain, aout, null);
    }

    public void forEach_rotate180cw(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_rotate180cw, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_rotate270cw = 6;
    public Script.KernelID getKernelID_rotate270cw() {
        return createKernelID(mExportForEachIdx_rotate270cw, 59, null, null);
    }

    public void forEach_rotate270cw(Allocation ain, Allocation aout) {
        forEach_rotate270cw(ain, aout, null);
    }

    public void forEach_rotate270cw(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_rotate270cw, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_flipH = 7;
    public Script.KernelID getKernelID_flipH() {
        return createKernelID(mExportForEachIdx_flipH, 59, null, null);
    }

    public void forEach_flipH(Allocation ain, Allocation aout) {
        forEach_flipH(ain, aout, null);
    }

    public void forEach_flipH(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_flipH, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_flipHRotate90cw = 8;
    public Script.KernelID getKernelID_flipHRotate90cw() {
        return createKernelID(mExportForEachIdx_flipHRotate90cw, 59, null, null);
    }

    public void forEach_flipHRotate90cw(Allocation ain, Allocation aout) {
        forEach_flipHRotate90cw(ain, aout, null);
    }

    public void forEach_flipHRotate90cw(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_flipHRotate90cw, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_flipV = 9;
    public Script.KernelID getKernelID_flipV() {
        return createKernelID(mExportForEachIdx_flipV, 59, null, null);
    }

    public void forEach_flipV(Allocation ain, Allocation aout) {
        forEach_flipV(ain, aout, null);
    }

    public void forEach_flipV(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_flipV, ain, aout, null, sc);
    }

    private final static int mExportForEachIdx_flipVRotate90cw = 10;
    public Script.KernelID getKernelID_flipVRotate90cw() {
        return createKernelID(mExportForEachIdx_flipVRotate90cw, 59, null, null);
    }

    public void forEach_flipVRotate90cw(Allocation ain, Allocation aout) {
        forEach_flipVRotate90cw(ain, aout, null);
    }

    public void forEach_flipVRotate90cw(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
            (t0.getX() != t1.getX()) ||
            (t0.getY() != t1.getY()) ||
            (t0.getZ() != t1.getZ()) ||
            (t0.hasFaces()   != t1.hasFaces()) ||
            (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_flipVRotate90cw, ain, aout, null, sc);
    }

}

