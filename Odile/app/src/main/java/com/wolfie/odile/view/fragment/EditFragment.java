package com.wolfie.odile.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wolfie.odile.R;
import com.wolfie.odile.model.Entry;
import com.wolfie.odile.presenter.EditPresenter;
import com.wolfie.odile.presenter.EditPresenter.EditUi;
import com.wolfie.odile.util.KeyboardUtils;
import com.wolfie.odile.view.activity.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditFragment extends ActionSheetFragment implements EditUi {

    @Nullable
    @BindView(R.id.text_title)
    TextView mTextTitle;

    @Nullable
    @BindView(R.id.text_description)
    TextView mTextDescription;

    @Nullable
    @BindView(R.id.edit_text_name)
    EditText mEditName;

    @Nullable
    @BindView(R.id.edit_text_group)
    EditText mEditGroup;

    @Nullable
    @BindView(R.id.edit_text_content)
    EditText mEditContent;

    @Nullable
    @BindView(R.id.text_error)
    TextView mTextError;

    @Nullable
    @BindView(R.id.button_save)
    Button mButtonSave;

    @Nullable
    @BindView(R.id.button_cancel)
    Button mButtonCancel;

    @Nullable
    @BindView(R.id.button_delete)
    Button mButtonDelete;

    private Unbinder mUnbinder2;

    private EditPresenter mEditPresenter;

    @Override
    public EditPresenter getPresenter() {
        return mEditPresenter;
    }

    public EditFragment() {
        mEditPresenter = new EditPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        View content = inflater.inflate(R.layout.fragment_edit, container, false);
        mHolderView.addView(content);
        // This bind will re-bind the superclass members, so the entire view hierarchy must be
        // available, hence the content should be added to the parent view first.
        mUnbinder2 = ButterKnife.bind(this, view);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditPresenter.onClickCancel();
            }
        });
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditPresenter.onClickSave();
            }
        });
        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditPresenter.onClickDelete();
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
    public void setTitleText(String title) {
        mTextTitle.setText(title);
    }

    @Override
    public void enableDeleteButton(boolean enable) {
        mButtonDelete.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTextValues(Entry entry) {
        mEditName.setText(entry.getEntryName());
        mEditGroup.setText(entry.getGroupName());
        mEditContent.setText(entry.getContent());
    }

    @Override
    public Entry getTextValues(Entry entry) {
        String name = mEditName.getText().toString();
        String group = mEditGroup.getText().toString();
        String content = mEditContent.getText().toString();
        entry.setEntryName(name);
        entry.setGroupName(group);
        entry.setContent(content);
        return entry;
    }

    @Override
    public boolean isEntryModified(Entry entry) {
        String name = mEditName.getText().toString();
        String group = mEditGroup.getText().toString();
        String content = mEditContent.getText().toString();
        return !equals(name, (entry == null) ? null : entry.getEntryName()) ||
                !equals(group, (entry == null) ? null : entry.getGroupName()) ||
                !equals(content, (entry == null) ? null : entry.getContent());
    }

    private boolean equals(@Nullable String val1, @Nullable String val2) {
        if (val1 == null) {
            val1 = "";
        }
        if (val2 == null) {
            val2 = "";
        }
        return val1.equals(val2);
    }

    @Override
    public void setDescription(@StringRes int resourceId) {
        mTextDescription.setText(resourceId);
    }

    @Override
    public void clearDescription() {
        mTextDescription.setText("");
    }

    @Override
    public void setErrorMessage(@StringRes int resourceId) {
        mTextError.setVisibility(View.VISIBLE);
        mTextError.setText(resourceId);
    }

    @Override
    public void clearErrorMessage() {
        mTextError.setVisibility(View.GONE);
    }

    @Override
    public void onShowComplete() {
        mEditPresenter.onShow();
    }

    @Override
    public void onHideComplete() {
    }

    @Override
    public void onTouchBackground() {
    }


}
