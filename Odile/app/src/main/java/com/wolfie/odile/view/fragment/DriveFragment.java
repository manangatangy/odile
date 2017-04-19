package com.wolfie.odile.view.fragment;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.wolfie.odile.R;
import com.wolfie.odile.presenter.DrivePresenter;
import com.wolfie.odile.presenter.DrivePresenter.DriveUi;
import com.wolfie.odile.presenter.DrivePresenter.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 */

public class DriveFragment extends ActionSheetFragment
        implements DriveUi, CompoundButton.OnCheckedChangeListener {

    public static final int PERMISSIONS_REQUEST_STORAGE = 123;
    public static final int REQUEST_BACKUP_EMAIL = 555;

    @Nullable
    @BindView(R.id.text_title)
    TextView mTextTitle;

    @Nullable
    @BindView(R.id.text_description)
    TextView mTextDescription;

    @Nullable
    @BindView(R.id.text_error)
    TextView mTextError;

    @Nullable
    @BindView(R.id.file_type_sheet)
    RadioButton mFileTypeSheet;

    @Nullable
    @BindView(R.id.file_type_json)
    RadioButton mFileTypeJson;

    @Nullable
    @BindView(R.id.button_select)
    Button mButtonSelect;

    @Nullable
    @BindView(R.id.button_cancel)
    Button mButtonCancel;

    @Nullable
    @BindView(R.id.overwrite_existing_switch)
    SwitchCompat mOverwriteSwitch;

    private boolean mAllowOnRequestCheckedChangeCallback = true;

    private Unbinder mUnbinder2;

    private DrivePresenter mDrivePresenter;

    @Override
    public DrivePresenter getPresenter() {
        return mDrivePresenter;
    }

    public DriveFragment() {
        mDrivePresenter = new DrivePresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        View content = inflater.inflate(R.layout.fragment_drive, container, false);
        mHolderView.addView(content);
        // This bind will re-bind the superclass members, so the entire view hierarchy must be
        // available, hence the content should be added to the parent view first.
        mUnbinder2 = ButterKnife.bind(this, view);
        mButtonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrivePresenter.onClickSelect();
            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrivePresenter.onClickCancel();
            }
        });
        mFileTypeSheet.setOnCheckedChangeListener(this);
        mFileTypeJson.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Only process if we're not in the middle of a call to setFileType().
        if (isChecked) {
            if (mAllowOnRequestCheckedChangeCallback) {
                buttonView.setChecked(false);
                FileType fileType = (buttonView == mFileTypeSheet) ? FileType.TYPE_SHEET : FileType.TYPE_JSON;
                // Ask listener to handle the click/checking.  They may make call to setFileType()
                // either while this call is active, or after it returns.  Regardless, it
                // will not result in further calls to this handler.
                mDrivePresenter.onRequestFileTypeSelect(fileType);
            }
        }
    }

    /**
     * Calls to setFileType do not cause a propagation to the
     * OnRequestCheckedChangeListener.
     */
    @Override
    public void setFileType(FileType fileType) {
        mAllowOnRequestCheckedChangeCallback = false;
        if (fileType == FileType.TYPE_SHEET) {
            mFileTypeSheet.setChecked(true);
            mFileTypeJson.setChecked(false);
        } else {
            mFileTypeSheet.setChecked(false);
            mFileTypeJson.setChecked(true);
        }
        mAllowOnRequestCheckedChangeCallback = true;
    }

    @Override
    public FileType getFileType() {
        return (mFileTypeSheet.isChecked() ? FileType.TYPE_SHEET : FileType.TYPE_JSON);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder2.unbind();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        mDrivePresenter.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//        if (requestCode == REQUEST_BACKUP_EMAIL) {
//            mFilePresenter.onEmailActivityResult(resultCode, intent);
//        }
    }

    @Override
    public void setTitleText(@StringRes int resourceId) {
        mTextTitle.setText(resourceId);
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
    public void setErrorMessage(String text) {
        mTextError.setVisibility(View.VISIBLE);
        mTextError.setText(text);
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
    public boolean isOverwrite() {
        return mOverwriteSwitch.isChecked();
    }

    @Override
    public void onShowComplete() {
        mDrivePresenter.onShow();
    }

    @Override
    public void onHideComplete() {
    }

    @Override
    public void onTouchBackground() {
    }

}
