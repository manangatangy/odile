package com.wolfie.odile.talker;

import android.content.Context;

/**
 * Created by david on 24/03/17.
 */

public class TalkNotifier implements InfoChannel.InfoListener {

    private Context mContext;

    public TalkNotifier(Context context) {
        mContext = context;
    }

    @Override
    public void onSpeakerInfo(SpeakerInfo speakerInfo) {

    }
}
