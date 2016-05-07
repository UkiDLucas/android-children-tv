package com.cyberwalkabout.common.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class HeightResizeAnimation extends Animation {

    int targetHeight;
    int originalHeight;
    View view;
    boolean expand;
    int newHeight = 0;
    boolean fillParent;

    public HeightResizeAnimation(View view, int targetHeight, int originalHeight, boolean fillParent) {
        this.view = view;
        this.originalHeight = originalHeight;
        this.targetHeight = targetHeight;
        newHeight = originalHeight;
        if (originalHeight > targetHeight) {
            expand = false;
        } else {
            expand = true;
        }
        this.fillParent = fillParent;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (expand && newHeight < targetHeight) {
            newHeight = (int) (newHeight + (targetHeight - newHeight) * interpolatedTime);
        }

        if (!expand && newHeight > targetHeight) {
            newHeight = (int) (newHeight - (newHeight - targetHeight) * interpolatedTime);
        }
        if (fillParent && interpolatedTime == 1.0) {
            view.getLayoutParams().height = -1;

        } else {
            view.getLayoutParams().height = newHeight;
        }
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}