package com.cyberwalkabout.common.expandableadapter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.ListAdapter;
import com.cyberwalkabout.common.animation.HeightResizeAnimation;

/**
 * Wraps a ListAdapter to give it expandable list view functionality.
 * The main thing it does is add a listener to the getToggleButton
 * which expands the getExpandableView for each list item.
 *
 * @author tjerk
 * @date 6/9/12 4:41 PM
 */
public abstract class AbstractSlideExpandableListAdapter implements ListAdapter {
    private ListAdapter wrapped;

    public AbstractSlideExpandableListAdapter(ListAdapter wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return wrapped.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int i) {
        return wrapped.isEnabled(i);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        wrapped.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        wrapped.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public int getCount() {
        return wrapped.getCount();
    }

    @Override
    public Object getItem(int i) {
        return wrapped.getItem(i);
    }

    @Override
    public long getItemId(int i) {
        return wrapped.getItemId(i);
    }

    @Override
    public boolean hasStableIds() {
        return wrapped.hasStableIds();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = wrapped.getView(i, view, viewGroup);
        enableFor(view);
        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return wrapped.getItemViewType(i);
    }

    @Override
    public int getViewTypeCount() {
        return wrapped.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    private static View lastOpen = null;

    /**
     * This method is used to get the Button view that should
     * expand or collapse the Expandable View.
     * <br/>
     * Normally it will be implemented as:
     * <pre>
     * return (Button)parent.findViewById(R.id.expand_toggle_button)
     * </pre>
     * <p/>
     * A listener will be attached to the button which will
     * either expand or collapse the expandable view
     *
     * @param parent the list view item
     * @return a child of parent which is a button
     * @see getExpandableView
     */
    public abstract View getExpandToggleButton(View parent);

    /**
     * This method is used to get the view that will be hidden
     * initially and expands or collapse when the ExpandToggleButton
     * is pressed @see getExpandToggleButton
     * <br/>
     * Normally it will be implemented as:
     * <pre>
     * return parent.findViewById(R.id.expandable)
     * </pre>
     *
     * @param parent the list view item
     * @return a child of parent which is a view (or often ViewGroup)
     *         that can be collapsed and expanded
     * @see getExpandToggleButton
     */
    public abstract View getExpandableView(View parent);


    public abstract View getCollapseToggleButton(View parent);

    public void enableFor(View parent) {
        View more = getExpandToggleButton(parent);
        View less = getCollapseToggleButton(parent);
        View itemToolbar = getExpandableView(parent);
        enableFor(more, less, itemToolbar);
    }

    public static void enableFor(final View buttonMore, View buttonLess, final View target) {

        final ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int height = target.getHeight();
                target.setTag(height);
                ViewGroup.LayoutParams lp = target.getLayoutParams();
                lp.height = 0;
                target.setLayoutParams(lp);
                target.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };

        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAnimation(null);
                HeightResizeAnimation anim = new HeightResizeAnimation(target, Integer.parseInt(target.getTag().toString()), 0, false);
                anim.setDuration(500);
                buttonMore.setVisibility(View.GONE);
                view.startAnimation(anim);

            }
        });

        buttonLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAnimation(null);
                HeightResizeAnimation anim = new HeightResizeAnimation(target, 0, Integer.parseInt(target.getTag().toString()), false);
                anim.setDuration(500);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        buttonMore.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                view.startAnimation(anim);
            }
        });

        if (target.getTag() == null || target.getTag().toString().equals("reused")) {
            target.getViewTreeObserver().addOnPreDrawListener(listener);
        } else {
            ViewGroup.LayoutParams lp = target.getLayoutParams();
            lp.height = 0;
            target.setLayoutParams(lp);
        }

        buttonMore.setVisibility(View.VISIBLE);

    }

}
