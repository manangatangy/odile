package com.wolfie.odile.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wolfie.odile.R;
import com.wolfie.odile.presenter.TalkPresenter;
import com.wolfie.odile.presenter.TalkPresenter.TalkUi;
import com.wolfie.odile.talker.ServiceCommand;
import com.wolfie.odile.talker.TalkService;
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
    @BindView(R.id.button_1)
    Button mButton1;

    @Nullable
    @BindView(R.id.button_2)
    Button mButton2;

    @Nullable
    @BindView(R.id.button_3)
    Button mButton3;

    @Nullable
    @BindView(R.id.button_4)
    Button mButton4;

    @Nullable
    @BindView(R.id.button_5)
    Button mButton5;

    @Nullable
    @BindView(R.id.button_6)
    Button mButton6;

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
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickButton1();
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickButton2();
            }
        });
        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickButton3();
            }
        });
        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickButton4();
            }
        });
        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickButton5();
            }
        });
        mButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTalkPresenter.onClickButton6();
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
        Log.d("TalkFragment", "bindServiceAndListen");
        mServiceBinder.bindService(getOdileActivity(), getContext());
        mServiceBinder.setServiceBinderListener(serviceBinderListener);

    }

    @Override
    public void unbindServiceAndIgnore() {
        Log.d("TalkFragment", "unbindServiceAndIgnore");
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
    public void setButton1Text(String text) {
        mButton1.setText(text);
    }

    @Override
    public void setButton1Enabled(boolean enabled) {
        mButton1.setEnabled(enabled);
    }

    @Override
    public void setButton2Text(String text) {
        mButton2.setText(text);
    }

    @Override
    public void setButton2Enabled(boolean enabled) {
        mButton2.setEnabled(enabled);
    }

    @Override
    public void setButton3Text(String text) {
        mButton3.setText(text);
    }

    @Override
    public void setButton3Enabled(boolean enabled) {
        mButton3.setEnabled(enabled);
    }

    @Override
    public void setButton4Text(String text) {
        mButton4.setText(text);
    }

    @Override
    public void setButton4Enabled(boolean enabled) {
        mButton4.setEnabled(enabled);
    }

    @Override
    public void setButton5Text(String text) {
        mButton5.setText(text);
    }

    @Override
    public void setButton5Enabled(boolean enabled) {
        mButton5.setEnabled(enabled);
    }

    @Override
    public void setButton6Text(String text) {
        mButton6.setText(text);
    }

    @Override
    public void setButton6Enabled(boolean enabled) {
        mButton6.setEnabled(enabled);
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
    public void startService(ServiceCommand serviceCommand) {
        Intent intent = new Intent(getContext(), TalkService.class);
        intent.putExtra(TalkService.COMMAND_KEY, serviceCommand);
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
