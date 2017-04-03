package com.wolfie.odile.talker;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * StatusChannel is a message channel for passing TalkerStatus to interested listeners.
 * The sender and the receiver may be in different threads.
 */
public class StatusChannel extends Handler {

    private TalkerStatus mMostRecentTalkerStatus = null;
    private List<StatusListener> mStatusListenerList = new ArrayList<StatusListener>();

    public interface StatusListener {
        void onStatus(TalkerStatus talkerStatus);
    }

    public void addStatusListener(StatusListener statusListener) {
        mStatusListenerList.add(statusListener);
        if (mMostRecentTalkerStatus != null) {
            statusListener.onStatus(mMostRecentTalkerStatus);
        }
    }
    public void removeStatusListener(StatusListener statusListener) {
        if (mStatusListenerList.contains(statusListener)) {
            mStatusListenerList.remove(statusListener);
        }
    }

    /**
     * May be called on threads other than the one that created the StatusChannel.
     * @param talkerStatus
     */
    public void sendStatus(TalkerStatus talkerStatus) {
        Message message = Message.obtain(this, 0, talkerStatus);
        sendMessage(message);
    }

    @Override
    public void handleMessage(Message msg) {
        mMostRecentTalkerStatus = (TalkerStatus)msg.obj;
        for (StatusListener statusListener : mStatusListenerList) {
            statusListener.onStatus(mMostRecentTalkerStatus);
        }
    }

}
