package com.wolfie.odile.talker;

import android.util.Log;

/**
 * Created by david on 23/03/17.
 */

public class TalkerThread extends TimerHandlerThread {

    private StatusHandler mStatusHandler;   // For sending status updates to.

    public TalkerThread(StatusHandler statusHandler) {
        super("TalkerThread");
        mStatusHandler = statusHandler;
    }

    @Override
    public void handleCommand(TalkerCommand cmd) {
        // ...
        exit();
    }

    @Override
    public long handleTimer() {
        return TIMER_CANCEL;
    }

    private void exit() {
        mStatusHandler.sendMessage(new TalkerStatus());     // Set status back to standby ?
        cancelTimer();  // Just in case.
        quit();         // Quit the child thread.
    }

}
