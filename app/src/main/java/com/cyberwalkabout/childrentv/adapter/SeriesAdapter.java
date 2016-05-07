package com.cyberwalkabout.childrentv.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
/**
 *
 * @author Maria Dzyokh
 */
public class SeriesAdapter extends AbstractVideosListAdapter {

    public SeriesAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.ageGroup.setVisibility(View.GONE);
        holder.btnMore.setVisibility(View.GONE);
    }

}
