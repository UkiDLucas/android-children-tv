package com.cyberwalkabout.youtube.lib.model;

/**
 * @author Maria Dzyokh
 */
public enum AgeGroup {

    ANY("Suits Any Age"), AGE_2("2+"), AGE_4("4+"), AGE_6("6+"), AGE_8("8+"), ALL("All");

    private final String displayName;

    private AgeGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
