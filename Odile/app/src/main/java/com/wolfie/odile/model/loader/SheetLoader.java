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
import com.wolfie.odile.model.Phrase;
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
                List<Phrase> phrases = getDataFromSheet(driveId);

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
                driveResult = LoaderResult.makeSuccess("Restored from Google Sheets " + phrases.size() + " phrases");

            } catch (IOException ioe) {
                driveResult = LoaderResult.makeFailure("IOException reading Google Sheets file");
            }
            return driveResult;
        }

        private List<Phrase> getDataFromSheet(DriveId driveId) throws IOException {
            String spreadsheetId = driveId.getResourceId();
            String range = "!A2:C";
            List<Phrase> phrases = new ArrayList<Phrase>();
            ValueRange response = this.mService.spreadsheets().values().get(spreadsheetId, range).execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                String group = null;
                for (List row : values) {
                    String russian = getField(0, row);
                    String english = getField(1, row);
                    String translit = getField(2, row);
                    // If there is only one field value (that's not empty or starts with
                    // a dash, then the value is the new current group. No phrases are to
                    // be created until a group record is encountered.
                    if (russian != null && english == null && translit == null) {
                        group = russian;
                    } else if (group != null && russian != null && english != null) {
                        if (translit == null) {
                            translit = "";
                        }
                        Phrase phrase = Phrase.create(group, russian, english, translit,  null);
                        phrases.add(phrase);
                    }
                }
            }
            return phrases;
        }

        private String getField(int i, List row) {
            // Returns null if the field is empty, blank, or starts with dash character.
            String field = null;
            if (i < row.size()) {
                field = row.get(i).toString();
                if (field.startsWith("-")) {
                    field = null;
                }
            }
            return field;
        }
    }

}

