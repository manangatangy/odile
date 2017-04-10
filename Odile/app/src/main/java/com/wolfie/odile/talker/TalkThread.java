package com.wolfie.odile.talker;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Class for a thread that processes Commands, which are issued
 * in the owning/parent/creating thread, and received in this thread.
 * Protocol: parent thread calls {@link TalkThread#start()}, then
 * {@link TalkThread#waitUntilReady()} which blocks when the thread is
 * ready for messages.  Ref: http://stackoverflow.com/a/4855788
 * Parent thread also calls {@link TalkThread#sendCommand(ServiceCommand)}.
 */
public class TalkThread extends HandlerThread
        implements Handler.Callback, TextToSpeechManager.SpeakerListener {

    // Events handled by this thread queue here.
    private Handler mHandler;	        // Owned in the new, child thread.
    private InfoChannel mInfoChannel;   // For sending info updates to.

    private SpeakerInfo mSpeakerInfo = new SpeakerInfo();

    private Stepper mStepper = new Stepper();
    private Stepper.Step mStep;
    boolean mWholePhrase = false;

    private TextToSpeechManager mTextToSpeechManager;

    //region -- these methods are called from the parent thread --

    public TalkThread(InfoChannel infoChannel, Context context) {
        super("TalkThread");
        mInfoChannel = infoChannel;
        // TODO move the ctor call to the child thread
        mTextToSpeechManager = new TextToSpeechManager(context);
    }

    public synchronized void waitUntilReady() {
        mHandler = new Handler(getLooper(), this);
    }

    public void sendCommand(ServiceCommand serviceCommand) {
        Message message = Message.obtain(mHandler, serviceCommand.getCommand(), 0, 0, serviceCommand);
        mHandler.sendMessage(message);
    }

    public void sendQuit() {
        mHandler.sendEmptyMessage(TalkThread.Event.QUIT);
    }

    //endregion -- these methods are called from the parent thread --

    @Override
    public boolean handleMessage(Message msg) {
        handleEvent(msg.what, (ServiceCommand)msg.obj);
        return false;
    }

    private void handleEvent(@TalkThread.Event int event, @Nullable ServiceCommand serviceCommand) {
        Log.d("TalkThread", "handleEvent, event=" + eventName(event));
        switch (event) {
            case TalkThread.Event.SPEAK:
                mSpeakerInfo.setState(SpeakerInfo.State.SPEAKING);
                // Fall through to timeout case.
            case TalkThread.Event.TIMEOUT:
                // While state is speaking (and we are awaiting timeout events), mStep
                // points to the current step being spoken. When timeout occurs, we fetch the
                // next step, speak it, and assign it back into mStep. The flag mWholeStep
                // indicates to move next on to the the immediate next step, or to the
                // start of the next phrase (if true).
                mStep = mStepper.next(mStep, mWholePhrase);
                mWholePhrase = false;
                if (mStep == null) {        // Reached end of the list.
                    resetAndCancelPendingEvents();
                } else {
                    speak(mStep, this);     // Next event -> UTTERED
                }
                break;
            case TalkThread.Event.UTTERED:
                setTimer(mStep.getSpeechParm().getDelay());
                // Next event -> TIMEOUT, using delay from last iteration.
                break;
            case TalkThread.Event.PAUSE:
                // On pause, point mStep back to the start of the previous phrase, so that upon
                // speak event, mStep will be advanced to the start (ie mWholePhrase set)
                // of the same current step.
                mSpeakerInfo.setState(SpeakerInfo.State.PAUSED);
                cancelTimer();
                cancelSpeech();
                mStep = mStepper.previous(mStep, mWholePhrase = true);
                break;
            case TalkThread.Event.PREVIOUS:
                cancelSpeech();
                if (mSpeakerInfo.getState() == SpeakerInfo.State.PAUSED) {
                    // In paused state, mWholePhrase is true and mStep positioned at (current - 1)
                    speak(mStep, null);
                    mStep = mStepper.previous(mStep, true);
                } else {
                    cancelTimer();
                    // Arrange to speak the first step of the previous phrase
                    mStep = mStepper.previous(mStep, mWholePhrase = true);
                    mStep = mStepper.previous(mStep, mWholePhrase);
                    setTimer(10);          // Next event -> TIMEOUT
                }
                break;
            case TalkThread.Event.REPEAT:
                cancelSpeech();
                if (mSpeakerInfo.getState() == SpeakerInfo.State.PAUSED) {
                    // In paused state, mWholePhrase is true and mStep positioned at (current - 1)
                    speak(mStepper.next(mStep, true), null);
                } else {
                    cancelTimer();
                    mStep = mStepper.previous(mStep, mWholePhrase = true);
                    setTimer(10);          // Next event -> TIMEOUT
                }
                break;
            case TalkThread.Event.NEXT:
                cancelSpeech();
                if (mSpeakerInfo.getState() == SpeakerInfo.State.PAUSED) {
                    // In paused state, mWholePhrase is true and mStep positioned at (current - 1)
                    mStep = mStepper.next(mStep, true);
                    speak(mStepper.next(mStep, true), null);
                } else {
                    cancelTimer();
                    mWholePhrase = true;
                    setTimer(10);          // Next event -> TIMEOUT
                }
                break;


            case TalkThread.Event.RESET:
                // It's ok to send RESET with null lists.
                mStepper.init(
                        serviceCommand.getPhraseGroups(),
                        serviceCommand.getSpeechParms());
                mSpeakerInfo.setTotal(mStepper.getPhrasesSize());
                resetAndCancelPendingEvents();
                break;

            case TalkThread.Event.QUIT:
                // TODO Release resources.
                resetAndCancelPendingEvents();
                mTextToSpeechManager.shutdown();
                quitSafely();
                break;
        }
        mInfoChannel.sendInfo(mSpeakerInfo);
    }

    private void speak(Stepper.Step step, TextToSpeechManager.SpeakerListener speakerListener) {
        if (step != null) {
            mTextToSpeechManager.setSpeakerListener(speakerListener);
            String speakError = mTextToSpeechManager.speak(step);
            if (speakError != null) {
                Log.d("TalkThread", "handleEvent, speakError=" + speakError);
            }
            mSpeakerInfo.setText(step.getText());
            mSpeakerInfo.setCounter(step.getPhraseIndex() + 1);
        }
    }

    /**
     * Clears state of list iteration (Mset = null, mWholePhrase = false) so that the next
     * call to Stepper.next() returns the first Step in the list.
     */
    private void resetAndCancelPendingEvents() {
        cancelTimer();
        cancelSpeech();
        mSpeakerInfo.setCounter(0);
        mSpeakerInfo.setText(null);
        mSpeakerInfo.setState(SpeakerInfo.State.STOPPED);
        mStep = null;
        mWholePhrase = false;
    }

    @Override
    public void onDoneUttering(boolean error) {
        mHandler.sendEmptyMessage(TalkThread.Event.UTTERED);
    }

    private void setTimer(int timerPeriod) {
        cancelTimer();
        mHandler.sendEmptyMessageDelayed(TalkThread.Event.TIMEOUT, timerPeriod);
        Log.d("TalkThread", "setTimer for " + timerPeriod);
    }

    private void cancelTimer() {
        mHandler.removeMessages(TalkThread.Event.TIMEOUT);
    }

    private void cancelSpeech() {
        mTextToSpeechManager.setSpeakerListener(null);
        mTextToSpeechManager.stop();
    }

    private String eventName(@TalkThread.Event int event) {
        switch (event) {
            case TalkThread.Event.RESET:
                return "RESET";
            case TalkThread.Event.SPEAK:
                return "SPEAK";
            case TalkThread.Event.PAUSE:
                return "PAUSE";
            case TalkThread.Event.REPEAT:
                return "REPEAT";
            case TalkThread.Event.PREVIOUS:
                return "PREVIOUS";
            case TalkThread.Event.NEXT:
                return "NEXT";
            case TalkThread.Event.TIMEOUT:
                return "TIMEOUT";
            case TalkThread.Event.UTTERED:
                return "UTTERED";
            case TalkThread.Event.QUIT:
                return "QUIT";
        }
        return null;
    }

    @IntDef({
            TalkThread.Event.RESET,
            TalkThread.Event.SPEAK,
            TalkThread.Event.PAUSE,
            TalkThread.Event.REPEAT,
            TalkThread.Event.PREVIOUS,
            TalkThread.Event.NEXT,
            TalkThread.Event.TIMEOUT,
            TalkThread.Event.UTTERED,
            TalkThread.Event.QUIT
    })
    public @interface Event {
        int RESET       = TalkService.Command.RESET;
        int SPEAK       = TalkService.Command.SPEAK;
        int PAUSE       = TalkService.Command.PAUSE;
        int REPEAT      = TalkService.Command.REPEAT;
        int PREVIOUS    = TalkService.Command.PREVIOUS;
        int NEXT        = TalkService.Command.NEXT;
        int TIMEOUT     = 10;
        int UTTERED     = 11;
        int QUIT        = 12;
    }
}
