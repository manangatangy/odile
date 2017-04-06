package com.wolfie.odile.talker;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
            case TalkThread.Event.RESET:
                // It's ok to send RESET with null lists.
                mStepper.init(
                        serviceCommand.getPhraseGroups(),
                        serviceCommand.getSpeechParms());
                mSpeakerInfo.setTotal(mStepper.getPhrasesSize());
                resetAndCancelPendingEvents();
                break;
            case TalkThread.Event.SPEAK:
                mSpeakerInfo.setState(SpeakerInfo.State.SPEAKING);
                setTimer(100);          // Next event -> TIMEOUT
                break;
            case TalkThread.Event.PAUSE:
                mSpeakerInfo.setState(SpeakerInfo.State.PAUSED);
                cancelTimer();
                cancelSpeech();
                // Arrange so that the current phrase (first step) will be repeated upon SPEAK.
                mStepper.resetToPhrase(true);
                break;
            case TalkThread.Event.TIMEOUT:
                Stepper.Step step = mStepper.nextStep();
                if (step == null) {
                    // Reached end of the list; resetAndCancelPendingEvents for nextStep cycle.
                    resetAndCancelPendingEvents();
                } else {
                    mTextToSpeechManager.setSpeakerListener(this);      // Next event -> UTTERED
                    String speakError = mTextToSpeechManager.speak(step);
                    if (speakError != null) {
                        Log.d("TalkThread", "handleEvent, speakError=" + speakError);
                    }
                    mSpeakerInfo.setText(step.getText());
                    mSpeakerInfo.setCounter(mStepper.getPhraseIndex());
                }
                break;
            case TalkThread.Event.UTTERED:
                setTimer(mTextToSpeechManager.getDelayFromLastStep());
                // Next event -> TIMEOUT, using delay from last iteration.
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

    private void resetAndCancelPendingEvents() {
        cancelTimer();
        cancelSpeech();
        mSpeakerInfo.setCounter(0);
        mSpeakerInfo.setText(null);
        mSpeakerInfo.setState(SpeakerInfo.State.STOPPED);
        mStepper.reset();
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
            TalkThread.Event.TIMEOUT,
            TalkThread.Event.UTTERED,
            TalkThread.Event.QUIT
    })
    public @interface Event {
        int RESET   = TalkService.Command.RESET;
        int SPEAK   = TalkService.Command.SPEAK;
        int PAUSE   = TalkService.Command.PAUSE;
        int TIMEOUT = 3;
        int UTTERED = 4;
        int QUIT    = 5;
    }
}
