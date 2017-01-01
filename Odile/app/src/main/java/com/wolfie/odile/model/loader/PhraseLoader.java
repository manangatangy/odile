package com.wolfie.odile.model.loader;

import android.support.annotation.Nullable;

import com.wolfie.odile.model.DataSet;
import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.database.Source;

import java.util.List;

/**
 * Accesses Phrase's via the database Source. The CRUD operations occur in background threads
 * with results passed back to the calling thread listener.
 */
public class PhraseLoader {

    private Source mSource;

    public PhraseLoader(Source source) {
        mSource = source;
    }

    public void read(AsyncListeningTask.Listener<DataSet> listener) {
        new PhraseLoader.ReadTask(listener).execute();
    }

    public void insert(Phrase phrase, @Nullable AsyncListeningTask.Listener<Boolean> listener) {
        new PhraseLoader.InsertTask(listener).execute(phrase);
    }

    public void update(Phrase phrase, @Nullable AsyncListeningTask.Listener<Boolean> listener) {
        new PhraseLoader.UpdateTask(listener).execute(phrase);
    }

    public void delete(Phrase phrase, @Nullable AsyncListeningTask.Listener<Boolean> listener) {
        new PhraseLoader.DeleteTask(listener).execute(phrase);
    }

    private class ReadTask extends AsyncListeningTask<Void, DataSet> {
        public ReadTask(@Nullable Listener<DataSet> listener) {
            super(listener);
        }
        @Override
        public DataSet runInBackground(Void arg) {
            List<Phrase> phrases = mSource.read();
            DataSet.sort(phrases);
            return new DataSet(phrases);
        }
    }

    private class InsertTask extends AsyncListeningTask<Phrase, Boolean> {
        public InsertTask(@Nullable Listener<Boolean> listener) {
            super(listener);
        }
        @Override
        public Boolean runInBackground(Phrase phrase) {
            return mSource.insert(phrase);
        }
    }

    private class UpdateTask extends AsyncListeningTask<Phrase, Boolean> {
        public UpdateTask(@Nullable Listener<Boolean> listener) {
            super(listener);
        }
        @Override
        public Boolean runInBackground(Phrase phrase) {
            return mSource.update(phrase);
        }
    }

    private class DeleteTask extends AsyncListeningTask<Phrase, Boolean> {
        public DeleteTask(@Nullable Listener<Boolean> listener) {
            super(listener);
        }
        @Override
        public Boolean runInBackground(Phrase phrase) {
            return mSource.delete(phrase);
        }
    }


}
