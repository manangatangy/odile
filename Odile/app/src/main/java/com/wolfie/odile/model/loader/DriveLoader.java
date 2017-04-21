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

    private Source mDataSource;
    private Context mContext;

    // During restore, optionally delete existing entries first.
    private boolean mIsOverwrite;

    public DriveLoader(Context context, Source dataSource) {
        mContext = context;
        mDataSource = dataSource;
    }

    public void restore(boolean isOverwrite, DriveId driveId, String accountName,
                        AsyncListeningTask.Listener<LoaderResult> listener) {
        mIsOverwrite = isOverwrite;
        new RestoreFromDriveTask(accountName, listener).execute(driveId);
    }

    @Deprecated
    public void restore(boolean isOverwrite, DriveId driveId, AsyncListeningTask.Listener<LoaderResult> listener) {
        mIsOverwrite = isOverwrite;
        new RestoreFromDriveTask("david.x.weiss@gmail.com", listener).execute(driveId);
    }

    private class RestoreFromDriveTask extends AsyncConnectedTask<DriveId, LoaderResult> {
        public RestoreFromDriveTask(String accountName, @Nullable Listener<LoaderResult> listener) {
            super(mContext, accountName, listener);
        }

        @Override
        public LoaderResult runInBackgroundConnected(DriveId driveId) {
            Log.i(MainPresenter.TAG, "runInBackgroundConnected");
            DriveFile driveFile = driveId.asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult =
                    driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            Log.i(MainPresenter.TAG, "driveFile.open");
            if (!driveContentsResult.getStatus().isSuccess()) {
                return LoaderResult.makeFailure("Can't open Google Drive file");
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            Log.i(MainPresenter.TAG, "getDriveContents");
            LoaderResult driveResult = null;
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
                    Phrase phrase = phrases.get(i);
                    if (phrase != null) {
                        // Protect against empty last record, caused by trailing comma in json
                        mDataSource.insert(phrase);
                    }
                }
                driveResult = LoaderResult.makeSuccess("Restored from Google Drive " + phrases.size() + " phrases");
            } catch (JsonIOException jioe) {
                return LoaderResult.makeFailure("JsonIOException reading Google Drive file");
            } catch (JsonSyntaxException jse) {
                return LoaderResult.makeFailure("JsonSyntaxException parsing Google Drive file");
            } finally {
                driveContents.discard(getGoogleApiClient());
                try {
                    if (isr != null) {
                        isr.close();
                    }
                } catch (IOException ioe) {
                    driveResult = LoaderResult.makeFailure("IOException closing Google Drive file");
                    // Won't be returned if exception was thrown prior to the finally clause executing.
                }
            }
            return driveResult;
        }
    }

}
