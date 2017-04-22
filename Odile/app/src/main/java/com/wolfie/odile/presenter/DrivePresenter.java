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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.wolfie.odile.R;
import com.wolfie.odile.model.loader.AsyncListeningTask;
import com.wolfie.odile.model.loader.LoaderResult;
import com.wolfie.odile.view.ActionSheetUi;
import com.wolfie.odile.presenter.DrivePresenter.DriveUi;
import com.wolfie.odile.view.fragment.ListFragment;

import java.util.Arrays;

import static android.app.Activity.RESULT_OK;

/**
 * Support reading data from Google Drive/Sheets.  This is complicated because Drive and Sheets
 * use different apis with different authentication, both needing to know the user account name.
 * It is further complicated because the file selection is performed by the Drive api, which is
 * needed to access a Sheet file. Therefore to restore from a Sheet file requires using both
 * Drive and Sheets protocols.
 *
 * The overall structure of this code is based on two examples.
 * 1) shows how to connect to drive and select a file.  It comes from
 * https://github.com/googledrive/android-demos, which has heaps of demos.
 * The one I used is "Pick a file with opener activity" at
 * https://github.com/googledrive/android-demos/blob/master/app/src/main/java/com/google/android/gms/drive/sample/demo/PickFileWithOpenerActivity.java
 * from which I developed the {@link com.wolfie.odile.model.loader.AsyncConnectedTask} and the use
 * of GoogleApiClient.
 * 2) https://developers.google.com/sheets/api/quickstart/android
 * This has code to open and read sheet file.
 *
 * Credentials
 * Following https://developers.google.com/sheets/api/quickstart/android and
 * https://support.google.com/googleapi/answer/6158849?hl=en#installedapplications&android
 * I went to https://console.developers.google.com/apis/credentials?project=odile-155002
 * and edited an existing oauth2.0 clientId, renaming it to "Android client 1 - work mbp"
 * since it uses the SHA1 from my work mbp.  There is some useful info about credentials,
 * access, security and identity at https://support.google.com/googleapi/answer/6158857?hl=en
 * Then I moved the app from com.example.quickstart to com.wolfie.odile, altering the
 * manifest and changing the credentials to com.wolfie.odile also.

 * I had to read far and wide to develop this code.  One user in particular was useful
 * http://stackoverflow.com/a/40924056
 * http://stackoverflow.com/a/42546269
 * Both by the same user, who seems well informed (perhaps is a googler).
 */
public class DrivePresenter extends BasePresenter<DriveUi> implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        AsyncListeningTask.Listener<LoaderResult> {

