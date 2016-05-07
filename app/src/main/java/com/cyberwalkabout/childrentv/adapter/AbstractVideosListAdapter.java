package com.cyberwalkabout.childrentv.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chicagoandroid.childrentv.R;
import com.cyberwalkabout.childrentv.youtube.YoutubeUtils;
import com.cyberwalkabout.childrentv.data.db.DbHelper;
import com.cyberwalkabout.childrentv.model.LocalVideo;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author Maria Dzyokh
 */
public abstract class AbstractVideosListAdapter extends CursorAdapter {

    protected LayoutInflater mInflater;
    protected ViewGroup parent;
    protected DbHelper dbHelper;

    public AbstractVideosListAdapter(Context context, Cursor c) {
        super(context, c, false);
        mInflater = LayoutInflater.from(context);
        dbHelper = DbHelper.get(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        parent = viewGroup;
        View view = mInflater.inflate(R.layout.video_list_item, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.ageGroup = (ImageView) view.findViewById(R.id.video_age_group);
        viewHolder.btnMore = (Button) view.findViewById(R.id.btn_more);
        viewHolder.description = (TextView) view.findViewById(R.id.video_description);
        viewHolder.title = (TextView) view.findViewById(R.id.video_title);
        viewHolder.duration = (TextView) view.findViewById(R.id.video_duration);
        viewHolder.thumbnail = (ImageView) view.findViewById(R.id.video_icon);
        viewHolder.language = (ImageView) view.findViewById(R.id.language_icon);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.title.setText(cursor.getString(cursor.getColumnIndex(LocalVideo.TITLE)));
        holder.description.setText(cursor.getString(cursor.getColumnIndex(LocalVideo.DESCRIPTION)));
        holder.language.setImageResource(getLanguageIconResId(context, cursor.getString(cursor.getColumnIndex(LocalVideo.LANGUAGE))));

        String duration = cursor.getString(cursor.getColumnIndex(LocalVideo.DURATION));
        // TODO: figure out what is going on with duration string
        if (!TextUtils.isEmpty(duration) && duration.length() > 3) {
            if (duration.substring(duration.length() - 2, duration.length()).equals("00")) {
                holder.duration.setText(duration.substring(0, duration.length() - 3));
            } else {
                holder.duration.setText(duration);
            }
            holder.duration.setVisibility(View.VISIBLE);
        } else {
            holder.duration.setVisibility(View.GONE);
        }
        String imageUrl = YoutubeUtils.getThumbnailUrl(cursor.getString(cursor.getColumnIndex(LocalVideo.YOUTUBE_ID)));
        ImageLoader.getInstance().displayImage(imageUrl, holder.thumbnail);
    }

    protected static class ViewHolder {
        TextView title;
        TextView description;
        TextView duration;
        ImageView ageGroup;
        ImageView thumbnail;
        ImageView language;
        Button btnMore;
    }

    private int getLanguageIconResId(Context ctx, String language) {
        String uri = ctx.getPackageName() + ":drawable/" + language + "_ac";
        return ctx.getResources().getIdentifier(uri, null, null);
    }
}
