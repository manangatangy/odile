package com.wolfie.odile.talker;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.wolfie.odile.model.PhraseGroup;

import java.util.ArrayList;
import java.util.List;

public class ServiceCommand implements Parcelable {

    private int mCommand;       // This type is @TalkService.Command (but this upsets Parcel.readInt)
    private List<PhraseGroup> mPhraseGroups;

    public ServiceCommand(@TalkService.Command int command) {
        mCommand = command;
    }

    public ServiceCommand(Parcel in) {
        read(in);
    }

    public ServiceCommand(@TalkService.Command int command, List<PhraseGroup> phraseGroups) {
        mCommand = command;
        mPhraseGroups = phraseGroups;
    }

    @TalkService.Command
    public int getCommand() {
        return mCommand;
    }

    @Nullable
    public List<PhraseGroup> getPhraseGroups() {
        return mPhraseGroups;
    }

    private void read(Parcel in) {
        mCommand = in.readInt();
        int size = in.readInt();
        if (size > 0) {
            mPhraseGroups = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                PhraseGroup phraseGroup = new PhraseGroup(in);
                mPhraseGroups.add(phraseGroup);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ServiceCommand> CREATOR =
            new Creator<ServiceCommand>() {
                public ServiceCommand createFromParcel(Parcel in) {
                    return new ServiceCommand(in);
                }

                public ServiceCommand[] newArray(int size) {
                    return new ServiceCommand[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCommand);
        int size = (mPhraseGroups == null) ? 0 : mPhraseGroups.size();
        dest.writeInt(size);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                PhraseGroup phraseGroup = mPhraseGroups.get(i);
                phraseGroup.writeToParcel(dest, flags);
            }
        }
    }
}
