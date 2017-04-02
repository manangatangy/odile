package com.wolfie.odile.talker;

/**
 * The status consists of several independent states and settings.
 */
public class TalkerStatus {

    private State mState;
    private int mCounter;               // base 0 index into phrase group
    private int mTotal;                 // Size of phrase group
    private String mText;               // Text being spoken.

    public TalkerStatus() {
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
        STOPPED("SELECTED: ", "SPEAK"),
        SPEAKING("SPEAKING: ", "PAUSE"),
        PAUSED("PAUSED AT: ", "RESUME");

        private String mTitle;
        private String mActionText;
        private State(String title, String actionText) {
            mTitle = title;
            mActionText = actionText;
        }
        public String title() {
            return mTitle;
        }
        public String getActionText() {
            return mActionText;
        }
    }

}
