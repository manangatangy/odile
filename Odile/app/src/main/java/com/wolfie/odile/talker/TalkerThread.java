package com.wolfie.odile.talker;

/**
 *
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
        if (cmd == null) {      // TODO null == "QUIT/STOP/EXIT"
            exit();
        }
    }

    @Override
    public long handleTimer() {
        return TIMER_CANCEL;
    }

    private void exit() {
        mStatusHandler.sendStatus(null);     // TODO null ==> "STOPPED/STANDBY"
        cancelTimer();  // Just in case.
        quit();         // Quit the child thread.
    }

}
