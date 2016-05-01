package com.cyberwalkabout.youtube.lib;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberwalkabout.youtube.lib.youtube.YoutubeUtils;
import com.cyberwalkabout.childrentv.shared.model.AgeGroupConst;
import com.cyberwalkabout.youtube.lib.analytics.FlurryAnalytics;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;


public class SubmitVideoActivity extends AbstractActivity {
    private static final String TAG = SubmitVideoActivity.class.getSimpleName();
    public static final String HTTPS_YOUTU_BE = "https://youtu.be/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_video);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        if (intent == null) {
            finish();
        } else {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(text)) {
                if (text.startsWith(HTTPS_YOUTU_BE)) {
                    SubmitVideoFragment fragment = new SubmitVideoFragment();
                    fragment.setArguments(intent.getExtras());
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, SubmitVideoFragment.class.getSimpleName()).commit();
                } else {
                    Toast.makeText(SubmitVideoActivity.this, "Video must be shared from Youtube app!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(SubmitVideoActivity.this, "No url shared!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_submit_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SubmitVideoFragment extends Fragment implements View.OnClickListener {

        public static final String DEFAULT_LANGUAGE = "en";
        private TextView titleTextView;
        private ImageView imageView;

        private String youtubeId;
        private Spinner ageSpinner;

        private AutoCompleteTextView languageTextView;

        public SubmitVideoFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_submit_video, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            imageView = (ImageView) getView().findViewById(R.id.image);
            titleTextView = (TextView) getView().findViewById(R.id.title);

            ageSpinner = (Spinner) getView().findViewById(R.id.ageSpinner);

            languageTextView = (AutoCompleteTextView) getView().findViewById(R.id.languageTextView);
            languageTextView.setCompletionHint("Please enter language");
            languageTextView.setHint("Any language");
            languageTextView.setThreshold(1);

            List<String> allLanguages = getAllLanguages();
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, allLanguages);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            languageTextView.setAdapter(dataAdapter);

            dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new String[]{AgeGroupConst.AGE_0, AgeGroupConst.AGE_2, AgeGroupConst.AGE_4, AgeGroupConst.AGE_6, AgeGroupConst.AGE_8});
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ageSpinner.setAdapter(dataAdapter);
            ageSpinner.setSelection(0);

            getView().findViewById(R.id.submit_video).setOnClickListener(this);

            Bundle arguments = getArguments();
            String title = arguments.getString(Intent.EXTRA_SUBJECT);
            String url = arguments.getString(Intent.EXTRA_TEXT);

            title = cleanupTitle(title);
            youtubeId = extractIdFromYoutubeUrl(url);

            titleTextView.setText(title);

            displayImage();
        }

        private void displayImage() {
            final DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(false)
                    .cacheOnDisk(false).build();

            ImageLoader.getInstance().displayImage(YoutubeUtils.getMaxQualityImageUrl(youtubeId), imageView, displayOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    ImageLoader.getInstance().displayImage(YoutubeUtils.getHighQualityImageUrl(youtubeId), imageView, displayOptions);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }

        @NonNull
        private String cleanupTitle(String title) {
            title = title.substring(title.indexOf("\"") + 1, title.lastIndexOf("\""));
            return title;
        }

        private String extractIdFromYoutubeUrl(String url) {
            return url.replace(HTTPS_YOUTU_BE, "");
        }

        @Override
        public void onClick(View v) {
            String language = languageTextView.getText().toString();

            Locale locale = getLocaleCodeForDisplayName(language);

            if (locale != null) {
                language = locale.getLanguage();
            } else {
                language = "any";
            }

            FlurryAnalytics.getInstance().submitYoutubeVideo(youtubeId, ageSpinner.getSelectedItem().toString(), language);
            getActivity().finish();
            Toast.makeText(getActivity(), R.string.thank_you, Toast.LENGTH_SHORT).show();
        }

        public List<String> getAllLanguages() {
            Set<String> languages = new TreeSet<>();
            for (Locale locale : Locale.getAvailableLocales()) {
                languages.add(locale.getDisplayLanguage());
            }
            return new ArrayList<>(languages);
        }

        public Locale getLocaleCodeForDisplayName(String displayName) {
            for (Locale locale : Locale.getAvailableLocales()) {
                if (displayName.equals(locale.getDisplayLanguage())) {
                    return locale;

                }
            }
            return null;
        }
    }
}
