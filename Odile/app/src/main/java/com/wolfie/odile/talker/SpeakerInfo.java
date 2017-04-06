package com.wolfie.odile.talker;

/**
 * Comprises state of the {@link TalkThread}, which is communicated
 * via {@link InfoChannel} to all {@link InfoChannel.InfoListener}s.
 * The status consists of several independent states and settings.
 */
public class SpeakerInfo {

    private State mState;
    private int mCounter;               // base 0 index into phrase group
    private int mTotal;                 // Size of phrase group
    private String mText;               // Text being spoken.

    public SpeakerInfo() {
        mState = State.STOPPED;
        mCounter = 0;
        mTotal = 0;
        mText = "";
    }

    public void setState(State speakerState) {
        mState = speakerState;
    }
    public State getState() {
        return mState;
    }

    public void setCounter(int counter) {
        mCounter = counter;
    }
    public int getCounter() {
        return mCounter;
    }

    public void setTotal(int total) {
        mTotal = total;
    }
    public int getTotal() {
        return mTotal;
    }

    public void setText(String text) {
        mText = text;
    }
    public String getText() {
        return mText;
    }

    public String getSubTitle() {
        String text = getTotal() + " phrases";
        if (getState() != State.STOPPED) {
            text = getCounter() + " of " + text;
        }
        return text;
    }

    public enum State {
        STOPPED(    "Selected: ",   "CLOSE",    "SPEAK",   ""),
        SPEAKING(   "Speaking: ",   "REPEAT",   "PAUSE",   "BACK 1"),
        PAUSED(     "Paused at: ",  "CLOSE",    "RESUME",  "BACK 1");

        private String mTitle;
        private String mAction1Text;
        private String mAction2Text;
        private String mAction3Text;
        private State(String title, String action1Text, String action2Text, String action3Text) {
            mTitle = title;
            mAction1Text = action1Text;
            mAction2Text = action2Text;
            mAction3Text = action3Text;
        }
        public String title() {
            return mTitle;
        }
        public String getAction1Text() {
            return mAction1Text;
        }
        public String getAction2Text() {
            return mAction2Text;
        }
        public String getAction3Text() {
            return mAction3Text;
        }
    }

}