    private final static String TAG = "DrivePresenter";
    private static final String KEY_DRIVE_ACTION_SHEET_SHOWING = "KEY_DRIVE_ACTION_SHEET_SHOWING";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] CREDENTIAL_OAUTH2_SCOPES = { SheetsScopes.SPREADSHEETS_READONLY };
    public static final String PERMISSION_GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;

    private GoogleApiClient mGoogleApiClient;

    private boolean mIsShowing;
    private String mAccountName;                                // Lazy loaded; use getAccountName().

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

    /**
     * Using prefs to hold the account name means not having to save/restore the member value.
     * Slightly inefficient because if no account name have yet been set, then each call to
     * this method will result in a call to read the prefs (which returns null).
     * @return the accont name as stored in the prefs (or null if not yet stored).
     */
    private String getAccountName() {
        if (mAccountName == null) {
            mAccountName = getUi().getActivity()
                    .getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
        }
        return mAccountName;
    }

    /**
     * Stores the account name in the prefs, for the app to use next time.
     * @param accountName
     */
    private void setAccountName(@NonNull String accountName) {
        SharedPreferences settings = getUi().getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.apply();
        mAccountName = accountName;
    }

    private GoogleAccountCredential getGoogleAccountCredential(String accountName) {
        // Initialize credentials and service object.
        GoogleAccountCredential googleAccountCredential = GoogleAccountCredential
                .usingOAuth2(getContext(),
                        Arrays.asList(CREDENTIAL_OAUTH2_SCOPES))
                .setBackOff(new ExponentialBackOff());
        if (accountName != null) {
            googleAccountCredential.setSelectedAccountName(accountName);
        }
        return googleAccountCredential;
    }

    public void onShow() {
    }

    public void onClickSelect() {
        getUi().dismissKeyboard(false);
        getUi().clearErrorMessage();
        chooseAccountAndConnect();
    }

    /**
     * This is the preliminary processing method. It can be re-tried after
     * step succeeds in order to proceed to the next step.
     */
    private void chooseAccountAndConnect() {
        if (getAccountName() == null) {
            chooseAccount();
        } else if (! getUi().isDeviceOnline()) {
            getUi().setErrorMessage(R.string.st052);
        } else {
            connectGoogleApiClient();
        }
    }

    /**
     * Request permission and then use it, to show a list of accounts to the user, who
     * selects one that the app should use (storing it in a user pref). The callbacks
     * are to  onRequestGetAccountsPermissionsResult() and onRequestAccountPickerResult().
     */
    private void chooseAccount() {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), PERMISSION_GET_ACCOUNTS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getUi().getActivity(), PERMISSION_GET_ACCOUNTS)) {
                getUi().setErrorMessage(R.string.st050);
            } else {
                getUi().requestGetAccountsPermissions();
            }
        } else {
            String accountName = getAccountName();
            if (accountName == null) {
                // Start a dialog from which the user can choose an account
                getUi().requestAccountPicker(getGoogleAccountCredential(null).newChooseAccountIntent());
            } else {
                chooseAccountAndConnect();            // Continue to next step.
            }
        }
    }

    public void onRequestGetAccountsPermissionsResult(int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay!
            chooseAccountAndConnect();            // Continue to next step.
        } else {
            getUi().setErrorMessage(R.string.st051);
        }
    }

    public void onRequestAccountPickerResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && intent != null && intent.getExtras() != null) {
            String selectedAccountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (selectedAccountName != null) {
                setAccountName(selectedAccountName);
                chooseAccountAndConnect();            // Continue to next step.
            }
        }
    }

    /**
     * Use the value in {@link #getAccountName()} to connect to google api.
     * Calling back to onConnectionFailed, onConnected or onConnectionSuspended
     */
    private void connectGoogleApiClient() {
        // At this stage, an account name must have been selected and stored by
        // chooseAccount/onRequestAccountPickerResult.
        disconnectGoogleApiClient();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .setAccountName(getAccountName())      // This inhibits account-select dialog
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
     * On the first connect request, the connection will fail with SIGN_IN_REQUIRED. Resolution
     * to this may ask the user to select an Account from the device, to be used to sign in.
     * Since accountName was supplied to the GoogleApiClientBuilder, requestDriveResolution()
     * will proceed to signin with this value.  This Account (whether specified programatically
     * or by the user) will be stored as the default-account for the app, and will be reused
     * automatically as needed.  The default sign in account will indeed be needed, when
     * the {@link com.wolfie.odile.model.loader.AsyncConnectedTask} creates its own
     * api-client (in a background thread).
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            getUi().requestDriveResolution(connectionResult);
        } else {
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(getUi().getActivity(), connectionResult.getErrorCode(), 0).show();
        }
    }

    public void onRequestDriveResolutionResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            // The resolution requested in onConnectionFailed, has succeeded so we attempt
            // to connect again, which this time should callback to onConnected.
            connectGoogleApiClient();
        } else {
            Toast.makeText(getContext(), "Can't resolve Google Drive connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()           // Specify the drive-file-selection activity
                .setMimeType(getUi().getFileType().getMimeType())
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
                if (getUi().getFileType() == FileType.TYPE_JSON) {
                    mainPresenter.getDriveLoader().restore(isOverwrite, driveId,
                            getAccountName(), this);
                } else {
                    mainPresenter.getSheetLoader().restore(isOverwrite, driveId,
                            getGoogleAccountCredential(getAccountName()), this);
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
            // Refresh list in UI from databse.
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
