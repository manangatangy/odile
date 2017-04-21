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
import com.wolfie.odile.model.loader.LoaderResult;
import com.wolfie.odile.model.loader.PhraseLoader;
import com.wolfie.odile.model.loader.SheetLoader;
import com.wolfie.odile.view.BaseUi;
import com.wolfie.odile.view.activity.BaseActivity;
import com.wolfie.odile.view.activity.OdileActivity;
import com.wolfie.odile.view.activity.SimpleActivity;
import com.wolfie.odile.view.fragment.DrawerFragment;
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
        AsyncListeningTask.Listener<LoaderResult> {

    public static final String TAG = "OdileMainPresenter";

    private Helper mHelper;
    private SQLiteDatabase mDatabase;
    private Source mSource;
    private IoLoader mIoLoader;
    private PhraseLoader mPhraseLoader;
    private DriveLoader mDriveLoader;
    private SheetLoader mSheetLoader;

    // This presenter cannot use getUi() no ui (all the ui is performed by the other frags)
    public MainPresenter(BaseUi baseUi, Context context) {
        super(baseUi);

        mHelper = new Helper(context);
        mDatabase = mHelper.getWritableDatabase();
        mSource = new Source(mDatabase);
        mIoLoader = new IoLoader(mSource);
        mPhraseLoader = new PhraseLoader(mSource);
        mDriveLoader = new DriveLoader(context, mSource);
        mSheetLoader = new SheetLoader(context, mSource);
    }

    public IoLoader getIoLoader() {
        return mIoLoader;
    }

    public PhraseLoader getPhraseLoader() {
        return mPhraseLoader;
    }

    public DriveLoader getDriveLoader() {
        return mDriveLoader;
    }

    public SheetLoader getSheetLoader() {
        return mSheetLoader;
    }

    /**
     * The Google drive code is here because it doesn't need any UI (ie Fragment), but it
     * does need onActivityResult handler.  Since there are visibility clashes between
     * AppCompatActivity.onDestroy and BasePresenter.onDestroy, we hack it by directly
     * coupling the OdileActivity and the MainPresenter.
     */
    public void setActivity(OdileActivity odileActivity) {
        mOdileActivity = odileActivity;
    }

    private OdileActivity mOdileActivity;
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

    // entry point
    public void restoreFromGoogleDrive() {
        mOdileActivity.showLoadingOverlay();
        disconnect();
        mGoogleApiClient = new GoogleApiClient.Builder(mOdileActivity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .setAccountName("david.x.weiss@gmail.com")      // This inhbits account-select dialog
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mOdileActivity.hideLoadingOverlay();
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mOdileActivity.hideLoadingOverlay();
        if (!connectionResult.hasResolution()) {      // Show the localized error dialog.
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(mOdileActivity, connectionResult.getErrorCode(), 0).show();
        } else {
            try {
                // This will open an account picker if no account yet selected.
                connectionResult.startResolutionForResult(mOdileActivity, REQUEST_DRIVE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while starting resolution activity", e);
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mOdileActivity.hideLoadingOverlay();
        Log.i(TAG, "GoogleApiClient connected");
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { "text/plain", "text/html", "application/vnd.google-apps.spreadsheet" })
                .build(mGoogleApiClient);
        try {
            mOdileActivity.showLoadingOverlay();
            mOdileActivity.startIntentSenderForResult(intentSender, REQUEST_DRIVE_OPENER, null, 0, 0, 0);
            // Calls back ==> retrieveFileContents()
        } catch (IntentSender.SendIntentException e) {
            mOdileActivity.hideLoadingOverlay();
            Log.w(TAG, "Unable to send intent", e);
        }
    }

    public void retrieveFileContents(@Nullable DriveId driveId) {
        mOdileActivity.hideLoadingOverlay();
        if (driveId == null) {
            showMessage("No Google Drive file was selected");
        } else {
            mOdileActivity.showLoadingOverlay();
            boolean isSheet = false;
            if (isSheet) {
                mSheetLoader.restore(true, driveId, this);
            } else {
                mDriveLoader.restore(true, driveId, this);
            }
        }
    }

    @Override
    public void onCompletion(LoaderResult loaderResult) {
        if (loaderResult.mFailureMessage != null) {
            showMessage(loaderResult.mFailureMessage);
        } else if (loaderResult.mSuccessMessage != null) {
            // Refresh list
            ListPresenter listPresenter = mOdileActivity.findPresenter(ListFragment.class);
            listPresenter.loadPhrases();
            showMessage(loaderResult.mSuccessMessage);
        }
        mOdileActivity.closeMenuDrawer();
        mOdileActivity.hideLoadingOverlay();
    }

    public void showMessage(String message) {
        Toast.makeText(mOdileActivity, message, Toast.LENGTH_LONG).show();
    }

}
