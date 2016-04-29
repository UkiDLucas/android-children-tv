package com.cyberwalkabout.common.endlessadapter;

/**
 * @author Maria Dzyokh
 */
public interface PageListener<T> {

    void onNewPage(PageInfo<T> page);
}
