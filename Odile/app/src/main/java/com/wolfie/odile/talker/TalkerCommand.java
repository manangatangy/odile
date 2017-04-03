package com.wolfie.odile.talker;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;

import java.util.ArrayList;
import java.util.List;

public class TalkerCommand implements Parcelable {

    public Command mCommand;
    @NonNull
    public List<PhraseGroup> mPhraseGroups;

    private TalkerCommand() {
        mPhraseGroups = new ArrayList<>();
    }

    public TalkerCommand(Command command) {
        this();
        mCommand = command;
    }

    public TalkerCommand(Parcel in) {
        this();
        read(in);
    }

    public TalkerCommand(Command command, @NonNull List<PhraseGroup> phraseGroups) {
        mCommand = command;
        mPhraseGroups = phraseGroups;
    }

    private void read(Parcel in) {
        mCommand = Command.CREATOR.createFromParcel(in);
        mPhraseGroups.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            PhraseGroup phraseGroup = new PhraseGroup(in);
            mPhraseGroups.add(phraseGroup);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TalkerCommand> CREATOR =
            new Creator<TalkerCommand>() {
                public TalkerCommand createFromParcel(Parcel in) {
                    return new TalkerCommand(in);
                }

                public TalkerCommand[] newArray(int size) {
                    return new TalkerCommand[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mCommand.writeToParcel(dest, flags);
        int size = mPhraseGroups.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            PhraseGroup phraseGroup = mPhraseGroups.get(i);
            phraseGroup.writeToParcel(dest, flags);
        }
    }

    public enum Command implements Parcelable {
        // Parcelable idea from: http://stackoverflow.com/a/7497787
        // An even better idea: http://stackoverflow.com/a/9753178
        SET_MODE,
        SET_PHRASES,        // Also resets the counter
        SPEAK,
        PAUSE;

        private static Command[] mValues = Command.values();
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeInt(ordinal());
        }

        public static final Creator<Command> CREATOR = new Creator<Command>() {
            @Override
            public Command createFromParcel(final Parcel source) {
                return mValues[source.readInt()];
            }

            @Override
            public Command[] newArray(final int size) {
                return new Command[size];
            }
        };
    }
}
