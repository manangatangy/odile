package com.wolfie.odile.talker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

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

 * The Service owns the InfoChannel and the the MessageThread.
 * Ref: http://stackoverflow.com/a/15772151
 */

/**
 * Some references:
 * http://stackoverflow.com/questions/4300291/example-communication-between-activity-and-service-using-messaging?rq=1
 * This has a general example of a service/activity communication and it also has a tip
 * about placing the service in a different thread to the activity, as follows:
 * <service android:name="sname" android:process=":myservicename" />
 */
public class TalkService extends Service {

    public static final String COMMAND_KEY = "KEY_TALKER_COMMAND";

    private final IBinder mBinder = new LocalBinder();

    private InfoChannel mInfoChannel = new InfoChannel();

    private MessageThread mMessageThread;
    private TalkNotifier mTalkNotifier;

    public class LocalBinder extends Binder {
        public TalkService getService() {
            return TalkService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d("TalkService", "onCreate");
        mMessageThread = new MessageThread(mInfoChannel, this);
        mMessageThread.start();
        mMessageThread.waitUntilReady();
        // notifier should only be created when backgrounding the activity
//        mTalkNotifier = new TalkNotifier(this);
//        mInfoChannel.addStatusListener(mTalkNotifier);
    }

    @Override
    public void onDestroy() {
        Log.d("TalkService", "onDestroy");
        if (mMessageThread != null) {
            mMessageThread.sendQuit();
            mMessageThread = null;
        }
        // TODO shutdown the notifier
        // No need to remove listeners from InfoChannel; it is destroyed.
    }

    public InfoChannel getStatusChannel() {
        return mInfoChannel;
    }

    /**
     * START:
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get ServiceCommand from intent
        if (intent != null && intent.getExtras() != null) {
            ServiceCommand serviceCommand = intent.getExtras().getParcelable(COMMAND_KEY);
            if (serviceCommand != null) {
                mMessageThread.sendCommand(serviceCommand);
            }
        }
        return START_STICKY;
    }

    @IntDef({
            Command.SETMODE,
            Command.RESET,
            Command.SPEAK,
            Command.PAUSE
    })
    public @interface Command {
        int SETMODE = 0;
        int RESET = 1;
        int SPEAK = 2;
        int PAUSE = 3;
    }

}
