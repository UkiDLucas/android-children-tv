package com.cyberwalkabout.youtube.lib.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.cyberwalkabout.childrentv.shared.model.AgeGroupConst;
import com.cyberwalkabout.youtube.lib.AboutUs;
import com.cyberwalkabout.youtube.lib.AllVideosScreen;
import com.cyberwalkabout.youtube.lib.util.AppSettings;
import com.cyberwalkabout.youtube.lib.R;

/**
 * @author Maria Dzyokh
 */
public class AgeSettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.age_filter, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null) {
            getListView().setAdapter(new AgeGroupsAdapter(getActivity()));
        }
    }

    public ListView getListView() {
        return (ListView) getView().findViewById(R.id.age_groups_list);
    }

    private class AgeGroupsAdapter extends BaseAdapter {

        private String[] ageGroups = AgeGroupConst.AGE_GROUPS;
        private LayoutInflater li;
        private AppSettings appSettings;
        private Context ctx;

        public AgeGroupsAdapter(Context ctx) {
            this.li = LayoutInflater.from(ctx);
            this.appSettings = new AppSettings(ctx);
            this.ctx = ctx;
        }

        @Override
        public int getCount() {
            return ageGroups.length + 1;
        }

        @Override
        public String getItem(int position) {
            return ageGroups[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (position == 0) {
                convertView = li.inflate(R.layout.age_group_list_item_text, null);
            } else if (position == getCount() - 1) {
                convertView = li.inflate(R.layout.btn_info, null);
            } else {
                convertView = li.inflate(R.layout.age_group_list_item_image, null);
            }
            if (position == 0) {
                ((TextView) convertView.findViewById(R.id.text)).setText(getItem(position));
            } else if (position == getCount() - 1) {
                convertView.findViewById(R.id.btn_info).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AboutUs.class));
                    }
                });
            } else {
                ((ImageView) convertView.findViewById(R.id.image)).setImageResource(AppSettings.getAgeGroupIconId(getItem(position)));
            }
            if (position != getCount() - 1) {
                CheckBox checkBox = ((CheckBox) convertView.findViewById(R.id.checkbox));
                checkBox.setChecked(appSettings.isAgeGroupSelected(getItem(position)));

                convertView.findViewById(R.id.cover).setTag(position);
                convertView.findViewById(R.id.cover).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = Integer.parseInt(view.getTag().toString());
                        boolean checked = appSettings.isAgeGroupSelected(getItem(position));
                        checked = !checked;
                        if (!checked && !isAnyElseAgeGroupChecked(getItem(position))) {
                            ((CheckBox) ((RelativeLayout) view.getParent()).findViewById(R.id.checkbox)).setChecked(true);
                            Toast.makeText(ctx, "At least one age group needs to be selected.", Toast.LENGTH_SHORT).show();
                        } else {
                            appSettings.updateAgeGroup(getItem(position), checked);
                            if (position == 0) {
                                if (checked) {
                                    checkAllAgeGroups();
                                } else {
                                    uncheckAllAgeGroups();
                                }
                            } else {
                                if (!checked) {
                                    uncheckAppOption();
                                }
                                if (checked && isAllElseAgeGroupChecked(getItem(position))) {
                                    checkAllOption();
                                }
                            }
                            notifyDataSetChanged();
                            LocalBroadcastManager.getInstance(ctx).sendBroadcast(new Intent(AllVideosScreen.FILTER_CHANGED_ACTION));
                        }
                    }
                });
            }

            return convertView;
        }

        private void checkAllAgeGroups() {
            for (String ageGroup : ageGroups) {
                appSettings.updateAgeGroup(ageGroup, true);
            }
            notifyDataSetChanged();
        }

        private boolean isAnyElseAgeGroupChecked(String currentLanguage) {
            boolean isAnyElseLanguageChecked = false;
            for (String ageGroup : ageGroups) {
                if (ageGroup.equals(currentLanguage)) {
                    continue;
                }
                if (appSettings.isAgeGroupSelected(ageGroup)) {
                    isAnyElseLanguageChecked = true;
                }
            }
            return isAnyElseLanguageChecked;
        }

        private boolean isAllElseAgeGroupChecked(String currentLanguage) {
            boolean areAllLanguagesChecked = true;
            for (int i = 1; i < ageGroups.length; i++) {
                if (ageGroups[i].equals(currentLanguage)) {
                    continue;
                }
                if (!appSettings.isAgeGroupSelected(ageGroups[i])) {
                    areAllLanguagesChecked = false;
                }
            }
            return areAllLanguagesChecked;
        }

        private void checkAllOption() {
            appSettings.updateAgeGroup(ageGroups[0], true);
        }

        private void uncheckAppOption() {
            appSettings.updateAgeGroup(ageGroups[0], false);
        }

        private void uncheckAllAgeGroups() {
            for (int i = 0; i < ageGroups.length; i++) {
                appSettings.updateAgeGroup(ageGroups[i], i == 1 ? true : false);
            }
        }
    }
}
