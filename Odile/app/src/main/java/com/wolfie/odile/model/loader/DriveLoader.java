package com.wolfie.odile.model.loader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.wolfie.odile.model.IoHelper;
import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.database.Source;
import com.wolfie.odile.presenter.MainPresenter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class DriveLoader {

    public class DriveResult {
        public String mSuccessMessage;
        public String mFailureMessage;
    }

    private Source mDataSource;
    private Context mContext;

    // During restore, optionally delete existing entries first.
    private boolean mIsOverwrite;

    public DriveLoader(Context context, Source dataSource) {
        mContext = context;
        mDataSource = dataSource;
    }

    public void restore(boolean isOverwrite, DriveId driveId, AsyncListeningTask.Listener<DriveResult> listener) {
        mIsOverwrite = isOverwrite;
        new RestoreFromDriveTask(listener).execute(driveId);
    }

    public class SuccessResult extends DriveResult {
        public SuccessResult(String successMessage) {
            mSuccessMessage = successMessage;
        }
    }

    public class FailureResult extends DriveResult {
        public FailureResult(String failureMessage) {
            mFailureMessage = failureMessage;
        }
    }

    private class RestoreFromDriveTask extends AsyncConnectedTask<DriveId, DriveResult> {
        public RestoreFromDriveTask(@Nullable Listener<DriveResult> listener) {
            super(mContext, listener);
        }

        @Override
        public DriveResult runInBackgroundConnected(DriveId driveId) {
            Log.i(MainPresenter.TAG, "runInBackgroundConnected");
            DriveFile driveFile = driveId.asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult =
                    driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            Log.i(MainPresenter.TAG, "driveFile.open");
            if (!driveContentsResult.getStatus().isSuccess()) {
                return new FailureResult("Can't open Google Drive file");
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            Log.i(MainPresenter.TAG, "getDriveContents");
            DriveResult driveResult = null;
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(driveContents.getInputStream());
                Log.i(MainPresenter.TAG, "getInputStream");
                List<Phrase> phrases = new IoHelper().inport(isr);
                Log.i(MainPresenter.TAG, "inport");
                // Load into database, optionally clearing existing data first.
                if (mIsOverwrite) {
                    mDataSource.deleteAll();
                }
                for (int i = 0; i < phrases.size(); i++) {
                    mDataSource.insert(phrases.get(i));
                }
                driveResult = new SuccessResult("Restored from Google Drive " + phrases.size() + " phrases");
            } catch (JsonIOException jioe) {
                return new FailureResult("JsonIOException reading Google Drive file");
            } catch (JsonSyntaxException jse) {
                return new FailureResult("JsonSyntaxException parsing Google Drive file");
            } finally {
                driveContents.discard(getGoogleApiClient());
                try {
                    if (isr != null) {
                        isr.close();
                    }
                } catch (IOException ioe) {
                    driveResult = new FailureResult("IOException closing Google Drive file");
                    // Won't be returned if exception was thrown prior to the finally clause executing.
                }
            }
            return driveResult;
        }
    }

}
