package com.cyberwalkabout.common.endlessadapter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Maria Dzyokh
 */
public class PaginationInfo implements Parcelable {

    protected int page;
    protected boolean isLast;
    protected String nextPageToken;

    public PaginationInfo() {}

    public PaginationInfo(int page, boolean isLast, String nextPageToken) {
        this.page = page;
        this.isLast = isLast;
        this.nextPageToken = nextPageToken;
    }

    public static Creator<PaginationInfo> CREATOR = new Creator<PaginationInfo>() {
        @Override
        public PaginationInfo createFromParcel(Parcel parcel) {
            PaginationInfo pagination = new PaginationInfo();
            pagination.setPage(parcel.readInt());
            pagination.setLast(parcel.readInt() == 1 ? true : false);
            pagination.setNextPageToken(parcel.readString());
            return pagination;
        }

        @Override
        public PaginationInfo[] newArray(int size) {
            return new PaginationInfo[size];
        }
    };


    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(page);
        parcel.writeInt(isLast ? 1 : 0);
        parcel.writeString(nextPageToken);
    }

}
