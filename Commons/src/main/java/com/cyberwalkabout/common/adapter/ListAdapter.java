package com.cyberwalkabout.common.adapter;

import java.util.List;

import android.widget.BaseAdapter;

public abstract class ListAdapter<T> extends BaseAdapter {
	private List<T> data;

	public void setData(List<T> data) {
		this.data = data;
		notifyDataSetChanged();
	}

	public int getCount() {
		return data == null ? 0 : data.size();
	}

	public T getItem(int i) {
		return data == null ? null : data.get(i);
	}

	public long getItemId(int i) {
		return i;
	}
}
