package com.cyberwalkabout.common.adapter;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrii Kovalov
 */
public class Pagination implements Parcelable {
    protected int total;
    protected int limit;
    protected int offset;

    public static Creator<Pagination> CREATOR = new Creator<Pagination>() {
        @Override
        public Pagination createFromParcel(Parcel parcel) {
            Pagination pagination = new Pagination();
            pagination.setTotal(parcel.readInt());
            pagination.setLimit(parcel.readInt());
            pagination.setOffset(parcel.readInt());
            return pagination;
        }

        @Override
        public Pagination[] newArray(int size) {
            return new Pagination[size];
        }
    };

    public static Pagination parse(JSONObject json) throws JSONException {
        Pagination pagination = new Pagination();
        pagination.setTotal(json.getInt("total"));
        pagination.setLimit(json.getInt("limit"));
        pagination.setOffset(json.getInt("offset"));
        return pagination;
    }

    public boolean isLastPage() {
        return offset >= total || limit >= total;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(total);
        parcel.writeInt(limit);
        parcel.writeInt(offset);
    }
}
