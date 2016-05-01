package pub.uki.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pub.uki.sns.R;
import pub.uki.social.util.Extras;
import pub.uki.social.util.ShareMessage;

public class ShareActivity extends Activity {

    private static final String APP_NAME = "app_name";

    public static final int EXTRA_SHARE_FACEBOOK = 1;

    public static final int EXTRA_SHARE_TWITTER = 0;

    public static final String EXTRA_SHARE_TYPE = "share_type";

    public static final String EXTRA_TEXT = "share_text";

    private Button btnShare;

    private int defaultColor = 0;

    private EditText edittext;

    private boolean isTwitter = false;

    private int length = 0;

    private String mAppName;

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateTextLength();
        }
    };

    private TextView tvStatus;

    private int twitterColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_share);

        final Intent intent = getIntent();

        mAppName = intent.getStringExtra(APP_NAME);

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isTwitter) {
                    new SNServices(mAppName).postToWallTwitter(edittext.getText().toString(), ShareActivity.this);
                } else { // is facebook
                    if (intent.hasExtra(Extras.EXTRA_SHARE_MESSAGE)) {
                        new SNServices(mAppName).updateFacebookStatus((ShareMessage) intent.getSerializableExtra(Extras.EXTRA_SHARE_MESSAGE), edittext.getText().toString(), ShareActivity.this);
                    } else {
                        new SNServices(mAppName).updateFacebookStatus(edittext.getText().toString(), ShareActivity.this);
                    }

                }

                finish();
            }
        };

        if (intent.getComponent().getPackageName().equals(getPackageName())) {
            String text = null;
            if (intent.hasExtra(Extras.EXTRA_SHARE_MESSAGE)) {
                text = ((ShareMessage) intent.getSerializableExtra(Extras.EXTRA_SHARE_MESSAGE)).getMessage().toString();
            } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                text = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
            if (text != null) {

                int shareType = intent.getIntExtra(EXTRA_SHARE_TYPE, EXTRA_SHARE_TWITTER);

                if (shareType == EXTRA_SHARE_TWITTER) {
                    isTwitter = true;
                }

                edittext = (EditText) findViewById(R.id.edittext);
                edittext.setText(text);
                edittext.addTextChangedListener(textWatcher);

                tvStatus = (TextView) findViewById(R.id.tvStatus);

                defaultColor = getResources().getColor(R.color.share_status_default);
                twitterColor = getResources().getColor(R.color.share_status_twitter);

                TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
                tvTitle.setText(isTwitter ? R.string.share_twitter : R.string.share_facebook);

                btnShare = (Button) findViewById(R.id.btnShare);
                btnShare.setOnClickListener(listener);

                updateTextLength();
            }
        } else {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show();
        }
    }

    private void updateTextLength() {
        length = edittext.length();

        if (isTwitter) {
            tvStatus.setText(getString(R.string.share_text_length) + length + "/140");
            if (length > 140) {
                tvStatus.setTextColor(twitterColor);
                btnShare.setEnabled(false);
            } else {
                tvStatus.setTextColor(defaultColor);
                btnShare.setEnabled(true);
            }
        } else {
            // otherwise it is facebook
            tvStatus.setText(getString(R.string.share_text_length) + length + "/420");
            if (length > 420) {
                tvStatus.setTextColor(twitterColor);
                btnShare.setEnabled(false);
            } else {
                tvStatus.setTextColor(defaultColor);
                btnShare.setEnabled(true);
            }
        }
    }

}
