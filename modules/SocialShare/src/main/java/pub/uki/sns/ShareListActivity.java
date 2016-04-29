package pub.uki.sns;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import com.chicagoandroid.sns.util.Extras;
import com.chicagoandroid.sns.util.ShareMessage;
import com.chicagoandroid.sns.util.ShareMessageBundle;
import com.chicagoandroid.sns.util.ShareMessageType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareListActivity extends Activity {

    private static final String NAME = "NAME";

    private static final String GMAIL = "com.google.android.gm";
    private static final String GOOGLE_PLUS = "com.google.android.apps.plus";
    private static final String FACEBOOK_NATIVE = "com.facebook.katana";

    private static final String[] TESTED_LIST = new String[]{GMAIL, GOOGLE_PLUS, "com.android.mms", "com.levelup.touiteur", "com.tweetdeck.app", "com.thedeck.android.app", "com.htc.friendstream", "jp.r246.twicca", "com.threebanana.notes", FACEBOOK_NATIVE};

    private ExpandableListView elv;

    private MyExpandableListAdapter adapter;

    private PackageManager pm;

    private List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();

    private List<List<ItemType>> childData = new ArrayList<List<ItemType>>();

    private Intent intent;

    private ShareMessageBundle messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.share_list);

        final Intent savedIntent = getIntent();

        messages = (ShareMessageBundle) getIntent().getSerializableExtra(Extras.EXTRA_MESSAGE_BUNDLE);

        intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        pm = getPackageManager();

        Resources r = getResources();
        List<ResolveInfo> listInfo = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        addNewGroup(getString(R.string.share_recommended));

        List<ItemType> testedApps = new ArrayList<ItemType>();
        List<ItemType> untestedItems = new ArrayList<ItemType>();
        List<ItemType> tempItems = new ArrayList<ItemType>();

        childData.add(testedApps);

        ItemType itemFacebook = new ItemType();
        itemFacebook.name = getString(R.string.share_facebook_via_web);
        itemFacebook.icon = r.getDrawable(R.drawable.fb);
        itemFacebook.intent = new Intent(intent);

        if (messages != null) {
            ShareMessage message = messages.getMessage(ShareMessageType.FACEBOOK);
            itemFacebook.intent.putExtra(Extras.EXTRA_SHARE_MESSAGE, message);
            itemFacebook.intent.setType(message.getMimeType());
        } else {
            itemFacebook.intent.putExtra(Intent.EXTRA_TEXT, savedIntent.getStringExtra(Intent.EXTRA_TEXT));
        }

        itemFacebook.intent.setClass(this, ShareActivity.class);
        itemFacebook.intent.putExtra(ShareActivity.EXTRA_SHARE_TYPE, ShareActivity.EXTRA_SHARE_FACEBOOK);
        testedApps.add(itemFacebook);

        ItemType itemTwitter = new ItemType();
        itemTwitter.name = getString(R.string.share_twitter_via_web);
        itemTwitter.icon = r.getDrawable(R.drawable.tw);
        itemTwitter.intent = new Intent(intent);
        itemTwitter.intent.setClass(this, ShareActivity.class);
        if (messages != null) {
            ShareMessage message = messages.getMessage(ShareMessageType.TWITTER);
            itemTwitter.intent.putExtra(Intent.EXTRA_TEXT, message.getMessage());
            itemTwitter.intent.setType(message.getMimeType());
        } else {
            itemTwitter.intent.putExtra(Intent.EXTRA_TEXT, savedIntent.getStringExtra(Intent.EXTRA_TEXT));
        }
        itemTwitter.intent.putExtra(ShareActivity.EXTRA_SHARE_TYPE, ShareActivity.EXTRA_SHARE_TWITTER);
        testedApps.add(itemTwitter);

        for (ResolveInfo info : listInfo) {
            ItemType item = new ItemType();
            CharSequence label = pm.getApplicationLabel(info.activityInfo.applicationInfo);
            String labelName = label == null ? "" : label.toString();
            item.name = labelName;
            item.icon = pm.getApplicationIcon(info.activityInfo.applicationInfo);
            item.info = info;
            item.intent = new Intent(intent);
            if (messages != null) {
                item.intent.putExtra(Intent.EXTRA_TEXT, messages.getMessage(ShareMessageType.DEFAULT).getMessage());
            } else {
                item.intent.putExtra(Intent.EXTRA_TEXT, savedIntent.getStringExtra(Intent.EXTRA_TEXT));
            }
            item.intent.setPackage(item.info.activityInfo.packageName);

            if (GMAIL.equals(info.activityInfo.packageName)) {
                item.intent.putExtra(Intent.EXTRA_SUBJECT, savedIntent.getStringExtra(Intent.EXTRA_SUBJECT));
                if (messages != null && messages.containsMessage(ShareMessageType.EMAIL)) {
                    ShareMessage message = messages.getMessage(ShareMessageType.EMAIL);
                    CharSequence content = message.getMessage();
                    if ("text/html".equals(message.getMimeType())) {
                        content = Html.fromHtml(content.toString());
                    }
                    item.intent.putExtra(Intent.EXTRA_TEXT, content);
                    item.intent.setType(message.getMimeType());
                } else {
                    item.intent.putExtra(Intent.EXTRA_TEXT, savedIntent.getStringExtra(Intent.EXTRA_TEXT));
                    item.intent.putExtra(Intent.EXTRA_STREAM, savedIntent.getParcelableExtra(Intent.EXTRA_STREAM));
                }
            }

            if (FACEBOOK_NATIVE.equals(info.activityInfo.packageName)) {
                if (messages != null && messages.containsMessage(ShareMessageType.FACEBOOK)) {
                    item.intent.putExtra(Intent.EXTRA_TEXT, messages.getMessage(ShareMessageType.FACEBOOK).getLink());
                }
            }

            if (isTested(info.activityInfo.packageName)) {
                tempItems.add(item);
            } else {
                untestedItems.add(item);
            }
        }

        for (int i = 0; i < TESTED_LIST.length; i++) {
            for (ItemType item : tempItems) {
                if (item.intent.getPackage().equals(TESTED_LIST[i])) {
                    testedApps.add(item);
                }
            }
        }

        if (untestedItems.size() != 0) {
            addNewGroup(getString(R.string.share_you_may_also_try_with));
            childData.add(untestedItems);
        }

        adapter = new MyExpandableListAdapter(groupData, childData);

        elv = (ExpandableListView) findViewById(R.id.elist);
        elv.setAdapter(adapter);
        elv.setOnChildClickListener(adapter);
        expandGroup(true);

        elv.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                elv.expandGroup(groupPosition);
            }
        });
    }

    private void addNewGroup(String name) {
        Map<String, String> curGroupMap = new HashMap<String, String>();
        curGroupMap.put(NAME, name);

        groupData.add(curGroupMap);
    }

    private void expandGroup(boolean expand) {
        ExpandableListAdapter adapter = elv.getExpandableListAdapter();
        if (adapter != null) for (int i = 0; i < adapter.getGroupCount(); i++) {
            if (expand) elv.expandGroup(i);
            else elv.collapseGroup(i);
        }
    }

    private class MyExpandableListAdapter extends BaseExpandableListAdapter implements OnChildClickListener {
        private List<Map<String, String>> myGroupData;

        private List<List<ItemType>> myChildData;

        private MyExpandableListAdapter(List<Map<String, String>> groupData, List<List<ItemType>> childData) {
            myGroupData = groupData;
            myChildData = childData;
        }

        public ItemType getChild(int groupPosition, int childPosition) {
            return myChildData.get(groupPosition).get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return myChildData.get(groupPosition).size();
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.share_list_item, parent, false);
            }

            final ItemType item = getChild(groupPosition, childPosition);

            TextView textView = (TextView) convertView.findViewById(R.id.tvItem);
            textView.setText(item.name);

            ImageView icon = (ImageView) convertView.findViewById(R.id.ivIcon);
            icon.setImageDrawable(item.icon);

            return convertView;
        }

        public Map<String, String> getGroup(int groupPosition) {
            return myGroupData.get(groupPosition);
        }

        public int getGroupCount() {
            return myGroupData.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.share_list_group, null);
            }

            TextView textView = (TextView) convertView;
            textView.setText(getGroup(groupPosition).get(NAME));
            return convertView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            ItemType item = getChild(groupPosition, childPosition);
            startActivity(item.intent);
            finish();

            return false;
        }
    }

    private class ItemType {
        String name;

        Drawable icon;

        ResolveInfo info;

        Intent intent;
    }

    private boolean isTested(String toCheck) {
        for (String pkg : TESTED_LIST) {
            if (pkg.equals(toCheck)) {
                return true;
            }
        }

        return false;
    }

}
