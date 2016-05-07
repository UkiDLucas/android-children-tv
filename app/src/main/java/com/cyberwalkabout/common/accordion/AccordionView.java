package com.cyberwalkabout.common.accordion;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.chicagoandroid.childrentv.R;
import com.cyberwalkabout.common.animation.HeightResizeAnimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maria Dzyokh
 */
public class AccordionView extends LinearLayout {

    private static final int DEFAULT_ANIMATION_DURATION = 500;

    private boolean initialized = false;

    private boolean expanded;
    private int animationDuration;

    private List<View> sectionHeaders;
    private List<View> sections;

    private View[] children;

    private Map<Integer, Integer> childrenHeight;

    public AccordionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AccordionView);
            expanded = a.getBoolean(R.styleable.AccordionView_expanded, false);
            animationDuration = a.getInt(R.styleable.AccordionView_animation_duration, DEFAULT_ANIMATION_DURATION);
            a.recycle();
        }

        setOrientation(VERTICAL);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getChildrenHeight();
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        if (initialized) {
            super.onFinishInflate();
            return;
        }
        int childCount = getChildCount();

        childrenHeight = new HashMap<Integer, Integer>();
        sectionHeaders = new ArrayList<View>(childCount);
        sections = new ArrayList<View>(childCount);
        children = new View[childCount];

        for (int i = 0; i < childCount; i++) {
            children[i] = getChildAt(i);
        }

        removeAllViews();

        for (int i = 0; i < childCount; i++) {

            String resName = getContext().getResources().getResourceName(children[i].getId());

            if (resName.startsWith(getContext().getPackageName() + ":id/section")) {
                LinearLayout section = new LinearLayout(getContext());
                section.setOrientation(LinearLayout.VERTICAL);

                setUpListener(children[i], children[i + 1]);

                section.addView(children[i]);
                section.addView(children[i + 1]);

                sectionHeaders.add(children[i]);
                sections.add(children[i + 1]);

                addView(section);

                continue;
            }

            if (!resName.startsWith(getContext().getPackageName() + ":id/content")) {
                addView(children[i]);
            }

        }

        initialized = true;
        super.onFinishInflate();
    }


    private void setUpListener(View headerView, final View sectionView) {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expanded = Boolean.parseBoolean(sectionView.getTag().toString());
                sectionView.setTag(!expanded);
                if (expanded) {
                    sectionView.getLayoutParams().height = 0;
                    sectionView.requestLayout();

//                    HeightResizeAnimation anim = new HeightResizeAnimation(sectionView, 0, childrenHeight.get(sectionView.getId()), false);
//                    anim.setDuration(animationDuration);
//                    sectionView.startAnimation(anim);
                } else {
                    collapseExpandedSections(sectionView.getId());
                    sectionView.getLayoutParams().height = childrenHeight.get(sectionView.getId());
                    sectionView.requestLayout();

//                    HeightResizeAnimation anim = new HeightResizeAnimation(sectionView, childrenHeight.get(sectionView.getId()), 0, false);
//                    anim.setDuration(animationDuration);
//                    sectionView.startAnimation(anim);
//                    collapseExpandedSections(sectionView.getId());
                }

            }
        };
        headerView.setOnClickListener(listener);

        if (headerView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) headerView).getChildCount(); i++) {
                ((ViewGroup) headerView).getChildAt(i).setOnClickListener(listener);
            }
        }
    }

    private void collapseExpandedSections(int sectionToExpandId) {
        for (View section : sections) {
            if (section.getId() != sectionToExpandId) {
                boolean expanded = Boolean.parseBoolean(section.getTag().toString());
                if (expanded) {
                    section.setTag(!expanded);
                    section.getLayoutParams().height = 0;
                    section.requestLayout();

//                    HeightResizeAnimation anim = new HeightResizeAnimation(section, 0, childrenHeight.get(section.getId()), false);
//                    anim.setDuration(animationDuration);
//                    section.startAnimation(anim);
                }
            }
        }
    }

    public void collapseAllSections() {
        for (View view : sections) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height = 0;
            view.setLayoutParams(lp);
            view.setTag(false);
            view.requestLayout();
        }
    }

    public boolean isExpanded(int sectionViewId) {
        boolean isExpanded = false;
        for (View section : sections) {
            if (section.getId() == sectionViewId) {
                isExpanded = Boolean.parseBoolean(section.getTag().toString());
                break;
            }
        }
        return isExpanded;
    }

    public void notifySectionHeightChnaged(int sectionViewId, int newHeight) {
        if (sections.size() == childrenHeight.size()) {
            for (View section : sections) {
                if (section.getId() == sectionViewId) {
                    int oldHeight = childrenHeight.get(sectionViewId);
                    childrenHeight.put(sectionViewId, newHeight);

                    boolean expanded = Boolean.parseBoolean(section.getTag().toString());
                    if (expanded) {
                        section.getLayoutParams().height = newHeight;
                        section.requestLayout();

//                        HeightResizeAnimation anim = new HeightResizeAnimation(section, newHeight, oldHeight, false);
//                        anim.setDuration(animationDuration);
//                        section.startAnimation(anim);
                    }
                    break;
                }
            }
        }
    }

    public boolean isSectionHeightEqual(int sectionViewId, int newHeight) {
        if (childrenHeight.size() == sections.size()) {
            for (View section : sections) {
                if (section.getId() == sectionViewId) {
                    return childrenHeight.get(sectionViewId) == newHeight;
                }
            }
        }
        return false;
    }

    public void getChildrenHeight() {
        if (childrenHeight.size() == 0) {
            for (View view : sections) {
                View v = findViewById(view.getId());
                childrenHeight.put(v.getId(), v.getHeight());
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                lp.height = 0;
                v.setLayoutParams(lp);
                v.setTag(false);
            }
        }
    }

    public void expandSection(int sectionId) {
        for (View sectionView : sections) {
            if (sectionView.getId() == sectionId) {
                boolean expanded = Boolean.parseBoolean(sectionView.getTag().toString());
                if (!expanded) {
                    sectionView.setTag(!expanded);
                    HeightResizeAnimation anim = new HeightResizeAnimation(sectionView, childrenHeight.get(sectionView.getId()), 0, false);
                    anim.setDuration(animationDuration);
                    sectionView.startAnimation(anim);
                    collapseExpandedSections(sectionView.getId());
                }
                break;
            }
        }
    }
}
