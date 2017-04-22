package com.wolfie.odile.model.loader;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.drive.DriveId;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.wolfie.odile.model.database.Source;
import com.wolfie.odile.presenter.MainPresenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SheetLoader {

    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS_READONLY };

    private Source mDataSource;
    private Context mContext;

    // During restore, optionally delete existing entries first.
    private boolean mIsOverwrite;
    private GoogleAccountCredential mGoogleAccountCredential;

    public SheetLoader(Context context, Source dataSource) {
        mContext = context;
        mDataSource = dataSource;
    }

    public void restore(boolean isOverwrite, DriveId driveId,
                        GoogleAccountCredential googleAccountCredential,
                        AsyncListeningTask.Listener<LoaderResult> listener) {
        mIsOverwrite = isOverwrite;
        mGoogleAccountCredential = googleAccountCredential;
        new SheetLoader.RestoreFromSheetTask(listener).execute(driveId);
    }

    // Google sheet http://stackoverflow.com/a/42424918
    // Android google api: https://developers.google.com/android/guides/api-client
    // Downloading docs using Drive api (web): https://developers.google.com/drive/v3/web/manage-downloads#downloading_google_documents
    // Google sheets: https://developers.google.com/sheets/
    // Wes discusses gdata cf v4 api: http://stackoverflow.com/a/42546269
    // Determining mimetype: https://developers.google.com/drive/android/metadata
    // Wes discusses mimetype: http://stackoverflow.com/a/38406284
    // Wes has python demo export as csv: http://wescpy.blogspot.com.au/2016/07/exporting-google-sheet--as-csv.html
    // Sheets demo: https://developers.google.com/sheets/api/quickstart/android

    private class RestoreFromSheetTask extends AsyncListeningTask<DriveId, LoaderResult> {

        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        public RestoreFromSheetTask(@Nullable Listener<LoaderResult> listener) {
            super(listener);
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, mGoogleAccountCredential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        @Override
        public LoaderResult runInBackground(DriveId driveId) {
            LoaderResult driveResult = null;
            try {
                List<String> list = getDataFromSheet(driveId);
                Log.i(MainPresenter.TAG, TextUtils.join("\n", list));
                driveResult = LoaderResult.makeSuccess("Restored sheet");
            } catch (IOException ioe) {
                driveResult = LoaderResult.makeFailure("IOException closing Google Drive file");
            }
            return driveResult;

//            DriveFile driveFile = driveId.asDriveFile();
//            DriveApi.DriveContentsResult driveContentsResult =
//                    driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
//            Log.i(MainPresenter.TAG, "driveFile.open");
//            if (!driveContentsResult.getStatus().isSuccess()) {
//                return LoaderResult.makeFailure("Can't open Google Drive file");
//            }
//            DriveContents driveContents = driveContentsResult.getDriveContents();
//            Log.i(MainPresenter.TAG, "getDriveContents");
//            LoaderResult driveResult = null;
//            InputStreamReader isr = null;
//            try {
//                isr = new InputStreamReader(driveContents.getInputStream());
//                Log.i(MainPresenter.TAG, "getInputStream");
//                List<Phrase> phrases = new IoHelper().inport(isr);
//                Log.i(MainPresenter.TAG, "inport");
//                // Load into database, optionally clearing existing data first.
//                if (mIsOverwrite) {
//                    mDataSource.deleteAll();
//                }
//                for (int i = 0; i < phrases.size(); i++) {
//                    Phrase phrase = phrases.get(i);
//                    if (phrase != null) {
//                        // Protect against empty last record, caused by trailing comma in json
//                        mDataSource.insert(phrase);
//                    }
//                }
//                driveResult = LoaderResult.makeSuccess("Restored from Google Drive " + phrases.size() + " phrases");
//            } catch (JsonIOException jioe) {
//                return LoaderResult.makeFailure("JsonIOException reading Google Drive file");
//            } catch (JsonSyntaxException jse) {
//                return LoaderResult.makeFailure("JsonSyntaxException parsing Google Drive file");
//            } finally {
//                driveContents.discard(getGoogleApiClient());
//                try {
//                    if (isr != null) {
//                        isr.close();
//                    }
//                } catch (IOException ioe) {
//                    driveResult = LoaderResult.makeFailure("IOException closing Google Drive file");
//                    // Won't be returned if exception was thrown prior to the finally clause executing.
//                }
//            }
//            return driveResult;
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
            String range = "Class Data!A2:E";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                results.add("Name, Major");
                for (List row : values) {
                    results.add(row.get(0) + ", " + row.get(4));
                }
            }
            return results;
        }

        /**
         */
        private List<String> getDataFromSheet(DriveId driveId) throws IOException {
            String spreadsheetId = driveId.getResourceId();
            String range = "!A2:C";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                for (List row : values) {
                    results.add(getField(0, row) + ", " + getField(1, row) + ", " + getField(2, row));
                }
            }
            return results;
        }

        private String getField(int i, List row) {
            return (i < row.size()) ? row.get(i).toString() : "";
        }

    }

}

