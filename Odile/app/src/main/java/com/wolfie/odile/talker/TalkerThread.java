package com.wolfie.odile.talker;

import com.wolfie.odile.model.PhraseGroup;

/**
 *
 */
public class TalkerThread extends TimerHandlerThread {

    private StatusHandler mStatusHandler;   // For sending status updates to.
    private TalkerStatus mTalkerStatus = new TalkerStatus();
    private PhraseGroup mPhraseGroup;

    public TalkerThread(StatusHandler statusHandler) {
        super("TalkerThread");
        mStatusHandler = statusHandler;
    }

    /**
     */
    @Override
    public void handleCommand(TalkerCommand talkerCommand) {
        switch (talkerCommand.mCommand) {
            case SET_MODE:
                // TODO ???
                break;
            case SET_PHRASES:
                mPhraseGroup = talkerCommand.mPhraseGroup;
                mTalkerStatus.setTotal(mPhraseGroup.getPhrases().size());
                // State should not be running; make sure of it.
                mTalkerStatus.setCounter(0);
                mTalkerStatus.setState(TalkerStatus.State.STOPPED);
                cancelTimer();
                break;
            case SPEAK:
                mTalkerStatus.setState(TalkerStatus.State.SPEAKING);
                setTimer(100);
                break;
            case PAUSE:
                mTalkerStatus.setState(TalkerStatus.State.PAUSED);
                cancelTimer();
                break;
        }
        mStatusHandler.sendStatus(mTalkerStatus);
    }

    @Override
    public long handleTimer() {
        return TIMER_CANCEL;
    }

    private void exit() {
        cancelTimer();  // Just in case.
    }

}
