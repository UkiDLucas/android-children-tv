package com.cyberwalkabout.common.calendar;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.cyberwalkabout.common.util.CursorUtils;
import com.cyberwalkabout.common.util.Env;

import java.util.ArrayList;
import java.util.TimeZone;

public class CalendarUtils {

    // private static final int CALENDAR_OWNER_ACCESS_LEVEL = 700;
    private static final int CALENDAR_EDITOR_ACCESS_LEVEL = 600;

    private static final int REMINDER_METHOD_ALERT = 1;

    public static enum Reminder {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    ;

    public static void addEventToCalendar(Context context, String title, String description, long startTime, long endTime) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", startTime);
        intent.putExtra("allDay", false);
        intent.putExtra("hasAlarm", true);
        intent.putExtra("endTime", endTime);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        context.startActivity(intent);
    }

    public static void addToCalendar(final Context context, final String title, final String description, final long startTime, final long endTime, final CalendsrEventListener listener) {

        final ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        try {
            if (Env.atLeastFroyo()) {
                cursor = cr.query(Uri.parse("content://com.android.calendar/calendars"), null, null, null, null);
            } else
                cursor = cr.query(Uri.parse("content://calendar/calendars"), null, null, null, null);
            if (cursor.moveToFirst()) {
                final ArrayList<CalendarItem> calItems = new ArrayList<CalendarItem>();
                do {
                    int accessLevel = 0;
                    if (Env.atLeastICS()) {
                        accessLevel = cursor.getInt(cursor.getColumnIndex("calendar_access_level"));
                    } else {
                        accessLevel = cursor.getInt(cursor.getColumnIndex("access_level"));
                    }
                    if (accessLevel >= CALENDAR_EDITOR_ACCESS_LEVEL) {
                        CalendarItem calendarItem = new CalendarItem();
                        calendarItem.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                        calendarItem.setName(cursor.getString(cursor.getColumnIndex("name")));
                        if (Env.atLeastICS()) {
                            calendarItem.setSyncAccountName(cursor.getString(cursor.getColumnIndex("account_name")));
                        } else {
                            calendarItem.setSyncAccountName(cursor.getString(cursor.getColumnIndex("_sync_account")));
                        }

                        calItems.add(calendarItem);
                    }
                } while (cursor.moveToNext());

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Select calendar");
                builder.setAdapter(new CalendarsAdapter(context, calItems), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues cv = new ContentValues();
                        cv.put("calendar_id", calItems.get(which).getId());
                        cv.put("title", title);
                        cv.put("dtstart", startTime);
                        cv.put("hasAlarm", 1);
                        cv.put("dtend", endTime);
                        cv.put("description", description);

                        if (Env.atLeastICS()) {
                            cv.put("eventTimezone", TimeZone.getDefault().getID());
                        }

                        Uri newEvent;
                        if (Integer.parseInt(Build.VERSION.SDK) >= 8)
                            newEvent = cr.insert(Uri.parse("content://com.android.calendar/events"), cv);
                        else
                            newEvent = cr.insert(Uri.parse("content://calendar/events"), cv);

                        if (newEvent != null) {
                            long id = Long.parseLong(newEvent.getLastPathSegment());
                            if (listener != null) {
                                listener.calendarEventCreated(id);
                            }
                            ContentValues values = new ContentValues();
                            values.put("event_id", id);
                            values.put("method", REMINDER_METHOD_ALERT);
                            values.put("minutes", 10); // 10 minutes
                            if (Integer.parseInt(Build.VERSION.SDK) >= 8)
                                cr.insert(Uri.parse("content://com.android.calendar/reminders"), values);
                            else
                                cr.insert(Uri.parse("content://calendar/reminders"), values);

                        }
                        dialog.cancel();
                        Toast.makeText(context, "Your reminder has been set!", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.create().show();
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }

    }

    public interface CalendsrEventListener {
        public void calendarEventCreated(long eventId);
    }
}
