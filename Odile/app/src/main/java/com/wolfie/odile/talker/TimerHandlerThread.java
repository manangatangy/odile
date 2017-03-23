package com.wolfie.odile.talker;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Class for creating a new (child) thread that processes Commands, which are issued
 * in the owning (parent) thread, and received in the child thread.
 * Protocol: parent thread calls {@link #start()}, then {@link #waitUntilReady()} and then
 * may call {@link #sendCommand(TalkerCommand)} repeatedly.  In the child thread, the messages
 * arrive at {@link #handleCommand(TalkerCommand)} and also {@link #handleTimer()} (if the timer
 * was started via {@link #setTimer(int)}).
 * The child may also call {@link #quitSafely()} or {@link #quit()}
 */
public abstract class TimerHandlerThread extends HandlerThread {

    protected static final long TIMER_PERIOD_UNCHANGED = 0;
    protected static final long TIMER_CANCEL = -1;

    private Handler mCommandHandler;		// Owned in the new, child thread.
    private Handler mTimerHandler;          // Owned in the new, child thread.

    public TimerHandlerThread(String name) {
        super(name);
    }

    /**
     * Ref: http://stackoverflow.com/a/4855788
     */
    public synchronized void waitUntilReady() {
        mCommandHandler = new Handler(getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                TalkerCommand cmd = (TalkerCommand)msg.obj;
                handleCommand(cmd);
                return false;
            }
        });
    }

    public void sendCommand(TalkerCommand cmd) {
        Message message = Message.obtain(mCommandHandler, 0, 0, 0, cmd);
        mCommandHandler.sendMessage(message);
    }

    public abstract void handleCommand(TalkerCommand cmd);

    /**
     * Process a timer event.
     * @return TIMER_PERIOD_UNCHANGED (causes the next callback to onTimer to occur in the
     * same interval as the original call to setTimer), or TIMER_CANCEL (to cease callbacks
     * to handleTimer), else return the interval in millis for the next callback.
     */
    public abstract long handleTimer();

    /**
     * Arrange for the onTimer() method to be called in initialTimerPeriod millisecs.
     * Each invocation of onTimer() can use its return value to determine when or if
     * another timed invocation should be arranged.
     */
    protected void setTimer(final int initialTimerPeriod) {
        mTimerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                long nextTimerPeriod = handleTimer();
                if (nextTimerPeriod != TIMER_CANCEL) {
                    if (nextTimerPeriod == TIMER_PERIOD_UNCHANGED) {
                        nextTimerPeriod = initialTimerPeriod;
                    }
                    sendEmptyMessageDelayed(0, nextTimerPeriod);
                }
            }
        };
        mTimerHandler.sendEmptyMessageDelayed(0, initialTimerPeriod);
    }

    /**
     * Cancel any pending calls to handleTimer().
     */
    protected void cancelTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeMessages(0);
        }
        mTimerHandler = null;
    }

}
