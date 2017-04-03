package com.wolfie.odile.view.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wolfie.odile.R;
import com.wolfie.odile.presenter.TalkPresenter;
import com.wolfie.odile.presenter.TalkPresenter.TalkUi;
import com.wolfie.odile.talker.StatusHandler;
import com.wolfie.odile.talker.TalkerCommand;
import com.wolfie.odile.talker.TalkerService;
import com.wolfie.odile.view.activity.OdileActivity;
import com.wolfie.odile.view.activity.ServiceBinder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TalkFragment extends ActionSheetFragment implements TalkUi {

    @Nullable
    @BindView(R.id.text_title)
    TextView mTextTitle;

    @Nullable
    @BindView(R.id.text_sub_title)
    TextView mTextSubTitle;

    @Nullable
    @BindView(R.id.text_description)
    TextView mTextDescription;

    @Nullable
    @BindView(R.id.button_close)
    Button mButtonClose;

    @Nullable
    @BindView(R.id.button_action)
    Button mButtonAction;

    @Nullable
    @BindView(R.id.button_repeat)
    Button mButtonRepeat;

    @Nullable
    @BindView(R.id.text_error)
    TextView mTextError;

    private Unbinder mUnbinder2;
    private ServiceBinder mServiceBinder = new ServiceBinder();
    private TalkPresenter mTalkPresenter;

    @Override
    public TalkPresenter getPresenter() {
        return mTalkPresenter;
    }

    public TalkFragment() {
        mTalkPresenter = new TalkPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        View content = inflater.inflate(R.layout.fragment_talk, container, false);
        mHolderView.addView(content);
        // This bind will re-bind the superclass members, so the entire view hierarchy must be
        // available, hence the content should be added to the parent view first.
        mUnbinder2 = ButterKnife.bind(this, view);
        mButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickClose();
            }
        });
        mButtonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickAction();
            }
        });
        mButtonRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickRepeat();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder2.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void bindServiceAndListen(ServiceBinder.ServiceBinderListener serviceBinderListener) {
        mServiceBinder.bindService(getOdileActivity(), getContext());
        mServiceBinder.setServiceBinderListener(serviceBinderListener);

    }

    @Override
    public void unbindServiceAndIgnore() {
        mServiceBinder.unbindService(getOdileActivity());
        mServiceBinder.setServiceBinderListener(null);
    }

    @Override
    public void setTitleText(String title) {
        mTextTitle.setText(title);
    }
    @Override
    public void setSubTitleText(String subTitle) {
        mTextSubTitle.setText(subTitle);
    }

    @Override
    public void setDescriptionText(String description) {
        mTextDescription.setText(description);
    }

    @Override
    public void setCloseButtonEnabled(boolean enabled) {
        mButtonClose.setEnabled(enabled);
    }

    @Override
    public void setActionButtonText(String text) {
        mButtonAction.setText(text);
    }

    @Override
    public void setActionButtonEnabled(boolean enabled) {
        mButtonAction.setEnabled(enabled);
    }

    @Override
    public void setRepeatButtonText(String text) {
        mButtonRepeat.setText(text);
    }

    @Override
    public void setRepeatButtonEnabled(boolean enabled) {
        mButtonRepeat.setEnabled(enabled);
    }

    @Override
    public void setErrorMessage(@StringRes int resourceId) {
        mTextError.setText(resourceId);
        mTextError.setVisibility(View.VISIBLE);
    }

    @Override
    public void clearErrorMessage() {
        mTextError.setVisibility(View.GONE);
    }

    @Override
    public void startService(TalkerCommand talkerCommand) {
        Intent intent = new Intent(getContext(), TalkerService.class);
        intent.putExtra(TalkerService.COMMAND_KEY, talkerCommand);
        getOdileActivity().startService(intent);
    }

    @Override
    public void onShowComplete() {
        mTalkPresenter.onShow();
    }

    @Override
    public void onHideComplete() {
    }

    @Override
    public void onTouchBackground() {
    }
}
