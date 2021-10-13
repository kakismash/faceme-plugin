/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * CyberLink FaceMe (R) SDK
 * Copyright (C) 2020 CyberLink Corp. All rights reserved.
 * https://www.cyberlink.com
 */
package com.dare.plugin.liveness;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Strings {
    private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);

    public static String formatNumber(double value, int dot) {
        String format = "%." + dot + "f";
        return String.format(Locale.US, format, value);
    }

    public static String formatDateTime(long ms) {
        return datetimeFormat.format(new Date(ms));
    }

    public static String formatHHMMSS(long ms) {
        ms = ms + 500; // Round on second
        final long hours = ms / 3600_000;
        ms %= 3600_000;
        final long mins = ms / 60_000;
        ms %= 60_000;
        final long sec = ms / 1_000;

        if (hours > 0)
            return String.format(Locale.US, "%02d:%02d:%02d", hours, mins, sec);
        else
            return String.format(Locale.US, "%02d:%02d", mins, sec);
    }

    public static Spannable stringSpan(@NonNull String text, @ColorInt int foregroundColor) {
        Spannable span = new SpannableString(text);
        span.setSpan(new ForegroundColorSpan(foregroundColor), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }
}
