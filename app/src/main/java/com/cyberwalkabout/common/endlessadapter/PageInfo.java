package com.cyberwalkabout.common.endlessadapter;

/**
 * @author Maria Dzyokh
 */
public class PageInfo<T> extends PaginationInfo {

    private T[] data;

    public PageInfo(T[] data, PaginationInfo pagination)
    {
        this.data = data;
        this.page = pagination.page;
        this.isLast = pagination.isLast;
        this.nextPageToken = pagination.nextPageToken;
    }

    public T[] getData()
    {
        return data;
    }

    public void setData(T[] data)
    {
        this.data = data;
    }

    public boolean isLastPage()
    {
        return this.isLast;
    }

    public boolean hasData()
    {
        return data != null && data.length>0;
    }
}
