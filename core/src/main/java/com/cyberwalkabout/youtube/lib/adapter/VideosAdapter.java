package com.cyberwalkabout.youtube.lib.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;

import com.cyberwalkabout.common.util.UiUtils;
import com.cyberwalkabout.youtube.lib.SeriesScreen;
import com.cyberwalkabout.youtube.lib.model.LocalVideo;
import com.cyberwalkabout.youtube.lib.util.AppSettings;

/**
 * @author Maria Dzyokh, Andrii Kovalov
 */
public class VideosAdapter extends AbstractVideosListAdapter {
    public static final String TAG = VideosAdapter.class.getSimpleName();

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Context context = view.getContext();

            Intent intent = new Intent(context, SeriesScreen.class);
            intent.putExtra(LocalVideo.SERIES_ID, Integer.parseInt(view.getTag().toString()));
            context.startActivity(intent);
        }
    };

    public VideosAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        ViewHolder holder = (ViewHolder) view.getTag();

        /*int localRating = cursor.getInt(cursor.getColumnIndex(LocalVideoRating.LOCAL_RATING));

        if (localRating > 0) {
            view.setBackgroundResource(R.drawable.video_list_item_selector_high_rating);
        } else {
            // TODO: it causes StackOverflow
            view.setBackgroundResource(R.drawable.video_list_item_selector);
        }*/

        holder.ageGroup.setImageResource(AppSettings.getAgeGroupIconId(cursor.getString(cursor.getColumnIndex(LocalVideo.AGE_GROUP))));
        int seriesId = cursor.getInt(cursor.getColumnIndex(LocalVideo.SERIES_ID));
        int seriesCount = dbHelper.getSeriesCount(seriesId);
        if (seriesId > 0 && seriesCount > 1) {
            holder.btnMore.setText(String.valueOf(seriesCount - 1));
            holder.btnMore.setVisibility(View.VISIBLE);
            holder.btnMore.setTag(seriesId);
            holder.btnMore.setOnClickListener(onClickListener);
        } else {
            holder.btnMore.setVisibility(View.GONE);
        }
        UiUtils.expandTouchableArea(holder.btnMore, 50);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(LocalVideo.TITLE));
    }
}
