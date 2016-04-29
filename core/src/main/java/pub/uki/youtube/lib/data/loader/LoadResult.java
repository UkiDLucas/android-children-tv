package com.cyberwalkabout.youtube.lib.data.loader;

/**
 * @author Andrii Kovalov
 */
public class LoadResult {
    private boolean success;
    private int newRecordsCount;
    private String failReason;

    public LoadResult(boolean success, int newRecordsCount) {
        this.success = success;
        this.newRecordsCount = newRecordsCount;
    }

    public LoadResult(boolean success, int newRecordsCount, String failReason) {
        this.success = success;
        this.newRecordsCount = newRecordsCount;
        this.failReason = failReason;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getNewRecordsCount() {
        return newRecordsCount;
    }

    public void setNewRecordsCount(int newRecordsCount) {
        this.newRecordsCount = newRecordsCount;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
