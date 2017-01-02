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
import com.wolfie.odile.presenter.FilePresenter;
import com.wolfie.odile.presenter.FilePresenter.FileUi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FileFragment extends ActionSheetFragment
        implements FileUi, CompoundButton.OnCheckedChangeListener {

    public static final int PERMISSIONS_REQUEST_STORAGE = 123;
    public static final int REQUEST_BACKUP_EMAIL = 555;

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
    @BindView(R.id.text_error)
    TextView mTextError;

    @Nullable
    @BindView(R.id.storage_type_private)
    RadioButton mStorageTypePrivate;

    @Nullable
    @BindView(R.id.storage_type_public)
    RadioButton mStorageTypePublic;

    @Nullable
    @BindView(R.id.storage_type_internal)
    RadioButton mStorageTypeInternal;

    @Nullable
    @BindView(R.id.button_ok)
    Button mButtonOk;

    @Nullable
    @BindView(R.id.button_cancel)
    Button mButtonCancel;

    @Nullable
    @BindView(R.id.overwrite_existing_switch)
    SwitchCompat mOverwriteSwitch;

    @Nullable
    @BindView(R.id.email_backup_file_switch)
    SwitchCompat mEmailSwitch;

    private boolean mAllowOnRequestCheckedChangeCallback = true;

    private Unbinder mUnbinder2;

    private FilePresenter mFilePresenter;

    @Override
    public FilePresenter getPresenter() {
        return mFilePresenter;
    }

    public FileFragment() {
        mFilePresenter = new FilePresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        View content = inflater.inflate(R.layout.fragment_file, container, false);
        mHolderView.addView(content);
        // This bind will re-bind the superclass members, so the entire view hierarchy must be
        // available, hence the content should be added to the parent view first.
        mUnbinder2 = ButterKnife.bind(this, view);
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilePresenter.onClickOk();
            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilePresenter.onClickCancel();
            }
        });
        mStorageTypePrivate.setOnCheckedChangeListener(this);
        mStorageTypePublic.setOnCheckedChangeListener(this);
        mStorageTypeInternal.setOnCheckedChangeListener(this);
        mEmailSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFilePresenter.onEmailSwitchChanged(isChecked);
            }
        });
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Only process if we're not in the middle of a call to setStorageType().
        if (isChecked) {
            if (mAllowOnRequestCheckedChangeCallback) {
                buttonView.setChecked(false);
                FilePresenter.StorageType storageType =
                        (buttonView == mStorageTypePrivate) ? FilePresenter.StorageType.TYPE_PRIVATE :
                                (buttonView == mStorageTypePublic) ? FilePresenter.StorageType.TYPE_PUBLIC :
                                        FilePresenter.StorageType.TYPE_INTERNAL;
                // Ask listener to handle the click/checking.  They may make call to setStorageType()
                // either while this call is active, or after it returns.  Regardless, it
                // will not result in further calls to this handler.
                mFilePresenter.onRequestStorageTypeSelect(storageType);
            }
        }
    }

    /**
     * Calls to setStorageType do not cause a propagation to the
     * OnRequestCheckedChangeListener.
     */
    @Override
    public void setStorageType(FilePresenter.StorageType storageType) {
        mAllowOnRequestCheckedChangeCallback = false;
        if (storageType == FilePresenter.StorageType.TYPE_PUBLIC) {
            mStorageTypePublic.setChecked(true);
            mStorageTypePrivate.setChecked(false);
            mStorageTypeInternal.setChecked(false);
        } else if (storageType == FilePresenter.StorageType.TYPE_PRIVATE) {
            mStorageTypePublic.setChecked(false);
            mStorageTypePrivate.setChecked(true);
            mStorageTypeInternal.setChecked(false);
        } else {
            mStorageTypePublic.setChecked(false);
            mStorageTypePrivate.setChecked(false);
            mStorageTypeInternal.setChecked(true);
        }
        mAllowOnRequestCheckedChangeCallback = true;
    }

    @Override
    public String getFileName() {
        return mEditName.getText().toString();
    }

    @Override
    public boolean isOverwrite() {
        return mOverwriteSwitch.isChecked();
    }

    @Override
    public void setOverwriteSwitchVisibility(boolean isVisible) {
        mOverwriteSwitch.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isEmailBackup() {
        return mEmailSwitch.isChecked();
    }

    @Override
    public void setEmailBackupSwitchVisibility(boolean isVisible) {
        mEmailSwitch.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setStorageTypePrivateEnabled(boolean enable) {
        mStorageTypePrivate.setEnabled(enable);
    }

    @Override
    public void setStorageTypePublicEnabled(boolean enable) {
        mStorageTypePublic.setEnabled(enable);
    }

    @Override
    public void setStorageTypeInternalEnabled(boolean enable) {
        mStorageTypeInternal.setEnabled(enable);
    }

    @Override
    public FilePresenter.StorageType getStorageType() {
        return (mStorageTypePublic.isChecked() ? FilePresenter.StorageType.TYPE_PUBLIC :
                (mStorageTypePrivate.isChecked() ? FilePresenter.StorageType.TYPE_PRIVATE :
                        FilePresenter.StorageType.TYPE_INTERNAL));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder2.unbind();
    }

    @Override
    public void requestStoragePermission() {
        mBaseActivity.requestPermissions(this,
                new String[] { FilePresenter.STORAGE_PERMISSION },
                PERMISSIONS_REQUEST_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mFilePresenter.onRequestStoragePermissionsResult(grantResults);
    }

    @Override
    public void setTitleText(@StringRes int resourceId) {
        mTextTitle.setText(resourceId);
    }

    @Override
    public void setFileName(String fileName) {
        mEditName.setText(fileName);
    }

    @Override
    public void setDescription(@StringRes int resourceId) {
        mTextDescription.setText(resourceId);
    }

    @Override
    public void setErrorMessage(String text) {
        mTextError.setVisibility(View.VISIBLE);
        mTextError.setText(text);
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
    public void setEnabledOkButton(boolean enabled) {
        mButtonOk.setEnabled(enabled);
    }

    @Override
    public void clearErrorMessage() {
        mTextError.setVisibility(View.GONE);
    }

    @Override
    public void setOkButtonText(@StringRes int resourceId) {
        mButtonOk.setText(resourceId);
    }

    @Override
    public void setPrivateButtonLabel(String text) {
        mStorageTypePrivate.setText(text);
    }

    @Override
    public void setInternalButtonLabel(String text) {
        mStorageTypeInternal.setText(text);
    }

    @Override
    public void setPublicButtonLabel(String text) {
        mStorageTypePublic.setText(text);
    }

    @Override
    public void onShowComplete() {
        mFilePresenter.onShow();
    }

    @Override
    public void onHideComplete() {
    }

    @Override
    public void onTouchBackground() {
    }


    @Override
    public void navigateToEmail(String emailAddress, String subject, File backupFile) {
        Uri uri = FileProvider.getUriForFile(
                getContext(),
                getContext().getString(R.string.content_provider_authority),
                backupFile);
        ArrayList<Uri> attachments = new ArrayList<>();
        attachments.add(uri);
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_STREAM, attachments);
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { emailAddress });
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
//        i.putExtra(Intent.EXTRA_TEXT, "some text for the body of the email");
        Intent emailOnlyIntent = createEmailOnlyChooserIntent(i, "Send via email");
        startActivityForResult(emailOnlyIntent, REQUEST_BACKUP_EMAIL);
    }
    /**
     * Returns an intent that will be only responded to by activities that can handle
     * the source intent as well as SENDTO. This will exclude things like skype and
     * google-drive and the sms-messenger etc.
     * ref: http://stackoverflow.com/a/12804063
     */
    public Intent createEmailOnlyChooserIntent(Intent source, CharSequence chooserTitle) {
        Stack<Intent> intents = new Stack<Intent>();
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "info@domain.com", null));
        List<ResolveInfo> activities = getActivity().getPackageManager().queryIntentActivities(i, 0);

        for(ResolveInfo ri : activities) {
            Intent target = new Intent(source);
            if (!"com.android.fallback".equals(ri.activityInfo.packageName)) {
                target.setPackage(ri.activityInfo.packageName);
                intents.add(target);
            }
        }
        if(!intents.isEmpty()) {
            Intent chooserIntent = Intent.createChooser(intents.remove(0), chooserTitle);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
            return chooserIntent;
        } else {
            return Intent.createChooser(source, chooserTitle);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_BACKUP_EMAIL) {
            mFilePresenter.onEmailActivityResult(resultCode, intent);
        }
    }

}
