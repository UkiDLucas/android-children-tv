package com.cyberwalkabout.childrentv.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chicagoandroid.childrentv.R;


/**
 * @author Maria Dzyokh
 */
public class TutorialFragment extends Fragment {

    private static final String KEY_IMAGE_RES_ID = "com.cyberwalkabout.localguide.fragment.KEY_IMAGE_RES_ID";
    private static final String KEY_DESCRIPTION = "com.cyberwalkabout.localguide.fragment.KEY_DESCRIPTION";

    public static TutorialFragment newInstance(int imageResourcedId, String description) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_IMAGE_RES_ID, imageResourcedId);
        args.putString(KEY_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    private int imageResId;
    private String description;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            imageResId = getArguments().getInt(KEY_IMAGE_RES_ID);
            description = getArguments().getString(KEY_DESCRIPTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.tutorial_fragment, null);
        ((ImageView) layout.findViewById(R.id.tutorial_image)).setImageResource(imageResId);
        ((TextView) layout.findViewById(R.id.txt_description)).setText(description);
        return layout;
    }

}
