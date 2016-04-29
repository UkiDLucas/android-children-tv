package com.cyberwalkabout.common.adapter;

import java.util.List;

/**
 * @author Andrii Kovalov
 */
public class Page<T> extends Pagination
{
	private List<T> data;

	public Page(List<T> data, Pagination pagination)
	{
		this.data = data;
		this.limit = pagination.limit;
		this.total = pagination.total;
		this.offset = pagination.offset;
	}

	public List<T> getData()
	{
		return data;
	}

	public void setData(List<T> data)
	{
		this.data = data;
	}

	public boolean isLastPage()
	{
		return data.size() + offset >= total;
	}

	public boolean hasData()
	{
		return data != null && !data.isEmpty();
	}
}
