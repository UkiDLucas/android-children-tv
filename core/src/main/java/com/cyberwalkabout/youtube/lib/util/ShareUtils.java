package com.cyberwalkabout.youtube.lib.util;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.cyberwalkabout.social.ShareSettings;

public class ShareUtils {

    public static ShareSettings getShareSettings(Context ctx) {
        return new ShareSettings(ctx.getString(R.string.twitter_callback_url), ctx.getString(R.string.twitter_consumer_key), ctx.getString(R.string.twitter_consumer_secret),
                ctx.getString(R.string.facebook_callback_url), ctx.getString(R.string.facebook_app_id), ctx.getString(R.string.facebook_app_secret));
    }


    public static void sendEmail(Context ctx, String subject, String message, String to) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (to == null) {
            to = "";
        }
        emailIntent.setData(Uri.parse("mailto:" + to));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        ctx.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public static void rateApp(Context ctx) {
        Uri uri = Uri.parse("market://details?id=" + ctx.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            ctx.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            new AlertDialog.Builder(ctx).setMessage("Couldn't launch the market").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
    }

}
