package com.cyberwalkabout.common.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.concurrent.atomic.AtomicInteger;

public class PaginatedAdapter<I> extends BaseAdapter implements PaginationListener<I> {
    public static final int MODE_LOADING = 0x1;
    public static final int MODE_READY = 0x2;

    protected Context ctx;

    private int pendingResource = -1;
    private View pendingView = null;

    private AtomicInteger mode = new AtomicInteger(MODE_LOADING);

    private DataSource dataSource;

    private int pageSize = 10;

    private ArrayAdapter<I> arrayAdapter;

    private PaginatedAdapter(Context ctx, int pendingResource, DataSource dataSource, int pageSize, ArrayAdapter<I> adapter) {
        this.ctx = ctx;
        this.pendingResource = pendingResource;
        this.dataSource = dataSource;
        this.pageSize = pageSize;
        this.arrayAdapter = adapter;
        this.arrayAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                notifyDataSetChanged();
            }

            public void onInvalidated() {
                notifyDataSetInvalidated();
            }
        });
    }

    @Override
    public void onNewPage(Page<I> page) {
        if (Build.VERSION.SDK_INT >= 11) {
            arrayAdapter.addAll(page.getData());
        } else {
            if (page.hasData()) {
                for (I item : page.getData()) {
                    arrayAdapter.add(item);
                }
            }
        }
        if (page.isLastPage()) {
            mode.set(MODE_READY);
        }
    }

    @Override
    public int getCount() {
        if (mode.get() == MODE_LOADING) {
            return (arrayAdapter.getCount() + 1);
        }
        return arrayAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return arrayAdapter.getItem(position);
    }

    @Override
    public long getItemId(int id) {
        return arrayAdapter.getItemId(id);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == arrayAdapter.getCount()) {
            return (IGNORE_ITEM_VIEW_TYPE);
        }

        return arrayAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return arrayAdapter.getViewTypeCount() + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == arrayAdapter.getCount() && mode.get() == MODE_LOADING) {
            if (pendingView == null) {
                pendingView = getPendingView(parent);
            }
            mode.set(MODE_LOADING);
            dataSource.requestData(pageSize, arrayAdapter.getCount(), this);
            return pendingView;
        }
        return arrayAdapter.getView(position, convertView, parent);
    }

    protected View getPendingView(ViewGroup parent) {
        if (ctx != null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(pendingResource, parent, false);
        }

        throw new RuntimeException("You must either override getPendingView() or supply a pending View resource via the constructor");
    }

    public interface DataSource {
        void requestData(int limit, int offset, PaginationListener listener);
    }

    public static class Builder<I> {
        private Context ctx;

        private int pendingResource;
        private DataSource dataSource;
        private int pageSize;
        private ArrayAdapter<I> adapter;

        public Builder(Context ctx) {
            this.ctx = ctx;
        }

        public Builder<I> pendingResource(int pendingResource) {
            this.pendingResource = pendingResource;
            return this;
        }

        public Builder<I> dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Builder<I> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<I> adapter(ArrayAdapter<I> adapter) {
            this.adapter = adapter;
            return this;
        }

        public PaginatedAdapter<I> build() {
            return new PaginatedAdapter<I>(ctx, pendingResource, dataSource, pageSize, adapter);
        }
    }
}