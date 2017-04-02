package com.wolfie.odile.talker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Send command to the service like this:
 *
 Intent intent = new Intent(getBaseContext(), SimService.class);
 intent.putExtra("KEY_FOO", aParcelable);
 Bundle extras = new Bundle();
 extras.putSerializable(SimService.SETTINGS_KEY, simSettings);
 intent.putExtras(extras);
 startService(intent);

 Foo foo = getIntent().getExtras().getParcelable("KEY_FOO");

 * The Service owns the StatusHandler and the the TalkerThread.
 */

public class TalkerService extends Service {

    public static final String COMMAND_KEY = "KEY_TALKER_COMMAND";

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
        mTalkerThread = new TalkerThread(mStatusHandler);
        mTalkerThread.start();
        mTalkerThread.waitUntilReady();


        // notifier should only be created when backgrounding the activity
//        mTalkerNotifier = new TalkerNotifier(this);
//        mStatusHandler.addStatusChangeListener(mTalkerNotifier);
    }

    @Override
    public void onDestroy() {
        if (mTalkerThread != null) {
            mTalkerThread.quitSafely();
            mTalkerThread = null;
        }
        // TODO shutdown the notifier
        // No need to remove listeners from StatusHandler; it is destroyed.
    }

    public StatusHandler getStatusHandler() {
        return mStatusHandler;
    }

    /**
     * START:
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get TalkerCommand from intent
        if (intent != null && intent.getExtras() != null) {
            TalkerCommand talkerCommand = intent.getExtras().getParcelable(COMMAND_KEY);
            if (talkerCommand != null) {
                mTalkerThread.sendCommand(talkerCommand);
            }
        }
        return START_STICKY;
    }
}
