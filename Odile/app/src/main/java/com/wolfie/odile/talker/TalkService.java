package com.wolfie.odile.talker;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

/**
 * Responds to ServiceCommands and owns/controls the TalkThread and TalkNotifier.
 * Has an InfoChannel that receives SpeakerInfo messages from the TalkThread and
 * notifies various listeners, which are TalkNotifier and TalkPresenter.
 * Some references:
 * http://stackoverflow.com/a/15772151
 * http://stackoverflow.com/questions/4300291/example-communication-between-activity-and-service-using-messaging?rq=1
 * This has a general example of a service/activity communication and it also has a tip
 * about placing the service in a different thread to the activity, as follows:
 * <service android:name="sname" android:process=":myservicename" />
 */
public class TalkService extends Service {

    public static final String COMMAND_KEY = "KEY_TALKER_COMMAND";

    private final IBinder mBinder = new LocalBinder();

    private InfoChannel mInfoChannel = new InfoChannel();

    private TalkThread mTalkThread;
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
        mTalkThread = new TalkThread(mInfoChannel, this);
        mTalkThread.start();
        mTalkThread.waitUntilReady();
        // notifier should only be created when backgrounding the activity
//        mTalkNotifier = new TalkNotifier(this);
//        mInfoChannel.addStatusListener(mTalkNotifier);
    }

    @Override
    public void onDestroy() {
        Log.d("TalkService", "onDestroy");
        if (mTalkThread != null) {
            mTalkThread.sendQuit();
            mTalkThread = null;
        }
        // TODO shutdown the notifier
        // No need to remove listeners from InfoChannel; it is destroyed.
    }

    /**
     * @return InfoChannel for clients wishing to listen for SpeakerInfo.
     */
    public InfoChannel getStatusChannel() {
        return mInfoChannel;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get ServiceCommand from intent
        if (intent != null && intent.getExtras() != null) {
            ServiceCommand serviceCommand = intent.getExtras().getParcelable(COMMAND_KEY);
            if (serviceCommand != null) {
                mTalkThread.sendCommand(serviceCommand);
            }
        }
        return START_STICKY;
    }

    @IntDef({
            Command.RESET,
            Command.SPEAK,
            Command.PAUSE
    })
    public @interface Command {
        int RESET = 0;
        int SPEAK = 1;
        int PAUSE = 2;
    }

}
