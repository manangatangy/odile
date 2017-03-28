package com.wolfie.odile.talker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by david on 24/03/17.
 */

public class TalkerService extends Service implements StatusHandler.StatusChangeListener {

    private final IBinder mBinder = new LocalBinder();
    private StatusHandler mStatusHandler = new StatusHandler();
    private TalkerThread mTalkerThread;
    private TalkerNotifier mTalkerNotifier;

    public class LocalBinder extends Binder {
        public TalkerService getService() {
            return TalkerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mTalkerNotifier = new TalkerNotifier(this);
        mStatusHandler.addStatusChangeListener(mTalkerNotifier);
        mStatusHandler.addStatusChangeListener(this);
    }

    @Override
    public void onDestroy() {
        if (mTalkerThread != null) {
            // Send thread the quit command, which will callback to
            // onStatusChange(STANDBY) and then quit the thread.
            mTalkerThread.sendCommand(null);        // TODO null ==> "QUIT/STOP/EXIT ??"
        }
        // No need to remove listeners from StatusHandler; it is destroyed.
    }

    @Override
    public void onStatusChange(TalkerStatus talkerStatus) {
        // We need to know when the thread has quit, in order to release our reference.
        if (talkerStatus == null) {     // TODO status == "STANDBY" means: thread has quit
            mTalkerThread = null;       // Allow garbage collection
        }
    }

    public void addStatusChangeListener(StatusHandler.StatusChangeListener statusChangeListener) {
        mStatusHandler.addStatusChangeListener(statusChangeListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // get TalkerCommand from intent
        TalkerCommand talkerCommand = null;

        if (mTalkerThread == null) {
            mTalkerThread = new TalkerThread(mStatusHandler);
            mTalkerThread.start();
            mTalkerThread.waitUntilReady();
        }
        mTalkerThread.sendCommand(talkerCommand);
        return START_STICKY;
    }
}
