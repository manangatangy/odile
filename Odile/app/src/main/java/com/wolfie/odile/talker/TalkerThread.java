package com.wolfie.odile.talker;

import android.content.Context;
import android.support.annotation.NonNull;

import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;

import java.util.ArrayList;
import java.util.List;

public class TalkerThread extends TimerHandlerThread {

    private StatusChannel mStatusChannel;   // For sending status updates to.
    private TalkerStatus mTalkerStatus = new TalkerStatus();
    private List<Phrase> mPhrases;
    private Speaker mSpeaker;

    /**
     * This instance exists until handleCommand(null) is called.
     * This informs the instance to release all its resources and quit.
     */
    public TalkerThread(StatusChannel statusChannel, Context context) {
        super("TalkerThread");
        mStatusChannel = statusChannel;
        mSpeaker = new Speaker(context);
    }

    private void exit() {
        cancelTimer();  // Just in case.
        // TODO Release resources.
        mSpeaker.stop();
        quitSafely();
    }

    /**
     */
    @Override
    public void handleCommand(TalkerCommand talkerCommand) {
        if (talkerCommand == null) {
            exit();
            return;
        }
        switch (talkerCommand.mCommand) {
            case SET_MODE:
                // TODO ???
                break;
            case SET_PHRASES:       // TODO rename to RESET
                mPhrases = getPhrases(talkerCommand.mPhraseGroups);
                mTalkerStatus.setTotal(mPhrases.size());
                // State should not be running; make sure of it.
                resetStatus();
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
        mStatusChannel.sendStatus(mTalkerStatus);
    }

    private void resetStatus() {
        mTalkerStatus.setCounter(0);
        mTalkerStatus.setText(null);
        mTalkerStatus.setState(TalkerStatus.State.STOPPED);
    }
    /**
     * Fetch the phrase at [mTalkerStatus.counter++] and speak it.
     * Then schedule the next callback.
     */
    @Override
    public long handleTimer() {
        long rc;
        int count = mTalkerStatus.getCounter();
        if (mPhrases != null && count < mPhrases.size()) {
            String text = mPhrases.get(count).getRussian();
            mSpeaker.speak(text);
            mTalkerStatus.setText(text);
            mTalkerStatus.setCounter(count + 1);
            rc = 1000;      // Millisecs until next call.
        } else {
            // End of the list; reset for next cycle.
            resetStatus();
            mStatusChannel.sendStatus(mTalkerStatus);
            rc = TIMER_CANCEL;
        }
        mStatusChannel.sendStatus(mTalkerStatus);
        return rc;
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
