package com.cyberwalkabout.youtube.lib;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import com.cyberwalkabout.youtube.lib.analytics.FlurryAnalytics;
import com.cyberwalkabout.youtube.lib.subscription.IabHelper;
import com.cyberwalkabout.youtube.lib.subscription.IabResult;
import com.cyberwalkabout.youtube.lib.subscription.SubscriptionHelper;
import com.cyberwalkabout.youtube.lib.util.AppSettings;
import com.cyberwalkabout.youtube.lib.app.ChildrenTVApp;
import com.cyberwalkabout.youtube.lib.subscription.Inventory;
import com.cyberwalkabout.youtube.lib.subscription.Purchase;
import com.flurry.android.FlurryAgent;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * @author Maria Dzyokh
 */
public class AbstractVideosActivity extends SlidingFragmentActivity {

    public static final String PURCHASE_SUBSCRIPTION_ACTION = "com.cyberwalkabout.childrentv.purchase_subscription";

    private static final String TAG = AbstractVideosActivity.class.getSimpleName();
    public static final int RESPONSE_CODE_PURCHASE_CANCELED = -1005;

    private BroadcastReceiver contributeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startDonateFlow();
        }
    };

    // Does the user have an active subscription to the annual videos plan?
    boolean hasSubscription = false;

    // SKU for our subscription (infinite videos for a year)
    private String sku;

    private String appKey;

    // request code for the purchase flow
    private static final int PURCHASE_FLOW_REQUEST_CODE = 10001;

    // The helper object
    private IabHelper mHelper;

    private ProgressDialog progressDialog;

    private SubscriptionHelper subscriptionHelper;
    private AppSettings appSettings;

    // Listener that's called when we finish querying the items and subscriptions we own
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;
            mHelper.flagEndAsync();
            // Is it a failure?
            if (result.isFailure() && Boolean.valueOf(getString(R.string.debug))) {
                complain("Failed to query inventory: " + result);
                return;
            }
            Log.d(TAG, "Query inventory was successful.");
            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See verifyDeveloperPayload().
             */
            // Did we subscribe to annual videos plan?
            Purchase annualVideosPurchase = inventory != null ? inventory.getPurchase(sku) : null;
            hasSubscription = (annualVideosPurchase != null && verifyDeveloperPayload(annualVideosPurchase));
            Log.d(TAG, "User " + (hasSubscription ? "HAS" : "DOES NOT HAVE") + " annual videos subscription.");

            if (Boolean.valueOf(getString(R.string.debug))) {
                // debug mode only
                if (appSettings.isFirstLaunch() && hasSubscription) {
                    mHelper.consumeAsync(inventory.getPurchase(sku), mConsumeFinishedListener);
                    appSettings.setFirstLaunch(false);
                } else {
                    subscriptionHelper.setSubscriptionValid(hasSubscription);
                }
            } else {
                subscriptionHelper.setSubscriptionValid(hasSubscription);
            }
            showWaitDialog(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    // Called when consumption is complete
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
            mHelper.flagEndAsync();
            // We know this is the "1 Year Subscription" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
                Log.e(TAG, "Error while consuming: " + result);
            }
            showWaitDialog(false);
            Log.d(TAG, "End consumption flow.");
        }
    };

    public void onStart() {
        super.onStart();
        FlurryAgent.setReportLocation(false);
        FlurryAgent.onStartSession(this, FlurryAnalytics.FLURRY_APP_KEY);
    }

    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChildrenTVApp.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChildrenTVApp.activityPaused();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense_key));
        setBehindContentView(com.cyberwalkabout.youtube.lib.R.layout.menu_frame);

        getSlidingMenu().setShadowWidthRes(com.cyberwalkabout.youtube.lib.R.dimen.shadow_width);
        getSlidingMenu().setShadowDrawable(R.drawable.shadow_left);
        getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadow_right);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        getSlidingMenu().setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
        getSlidingMenu().setBehindWidth(getResources().getDimensionPixelSize(R.dimen.sliding_menu_width));
        getSlidingMenu().setFadeEnabled(true);
        getSlidingMenu().setFadeDegree(0.35f);
        getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
        getSlidingMenu().setSecondaryMenu(R.layout.secondary_menu_frame);
        getSlidingMenu().setBehindScrollScale(0.35f);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        subscriptionHelper = new SubscriptionHelper(this);
        appSettings = new AppSettings(this);

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
        appKey = getString(R.string.app_key);

        sku = getString(R.string.sku);

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, appKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(Boolean.valueOf(getString(R.string.debug)));

        // Start setup. This is asynchronous and the specified listener will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    if (Boolean.valueOf(getString(R.string.debug))) {
                        complain("Problem setting up in-app billing: " + result);
                    }
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                // IAB is fully set up. Now, if subscriptions are supported let's get an inventory of stuff we own.
                mHelper.flagEndAsync();
                if (mHelper.subscriptionsSupported()) {
                    Log.d(TAG, "Setup successful. Querying inventory.");
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } else {
                    subscriptionHelper.setSubscriptionValid(true);
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(contributeReceiver, new IntentFilter(PURCHASE_SUBSCRIPTION_ACTION));
    }

    // "Contribute" button clicked. Start purchase flow for subscription.
    public void startDonateFlow() {
        if (!mHelper.subscriptionsSupported()) {
            FlurryAnalytics.getInstance().subscriptionPurchaseFailed("subscriptions not supported on device");
            if (Boolean.valueOf(getString(R.string.debug))) {
                complain("Subscriptions not supported on your device yet. Sorry!");
            }
            return;
        }

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        showWaitDialog(true);
        Log.d(TAG, "Launching purchase flow for subscription.");

        try {
            String skuType = getString(R.string.sku_type);
            mHelper.launchPurchaseFlow(this, sku, skuType, PURCHASE_FLOW_REQUEST_CODE, mPurchaseFinishedListener, payload);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (requestCode == PURCHASE_FLOW_REQUEST_CODE) {
            // Pass on the activity result to the helper for handling
            if (mHelper != null && mHelper.handleActivityResult(requestCode, resultCode, data)) {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
        /* TODO: we don't want to display this popup for now
        } else if (resultCode == PlayerViewActivity.RESULT_SHOW_POPUP) {
            FlurryAnalytics.contributionPopupShown();
            startActivity(new Intent(this, ContributionPopup.class));
        */
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO:
         * verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
            mHelper.flagEndAsync();
            if (result.isFailure()) {
                // User cancelled purchase
                if (result.getResponse() == RESPONSE_CODE_PURCHASE_CANCELED) {
                    FlurryAnalytics.getInstance().subscriptionPurchaseCancelled();
                } else {
                    FlurryAnalytics.getInstance().subscriptionPurchaseFailed(result.getMessage());
                }
                if (Boolean.valueOf(getString(R.string.debug))) {
                    complain("Error purchasing: " + result.getMessage());
                }
                showWaitDialog(false);
                return;
            }

            if (!verifyDeveloperPayload(purchase)) {
                FlurryAnalytics.getInstance().subscriptionPurchaseFailed("Authenticity verification failed");
                if (Boolean.valueOf(getString(R.string.debug))) {
                    complain("Error purchasing. Authenticity verification failed.");
                }
                showWaitDialog(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(sku)) {
                // bought the infinite videos subscription
                FlurryAnalytics.getInstance().subscriptionPurchaseCompleted();
                Log.d(TAG, "Subscription purchased.");
                alert("Thank you for contributing!");
                hasSubscription = true;
                subscriptionHelper.setSubscriptionValid(true);
                showWaitDialog(false);
            }
        }
    };

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            // very important:
            Log.d(TAG, "Destroying helper.");
            // TODO: think if we have to move to onPause to guarantee dispose of the helper
            if (mHelper != null) {
                mHelper.dispose();
                mHelper = null;
            }
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contributeReceiver);
        BugSenseHandler.closeSession(this);
    }

    private void complain(String message) {
        alert("Error: " + message);
    }

    private void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    private void showWaitDialog(boolean show) {
        if (show) {
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {

            }
        }
    }
}
