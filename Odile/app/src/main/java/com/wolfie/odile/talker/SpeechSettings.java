package com.wolfie.odile.talker;

import android.support.annotation.IntDef;

/**
 *
 */
public class SpeechSettings {

    @Language
    private int mLanguage;
    @Rate
    private int mRate;
    @Pitch
    private int mPitch;
    private int mDelay;

    public SpeechSettings(
            @Language int language,
            @Rate int rate,
            @Pitch int pitch,
            int delay) {
        mLanguage = language;
        mRate = rate;
        mPitch = pitch;
        mDelay = delay;
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

    public int getDelay() {
        return mDelay;
    }

    @IntDef({
            Language.NONE,
            Language.RUSSIAN,
            Language.ENGLISH
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

}
