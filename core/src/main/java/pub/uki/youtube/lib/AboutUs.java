package com.cyberwalkabout.youtube.lib;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.chicagoandroid.sns.SNServices;
import com.cyberwalkabout.youtube.lib.analytics.FlurryAnalytics;
import com.cyberwalkabout.youtube.lib.util.ShareUtils;

public class AboutUs extends AbstractActivity implements View.OnClickListener {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.cyberwalkabout.youtube.lib.R.layout.about_us);

        setupNavigationBar();

        findViewById(R.id.website).setOnClickListener(this);
        findViewById(R.id.btn_share_this_app).setOnClickListener(this);
        findViewById(R.id.btn_feedback).setOnClickListener(this);
        findViewById(R.id.btn_rate_this_app).setOnClickListener(this);
        findViewById(R.id.btn_tutorial).setOnClickListener(this);
        findViewById(R.id.btn_suggest_video).setOnClickListener(this);
        findViewById(R.id.btn_donate).setOnClickListener(this);
        findViewById(R.id.btn_facebook).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_share_this_app) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    SNServices.share("", getString(R.string.app_name) + " app " + getString(R.string.app_market_url_short), Uri.EMPTY, AboutUs.this, ShareUtils.getShareSettings(AboutUs.this).updateAppName(AboutUs.this.getString(com.cyberwalkabout.youtube.lib.R.string.app_name)));
                }
            });
        } else if (view.getId() == com.cyberwalkabout.youtube.lib.R.id.btn_feedback) {
            String versionName = "";
            try {
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(AboutUs.class.getSimpleName(), e.getMessage(), e);
            }
            ShareUtils.sendEmail(AboutUs.this, getString(com.cyberwalkabout.youtube.lib.R.string.feedback_email_subject, versionName), "", getString(com.cyberwalkabout.youtube.lib.R.string.feedback_email));
        } else if (view.getId() == R.id.btn_rate_this_app) {
            ShareUtils.rateApp(AboutUs.this);
        } else if (view.getId() == R.id.btn_tutorial) {
            startActivity(new Intent(AboutUs.this, TutorialScreen.class).putExtra(TutorialScreen.EXTRA_CLASS_NAME, AboutUs.class.getSimpleName()));
        } else if (view.getId() == R.id.website) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(com.cyberwalkabout.youtube.lib.R.string.cyberwalkabout_page_url))));
        } else if (view.getId() == R.id.btn_suggest_video) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(com.cyberwalkabout.youtube.lib.R.string.video_submision_form_url)));
            startActivity(i);
        } else if (view.getId() == R.id.btn_donate) {
            FlurryAnalytics.getInstance().subscriptionShowPurchaseDialog();
            LocalBroadcastManager.getInstance(AboutUs.this).sendBroadcast(new Intent(AbstractVideosActivity.PURCHASE_SUBSCRIPTION_ACTION));
            finish();
        } else if (view.getId() == R.id.btn_facebook) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_page_url))));
        }
    }

    private void setupNavigationBar() {
        findViewById(R.id.left_btn_container).setVisibility(View.INVISIBLE);
        ImageButton btnBack = (ImageButton) findViewById(com.cyberwalkabout.youtube.lib.R.id.right_btn);
        btnBack.setImageResource(R.drawable.btn_close);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
