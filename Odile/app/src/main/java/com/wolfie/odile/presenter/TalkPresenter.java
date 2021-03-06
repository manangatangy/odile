package com.wolfie.odile.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.wolfie.odile.R;
import com.wolfie.odile.model.PhraseGroup;
import com.wolfie.odile.presenter.TalkPresenter.TalkUi;
import com.wolfie.odile.talker.InfoChannel;
import com.wolfie.odile.talker.ServiceCommand;
import com.wolfie.odile.talker.SpeechParm;
import com.wolfie.odile.talker.TalkService;
import com.wolfie.odile.talker.SpeakerInfo;
import com.wolfie.odile.view.ActionSheetUi;
import com.wolfie.odile.view.activity.OdileActivity;
import com.wolfie.odile.view.activity.ServiceBinder;
import com.wolfie.odile.view.activity.ServiceBinder.ServiceBinderListener;
import com.wolfie.odile.view.fragment.ListFragment;

import java.util.ArrayList;
import java.util.List;

public class TalkPresenter extends BasePresenter<TalkUi>
        implements ServiceBinderListener, InfoChannel.InfoListener {

    private final static String KEY_TALK_ACTION_SHEET_SHOWING = "KEY_TALK_ACTION_SHEET_SHOWING";

    private boolean mIsShowing;
    private SpeakerInfo.State mTalkerState;    // Only valid after onSpeakerInfo() called (not saved).

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
        getUi().bindServiceAndListen(this);
        // Causes callback to onServiceBound().
    }

    @Override
    public void pause() {
        super.pause();
        mIsShowing = getUi().isShowing();
        getUi().dismissKeyboard(false);
        getUi().unbindServiceAndIgnore();
    }

    @Override
    public void onSaveState(Bundle outState) {
        outState.putBoolean(KEY_TALK_ACTION_SHEET_SHOWING, mIsShowing);
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        mIsShowing = savedState.getBoolean(KEY_TALK_ACTION_SHEET_SHOWING, false);
    }

    @Override
    public void onServiceBound(TalkService mBoundTalkService) {
        Log.d("TalkPresenter", "onServiceBound");
        mBoundTalkService.getStatusChannel().addStatusListener(this);
        // Causes callback to onSpeakerInfo().
    }
    @Override
    public void onServiceUnBound(TalkService mBoundTalkService) {
        Log.d("TalkPresenter", "onServiceUnBound");
        mBoundTalkService.getStatusChannel().removeStatusListener(this);
    }

    @Override
    public void onSpeakerInfo(SpeakerInfo speakerInfo) {
        mTalkerState = speakerInfo.getState();

        getUi().setTitleText(speakerInfo.getState().title());
        getUi().setSubTitleText(speakerInfo.getSubTitle());
        String desc = (mTalkerState == SpeakerInfo.State.STOPPED) ? "" : speakerInfo.getText();
        getUi().setDescriptionText(desc);

        getUi().setButton1Enabled(mTalkerState != SpeakerInfo.State.SPEAKING);
        //getUi().setButton3Enabled(mTalkerState != SpeakerInfo.State.STOPPED);

//        getUi().setButton1Text(speakerInfo.getState().getAction1Text());
        getUi().setButton2Enabled(true);
        getUi().setButton2Text(speakerInfo.getState().getAction2Text());
        //getUi().setButton3Text(speakerInfo.getState().getAction3Text());

        getUi().setButton4Enabled(mTalkerState != SpeakerInfo.State.STOPPED);

    }

    /**
     * This is not called on resume, only when the view is shown by the user.
     */
    public void onShow() {
        // If we are resuming the activity it is possible that the service has been
        // speaking in the background (indicated by state PAUSED or SPEAKING).
        // Only set the phrases if the state is STOPPED (which will occur when
        // the speaker gets to the end of the list, of if the CLOSE button clicked).
        // Note: mTalkerState will be null until we trigger a status change by
        // sending a command.
        if (mTalkerState == null || mTalkerState == SpeakerInfo.State.STOPPED) {
            ListPresenter listPresenter = getUi().findPresenter(ListFragment.class);
            List<PhraseGroup> phraseGroups = listPresenter.getDisplayGroups();
            getUi().startService(
                    new ServiceCommand(TalkService.Command.RESET, phraseGroups, mSpeechSettings));
        }
    }

    static List<SpeechParm> mSpeechSettings = new ArrayList<SpeechParm>();
    static {
        mSpeechSettings.add(new SpeechParm(
                SpeechParm.Language.RUSSIAN,
                SpeechParm.Rate.NORMAL,
                SpeechParm.Pitch.NORMAL,
                SpeechParm.SilenceMode.SPEAK_IT,
                1000));
        mSpeechSettings.add(new SpeechParm(
                SpeechParm.Language.ENGLISH,
                SpeechParm.Rate.NORMAL,
                SpeechParm.Pitch.NORMAL,
                SpeechParm.SilenceMode.STAY_SILENT,
                1000));
    }

    /*
    state       button1     button2     button3     button4     button5     button6
    STOPPED     CLOSE       SPEAK       --          disabled    disabled    disable
    SPEAKING    disable     PAUSE       --          PREVIOUS    CURRENT     NEXT
    PAUSED      CLOSE       RESUME      --          PREVIOUS    CURRENT     NEXT
     */
    public void onClickButton1() {
        if (mTalkerState != SpeakerInfo.State.SPEAKING) {
            // Reset will set the state to STOPPED, so that the next time onShow()
            // occurs, the phrases can be assigned.
            getUi().startService(new ServiceCommand(TalkService.Command.RESET));
            getUi().hide();
        } else {
            getUi().startService(new ServiceCommand(TalkService.Command.REPEAT));
            getUi().clearErrorMessage();
        }
    }

    public void onClickButton2() {
        @TalkService.Command
        int command = (mTalkerState == SpeakerInfo.State.SPEAKING)
                ? TalkService.Command.PAUSE : TalkService.Command.SPEAK;
        getUi().startService(new ServiceCommand(command));
        getUi().clearErrorMessage();
    }

    public void onClickButton3() {
//        if (mTalkerState != SpeakerInfo.State.STOPPED) {
//            getUi().startService(new ServiceCommand(TalkService.Command.PREVIOUS));
//            getUi().clearErrorMessage();
//        }
    }

    public void onClickButton4() {
        sendCommand(TalkService.Command.PREVIOUS);
    }

    public void onClickButton5() {
        sendCommand(TalkService.Command.REPEAT);
    }

    public void onClickButton6() {
        sendCommand(TalkService.Command.NEXT);
    }

    private void sendCommand(@TalkService.Command int command) {
        if (mTalkerState != SpeakerInfo.State.STOPPED) {
            getUi().startService(new ServiceCommand(command));
            getUi().clearErrorMessage();
        }
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
        if (mTalkerState == SpeakerInfo.State.SPEAKING) {
            getUi().setErrorMessage(R.string.st040);
            return true;
        }
        return false;
    }

    public interface TalkUi extends ActionSheetUi {

        void bindServiceAndListen(ServiceBinder.ServiceBinderListener serviceBinderListener);
        void unbindServiceAndIgnore();

        void setTitleText(String title);
        void setSubTitleText(String subTitle);
        void setDescriptionText(String description);
        void setButton1Text(String text);
        void setButton1Enabled(boolean enabled);
        void setButton2Text(String text);
        void setButton2Enabled(boolean enabled);
        void setButton3Text(String text);
        void setButton3Enabled(boolean enabled);
        void setButton4Text(String text);
        void setButton4Enabled(boolean enabled);
        void setButton5Text(String text);
        void setButton5Enabled(boolean enabled);
        void setButton6Text(String text);
        void setButton6Enabled(boolean enabled);
        void setErrorMessage(@StringRes int resourceId);
        void clearErrorMessage();
        void startService(ServiceCommand serviceCommand);
        OdileActivity getOdileActivity();
    }

}
