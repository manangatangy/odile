package com.wolfie.odile.model.loader;

import android.os.AsyncTask;

/**
 * Extends AsyncTask with an optional listener callback onCompletion.
 * Used in the loaders
 * @param <PARAMS> passed in to the worker thread
 * @param <RESULT> passed to the onCompletion listener in the ctor's thread
 */
public abstract class AsyncListeningTask<PARAMS, RESULT> extends AsyncTask<PARAMS, Void, RESULT> {

    private Listener<RESULT> mListener;

    public AsyncListeningTask(Listener<RESULT> listener) {
        super();
        mListener = listener;
    }

    public abstract RESULT runInBackground(PARAMS entry);

    @Override
    protected RESULT doInBackground(PARAMS... entries) {
        PARAMS entry = null;
        if (entries != null && entries.length > 0) {
            entry = entries[0];
        }
        return runInBackground(entry);
    }

    @Override
    protected void onPostExecute(RESULT result) {
        if (mListener != null) {
            mListener.onCompletion(result);
        }
    }

    public interface Listener<RESULT> {
        void onCompletion(RESULT result);
    }
}
