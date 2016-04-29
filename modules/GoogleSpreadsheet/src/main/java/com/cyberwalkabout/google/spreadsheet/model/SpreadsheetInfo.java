package com.cyberwalkabout.google.spreadsheet.model;

import java.util.List;

public class SpreadsheetInfo extends GenericObject {
    private String authorName;
    private String authorEmail;
    private List<Worksheet> worksheetList;

    public SpreadsheetInfo() {
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public List<Worksheet> getWorksheetList() {
        return worksheetList;
    }

    public void setWorksheetList(List<Worksheet> workSheets) {
        this.worksheetList = workSheets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        SpreadsheetInfo spreadsheetInfo = (SpreadsheetInfo) o;

        if (worksheetList != null ? !worksheetList.equals(spreadsheetInfo.worksheetList)
                : spreadsheetInfo.worksheetList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (worksheetList != null ? worksheetList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SpreadsheetInfo{" +
                "authorName='" + authorName + '\'' +
                ", authorEmail='" + authorEmail + '\'' +
                ", worksheetList=" + worksheetList +
                "} " + super.toString();
    }


}
