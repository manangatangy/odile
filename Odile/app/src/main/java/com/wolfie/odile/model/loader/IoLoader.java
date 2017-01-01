package com.wolfie.odile.model.loader;

import android.support.annotation.Nullable;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.wolfie.odile.model.DataSet;
import com.wolfie.odile.model.Entry;
import com.wolfie.odile.model.IoHelper;
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

    // During import, the password is needed to decrypt the imported cipher text.
    private String mPassword;
    // Cleartext is indicated if we have no password (during import) or no masterdata (during export).
    private boolean mAsClearText;
    // During import/restore, optionally delete existing entries first.
    private boolean mIsOverwrite;

    public IoLoader(Source dataSource) {
        mDataSource = dataSource;
    }

    public class IoResult {
        public String mSuccessMessage;
        public String mFailureMessage;
    }

    public class SuccessResult extends IoResult {
        public SuccessResult(String successMessage) {
            mSuccessMessage = successMessage;
        }
    }

    public class FailureResult extends IoResult {
        public FailureResult(String failureMessage) {
            mFailureMessage = failureMessage;
        }
    }

    public void export(File file, AsyncListeningTask.Listener<IoResult> listener) {
        new ExportTask(listener).execute(file);
    }

    public void inport(boolean isOverwrite,
                       File file, AsyncListeningTask.Listener<IoResult> listener) {
        mIsOverwrite = isOverwrite;
        new ImportTask(listener).execute(file);
    }

    private class ExportTask extends AsyncListeningTask<File, IoResult> {
        public ExportTask(@Nullable Listener<IoResult> listener) {
            super(listener);
        }
        @Override
        public IoResult runInBackground(File file) {
            DataSet dataSet = mDataSource.read();
            List<Entry> entries = dataSet.getEntries();

            String json = new IoHelper().export(entries);

            IoResult ioResult = null;
            FileOutputStream fos = null;
            BufferedWriter bw = null;
            try {
                fos = new FileOutputStream(file);
                bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                bw.write(json);
                ioResult = new SuccessResult("Exported " + entries.size() + " entries");
            } catch (FileNotFoundException fnfe) {
                return new FailureResult("FileNotFound opening\n" + file.getName());
            } catch (UnsupportedEncodingException usce) {
                return new FailureResult("UnsupportedEncodingException\n" + file.getPath());
            } catch (IOException ioe) {
                return new FailureResult("IOException writing\n" + file.getPath());
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ioe) {
                    ioResult = new FailureResult("IOException closing\n" + file.getPath());
                    // Won't be returned if exception was thrown prior to the finally clause executing.
                }
            }
            return ioResult;
        }
    }

    private class ImportTask extends AsyncListeningTask<File, IoResult> {
        public ImportTask(@Nullable Listener<IoResult> listener) {
            super(listener);
        }
        @Override
        public IoResult runInBackground(File file) {
            IoResult ioResult = null;
            InputStreamReader isr = null;
            try {
                FileInputStream fis = new FileInputStream(file);
                isr = new InputStreamReader(fis);

                List<Entry> entries = new IoHelper().inport(isr);

                // Load into database, optionally clearing existing data first. (Retain existing session key).
                if (mIsOverwrite) {
                    mDataSource.deleteAll();
                }
                for (int i = 0; i < entries.size(); i++) {
                    mDataSource.insert(entries.get(i));
                }
                ioResult = new SuccessResult("Imported " + entries.size() + " entries");
            } catch (FileNotFoundException fnfe) {
                return new FailureResult("FileNotFound opening\n" + file.getPath());
            } catch (JsonIOException jioe) {
                return new FailureResult("JsonIOException reading\n" + file.getPath());
            } catch (JsonSyntaxException jse) {
                return new FailureResult("JsonSyntaxException parsing\n" + file.getPath());
            } finally {
                try {
                    if (isr != null) {
                        isr.close();
                    }
                } catch (IOException ioe) {
                    ioResult = new FailureResult("IOException closing\n" + file.getPath());
                    // Won't be returned if exception was thrown prior to the finally clause executing.
                }
            }
            return ioResult;
        }
    }

}
