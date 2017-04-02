package com.wolfie.odile.talker;

import android.os.Parcel;
import android.os.Parcelable;

import com.wolfie.odile.model.PhraseGroup;

public class TalkerCommand implements Parcelable {

    public Command mCommand;
    public PhraseGroup mPhraseGroup;

    public TalkerCommand(Command command, PhraseGroup phraseGroup) {
        mCommand = command;
        mPhraseGroup = phraseGroup;
    }

    public TalkerCommand(Parcel in) {
        read(in);
    }

    private void read(Parcel in) {
        mCommand = Command.CREATOR.createFromParcel(in);
        mPhraseGroup = new PhraseGroup(in);
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
        mPhraseGroup.writeToParcel(dest, flags);
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
