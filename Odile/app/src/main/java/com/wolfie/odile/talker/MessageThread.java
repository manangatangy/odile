package com.wolfie.odile.talker;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;

import java.util.List;

/**
 * Class for a thread that processes Commands, which are issued
 * in the owning/parent/creating thread, and received in this thread.
 * Protocol: parent thread calls {@link MessageThread#start()}, then
 * {@link MessageThread#waitUntilReady()} which blocks when the thread is
 * ready for messages.  Ref: http://stackoverflow.com/a/4855788
 * Parent thread also calls {@link MessageThread#sendCommand(ServiceCommand)}.
 */
public class MessageThread extends HandlerThread
        implements Handler.Callback, TextToSpeechManager.SpeakerListener {

    // Events handled by this thread queue here.
    private Handler mHandler;	        // Owned in the new, child thread.

    private InfoChannel mInfoChannel;   // For sending info updates to.
    private SpeakerInfo mSpeakerInfo = new SpeakerInfo();
    private List<Phrase> mPhrases;      // Currently spoken phrases.
    private TextToSpeechManager mTextToSpeechManager;

    //region -- these methods are called from the parent thread --

    public MessageThread(InfoChannel infoChannel, Context context) {
        super("MessageThread");
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
        mHandler.sendEmptyMessage(MessageThread.Event.QUIT);
    }

    //endregion -- these methods are called from the parent thread --

    @Override
    public boolean handleMessage(Message msg) {
        handleEvent(msg.what, (ServiceCommand)msg.obj);
        return false;
    }

    private void handleEvent(@MessageThread.Event int event, @Nullable ServiceCommand serviceCommand) {
        Log.d("MessageThread", "handleEvent, event=" + eventName(event));
        switch (event) {
            case MessageThread.Event.SETMODE:
                // TODO ???
                break;
            case MessageThread.Event.RESET:
                mPhrases = PhraseGroup.getAllPhrases(serviceCommand.getPhraseGroups());
                mSpeakerInfo.setTotal(mPhrases.size());
                resetAndCancelPendingEvents();
                break;
            case MessageThread.Event.SPEAK:
                mSpeakerInfo.setState(SpeakerInfo.State.SPEAKING);
                setTimer(100);          // Next event -> TIMEOUT
                break;
            case MessageThread.Event.PAUSE:
                mSpeakerInfo.setState(SpeakerInfo.State.PAUSED);
                cancelTimer();
                cancelSpeech();
                break;
            case MessageThread.Event.TIMEOUT:
                // Fetch the phrase at [mSpeakerInfo.counter++] and speak it.
                int count = mSpeakerInfo.getCounter();
                if (mPhrases == null || count >= mPhrases.size()) {
                    // Reached end of the list; resetAndCancelPendingEvents for next cycle.
                    resetAndCancelPendingEvents();
                } else {
                    String text = mPhrases.get(count).getRussian();
                    mTextToSpeechManager.setSpeakerListener(this);      // Next event -> UTTERED
                    String speakError = mTextToSpeechManager.speak(text);
                    if (speakError != null) {
                        Log.d("MessageThread", "handleEvent, speakError=" + speakError);
                    }
                    mSpeakerInfo.setText(text);
                    mSpeakerInfo.setCounter(count + 1);
                }
                break;
            case MessageThread.Event.UTTERED:
                setTimer(1000);         // Next event -> TIMEOUT
                break;
            case MessageThread.Event.QUIT:
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
    }

    @Override
    public void onDoneUttering(boolean error) {
        mHandler.sendEmptyMessage(MessageThread.Event.UTTERED);
    }

    private void setTimer(int timerPeriod) {
        cancelTimer();
        mHandler.sendEmptyMessageDelayed(MessageThread.Event.TIMEOUT, timerPeriod);
    }

    private void cancelTimer() {
        mHandler.removeMessages(MessageThread.Event.TIMEOUT);
    }

    private void cancelSpeech() {
        mTextToSpeechManager.setSpeakerListener(null);
        mTextToSpeechManager.stop();
    }

    private String eventName(@MessageThread.Event int event) {
        switch (event) {
            case MessageThread.Event.SETMODE:
                return "SETMODE";
            case MessageThread.Event.RESET:
                return "RESET";
            case MessageThread.Event.SPEAK:
                return "SPEAK";
            case MessageThread.Event.PAUSE:
                return "PAUSE";
            case MessageThread.Event.TIMEOUT:
                return "TIMEOUT";
            case MessageThread.Event.UTTERED:
                return "UTTERED";
            case MessageThread.Event.QUIT:
                return "QUIT";
        }
        return null;
    }

    @IntDef({
            MessageThread.Event.SETMODE,
            MessageThread.Event.RESET,
            MessageThread.Event.SPEAK,
            MessageThread.Event.PAUSE,
            MessageThread.Event.TIMEOUT,
            MessageThread.Event.UTTERED,
            MessageThread.Event.QUIT
    })
    public @interface Event {
        int SETMODE = TalkService.Command.SETMODE;
        int RESET   = TalkService.Command.RESET;
        int SPEAK   = TalkService.Command.SPEAK;
        int PAUSE   = TalkService.Command.PAUSE;
        int TIMEOUT = 4;
        int UTTERED = 5;
        int QUIT    = 6;
    }
}
