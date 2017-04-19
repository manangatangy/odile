package com.wolfie.odile.presenter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wolfie.odile.R;
import com.wolfie.odile.model.loader.AsyncListeningTask;
import com.wolfie.odile.model.loader.LoaderResult;
import com.wolfie.odile.view.ActionSheetUi;
import com.wolfie.odile.presenter.DrivePresenter.DriveUi;
import com.wolfie.odile.view.fragment.ListFragment;

import java.io.File;

import static com.wolfie.odile.presenter.SettingsPresenter.PREF_SESSION_BACKUP_EMAIL_ADDRESS;

/**
 */
public class DrivePresenter extends BasePresenter<DriveUi>
        implements AsyncListeningTask.Listener<LoaderResult>{

    private final static String KEY_DRIVE_ACTION_SHEET_SHOWING = "KEY_DRIVE_ACTION_SHEET_SHOWING";

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

    public void onShow() {
    }

    public void onClickSelect() {
        getUi().dismissKeyboard(false);
        getUi().clearErrorMessage();

//        File ioFile = getFile();

        MainPresenter mainPresenter = getUi().findPresenter(null);
        if (mainPresenter != null) {
//            IoLoader ioLoader = mainPresenter.getIoLoader();
//            ioLoader.inport(getUi().isOverwrite(), ioFile, this);
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
        TYPE_SHEET,
        TYPE_JSON
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

    }
}
