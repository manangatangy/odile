package com.wolfie.odile.presenter;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.wolfie.odile.R;
import com.wolfie.odile.model.loader.AsyncListeningTask;
import com.wolfie.odile.model.loader.DriveLoader;
import com.wolfie.odile.model.loader.IoLoader;
import com.wolfie.odile.model.loader.LoaderResult;
import com.wolfie.odile.model.loader.SheetLoader;
import com.wolfie.odile.view.ActionSheetUi;
import com.wolfie.odile.presenter.DrivePresenter.DriveUi;
import com.wolfie.odile.view.fragment.ListFragment;

import java.util.Arrays;

import static android.app.Activity.RESULT_OK;
import static com.wolfie.odile.view.activity.OdileActivity.REQUEST_DRIVE_RESOLUTION;

/**
 */
public class DrivePresenter extends BasePresenter<DriveUi> implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        AsyncListeningTask.Listener<LoaderResult> {

    public static final String GET_ACCOUNTS_PERMISSION = Manifest.permission.GET_ACCOUNTS;
    private final static String KEY_DRIVE_ACTION_SHEET_SHOWING = "KEY_DRIVE_ACTION_SHEET_SHOWING";
    private final static String TAG = "DrivePresenter";

    private boolean mIsShowing;

    public DrivePresenter(DriveUi driveUi) {
        super(driveUi);
    }

    public void init() {
        getUi().setFileType(FileType.TYPE_SHEET);
        getUi().show();
    }

    @Override
    public void resume() {
        super.resume();
        if (!mIsShowing) {
            hide();
        } else {
            // The user may have altered media/storage-access while we were paused, must re-check
            getUi().show();
        }
    }

    @Override
    public void pause() {
        super.pause();
        disconnectGoogleApiClient();
        mIsShowing = getUi().isShowing();
        getUi().dismissKeyboard(false);
    }

    @Override
    public void onSaveState(Bundle outState) {
        outState.putBoolean(KEY_DRIVE_ACTION_SHEET_SHOWING, mIsShowing);
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        mIsShowing = savedState.getBoolean(KEY_DRIVE_ACTION_SHEET_SHOWING, false);
    }

    public void onRequestFileTypeSelect(FileType requestedFileType) {
        // Can intercept radio-button clicking so that rather than just letting the radio
        // button be changed, we can perform processing and then programatically determine
        // whether or not to change the radio button state.
        // This feature isn't used here yet (ref to FilePresenter to see how it's used).
        getUi().setFileType(requestedFileType);
    }

    private FileType mFileType;

    private GoogleAccountCredential mGoogleAccountCredential;
    private GoogleApiClient mGoogleApiClient;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] CREDENTIAL_OAUTH2_SCOPES = { SheetsScopes.SPREADSHEETS_READONLY };

    public void onShow() {
    }

    public void onClickSelect() {
        getUi().dismissKeyboard(false);
        getUi().clearErrorMessage();
        mFileType = getUi().getFileType();
//        if (mFileType == FileType.TYPE_JSON) {
//            connectGoogleApiClient();
//        } else {
            // Initialize credentials and service object.
            mGoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
                    getContext(),
                    Arrays.asList(CREDENTIAL_OAUTH2_SCOPES)).setBackOff(new ExponentialBackOff());


        checkPreconditionsAndSelectDriveFile();
