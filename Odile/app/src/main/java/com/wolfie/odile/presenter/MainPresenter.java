package com.wolfie.odile.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.wolfie.odile.model.database.Helper;
import com.wolfie.odile.model.database.Source;
import com.wolfie.odile.model.loader.AsyncListeningTask;
import com.wolfie.odile.model.loader.DriveLoader;
import com.wolfie.odile.model.loader.IoLoader;
import com.wolfie.odile.model.loader.PhraseLoader;
import com.wolfie.odile.view.BaseUi;
import com.wolfie.odile.view.activity.BaseActivity;
import com.wolfie.odile.view.fragment.ListFragment;

import static com.wolfie.odile.view.activity.OdileActivity.REQUEST_DRIVE_OPENER;
import static com.wolfie.odile.view.activity.OdileActivity.REQUEST_DRIVE_RESOLUTION;

/**
 * The MainPresenter doesn't use a gui, so the BaseUi parameter to the ctor can be null.
 * It extends BasePresenter simply so that it can be returned by BaseFragment.findPresenter.
 */
public class MainPresenter extends BasePresenter<BaseUi> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AsyncListeningTask.Listener<DriveLoader.DriveResult> {

    private static final String TAG = "OdileMainPresenter";

    private Helper mHelper;
    private SQLiteDatabase mDatabase;
    private Source mSource;
    private IoLoader mIoLoader;
    private PhraseLoader mPhraseLoader;
    private DriveLoader mDriveLoader;

    // This presenter cannot use getUi() no ui (all the ui is performed by the other frags)
    public MainPresenter(BaseUi baseUi, Context context) {
        super(baseUi);

        mHelper = new Helper(context);
        mDatabase = mHelper.getWritableDatabase();
        mSource = new Source(mDatabase);
        mIoLoader = new IoLoader(mSource);
        mPhraseLoader = new PhraseLoader(mSource);
        mDriveLoader = new DriveLoader(context, mSource);
    }

    public IoLoader getIoLoader() {
        return mIoLoader;
    }

    public PhraseLoader getPhraseLoader() {
        return mPhraseLoader;
    }

    /**
     * The Google drive code is here because it doesn't need any UI (ie Fragment), but it
     * does need onActivityResult handler.  Since there are visibility clashes between
     * AppCompatActivity.onDestroy and BasePresenter.onDestroy, we hack it by directly
     * coupling the OdileActivity and the MainPresenter.
     */
    public void setActivity(BaseActivity baseActivity) {
        mBaseActivity = baseActivity;
    }

    private BaseActivity mBaseActivity;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void pause() {
        super.pause();
        disconnect();
    }

    private void disconnect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClient = null;
    }

    public void restoreFromGoogleDrive() {
        disconnect();
        mGoogleApiClient = new GoogleApiClient.Builder(mBaseActivity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!connectionResult.hasResolution()) {      // Show the localized error dialog.
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(mBaseActivity, connectionResult.getErrorCode(), 0).show();
        } else {
            try {
                connectionResult.startResolutionForResult(mBaseActivity, REQUEST_DRIVE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while starting resolution activity", e);
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { "text/plain", "text/html" })
                .build(mGoogleApiClient);
        try {
            mBaseActivity.startIntentSenderForResult(intentSender, REQUEST_DRIVE_OPENER, null, 0, 0, 0);
            // Calls back ==> retrieveFileContents()
        } catch (IntentSender.SendIntentException e) {
            Log.w(TAG, "Unable to send intent", e);
        }
    }

    public void retrieveFileContents(DriveId driveId) {
        mDriveLoader.restore(false, driveId, this);
    }

    @Override
    public void onCompletion(DriveLoader.DriveResult driveResult) {
        if (driveResult.mFailureMessage != null) {
            showMessage(driveResult.mFailureMessage);
        }
        if (driveResult.mSuccessMessage != null) {
            // Refresh list
            ListPresenter listPresenter = mBaseActivity.findPresenter(ListFragment.class);
            listPresenter.loadPhrases();
            showMessage(driveResult.mSuccessMessage);
        }
    }

    public void showMessage(String message) {
        Toast.makeText(mBaseActivity, message, Toast.LENGTH_LONG).show();
    }

}
