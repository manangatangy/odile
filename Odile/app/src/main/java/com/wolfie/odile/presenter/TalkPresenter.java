package com.wolfie.odile.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wolfie.odile.R;
import com.wolfie.odile.model.PhraseGroup;
import com.wolfie.odile.presenter.TalkPresenter.TalkUi;
import com.wolfie.odile.talker.StatusHandler;
import com.wolfie.odile.talker.TalkerCommand;
import com.wolfie.odile.talker.TalkerService;
import com.wolfie.odile.talker.TalkerStatus;
import com.wolfie.odile.view.ActionSheetUi;
import com.wolfie.odile.view.activity.OdileActivity;
import com.wolfie.odile.view.activity.ServiceBinder.ServiceBinderListener;
import com.wolfie.odile.view.fragment.ListFragment;

public class TalkPresenter extends BasePresenter<TalkUi>
        implements ServiceBinderListener, StatusHandler.StatusChangeListener {

    private final static String KEY_TALK_ACTION_SHEET_SHOWING = "KEY_TALK_ACTION_SHEET_SHOWING";

    private boolean mIsShowing;
    private boolean mIsSpeaking;

    public TalkPresenter(TalkPresenter.TalkUi talkUi) {
        super(talkUi);
    }

    @Override
    public void resume() {
        super.resume();
        if (!mIsShowing) {
            getUi().hide();
        } else {
            getUi().show();
        }
        getUi().getOdileActivity().setServiceBinderListener(this);
    }

    @Override
    public void pause() {
        super.pause();
        mIsShowing = getUi().isShowing();
        getUi().dismissKeyboard(false);
        getUi().getOdileActivity().setServiceBinderListener(null);
    }

    @Override
    public void onSaveState(Bundle outState) {
        outState.putBoolean(KEY_TALK_ACTION_SHEET_SHOWING, mIsShowing);
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        mIsShowing = savedState.getBoolean(KEY_TALK_ACTION_SHEET_SHOWING, false);
    }

    /**
     * This is not called on resume, only when the view is shown by the user.
     */
    public void onShow() {
        ListPresenter listPresenter = getUi().findPresenter(ListFragment.class);
        PhraseGroup phraseGroup = listPresenter.getDisplayGroups();
        getUi().startService(new TalkerCommand(TalkerCommand.Command.SET_PHRASES, phraseGroup));
    }

    @Override
    public void onServiceBound(TalkerService mBoundTalkerService) {
        mBoundTalkerService.getStatusHandler().addStatusChangeListener(this);
    }
    @Override
    public void onServiceUnBound(TalkerService mBoundTalkerService) {
        mBoundTalkerService.getStatusHandler().removeStatusChangeListener(this);
    }

    @Override
    public void onStatusChange(TalkerStatus talkerStatus) {
        getUi().setTitleText(talkerStatus.getState().title());
        getUi().setSubTitleText(talkerStatus.getSubTitle());
        String desc = (talkerStatus.getState() == TalkerStatus.State.STOPPED) ? ""
                : talkerStatus.getText();
        getUi().setDescriptionText(desc);

        mIsSpeaking = (talkerStatus.getState() == TalkerStatus.State.SPEAKING);
        getUi().setCloseButtonEnabled(!mIsSpeaking);
        getUi().setActionButtonEnabled(true);
        getUi().setRepeatButtonEnabled(talkerStatus.getState() == TalkerStatus.State.PAUSED);

        getUi().setActionButtonText(talkerStatus.getState().getActionText());
    }

    public void onClickClose() {
        // Check if talking and show error message if so, else close.
        if (!showErrorIfSpeaking()) {
            getUi().hide();
        }
    }

    public void onClickAction() {

    }
    public void onClickRepeat() {

    }

    @Override
    public boolean backPressed() {
        if (!getUi().isShowing() || getUi().isKeyboardVisible()) {
            return true;        // Means: not consumed here
        }
        // Check if talking and show error message if so, else close.
        if (!showErrorIfSpeaking()) {
            getUi().hide();
        }
        return false;
    }

    private boolean showErrorIfSpeaking() {
        if (mIsSpeaking) {
            getUi().setErrorMessage(R.string.st040);
        }
        return mIsSpeaking;
    }

    public interface TalkUi extends ActionSheetUi {

        void setTitleText(String title);
        void setSubTitleText(String subTitle);
        void setDescriptionText(String description);
        void setCloseButtonEnabled(boolean enabled);
        void setActionButtonText(String text);
        void setActionButtonEnabled(boolean enabled);
        void setRepeatButtonText(String text);
        void setRepeatButtonEnabled(boolean enabled);
        void setErrorMessage(@StringRes int resourceId);
        void clearErrorMessage();
        void startService(TalkerCommand talkerCommand);
        OdileActivity getOdileActivity();
    }

}
