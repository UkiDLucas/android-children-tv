package com.cyberwalkabout.youtube.lib;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.MediaRouteButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cyberwalkabout.childrentv.shared.model.AgeGroupConst;
import com.cyberwalkabout.youtube.lib.app.ChildrenTVApp;
import com.cyberwalkabout.youtube.lib.data.db.DbHelper;
import com.cyberwalkabout.youtube.lib.subscription.SubscriptionHelper;
import com.cyberwalkabout.youtube.lib.fragments.AgeSettingsFragment;
import com.cyberwalkabout.youtube.lib.fragments.LanguageSettingsFragment;
import com.cyberwalkabout.youtube.lib.fragments.VideosListFragment;
import com.cyberwalkabout.youtube.lib.util.AppSettings;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;

public class AllVideosScreen extends AbstractVideosActivity {
    public static final String UPDATE_VIDEOS_ACTION = "UPDATE_VIDEOS_ACTION";
    public static final String FILTER_CHANGED_ACTION = "FILTER_CHANGED_ACTION";

    private static final String TAG = AllVideosScreen.class.getSimpleName();

    private static final String SHOW_LANGUAGE_POPUP = "show_language_popup";

    private ImageButton btnAgeFilter;
    private ImageButton btnLanguageFilter;

    private AppSettings appSettings;
    private SubscriptionHelper subscriptionHelper;

    private VideoCastManager mCastManager;
    private MediaRouteButton mMediaRouteButton;

    private BroadcastReceiver filterChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            btnAgeFilter.setImageResource(getAgeFilterIconResId());
            btnLanguageFilter.setImageResource(AppSettings.getLanguageIconResId(AllVideosScreen.this, appSettings.getFirstSelectedLanguage(), true));
        }
    };

    private VideoCastConsumerImpl videoCastConsumer = new VideoCastConsumerImpl() {
        @Override
        public void onCastAvailabilityChanged(boolean castPresent) {
            Log.d(TAG, "onCastAvailabilityChanged(" + castPresent + ")");
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VideoCastManager.checkGooglePlayServices(this);
        setContentView(R.layout.videos_screen);

        appSettings = new AppSettings(this);
        subscriptionHelper = new SubscriptionHelper(this);

        initFragments();
        setupNavigationBar();

        if (getPreferences(MODE_PRIVATE).getBoolean(SHOW_LANGUAGE_POPUP, true)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSlidingMenu().showSecondaryMenu(true);
                }
            }, 1000);
            getPreferences(MODE_PRIVATE).edit().putBoolean(SHOW_LANGUAGE_POPUP, false).commit();
        }

        if (new AppSettings(this).isLastSyncFailed() && DbHelper.get(this).getAllVideosCount() == 0) {
            showDataLoadErrorDialog();
        }

        handleSubscriptionPurchaseRequest();

        mCastManager = VideoCastManager.getInstance();

        mCastManager.addVideoCastConsumer(videoCastConsumer);

        mMediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
        mCastManager.addMediaRouterButton(mMediaRouteButton);

        mCastManager.reconnectSessionIfPossible(20); // 20 sec
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSubscriptionPurchaseRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(filterChangedReceiver, new IntentFilter(AllVideosScreen.FILTER_CHANGED_ACTION));
        mCastManager.incrementUiCounter();
        mCastManager.startCastDiscovery();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(filterChangedReceiver);
        mCastManager.decrementUiCounter();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // mark all videos as old because once user seen list of new videos it is no longer new for user
        final DbHelper dbHelper = DbHelper.get(this);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                dbHelper.markAllVideosOld();
                return null;
            }
        }.execute();

        mCastManager.removeVideoCastConsumer(videoCastConsumer);
        super.onDestroy();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mCastManager.onDispatchVolumeKeyEvent(event, ChildrenTVApp.VOLUME_INCREMENT)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d(TAG, "User leaving current activity. It might be 'home button' press or simply transition to another activity.");

        subscriptionHelper.tryToScheduleNotification(getApplicationContext());
    }

    private void handleSubscriptionPurchaseRequest() {
        if (subscriptionHelper.isSubscriptionPurchaseRequested(getIntent())) {
            subscriptionHelper.initiatePurchaseProcess(this);
        }
    }

    private void initFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.menu_frame, new AgeSettingsFragment());
        transaction.replace(R.id.secondary_menu_frame, new LanguageSettingsFragment());
        transaction.replace(R.id.videos_fragment_container, new VideosListFragment());
        transaction.commit();
    }

    protected boolean lockBackButton() {
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled;
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                handled = super.onKeyDown(keyCode, event);
                break;
            case KeyEvent.KEYCODE_BACK:
                if (!getSlidingMenu().isMenuShowing()) {
                    if (lockBackButton()) {
                        Toast.makeText(this, R.string.message_back_button_press, Toast.LENGTH_LONG).show();
                        handled = true;
                    } else {
                        showQuitConfirmationDialog();
                        handled = true;
                    }
                } else {
                    handled = super.onKeyDown(keyCode, event);
                }
                break;
            default:
                handled = super.onKeyDown(keyCode, event);
                break;
        }
        return handled;
    }

    private void showQuitConfirmationDialog() {
        // TODO: convert to dialog fragment
        new AlertDialog.Builder(this).setMessage(this.getString(R.string.msg_exit_dialog)).setCancelable(false)
                .setPositiveButton(this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                }).setNegativeButton(this.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).create().show();
    }

    private void showDataLoadErrorDialog() {
        // TODO: convert to dialog fragment
        new AlertDialog.Builder(this).setMessage(this.getString(R.string.error_updating_videos)).setCancelable(false)
                .setPositiveButton(this.getString(android.R.string.ok), null).create().show();
    }

    private void setupNavigationBar() {
        btnAgeFilter = (ImageButton) findViewById(R.id.left_btn);
        btnAgeFilter.setImageResource(getAgeFilterIconResId());
        btnAgeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSlidingMenu().isMenuShowing()) {
                    getSlidingMenu().showContent();
                }
                getSlidingMenu().showMenu();
            }
        });
        btnLanguageFilter = (ImageButton) findViewById(R.id.right_btn);
        btnLanguageFilter.setImageResource(AppSettings.getLanguageIconResId(this, appSettings.getFirstSelectedLanguage(), true));
        btnLanguageFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSlidingMenu().isMenuShowing()) {
                    getSlidingMenu().showContent();
                }
                getSlidingMenu().showSecondaryMenu();
            }
        });
    }

    private int getAgeFilterIconResId() {
        int iconResId = R.drawable.ic_age_filter;
        for (int i = 1; i < AgeGroupConst.AGE_GROUPS.length; i++) { // skip "All Videos" item
            String ageGroup = AgeGroupConst.AGE_GROUPS[i];
            if (appSettings.isAgeGroupSelected(ageGroup)) {
                iconResId = AppSettings.getAgeGroupIconId(ageGroup);
                break;
            }
        }
        return iconResId;
    }
}
