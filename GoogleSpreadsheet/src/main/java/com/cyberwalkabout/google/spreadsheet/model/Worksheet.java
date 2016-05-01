package com.cyberwalkabout.google.spreadsheet.model;

public class Worksheet extends GenericObject {
    private int columnCount;
    private int rowCount;

    public Worksheet() {
    }

    public String getShortId() {
        return getId().substring(getId().length() - 3, getId().length());
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        Worksheet worksheet = (Worksheet) o;

        return true;
    }

    @Override
    public String toString() {
        return "Worksheet{" +
                "columnCount=" + columnCount +
                ", rowCount=" + rowCount + '\'' +
                "} " + super.toString();
    }


}
