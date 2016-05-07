package com.cyberwalkabout.common.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.chicagoandroid.childrentv.R;


/**
 * @author Maria Dzyokh
 */
public class CustomFontTextView extends TextView {

    private static final String TAG = CustomFontTextView.class.getSimpleName();

    private int strokeColor = Color.TRANSPARENT;
    private int strokeWidth = 0;

    public CustomFontTextView(Context context) {
        super(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
        String customFont = a.getString(R.styleable.CustomFontTextView_typeface);
        strokeColor = a.getColor(R.styleable.CustomFontTextView_textStrokeColor, strokeColor);
        strokeWidth = a.getDimensionPixelSize(R.styleable.CustomFontTextView_textStrokeWidth, strokeWidth);
        setCustomFont(context, customFont);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ColorStateList textColor = getTextColors();

        TextPaint paint = this.getPaint();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeMiter(10);
        this.setTextColor(strokeColor);
        paint.setStrokeWidth(strokeWidth);

        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);

        setTextColor(textColor);
        super.onDraw(canvas);
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        if (asset != null) {
            try {
                tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/" + asset);
            } catch (Exception e) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Could not get typeface: " + asset + " " + e.getMessage());
                }
                return false;
            }
            setTypeface(tf);
            return true;
        }
        return false;
    }
}