//        }
    }

    /**
     * Attempt to connect the GoogleApiClient, after verifying that all the
     * preconditions are satisfied. The preconditions are: Google Play Services
     * installed, an account was selected and the device currently has online access.
     * If any of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void checkPreconditionsAndSelectDriveFile() {
//        if (! isGooglePlayServicesAvailable()) {
//            acquireGooglePlayServices();
//        } else
        if (mGoogleAccountCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! getUi().isDeviceOnline()) {
            getUi().setErrorMessage(R.string.st052);
        } else {
            connectGoogleApiClient();
//            new SheetsActivity.MakeRequestTask(mCredential).execute();
        }
    }

    // TODO
    // 1. define String getAccountName that uses local member (mAccountName) and also checks the
    // prefs if local value in null (which happens after restore). If the member has not yet been
    // stored in prefs, then everytime this method is called, it will check the prefs, which is
    // a slight inefficiency.
    // 2. also define a setAccountName method which stores to prefs as well as local member.
    // 3. use the getAccountName value instead of mGoogleAccountCredential.getSelectedAccountName()
    // 4. define method getGoogleAccountCredential that is a lazy creator.
    // 5. remove member mGoogleAccountCredential and use lazy creator for newChooseAccountIntent
    // and for SheetLoader.restore
    // 6. use correct sheet id

    private void chooseAccount() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), GET_ACCOUNTS_PERMISSION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getUi().getActivity(), GET_ACCOUNTS_PERMISSION)) {
                getUi().setErrorMessage(R.string.st050);
            } else {
                getUi().requestGetAccountsPermissions();
            }
        } else {
            String accountName = getUi().getActivity().getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName == null) {
                // Start a dialog from which the user can choose an account
                getUi().requestAccountPicker(mGoogleAccountCredential.newChooseAccountIntent());
            } else {
                mGoogleAccountCredential.setSelectedAccountName(accountName);
                checkPreconditionsAndSelectDriveFile();            // Continue to next step.
            }
        }
    }

    public void onRequestGetAccountsPermissionsResult(int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay!
            checkPreconditionsAndSelectDriveFile();            // Continue to next step.
        } else {
            getUi().setErrorMessage(R.string.st051);
        }
    }

    public void onRequestAccountPickerResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && intent != null && intent.getExtras() != null) {
            String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                SharedPreferences settings = getUi().getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_ACCOUNT_NAME, accountName);
                editor.apply();
                mGoogleAccountCredential.setSelectedAccountName(accountName);
                checkPreconditionsAndSelectDriveFile();            // Continue to next step.
            }
        }
    }

    private void connectGoogleApiClient() {
        // At this stage, an account name must have been selected and stored by
        // chooseAccount/onRequestAccountPickerResult, so no need to null check.
        String accountName = getUi().getActivity()
                .getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);
        disconnectGoogleApiClient();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .setAccountName(accountName)      // This inhbits account-select dialog
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
    }

    private void disconnectGoogleApiClient() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClient = null;
    }

    /**
     * On the first connect, the connection will fail with SIGN_IN_REQUIRED. The resolution
     * to this will ask the user to select an Account from the device, to be used th sign in.
     * This Account will be stored as the default-account for the app, and will be reused
     * automatically as needed.  The default sign in account will indeed be needed, when
     * the {@link com.wolfie.odile.model.loader.AsyncConnectedTask} creates its own
     * api-client (in a background thread).
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        mOdileActivity.hideLoadingOverlay();
        if (connectionResult.hasResolution()) {
            getUi().requestDriveResolution(connectionResult);
        } else {
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(getUi().getActivity(), connectionResult.getErrorCode(), 0).show();
        }
    }

    public void onRequestDriveResolutionResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            connectGoogleApiClient();
        } else {
            Toast.makeText(getContext(), "Can't resolve Google Drive connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
//        mOdileActivity.hideLoadingOverlay();
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
//        mOdileActivity.hideLoadingOverlay();
        Log.i(TAG, "GoogleApiClient connected");
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(mFileType.getMimeType())
                .build(mGoogleApiClient);
        getUi().requestDriveOpener(intentSender);
    }

    public void onRequestDriveOpenerResult(int resultCode, Intent intent) {
        DriveId driveId = null;
        if (resultCode == RESULT_OK) {
            driveId = intent.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
        }
        if (driveId == null) {
            getUi().showBanner("No Google Drive file was selected");
        } else {
            boolean isOverwrite = getUi().isOverwrite();
            MainPresenter mainPresenter = getUi().findPresenter(null);
            if (mainPresenter != null) {
//            mOdileActivity.showLoadingOverlay();
                // accountName is available in
                String accountName = getUi().getActivity()
                        .getPreferences(Context.MODE_PRIVATE)
                        .getString(PREF_ACCOUNT_NAME, null);
                if (mFileType == FileType.TYPE_JSON) {
                    mainPresenter.getDriveLoader().restore(isOverwrite, driveId,
                            accountName, this);
                } else {
                    mainPresenter.getSheetLoader().restore(isOverwrite, driveId,
                            mGoogleAccountCredential, this);
                }
            }
        }
    }

    @Override
    public void onCompletion(LoaderResult ioResult) {
        if (ioResult.mFailureMessage != null) {
            getUi().setErrorMessage(ioResult.mFailureMessage);
        }
        if (ioResult.mSuccessMessage != null) {
            // Refresh list
            ListPresenter listPresenter = getUi().findPresenter(ListFragment.class);
            listPresenter.loadPhrases();
            getUi().showBanner(ioResult.mSuccessMessage);       // Result of file i/o
            hide();     // Close the action sheet.
        }
    }

    public void onRequestAuthorizationResult(int resultCode, Intent intent) {
    }

    public void onClickCancel() {
        getUi().dismissKeyboard(true);
    }

    @Override
    public boolean backPressed() {
        if (!getUi().isShowing() || getUi().isKeyboardVisible()) {
            return true;        // Means: not consumed here
        }
        hide();
        return false;
    }

    public void hide() {
        getUi().hide();
    }


    public enum FileType {
        TYPE_SHEET(new String[] { "application/vnd.google-apps.spreadsheet" }),
        TYPE_JSON(new String[] { "text/plain", "text/html" });

        private String[] mMimeType;

        FileType(String[] mimeType) {
            mMimeType = mimeType;
        }

        public String[] getMimeType() {
            return mMimeType;
        }
    }

    public interface DriveUi extends ActionSheetUi {

        void setTitleText(@StringRes int resourceId);
        void setDescription(@StringRes int resourceId);
        void clearDescription();
        void setErrorMessage(@StringRes int resourceId);
        void setErrorMessage(String text);
        void clearErrorMessage();

        boolean isOverwrite();
        void setFileType(FileType fileType);
        FileType getFileType();

        void requestAccountPicker(Intent intent);
        void requestAuthorization(Intent intent);
        void requestDriveResolution(ConnectionResult connectionResult);
        void requestDriveOpener(IntentSender intentSender);
        void requestGetAccountsPermissions();

        boolean isDeviceOnline();
    }
}
