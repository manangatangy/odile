package com.wolfie.odile.talker;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;

/**
 * This is values used by {@link TextToSpeechManager} to control how speech is produced.
 */
public class SpeechParm implements Parcelable {

    @Language
    private int mLanguage;
    @Rate
    private int mRate;
    @Pitch
    private int mPitch;
    @SilenceMode
    private int mSilenceMode;       // Useful to cause just a pure delay without any sound.
    private int mDelay;             // Delay in millsecs after uttering speech, until next step.

    public SpeechParm(
            @Language int language,
            @Rate int rate,
            @Pitch int pitch,
            @SilenceMode int silenceMode,
            int delay) {
        mLanguage = language;
        mRate = rate;
        mPitch = pitch;
        mSilenceMode = silenceMode;
        mDelay = delay;
    }

    public SpeechParm(Parcel in) {
        read(in);
    }

    @SuppressWarnings("WrongConstant")
    private void read(Parcel in) {
        mLanguage = in.readInt();
        mRate = in.readInt();
        mPitch = in.readInt();
        mSilenceMode = in.readInt();
        mDelay = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<SpeechParm> CREATOR =
            new Parcelable.Creator<SpeechParm>() {
                public SpeechParm createFromParcel(Parcel in) {
                    return new SpeechParm(in);
                }

                public SpeechParm[] newArray(int size) {
                    return new SpeechParm[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mLanguage);
        dest.writeInt(mRate);
        dest.writeInt(mPitch);
        dest.writeInt(mSilenceMode);
        dest.writeInt(mDelay);
    }

    @Language
    public int getLanguage() {
        return mLanguage;
    }

    @Rate
    public int getRate() {
        return mRate;
    }

    @Pitch
    public int getPitch() {
        return mPitch;
    }

    @SilenceMode
    public int getSilenceMode() {
        return mSilenceMode;
    }

    public int getDelay() {
        return mDelay;
    }

    @IntDef({
            Language.NONE,
            Language.RUSSIAN,
            Language.ENGLISH,
    })
    public @interface Language {
        int NONE = -1;
        // Note: the following values are indices into TextToSpeechManage.mLangSpecs.
        int RUSSIAN = 0;
        int ENGLISH = 1;
    }

    @IntDef({
            Rate.NONE,
            Rate.SLOW,
            Rate.NORMAL,
            Rate.FAST,
    })
    public @interface Rate {
        int NONE = -1;
        // Note: the following values are indices into TextToSpeechManage.mRateSpecs.
        int SLOW = 0;
        int NORMAL = 1;
        int FAST = 2;
    }

    @IntDef({
            Pitch.NONE,
            Pitch.LOW,
            Pitch.NORMAL,
            Pitch.HIGH,
    })
    public @interface Pitch {
        int NONE = -1;
        // Note: the following values are indices into TextToSpeechManage.mPitchSpecs.
        int LOW = 0;
        int NORMAL = 1;
        int HIGH = 2;
    }

    @IntDef({
            SilenceMode.SPEAK_IT,
            SilenceMode.STAY_SILENT,
    })
    public @interface SilenceMode {
        int SPEAK_IT = 0;
        int STAY_SILENT = 1;
    }

}
