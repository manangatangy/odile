package com.wolfie.odile.talker;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * InfoChannel is a message channel for passing SpeakerInfo to interested listeners.
 * The sender and the receiver may be in different threads.
 */
public class InfoChannel extends Handler {

    private SpeakerInfo mMostRecentSpeakerInfo = null;
    private List<InfoListener> mInfoListenerList = new ArrayList<InfoListener>();

    public interface InfoListener {
        void onSpeakerInfo(SpeakerInfo speakerInfo);
    }

    public void addStatusListener(InfoListener infoListener) {
        mInfoListenerList.add(infoListener);
        if (mMostRecentSpeakerInfo != null) {
            infoListener.onSpeakerInfo(mMostRecentSpeakerInfo);
        }
    }
    public void removeStatusListener(InfoListener infoListener) {
        if (mInfoListenerList.contains(infoListener)) {
            mInfoListenerList.remove(infoListener);
        }
    }

    /**
     * May be called on threads other than the one that created the InfoChannel.
     * @param speakerInfo
     */
    public void sendInfo(SpeakerInfo speakerInfo) {
        Message message = Message.obtain(this, 0, speakerInfo);
        sendMessage(message);
    }

    @Override
    public void handleMessage(Message msg) {
        mMostRecentSpeakerInfo = (SpeakerInfo)msg.obj;
        for (InfoListener infoListener : mInfoListenerList) {
            infoListener.onSpeakerInfo(mMostRecentSpeakerInfo);
        }
    }

}
