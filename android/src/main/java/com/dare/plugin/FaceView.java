package com.dare.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.cyberlink.facecamerasdkdemo.R;
import com.cyberlink.faceme.Emotion;
import com.cyberlink.faceme.FaceAttribute;
import com.cyberlink.faceme.FaceFeature;
import com.cyberlink.faceme.FaceInfo;
import com.cyberlink.faceme.FaceLandmark;
import com.cyberlink.faceme.FaceLivenessStatus;
import com.cyberlink.faceme.Gender;
import com.cyberlink.faceme.OcclusionStatus;
import com.cyberlink.faceme.Pose;
import com.cyberlink.facemedemo.ext.VisitService;
import com.cyberlink.facemedemo.sdk.FaceData;
import com.cyberlink.facemedemo.sdk.FaceHolder;
import com.cyberlink.facemedemo.sdk.PresentFacesHolder;
import com.cyberlink.facemedemo.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class FaceView extends View {
    private static final String TAG = "FaceMe.FaceView";

    public interface OnFaceClickListener {
        void onFaceClick(FaceHolder faceHolder);
    }

    private final int BOUNDING_WIDTH;
    private final int DEFAULT_FONT_SIZE;
    private final int MIN_FONT_SIZE;
    private final int PADDING;
    private final int LANDMARK_WIDTH;
    private final int LANDMARK_RADIUS;

    private final Pair<Integer, Integer> nonameBorderColor, namedBorderColor,
            autoNamedBorderColor, maleBorderColor, femaleBorderColor;

    private final GestureDetector gestureDetector;
    private final List<FaceHolder> faceHolders = new ArrayList<>();

    private final Paint boundingBoxPaint;
    private final Paint landmarkPaint;
    private final Paint textBgPaint;
    private final TextPaint textPaint;

    private final Paint livenessHintBackgroundPaint, livenessMarkerBackgroundPaint, livenessBoundingPaint;
    private final TextPaint livenessHintPaint, livenessMarkerPaint;
    private final int livenessColor, spoofingColor, livenessHintColor, livenessMarkColor, spoofingMarkColor, livenessBoundingColor, spoofingBoundingColor, livenessHintBoundingColor;

    private UiSettings uiSettings;
    private OnFaceClickListener onFaceClickListener;

    private long presentationMs;
    private int bitmapWidth;
    private int bitmapHeight;
    private float bitmapAspectRatio;

    private float relativeWidthRatio;
    private float relativeHeightRatio;

    private boolean isDevMode = false;

    public FaceView(Context context) {
        this(context, null);
    }

    public FaceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                determineFaceAndCallback(e.getX(), e.getY());
                return true;
            }
        });

        BOUNDING_WIDTH = context.getResources().getDimensionPixelSize(R.dimen.face_border_width);
        DEFAULT_FONT_SIZE = context.getResources().getDimensionPixelSize(R.dimen.face_attr_default_text_size);
        MIN_FONT_SIZE = context.getResources().getDimensionPixelSize(R.dimen.face_attr_min_text_size);
        PADDING = context.getResources().getDimensionPixelSize(R.dimen.face_attr_padding);
        LANDMARK_WIDTH = context.getResources().getDimensionPixelSize(R.dimen.landmark_width);
        LANDMARK_RADIUS = context.getResources().getDimensionPixelSize(R.dimen.landmark_radius);

        nonameBorderColor = Pair.create(
                ContextCompat.getColor(context, R.color.noname_border),
                ContextCompat.getColor(context, R.color.noname_corner_border)
        );
        namedBorderColor = Pair.create(
                ContextCompat.getColor(context, R.color.named_border),
                ContextCompat.getColor(context, R.color.named_corner_border)
        );
        autoNamedBorderColor = Pair.create(
                ContextCompat.getColor(context, R.color.auto_named_border),
                ContextCompat.getColor(context, R.color.auto_named_corner_border)
        );
        maleBorderColor = Pair.create(
                ContextCompat.getColor(context, R.color.male_border),
                ContextCompat.getColor(context, R.color.male_corner_border)
        );
        femaleBorderColor = Pair.create(
                ContextCompat.getColor(context, R.color.female_border),
                ContextCompat.getColor(context, R.color.female_corner_border)
        );

        boundingBoxPaint = new Paint();
        boundingBoxPaint.setStyle(Paint.Style.STROKE);

        landmarkPaint = new Paint();
        landmarkPaint.setColor(ContextCompat.getColor(context, R.color.landmark_border));
        landmarkPaint.setStrokeWidth(LANDMARK_WIDTH);
        landmarkPaint.setStyle(Paint.Style.STROKE);

        textBgPaint = new Paint();
        textBgPaint.setColor(ContextCompat.getColor(context, R.color.face_label_bg));
        textBgPaint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(DEFAULT_FONT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setLinearText(true);

        livenessHintBackgroundPaint = new Paint();
        livenessHintBackgroundPaint.setStyle(Paint.Style.FILL);

        livenessMarkerBackgroundPaint = new Paint();
        livenessHintBackgroundPaint.setStyle(Paint.Style.FILL);

        livenessBoundingPaint = new Paint();
        livenessBoundingPaint.setStyle(Paint.Style.STROKE);
        livenessBoundingPaint.setStrokeWidth(BOUNDING_WIDTH);

        livenessHintPaint = new TextPaint();
        livenessHintPaint.setColor(Color.WHITE);
        livenessHintPaint.setAntiAlias(true);
        livenessHintPaint.setTextSize(DEFAULT_FONT_SIZE * 1.25F);
        livenessHintPaint.setTextAlign(Paint.Align.CENTER);
        livenessHintPaint.setLinearText(true);

        livenessMarkerPaint = new TextPaint();
        livenessMarkerPaint.setColor(Color.WHITE);
        livenessMarkerPaint.setAntiAlias(true);
        livenessMarkerPaint.setTextSize(DEFAULT_FONT_SIZE * 1.5F);
        livenessMarkerPaint.setTextAlign(Paint.Align.CENTER);
        livenessMarkerPaint.setLinearText(true);

        livenessColor = ContextCompat.getColor(context, R.color.liveness_border);
        spoofingColor = ContextCompat.getColor(context, R.color.spoofing_border);
        livenessHintColor = ContextCompat.getColor(context, R.color.liveness_hint_border);

        livenessMarkColor = ContextCompat.getColor(context, R.color.green);
        spoofingMarkColor = ContextCompat.getColor(context, R.color.spoofing_marker_color);

        livenessBoundingColor = ContextCompat.getColor(context, R.color.liveness_bounding);
        spoofingBoundingColor = ContextCompat.getColor(context, R.color.red);
        livenessHintBoundingColor = ContextCompat.getColor(context, R.color.blue);
    }

    public void setUiSettings(UiSettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    public void setOnFaceClickListener(OnFaceClickListener listener) {
        this.onFaceClickListener = listener;
    }

    private void determineFaceAndCallback(float x, float y) {
        if (onFaceClickListener == null) return;

        int remapX = (int) (x / relativeWidthRatio);
        int remapY = (int) (y / relativeHeightRatio);

        FaceHolder faceHolder = null;
        for (FaceHolder holder : faceHolders) {
            if (holder.faceInfo.boundingBox.contains(remapX, remapY)) {
                faceHolder = holder;
                break;
            }
        }

        onFaceClickListener.onFaceClick(faceHolder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        return true;
    }

    @UiThread
    public void updateFaceAttributes(int width, int height, PresentFacesHolder presentFaces) {
        // Incoming presentationMs is out of date. Ignore it.
        if (this.presentationMs > presentFaces.presentationMs) return;
        this.presentationMs = presentFaces.presentationMs;

        reset(width, height);

        this.faceHolders.addAll(presentFaces.faces);

        invalidate();
    }

    private void reset(int width, int height) {
        bitmapWidth = width;
        bitmapHeight = height;
        bitmapAspectRatio = 1F * width / height;

        faceHolders.clear();
        isDevMode = DevTool.isDevMode();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        long tsDraw = System.currentTimeMillis();
        super.onDraw(canvas);

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        float canvasAspectRatio = 1F * viewWidth / viewHeight;
        // Ignore when aspect ratio is different. Wait for feed with new Face.
        if (Math.abs(bitmapAspectRatio - canvasAspectRatio) > 0.2) return;

        relativeWidthRatio = 1F * viewWidth / bitmapWidth;
        relativeHeightRatio = 1F * viewHeight / bitmapHeight;

        drawFaces(canvas);

        long duration = System.currentTimeMillis() - tsDraw;
        if (duration > 16)
            Log.w(TAG, "onDraw faces[" + faceHolders.size() + "] took " + duration + "ms");
    }

    private void drawFaces(Canvas canvas) {
        for (FaceHolder holder : faceHolders) {
            FaceInfo faceInfo = holder.faceInfo;
            FaceAttribute faceAttr = holder.faceAttribute;
            FaceFeature faceFeature = holder.faceFeature;
            float left = faceInfo.boundingBox.left * relativeWidthRatio;
            float top = faceInfo.boundingBox.top * relativeHeightRatio;
            float right = faceInfo.boundingBox.right * relativeWidthRatio;
            float bottom = faceInfo.boundingBox.bottom * relativeHeightRatio;

            Boolean namedBorder = determineNameBorder(holder.data);
            Boolean genderBorder = determineGenderBorder(faceAttr);

            drawFaceBoundingBox(canvas, namedBorder, genderBorder, left, top, right, bottom);
            drawFaceLandmarkIfNeeded(canvas, holder.faceLandmark);

            final float availableWidth = (right - left - PADDING * 2 + BOUNDING_WIDTH) * 1.5F;
            final float anchorX = left - BOUNDING_WIDTH / 2.0F;
            String label;
            float anchorY;

            // Draw face name if named before.
            if (faceFeature != null && !TextUtils.isEmpty(holder.data.name)) {
                label = holder.data.name;
                if (isDevMode) label += ", " + Strings.formatNumber(holder.data.confidence, 2);
                anchorY = Math.max(0, top - BOUNDING_WIDTH / 2.0F - DEFAULT_FONT_SIZE - PADDING * 2);
                drawText(canvas, anchorX, anchorY, label, availableWidth);
            }

            // Draw attributes.
            String attrInfo = getFaceAttributesInfo(holder);
            if (attrInfo.length() > 0) {
                anchorY = bottom + BOUNDING_WIDTH / 2.0F;
                drawText(canvas, anchorX, anchorY, attrInfo, availableWidth);
            }

            // Draw liveness marker.
            drawLivenessMarker(canvas, left, top, holder.liveness.status);
        }
    }

    private Boolean determineNameBorder(FaceData data) {
        if (TextUtils.isEmpty(data.name))
            return false;
        else if (("User#" + data.collectionId).equals(data.name))
            return null;
        else
            return true;
    }
    private Boolean determineGenderBorder(FaceAttribute faceAttribute) {
        if (isDevMode && faceAttribute != null && uiSettings.isShowGender()) {
            if (faceAttribute.gender == Gender.MALE)
                return true;
            else if (faceAttribute.gender == Gender.FEMALE)
                return false;
        }
        return null;
    }

    private void drawFaceBoundingBox(Canvas canvas, Boolean named, Boolean genderBorder, float left, float top, float right, float bottom) {
        Pair<Integer, Integer> borderColor;
        if (named == null) {
            borderColor = autoNamedBorderColor;
        } else if (named) {
            if (genderBorder == null) {
                borderColor = namedBorderColor;
            } else {
                borderColor = genderBorder ? maleBorderColor : femaleBorderColor;
            }
        } else {
            borderColor = nonameBorderColor;
        }

        boundingBoxPaint.setColor(borderColor.first);
        boundingBoxPaint.setStrokeWidth(BOUNDING_WIDTH);
        canvas.drawRect(left, top, right, bottom, boundingBoxPaint);

        boundingBoxPaint.setColor(borderColor.second);
        boundingBoxPaint.setStrokeWidth(BOUNDING_WIDTH * 1.333F);
        drawFaceBoundingBoxCorner(canvas, boundingBoxPaint, left, top, right, bottom);
    }

    private void drawFaceBoundingBoxCorner(Canvas canvas, Paint cornerPaint, float left, float top, float right, float bottom) {
        float width = right - left;
        float height = bottom - top;
        float edge = Math.min(width, height) * 0.25F;

        Path corners = new Path();
        corners.moveTo(left, top + edge);
        corners.lineTo(left, top);
        corners.lineTo(left + edge, top);

        corners.moveTo(right - edge, top);
        corners.lineTo(right, top);
        corners.lineTo(right, top + edge);

        corners.moveTo(right, bottom - edge);
        corners.lineTo(right, bottom);
        corners.lineTo(right - edge, bottom);

        corners.moveTo(left + edge, bottom);
        corners.lineTo(left, bottom);
        corners.lineTo(left, bottom - edge);

        canvas.drawPath(corners, cornerPaint);
    }

    private void drawFaceLandmarkIfNeeded(Canvas canvas, FaceLandmark landmark) {
        if (!uiSettings.isShowLandmark() || landmark == null) return;
        if (landmark.featurePoints == null || landmark.featurePoints.length == 0) return;

        int[] indexes = landmark.featurePoints.length > 5 ?
                new int[]{ 10, 25, 33, 57, 59 } : new int[]{ 0, 1, 2, 3, 4 };
        for (int index : indexes) {
            Point featurePoint = landmark.featurePoints[index];
            canvas.drawCircle(featurePoint.x * relativeWidthRatio,
                    featurePoint.y * relativeHeightRatio,
                    LANDMARK_RADIUS, landmarkPaint);
        }
    }

    private String getFaceAttributesInfo(FaceHolder holder) {
        String info = "";

        if (holder.faceAttribute != null) {
            if (uiSettings.isShowGender()) {
                info += reviseGender(holder.faceAttribute.gender);
            }

            if (uiSettings.isShowAge()) {
                info += reviseAge(holder.faceAttribute.age);
            }

            if (uiSettings.isShowEmotion()) {
                if (info.length() > 0) info += "\n";
                info += "Emotion: " + reviseEmotion(holder.faceAttribute.emotion);
            }

            if (uiSettings.isShowPose()) {
                if (info.length() > 0) info += "\n";
                info += "Angle: " + revisePose(holder.faceAttribute.pose);
            }
        }

        if (holder.faceInfo != null) {
            if (uiSettings.isShowMaskDetection()) {
                if (info.length() > 0) info += "\n";
                info += "Mask: " + reviseMask(holder.faceInfo.occlusion);
            }
        }

        if (uiSettings.isShowFeatures() && uiSettings.isShowVisitCount()) {
            VisitService.VisitData visitData = VisitService.Singleton.get(getContext()).touch(holder);
            if (visitData != null) {
                if (info.length() > 0) info += "\n";
                info += visitData.getOccurrence() + " visits, " + Strings.formatHHMMSS(visitData.getDuration());
            }
        }

        return info;
    }

    private String reviseAge(float age) {
        if (uiSettings.isAgeInRange()) {
            if (age <= 16)
                return " [ < 20 ]";
            else if (age >= 82)
                return " [ > 80 ]";
            else {
                // Each 5 years from 17 years old map to 10 years group. For example, if age is between 17 and 21 years old, show 15~25. If age is between 22 and 26, show 20~30.
                int base = (int) ((age - 2) / 5);
                int max = (base + 2) * 5;
                int min = (base) * 5;
                return " [ " + min + " ~ " + max + " ]";
            }
        } else {
            return " " + age;
        }
    }

    private String reviseGender(@Gender.EGender int gender) {
        switch (gender) {
            case Gender.MALE: return "\u2642";
            case Gender.FEMALE: return "\u2640";

            case Gender.UNKNOWN:
            default: return "\u003F";
        }
    }

    private String reviseEmotion(@Emotion.EEmotion int emotion) {
        switch (emotion) {
            case Emotion.HAPPY: return "Happy";
            case Emotion.SURPRISED: return "Surprised";
            case Emotion.SAD: return "Sad";
            case Emotion.ANGRY: return "Angry";
            case Emotion.NEUTRAL: return "Neutral";

            case Emotion.UNKNOWN:
            default: return "Unknown";
        }
    }

    private String revisePose(Pose pose) {
        return "(Y: " + (int)pose.yaw + ", P: " + (int)pose.pitch + ", R: " + (int)pose.roll + ")";
    }

    private String reviseMask(OcclusionStatus occlusion) {
        if (occlusion.OcclusionStatusMask) {
            if (occlusion.OcclusionStatusMouth && occlusion.OcclusionStatusNose) {
                return "Mask OK";
            } else {
                return "Mask Not OK";
            }
        }
        return "No Mask";
    }

    private float drawText(Canvas canvas, float left, float anchorY, String string, float availableWidth) {
        // Canvas.drawText cannot draw String with line break (\n). So there is a workaround to handle it.
        String[] stringLines = string.split("\n");

        Rect txtBounds = new Rect();
        int fontSize = DEFAULT_FONT_SIZE;
        for (String singleLine : stringLines) {
            Rect lineRect = new Rect();
            int lineFontSize = adjustFontSize(singleLine, availableWidth, lineRect);
            // Choose smaller font size.
            if (fontSize >= lineFontSize) {
                fontSize = lineFontSize;
                if (txtBounds.width() < lineRect.width()) {
                    txtBounds = lineRect;
                }
            }
        }
        textPaint.setTextSize(fontSize);

        float lineSpacing = PADDING / 2.0F;
        float bgHeight = txtBounds.height() * stringLines.length + lineSpacing * (stringLines.length - 1) + PADDING * 2;
        float bgWidth = txtBounds.width() + PADDING * 4;
        canvas.drawRect(left, anchorY, left + bgWidth, anchorY + bgHeight, textBgPaint);

        float textX = left + PADDING * 2;
        float textY = anchorY + PADDING * 4;
        for (String singleLine : stringLines) {
            canvas.drawText(singleLine, textX, textY, textPaint);
            textY += txtBounds.height() + lineSpacing;
        }

        anchorY += bgHeight;

        return anchorY;
    }

    private int adjustFontSize(String line, float availableWidth, Rect fontBounds) {
        int fontSize = DEFAULT_FONT_SIZE;
        do {
            textPaint.setTextSize(fontSize--);
            textPaint.getTextBounds(line, 0, line.length(), fontBounds);
        } while (fontBounds.width() >= availableWidth && fontSize > MIN_FONT_SIZE);
        return fontSize;
    }

    private void drawLivenessMarker(Canvas canvas, float left, float top, @FaceLivenessStatus.EFaceLivenessStatus int livenessStatus) {
        int backgroundColor;
        int boundingColor;
        String hintText;
        String markerText = null;
        switch (livenessStatus) {
            case FaceLivenessStatus.FACE_IS_LIVENESS:
                backgroundColor = livenessColor;
                livenessMarkerBackgroundPaint.setColor(livenessMarkColor);
                boundingColor = livenessBoundingColor;
                hintText = "Real Person";
                markerText = "✔";
                break;
            case FaceLivenessStatus.FACE_IS_SPOOFING:
                backgroundColor = spoofingColor;
                livenessMarkerBackgroundPaint.setColor(spoofingMarkColor);
                boundingColor = spoofingBoundingColor;
                hintText = "Spoofing";
                markerText = "✖";
                break;
            case FaceLivenessStatus.FACE_IS_INVALID_ANGLE:
                backgroundColor = livenessHintColor;
                boundingColor = livenessHintBoundingColor;
                hintText = "Turn to Camera";
                break;
            case FaceLivenessStatus.FACE_IS_TOO_FAR:
                backgroundColor = livenessHintColor;
                boundingColor = livenessHintBoundingColor;
                hintText = "Come Closer";
                break;
            case FaceLivenessStatus.FACE_IS_TOO_NEAR:
                backgroundColor = livenessHintColor;
                boundingColor = livenessHintBoundingColor;
                hintText = "Go Farther";
                break;
            case FaceLivenessStatus.FACE_IS_LIVE_UNKNOWN:
            default:
                return;
        }

        livenessHintBackgroundPaint.setColor(backgroundColor);
        livenessBoundingPaint.setColor(boundingColor);

        float txtWidth = getTextWidth(hintText, livenessHintPaint.getTextSize());
        float txtHeight = getTextHeight(livenessHintPaint.getTextSize());

        float backgroundLeft = left - txtHeight / 3F;
        float backgroundTop = top - txtHeight * 1.25F;
        float backgroundRight = left + txtWidth + txtHeight / 3F;
        float backgroundRightPadding = backgroundRight + txtHeight / 2F;
        float backgroundBottom = top;

        if (livenessStatus == FaceLivenessStatus.FACE_IS_LIVENESS || livenessStatus == FaceLivenessStatus.FACE_IS_SPOOFING) {
            canvas.drawRect(backgroundLeft, backgroundTop, backgroundRightPadding, backgroundBottom, livenessHintBackgroundPaint);
            canvas.drawRect(backgroundLeft, backgroundTop, backgroundRightPadding, backgroundBottom, livenessBoundingPaint);
            drawMark(canvas, backgroundRight, backgroundBottom, markerText);
        }
        else {
            canvas.drawRect(backgroundLeft, backgroundTop, backgroundRight, backgroundBottom, livenessHintBackgroundPaint);
            canvas.drawRect(backgroundLeft, backgroundTop, backgroundRight, backgroundBottom, livenessBoundingPaint);
        }

        canvas.drawText(hintText, left + txtWidth / 2F , top - txtHeight / 3F, livenessHintPaint);
    }

    private void drawMark(Canvas canvas, float startLeft, float bottom, String markerText) {
        float txtWidth = getTextWidth(markerText, livenessMarkerPaint.getTextSize());
        float txtHeight = getTextHeight(livenessMarkerPaint.getTextSize());

        float left = startLeft + txtHeight / 2F;
        float top = bottom - txtHeight * 1.25F;
        float right = left + txtWidth + txtHeight / 4F;

        Path corners = new Path();
        corners.moveTo(startLeft, bottom);
        corners.lineTo(left, top);
        corners.lineTo(right, top);
        corners.lineTo(right, bottom);
        corners.lineTo(startLeft, bottom);

        canvas.drawPath(corners, livenessMarkerBackgroundPaint);
        canvas.drawPath(corners, livenessBoundingPaint);

        canvas.drawText(markerText, left + txtWidth / 2F , top + txtHeight * 0.8F, livenessMarkerPaint);

    }

    private float getTextWidth(String text, float textSize){
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        return paint.measureText(text);
    }

    private float getTextHeight(float textSize){
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }
}
