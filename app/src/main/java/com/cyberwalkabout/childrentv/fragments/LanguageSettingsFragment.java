package com.cyberwalkabout.childrentv.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chicagoandroid.childrentv.R;
import com.cyberwalkabout.childrentv.activities.AboutUs;
import com.cyberwalkabout.childrentv.util.AppSettings;
import com.cyberwalkabout.childrentv.activities.AllVideosScreen;
import com.cyberwalkabout.childrentv.data.db.DbHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author Maria Dzyokh
 */
public class LanguageSettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.language_filter, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            new AsyncTask<List<String>, Void, LanguagesAdapter>() {
                @Override
                protected LanguagesAdapter doInBackground(List<String>... args) {
                    List<String> locales = new ArrayList<String>();
                    HashMap<String, String> localesMap = new HashMap<String, String>();

                    List<String> languages = args[0];
                    languages.add(0, "All Languages");
                    locales.add(0, "All Languages");
                    localesMap.put("All Languages", "All Languages");

                    for (int i = 1; i < languages.size(); i++) {
                        String locale = new Locale(languages.get(i)).getDisplayLanguage();
                        locales.add(i, locale);
                        localesMap.put(locale, languages.get(i));
                    }

                    Collections.sort(locales, new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            return s1.compareToIgnoreCase(s2);
                        }
                    });

                    return getActivity() != null ? new LanguagesAdapter(getActivity(), languages, locales, localesMap) : null;
                }

                @Override
                protected void onPostExecute(LanguagesAdapter languagesAdapter) {
                    if (languagesAdapter != null && getListView() != null) {
                        getListView().setAdapter(languagesAdapter);
                    }
                }
            }.execute(DbHelper.get(getActivity()).getLanguages());
        }
    }

    public ListView getListView() {
        return getView() != null ? (ListView) getView().findViewById(R.id.languages_list) : null;
    }

    private class LanguagesAdapter extends BaseAdapter {

        private List<String> languages;
        private List<String> locales;
        private HashMap<String, String> localesMap;
        private LayoutInflater li;
        private AppSettings appSettings;
        private Context ctx;

        public LanguagesAdapter(Context ctx, List<String> languages, List<String> locales, HashMap<String, String> localesMap) {
            this.ctx = ctx;
            this.localesMap = localesMap;
            this.locales = locales;
            this.languages = languages;
            this.li = LayoutInflater.from(ctx);
            this.appSettings = new AppSettings(ctx);
        }

        @Override
        public int getCount() {
            return languages.size() + 1;
        }

        @Override
        public String getItem(int position) {
            return languages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (position == getCount() - 1) {
                convertView = li.inflate(R.layout.btn_info, null);
            } else {
                convertView = li.inflate(R.layout.language_list_item, null);
            }
            if (position == getCount() - 1) {
                convertView.findViewById(R.id.btn_info).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AboutUs.class));
                    }
                });
            } else {
                ((TextView) convertView.findViewById(R.id.language)).setText(locales.get(position));
                String language = localesMap.get(locales.get(position));
                ((ImageView) convertView.findViewById(R.id.language_icon)).setImageResource(AppSettings.getLanguageIconResId(ctx, language, appSettings.isLanguageSelected(language)));

                CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
                checkBox.setChecked(appSettings.isLanguageSelected(language));

                convertView.findViewById(R.id.cover).setTag(position);
                convertView.findViewById(R.id.cover).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = Integer.parseInt(view.getTag().toString());
                        String language = localesMap.get(locales.get(position));
                        boolean checked = appSettings.isLanguageSelected(language);
                        checked = !checked;
                        if (!checked && !isAnyElseLanguageChecked(language)) {
                            ((CheckBox) ((RelativeLayout) view.getParent()).findViewById(R.id.checkbox)).setChecked(true);
                            Toast.makeText(ctx, "At least one language needs to be selected.", Toast.LENGTH_SHORT).show();
                        } else {
                            appSettings.updateLanguage(language, checked);
                            updateFirstSelectedLanguage();
                            if (position == 0) {
                                if (checked) {
                                    checkAllLanguages();
                                } else {
                                    uncheckAllLanguages();
                                }
                            } else {
                                if (!checked) {
                                    uncheckAllOption();
                                }
                                if (checked && isAllElseLanguagesChecked(language)) {
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

        private void checkAllLanguages() {
            for (String language : languages) {
                appSettings.updateLanguage(language, true);
            }
        }

        private void uncheckAllLanguages() {
            for (int i = 0; i < languages.size(); i++) {
                appSettings.updateLanguage(languages.get(i), languages.get(i).equals(Locale.UK.getLanguage()) ? true : false);
            }
        }

        private boolean isAnyElseLanguageChecked(String currentLanguage) {
            boolean isAnyElseLanguageChecked = false;
            for (String language : languages) {
                if (language.equals(currentLanguage)) {
                    continue;
                }
                if (appSettings.isLanguageSelected(language)) {
                    isAnyElseLanguageChecked = true;
                }
            }
            return isAnyElseLanguageChecked;
        }

        private boolean isAllElseLanguagesChecked(String currentLanguage) {
            boolean isAllelseLanguagesChecked = true;
            for (int i = 1; i < languages.size(); i++) {
                if (languages.get(i).equals(currentLanguage)) {
                    continue;
                }
                if (!appSettings.isLanguageSelected(languages.get(i))) {
                    isAllelseLanguagesChecked = false;
                }
            }
            return isAllelseLanguagesChecked;
        }

        private void checkAllOption() {
            appSettings.updateLanguage(languages.get(0), true);
        }

        private void uncheckAllOption() {
            appSettings.updateLanguage(languages.get(0), false);
        }

        private void updateFirstSelectedLanguage() {
            for (int i = 1; i < locales.size(); i++) {
                String language = localesMap.get(locales.get(i));
                if (appSettings.isLanguageSelected(language)) {
                    appSettings.setFirstSelectedLanguage(language);
                    break;
                }
            }
        }
    }

}
