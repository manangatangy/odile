package com.wolfie.odile.model.loader;

import android.support.annotation.Nullable;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.wolfie.odile.model.DataSet;
import com.wolfie.odile.model.IoHelper;
import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.database.Source;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Performs export and import tasks in a background thread.  Either clear text or cipher
 * text may be specified.  The IoHelper is a key helper class.
 */
public class IoLoader {

    private Source mDataSource;
    // During import/restore, optionally delete existing entries first.
    private boolean mIsOverwrite;

    public IoLoader(Source dataSource) {
        mDataSource = dataSource;
    }

    public void export(File file, AsyncListeningTask.Listener<LoaderResult> listener) {
        new ExportTask(listener).execute(file);
    }

    public void inport(boolean isOverwrite, File file, AsyncListeningTask.Listener<LoaderResult> listener) {
        mIsOverwrite = isOverwrite;
        new ImportTask(listener).execute(file);
    }

    private class ExportTask extends AsyncListeningTask<File, LoaderResult> {
        public ExportTask(@Nullable Listener<LoaderResult> listener) {
            super(listener);
        }
        @Override
        public LoaderResult runInBackground(File file) {
            List<Phrase> phrases = mDataSource.read();
            String json = new IoHelper().export(phrases);
            LoaderResult ioResult = null;
            FileOutputStream fos = null;
            BufferedWriter bw = null;
            try {
                fos = new FileOutputStream(file);
                bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                bw.write(json);
                ioResult = LoaderResult.makeSuccess("Exported " + phrases.size() + " phrases");
            } catch (FileNotFoundException fnfe) {
                return LoaderResult.makeFailure("FileNotFound opening\n" + file.getName());
            } catch (UnsupportedEncodingException usce) {
                return LoaderResult.makeFailure("UnsupportedEncodingException\n" + file.getPath());
            } catch (IOException ioe) {
                return LoaderResult.makeFailure("IOException writing\n" + file.getPath());
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ioe) {
                    ioResult = LoaderResult.makeFailure("IOException closing\n" + file.getPath());
                    // Won't be returned if exception was thrown prior to the finally clause executing.
                }
            }
            return ioResult;
        }
    }

    private class ImportTask extends AsyncListeningTask<File, LoaderResult> {
        public ImportTask(@Nullable Listener<LoaderResult> listener) {
            super(listener);
        }
        @Override
        public LoaderResult runInBackground(File file) {
            LoaderResult ioResult = null;
            InputStreamReader isr = null;
            try {
                FileInputStream fis = new FileInputStream(file);
                isr = new InputStreamReader(fis);
                List<Phrase> phrases = new IoHelper().inport(isr);

                // Load into database, optionally clearing existing data first.
                if (mIsOverwrite) {
                    mDataSource.deleteAll();
                }
                for (int i = 0; i < phrases.size(); i++) {
                    mDataSource.insert(phrases.get(i));
                }
                ioResult = LoaderResult.makeSuccess("Imported " + phrases.size() + " phrases");
            } catch (FileNotFoundException fnfe) {
                return LoaderResult.makeFailure("FileNotFound opening\n" + file.getPath());
            } catch (JsonIOException jioe) {
                return LoaderResult.makeFailure("JsonIOException reading\n" + file.getPath());
            } catch (JsonSyntaxException jse) {
                return LoaderResult.makeFailure("JsonSyntaxException parsing\n" + file.getPath());
            } finally {
                try {
                    if (isr != null) {
                        isr.close();
                    }
                } catch (IOException ioe) {
                    ioResult = LoaderResult.makeFailure("IOException closing\n" + file.getPath());
                    // Won't be returned if exception was thrown prior to the finally clause executing.
                }
            }
            return ioResult;
        }
    }

}
