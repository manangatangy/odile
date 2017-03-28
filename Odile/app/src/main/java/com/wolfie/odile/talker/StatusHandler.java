package com.wolfie.odile.talker;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * StatusHandler is a message channel for passing TalkerStatus to interested listeners.
 * The sender and the receiver may be in different threads.
 */
public class StatusHandler extends Handler {

    private TalkerStatus mMostRecentTalkerStatus = new TalkerStatus();
    private List<StatusChangeListener> mStatusChangeListenerList = new ArrayList<StatusChangeListener>();

    public interface StatusChangeListener {
        void onStatusChange(TalkerStatus talkerStatus);
    }

    public void addStatusChangeListener(StatusChangeListener statusChangeListener) {
        mStatusChangeListenerList.add(statusChangeListener);
        if (mMostRecentTalkerStatus != null) {
            statusChangeListener.onStatusChange(mMostRecentTalkerStatus);
        }
    }
    public void removeStatusChangeListener(StatusChangeListener statusChangeListener) {
        if (mStatusChangeListenerList.contains(statusChangeListener)) {
            mStatusChangeListenerList.remove(statusChangeListener);
        }
    }

    /**
     * May be called on threads other than the one that created the StatusHandler.
     * @param talkerStatus
     */
    public void sendStatus(TalkerStatus talkerStatus) {
        Message message = Message.obtain(this, 0, talkerStatus);
        sendMessage(message);
    }

    @Override
    public void handleMessage(Message msg) {
        mMostRecentTalkerStatus = (TalkerStatus)msg.obj;
        for (StatusChangeListener statusChangeListener : mStatusChangeListenerList) {
            statusChangeListener.onStatusChange(mMostRecentTalkerStatus);
        }
    }

}
