package com.wolfie.odile.talker;

import android.content.Context;

/**
 * Created by david on 24/03/17.
 */

public class TalkerNotifier implements StatusHandler.StatusChangeListener {

    private Context mContext;

    public TalkerNotifier(Context context) {
        mContext = context;
    }

    @Override
    public void onStatusChange(TalkerStatus talkerStatus) {

    }
}
