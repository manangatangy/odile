package com.wolfie.odile.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wolfie.odile.R;
import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.loader.AsyncListeningTask;
import com.wolfie.odile.view.ActionSheetUi;
import com.wolfie.odile.view.BaseUi;
import com.wolfie.odile.presenter.EditPresenter.EditUi;
import com.wolfie.odile.view.fragment.ListFragment;

public class EditPresenter extends BasePresenter<EditUi> implements
        AsyncListeningTask.Listener<Boolean> {

    private final static String KEY_EDIT_ACTION_SHEET_SHOWING = "KEY_EDIT_ACTION_SHEET_SHOWING";
    private final static String KEY_EDIT_ACTION_SHEET_ENTRY = "KEY_EDIT_ACTION_SHEET_ENTRY";

    private Phrase mPhrase;
    private boolean mIsShowing;

    public EditPresenter(EditUi editUi) {
        super(editUi);
    }

    @Override
    public void resume() {
        super.resume();
        if (!mIsShowing) {
            getUi().hide();
        } else {
            getUi().show();
        }
    }

    // TODO - as the key field (name) is edited to change from original, then the delete
    // key is renamed to save-as, and has semantics of creating a new entry, not modifying
    // the current entry (which is what the save button would do).

    @Override
    public void pause() {
        super.pause();
        mIsShowing = getUi().isShowing();
        getUi().dismissKeyboard(false);
    }

    @Override
    public void onSaveState(Bundle outState) {
        outState.putBoolean(KEY_EDIT_ACTION_SHEET_SHOWING, mIsShowing);
        // TODO save/restore Entry being editted
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        mIsShowing = savedState.getBoolean(KEY_EDIT_ACTION_SHEET_SHOWING, false);
    }

    /**
     * Use the existing values in the Phrase to populate the fields, and show the view.
     * A null entry is allowed, it means create a new phrase with all empty fields for editing.
     */
    public void editPhrase(@Nullable Phrase entry) {
        mPhrase = (entry != null) ? entry : Phrase.create("", "", "", "", null);
        getUi().show();
    }

    /**
     * Create a new Phrase for the specified group, and show its fields for editing.
     * If the groupname is name, then set all the fields to empty string
     */
    public void editNewPhrase(@Nullable String groupName) {
        editPhrase(groupName == null ? null : Phrase.create(groupName, "", "", "", null));
    }

    /**
     * This is not called on resume, only when the view is shown by the user.
     */
    public void onShow() {
        getUi().enableDeleteButton(!mPhrase.isNew());
        getUi().setTitleText(mPhrase.isNew() ? "Create Phrase" : "Modify Phrase");
        getUi().clearErrorMessage();
        getUi().clearDescription();
        getUi().setTextValues(mPhrase);
    }

    public void onClickSave() {
        mPhrase = getUi().getTextValues(mPhrase);
        getUi().dismissKeyboard(true);

        MainPresenter mainPresenter = getUi().findPresenter(null);
        if (mPhrase.isNew()) {
            mainPresenter.getPhraseLoader().insert(mPhrase, this);
        } else {
            mainPresenter.getPhraseLoader().update(mPhrase, this);
        }
        // TODO after edit, contract the view box
    }

    public void hide() {
        getUi().hide();
    }

    public void onClickDelete() {
        mPhrase = getUi().getTextValues(mPhrase);
        getUi().dismissKeyboard(true);

        MainPresenter mainPresenter = getUi().findPresenter(null);
        mainPresenter.getPhraseLoader().delete(mPhrase, this);
    }

    public void onClickCancel() {
        getUi().dismissKeyboard(true);
        if (!showErrorIfModified()) {
            getUi().hide();
        }
    }

    @Override
    public boolean backPressed() {
        if (!getUi().isShowing() || getUi().isKeyboardVisible()) {
            return true;        // Means: not consumed here
        }
        // Check if fields have been modified and show error message if so, else close.
        if (!showErrorIfModified()) {
            getUi().hide();
        }
        return false;
    }

    private boolean showErrorIfModified() {
        boolean isModified = getUi().isPhraseModified(mPhrase);
        if (isModified) {
            getUi().setErrorMessage(R.string.st010);
        }
        return isModified;
    }

    /**
     * callback from getPhraseLoader().insert/update/delete
     */
    @Override
    public void onCompletion(Boolean aBoolean) {
        ListPresenter listPresenter = getUi().findPresenter(ListFragment.class);
        if (listPresenter != null) {
            listPresenter.loadPhrases();
        }
    }

    public interface EditUi extends ActionSheetUi {

        void setTitleText(String title);
        void enableDeleteButton(boolean enable);
        void setTextValues(Phrase phrase);
        Phrase getTextValues(Phrase phrase);
        boolean isPhraseModified(Phrase phrase);
        void setDescription(@StringRes int resourceId);
        void clearDescription();
        void setErrorMessage(@StringRes int resourceId);
        void clearErrorMessage();

    }
}
