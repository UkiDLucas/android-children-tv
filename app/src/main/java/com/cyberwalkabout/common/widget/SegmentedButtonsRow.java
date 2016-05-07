package com.cyberwalkabout.common.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.chicagoandroid.childrentv.R;


/**
 * @author Maria Dzyokh
 */
public class SegmentedButtonsRow extends LinearLayout {

    private static final int DFAULT_TEXT_COLOR = Color.WHITE;
    private static final int ACTIVE_TEXT_COLOR = Color.BLUE;

    private OnValueChangedListener listener;
    private int activeButtonIndex;
    private Button[] createdButtons;

    private String[] titles;
    private String[] values;

    private Drawable activeButtonDrawable;
    private Drawable passiveButtonDrawable;

    private Drawable buttonBackground;
    private Drawable buttonsDivider;

    private int defaultTextColor;
    private int activeTextColor;


    public SegmentedButtonsRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((Activity) getContext()).getLayoutInflater().inflate(R.layout.segmented_buttons_row, this);

        TypedArray customAttributes = context.obtainStyledAttributes(attrs, R.styleable.SegmentedButtonsRow);

        values = context.getResources().getStringArray(customAttributes.getResourceId(R.styleable.SegmentedButtonsRow_vals, 0));
        titles = context.getResources().getStringArray(customAttributes.getResourceId(R.styleable.SegmentedButtonsRow_titles, 0));

        defaultTextColor = customAttributes.getColor(R.styleable.SegmentedButtonsRow_defaultTextColor, DFAULT_TEXT_COLOR);
        activeTextColor = customAttributes.getColor(R.styleable.SegmentedButtonsRow_activeTextColor, ACTIVE_TEXT_COLOR);
        activeButtonIndex = customAttributes.getInt(R.styleable.SegmentedButtonsRow_selectedPosition, 1) - 1;
        activeButtonDrawable = context.getResources().getDrawable(customAttributes.getResourceId(R.styleable.SegmentedButtonsRow_active_button_drawable, 0));
        activeButtonDrawable.setBounds(0, 0, activeButtonDrawable.getIntrinsicWidth(), activeButtonDrawable.getIntrinsicHeight());
        passiveButtonDrawable = context.getResources().getDrawable(customAttributes.getResourceId(R.styleable.SegmentedButtonsRow_passive_button_drawable, 0));
        passiveButtonDrawable.setBounds(0, 0, passiveButtonDrawable.getIntrinsicWidth(), passiveButtonDrawable.getIntrinsicHeight());

        buttonBackground = context.getResources().getDrawable(customAttributes.getResourceId(R.styleable.SegmentedButtonsRow_button_bg, 0));
        buttonsDivider = context.getResources().getDrawable(customAttributes.getResourceId(R.styleable.SegmentedButtonsRow_buttons_divider, 0));

        createdButtons = new Button[titles.length];
        LinearLayout root = (LinearLayout) findViewById(R.id.rootPanel);
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeue_Bold.ttf");

        for (int i = 0; i < titles.length; i++) {
            createdButtons[i] = new Button(context);
            createdButtons[i].setTypeface(tf);
            createdButtons[i].setText(titles[i]);
            createdButtons[i].setTag(i);
            createdButtons[i].setBackgroundDrawable(buttonBackground);
            createdButtons[i].setTextSize(12.0f);
            createdButtons[i].setPadding(-4, 10, -4, 10);
            createdButtons[i].setCompoundDrawablePadding(5);
            createdButtons[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) listener.onValueChanged(values[(Integer) view.getTag()]);
                    activeButtonIndex = (Integer) view.getTag();
                    setBackgrounds();
                }
            });
            root.addView(createdButtons[i], new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));

            if (i != titles.length - 1) {
                View divider = new View(context);
                divider.setBackgroundDrawable(buttonsDivider);
                root.addView(divider, new LayoutParams(2, LayoutParams.FILL_PARENT));
            }
        }

        customAttributes.recycle();
        setBackgrounds();
    }

    private void setBackgrounds() {
        for (int i = 0; i < createdButtons.length; i++) {
            Drawable d;
            int textColor;
            if (activeButtonIndex == i) {
                textColor = activeTextColor;
                d = activeButtonDrawable;
            } else {
                textColor = defaultTextColor;
                d = passiveButtonDrawable;
            }
            createdButtons[i].setTextColor(textColor);
            createdButtons[i].setCompoundDrawables(null, d, null, null);
        }
    }

    public void setCurrentValue(String arg) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(arg)) {
                activeButtonIndex = i;
                setBackgrounds();
                break;
            }
        }

    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.listener = listener;
    }

    public interface OnValueChangedListener {
        public void onValueChanged(String newValue);
    }

}
