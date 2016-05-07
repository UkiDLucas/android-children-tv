package com.cyberwalkabout.childrentv.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.bugsense.trace.BugSenseHandler;
import com.chicagoandroid.childrentv.R;
import com.cyberwalkabout.childrentv.fragments.TutorialFragment;
import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.IconPagerAdapter;

/**
 * @author Maria Dzyokh
 */
public class TutorialScreen extends AbstractActivity {

    public static final String EXTRA_CLASS_NAME = "EXTRA_CLASS_NAME";

    private String[] tutorial_descriptions;
    private String[] tutorial_images;

    private CustomViewPager pager;
    private Button btnSkip;

    private String launcherClassName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense_key));
        setContentView(R.layout.tutorial_screen);

        launcherClassName = getIntent().getStringExtra(EXTRA_CLASS_NAME);

        tutorial_images = getResources().getStringArray(R.array.tutorial_images);
        tutorial_descriptions = getResources().getStringArray(R.array.tutorial_descriptions);

        pager = (CustomViewPager) findViewById(R.id.pager);
        pager.setAdapter(new TutorialImagesAdapter(getSupportFragmentManager()));
        pager.setOnSwipeOutListener(new CustomViewPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {

            }

            @Override
            public void onSwipeOutAtEnd() {
                if (launcherClassName.equals(SplashScreen.class.getSimpleName())) {
                    startActivity(new Intent(TutorialScreen.this, AllVideosScreen.class));
                }
                TutorialScreen.this.finish();
            }
        });

        IconPageIndicator indicator = (IconPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == pager.getAdapter().getCount()-1) {
                    btnSkip.setVisibility(View.VISIBLE);
                    btnSkip.setText("Done");
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        btnSkip = (Button) findViewById(R.id.btn_skip);
        if (launcherClassName.equals(SplashScreen.class.getSimpleName())) {
            btnSkip.setVisibility(View.GONE);
        }
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (launcherClassName.equals(SplashScreen.class.getSimpleName())) {
                    startActivity(new Intent(TutorialScreen.this, AllVideosScreen.class));
                }
                TutorialScreen.this.finish();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int selectedPage = pager.getCurrentItem();
        pager.setAdapter(new TutorialImagesAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(selectedPage, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BugSenseHandler.closeSession(this);
    }

    public class TutorialImagesAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

        public TutorialImagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return tutorial_images.length;
        }

        @Override
        public int getIconResId(int index) {
            return R.drawable.pager_indicator_selector;
        }

        @Override
        public Fragment getItem(int position) {
            int resourceId = getResources().getIdentifier(tutorial_images[position], "build/intermediates/exploded-aar/com.android.support/mediarouter-v7/23.2.1/res/drawable",
                    getPackageName());
            return TutorialFragment.newInstance(resourceId, tutorial_descriptions[position]);
        }
    }

}
