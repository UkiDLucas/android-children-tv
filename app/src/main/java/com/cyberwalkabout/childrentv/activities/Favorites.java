package com.cyberwalkabout.childrentv.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.chicagoandroid.childrentv.R;
import com.cyberwalkabout.childrentv.fragments.FavoriteVideosFragment;


public class Favorites extends AbstractActivity {

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videos_screen);

        setupNavigationBar();
        getSupportFragmentManager().beginTransaction().replace(R.id.videos_fragment_container, new FavoriteVideosFragment()).commit();
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
