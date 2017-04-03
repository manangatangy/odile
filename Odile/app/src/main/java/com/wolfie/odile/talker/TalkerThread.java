package com.wolfie.odile.talker;

import android.support.annotation.NonNull;

import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TalkerThread extends TimerHandlerThread {

    private StatusHandler mStatusHandler;   // For sending status updates to.
    private TalkerStatus mTalkerStatus = new TalkerStatus();
    private List<Phrase> mPhrases;

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
                mPhrases = getPhrases(talkerCommand.mPhraseGroups);
                mTalkerStatus.setTotal(mPhrases.size());
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

    @NonNull
    private List<Phrase> getPhrases(@NonNull List<PhraseGroup> phraseGroups) {
        List<Phrase> phrases = new ArrayList<>();
        for (PhraseGroup phraseGroup : phraseGroups) {
            for (Phrase phrase : phraseGroup.getPhrases()) {
                phrases.add(phrase);
            }
        }
        return phrases;
    }
}
