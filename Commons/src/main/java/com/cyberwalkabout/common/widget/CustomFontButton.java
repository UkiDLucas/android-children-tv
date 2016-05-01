package com.cyberwalkabout.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import com.cyberwalkabout.common.R;


/**
 * @author Maria Dzyokh
 */
public class CustomFontButton extends Button {

    private static final String TAG = CustomFontButton.class.getSimpleName();

    public CustomFontButton(Context context) {
        super(context);
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomFontButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
        String customFont = a.getString(R.styleable.CustomFontTextView_typeface);
        setCustomFont(ctx, customFont);
        a.recycle();
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
