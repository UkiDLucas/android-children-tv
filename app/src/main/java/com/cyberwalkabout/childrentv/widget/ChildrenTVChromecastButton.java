package com.cyberwalkabout.childrentv.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.app.MediaRouteButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * @author Andrii Kovalov
 */
public class ChildrenTVChromecastButton extends MediaRouteButton {

    public ChildrenTVChromecastButton(Context context) {
        super(context);
    }

    public ChildrenTVChromecastButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChildrenTVChromecastButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() && event.getAction() == MotionEvent.ACTION_UP) {
            Toast toast = Toast.makeText(getContext(), "Google Chromecast is not available. More info www.google.com/chromecast", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, getActionBarSize());
            toast.show();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private int getActionBarSize() {
        int size = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            TypedValue typedValue = new TypedValue();
            int[] sizeAttr = new int[]{android.R.attr.actionBarSize};
            int indexOfAttrTextSize = 0;
            TypedArray typedArray = getContext().obtainStyledAttributes(typedValue.data, sizeAttr);
            size = typedArray.getDimensionPixelSize(indexOfAttrTextSize, -1);
            typedArray.recycle();
        }
        return size;
    }
}
