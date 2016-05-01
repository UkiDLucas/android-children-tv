package com.cyberwalkabout.google.spreadsheet.model;

import java.util.HashMap;
import java.util.Map;

public class WorksheetRow extends GenericObject {
    private int index;
    private String content;
    private Map<String, String> data;

    public WorksheetRow() {
    }

    public WorksheetRow(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public void putInData(String key, String value) {
        if (this.data == null) {
            this.data = new HashMap<String, String>();
        }

        this.data.put(key, value);
    }

    public void appendRawContent(String rawContent) {
        if (this.content == null) {
            this.content = rawContent;
        } else {
            this.content += rawContent;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        WorksheetRow worksheetRow = (WorksheetRow) o;

        if (index != worksheetRow.index) return false;
        if (content != null ? !content.equals(worksheetRow.content) : worksheetRow.content != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + index;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WorksheetRow{" +
                "index=" + index +
                ", content='" + content + '\'' +
                ", data=" + data +
                "} " + super.toString();
    }


}
