package com.cyberwalkabout.common.calendar;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chicagoandroid.childrentv.R;
import com.cyberwalkabout.common.util.Env;

import java.util.List;

public class CalendarsAdapter extends BaseAdapter {

    private List<CalendarItem> items;
    private LayoutInflater inflater;
    private Context ctx;

    public CalendarsAdapter(Context ctx, List<CalendarItem> items) {
        this.items = items;
        this.inflater = LayoutInflater.from(ctx);
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.calendar_list_item, null);
        }

        CalendarItem c = items.get(position);
        TextView calendarName = (TextView) convertView.findViewById(android.R.id.text1);
        if (!TextUtils.isEmpty(c.getName())) {
            calendarName.setText(items.get(position).getName());
        } else {
            calendarName.setText(items.get(position).getSyncAccountName());
        }

        if (Env.atLeastICS()) {
            calendarName.setTextColor(ctx.getResources().getColor(android.R.color.white));
        } else if (Env.atLeastEclair()) {
            calendarName.setTextColor(ctx.getResources().getColor(android.R.color.black));
        }

        ((TextView) convertView.findViewById(android.R.id.text2)).setText(items.get(position).getSyncAccountName());

        return convertView;
    }
}