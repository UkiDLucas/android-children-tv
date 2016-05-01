package com.cyberwalkabout.social;

import org.json.JSONObject;

public class FacebookError extends Throwable {

    private int mErrorCode = 0;
    private String mErrorType;
    private String mErrorMessage;

    public FacebookError(String message) {
        super(message);
    }

    public FacebookError(JSONObject error) {
        super(error.toString());
        mErrorCode = error.optInt("code");
        mErrorType = error.optString("type");
        setErrorMessage(error.optString("messge"));
    }

    public FacebookError(String message, String type, int code) {
        super(message);
        mErrorType = type;
        mErrorCode = code;
        mErrorMessage = message;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorType() {
        return mErrorType;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setErrorMessage(String mErrorMessage) {
        this.mErrorMessage = mErrorMessage;
    }

}
