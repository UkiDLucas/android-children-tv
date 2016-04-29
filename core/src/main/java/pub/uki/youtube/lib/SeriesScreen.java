package com.cyberwalkabout.youtube.lib;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.cyberwalkabout.youtube.lib.fragments.SeriesFragment;
import com.cyberwalkabout.youtube.lib.model.LocalVideo;

/**
 * @author Maria Dzyokh
 */
public class SeriesScreen extends AbstractVideosActivity {

    private int seriesId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videos_screen);

        setupNavigationBar();
        loadData();
        getSupportFragmentManager().beginTransaction().replace(R.id.videos_fragment_container, SeriesFragment.newInstance(seriesId)).commit();
    }

    private void loadData() {
        seriesId = getIntent().getIntExtra(LocalVideo.SERIES_ID, 0);
    }

    private void setupNavigationBar() {
        findViewById(R.id.right_btn_container).setVisibility(View.INVISIBLE);
        ImageButton btnBack = (ImageButton) findViewById(R.id.left_btn);
        btnBack.setImageResource(R.drawable.ic_action_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
