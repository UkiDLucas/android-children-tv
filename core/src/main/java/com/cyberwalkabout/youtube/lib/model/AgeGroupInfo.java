package com.cyberwalkabout.youtube.lib.model;

import android.widget.CheckBox;

public class AgeGroupInfo {

    private CheckBox checkBox;
    private AgeGroup agegroup;

    public AgeGroupInfo(AgeGroup ageGroup, CheckBox checkBox) {
        this.agegroup = ageGroup;
        this.checkBox = checkBox;
    }

    public AgeGroup getAgegroup() {
        return agegroup;
    }

    public void setAgegroup(AgeGroup agegroup) {
        this.agegroup = agegroup;
    }

    public boolean isSelected() {
        return checkBox.isChecked();
    }

    public void setSelected(boolean selected) {
        this.checkBox.setChecked(selected);
    }

    public int getCheckBoxId() {
        return checkBox.getId();
    }

    public String getAgeGroupStr() {
        return agegroup.getDisplayName();
    }

}
