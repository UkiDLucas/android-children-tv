package com.cyberwalkabout.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;

/**
 * @author Maria Dzyokh
 */
public class CustomFontCheckableTextView extends CustomFontTextView implements Checkable{

    private boolean checked = false;

    public CustomFontCheckableTextView(Context context) {
        super(context);
    }

    public CustomFontCheckableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFontCheckableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setChecked(boolean b) {
        if (checked!=b) {
            checked = b;
        }
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }
}
