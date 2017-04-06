package com.wolfie.odile.talker;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.wolfie.odile.model.PhraseGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * This specifies a {@link TalkService.Command} passed in an intent to the
 * {@link TalkService}.  Only the {@link #mCommand} field is mandatory.
 */
public class ServiceCommand implements Parcelable {

    @TalkService.Command
    private int mCommand;
    @Nullable
    private List<PhraseGroup> mPhraseGroups;
    private List<SpeechParm> mSpeechParms;

    public ServiceCommand(@TalkService.Command int command) {
        mCommand = command;
    }

    public ServiceCommand(Parcel in) {
        read(in);
    }

    public ServiceCommand(@TalkService.Command int command,
                          List<PhraseGroup> phraseGroups,
                          List<SpeechParm> speechParms) {
        mCommand = command;
        mPhraseGroups = phraseGroups;
        mSpeechParms = speechParms;
    }

    @TalkService.Command
    public int getCommand() {
        return mCommand;
    }

    @Nullable
    public List<PhraseGroup> getPhraseGroups() {
        return mPhraseGroups;
    }
    @Nullable
    public List<SpeechParm> getSpeechParms() {
        return mSpeechParms;
    }

    @SuppressWarnings("WrongConstant")
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
        size = in.readInt();
        if (size > 0) {
            mSpeechParms = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                SpeechParm speechParm = new SpeechParm(in);
                mSpeechParms.add(speechParm);
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
        size = (mSpeechParms == null) ? 0 : mSpeechParms.size();
        dest.writeInt(size);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                SpeechParm speechParm = mSpeechParms.get(i);
                speechParm.writeToParcel(dest, flags);
            }
        }
    }
}
