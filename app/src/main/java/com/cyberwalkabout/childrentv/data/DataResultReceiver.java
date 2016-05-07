package com.cyberwalkabout.childrentv.data;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class DataResultReceiver extends ResultReceiver {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILURE = 1;
    public static final int STATUS_PROGRESS = 2;

    private Receiver receiver;

    public DataResultReceiver(Handler handler) {
        super(handler);
    }

    public DataResultReceiver(Handler handler, Receiver receiver) {
        super(handler);
        this.receiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }
}
