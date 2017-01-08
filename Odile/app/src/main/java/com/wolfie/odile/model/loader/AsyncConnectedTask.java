package com.wolfie.odile.model.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.util.concurrent.CountDownLatch;

/**
 * Maintains a connected client.
 * Loosely base on Google's Drive API demo code.
 */
public abstract class AsyncConnectedTask<PARAMS, RESULT> extends AsyncListeningTask<PARAMS, RESULT> {

    private GoogleApiClient mClient;

    public AsyncConnectedTask(Context context, Listener<RESULT> listener) {
        super(listener);
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
        mClient = builder.build();
    }

    public abstract RESULT runInBackgroundConnected(PARAMS entry);

    /**
     * @return null if can't connect to the client
     */
    @Override
    public RESULT runInBackground(PARAMS entry) {
        final CountDownLatch latch = new CountDownLatch(1);
        mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int cause) {
            }

            @Override
            public void onConnected(Bundle arg0) {
                latch.countDown();
            }
        });
        mClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult arg0) {
                latch.countDown();
            }
        });
        mClient.connect();
        try {
            latch.await();
        } catch (InterruptedException e) {
            return null;
        }
        if (!mClient.isConnected()) {
            return null;
        }
        try {
            return runInBackgroundConnected(entry);
        } finally {
            mClient.disconnect();
        }
    }

    protected GoogleApiClient getGoogleApiClient() {
        return mClient;
    }

}
