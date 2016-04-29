package com.chicagoandroid.sns.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ShareMessageBundle implements Serializable {
    private Map<ShareMessageType, ShareMessage> messages;

    public ShareMessageBundle() {
        messages = new HashMap<ShareMessageType, ShareMessage>();
    }

    public void addMessage(ShareMessageType type, ShareMessage message) {
        messages.put(type, message);
    }

    public ShareMessage getMessage(ShareMessageType type) {
        if (containsMessage(type)) {
            return messages.get(type);
        }
        return messages.get(ShareMessageType.DEFAULT);
    }

    public boolean containsMessage(ShareMessageType type) {
        return messages.containsKey(type);
    }
}