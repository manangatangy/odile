package com.wolfie.odile.talker;

import android.util.Log;

import com.wolfie.odile.model.Phrase;

import java.util.ArrayList;
import java.util.List;

/**
 * Code to iterate through a list of {@link Phrase}s and a list of {@link SpeechSettings}s.
 */
public class StepIterator {

    private List<Phrase> mPhrases;
    private List<SpeechSettings> mSpeechSettings;
    private int mCurrentPhrase;
    private int mCurrentSpeechSetting;

    public void init(List<Phrase> phrases, List<SpeechSettings> speechSettings) {
        mPhrases = phrases;
        mSpeechSettings = speechSettings;
        mCurrentPhrase = 0;
        mCurrentSpeechSetting = 0;

        // TODO temp
        if (mSpeechSettings == null) {
            mSpeechSettings = new ArrayList<SpeechSettings>();
            mSpeechSettings.add(new SpeechSettings(
                    SpeechSettings.Language.RUSSIAN,
                    SpeechSettings.Rate.NORMAL,
                    SpeechSettings.Pitch.NORMAL,
                    1500));
            mSpeechSettings.add(new SpeechSettings(
                    SpeechSettings.Language.ENGLISH,
                    SpeechSettings.Rate.NORMAL,
                    SpeechSettings.Pitch.NORMAL,
                    1500));
        }
    }

    public void reset() {
        mCurrentPhrase = 0;
        mCurrentSpeechSetting = 0;
    }

    public int getPhrasesSize() {
        return (mPhrases == null) ? 0 : mPhrases.size();
    }

    public int getSpeechSettingsSize() {
        return (mSpeechSettings == null) ? 0 : mSpeechSettings.size();
    }

    public int getCurrentPhrase() {
        return mCurrentPhrase;
    }

    public int getCurrentSpeechSetting() {
        return mCurrentSpeechSetting;
    }

    /**
     * @return the next phrase/settings to be spoken, or null if we've reached end of lists.
     */
    public SpeechStep next() {
        if (getSpeechSettingsSize() == 0 || getPhrasesSize() == 0) {
            // Empty lists; can't process anything.
            return null;
        }
        if (mCurrentPhrase >= mPhrases.size()) {
            // End of phrases list; return nothing.
            mCurrentPhrase = 0;
            return null;
        }
        Log.d("StepIterator", "next at " + mCurrentSpeechSetting + " / " + mCurrentPhrase);
        SpeechSettings speechSettings = mSpeechSettings.get(mCurrentSpeechSetting);
        Phrase phrase = mPhrases.get(mCurrentPhrase);
        // Point to next speechSettings/phrase
        if (++mCurrentSpeechSetting >= mSpeechSettings.size()) {
            // End of settings list; repeat the speech settings iteration for the next phrase.
            mCurrentSpeechSetting = 0;
            mCurrentPhrase++;
        }
        return new SpeechStep(phrase, speechSettings);
    }

    /**
     * This class represent the application of a {@link SpeechSettings} to a {@link Phrase}.
     * It holds all the data needed by {@link TextToSpeechManager#speak(SpeechStep)}
     * and for the
     */
    public class SpeechStep {
        private Phrase mPhrase;                     // What should be spoken.
        private SpeechSettings mSpeechSettings;     // How it should be spoken.

        public SpeechStep(Phrase phrase, SpeechSettings speechSettings) {
            mPhrase = phrase;
            mSpeechSettings = speechSettings;
        }

        public String getText() {
            switch (mSpeechSettings.getLanguage()) {
                case SpeechSettings.Language.RUSSIAN:
                    return mPhrase.getRussian();
                case SpeechSettings.Language.ENGLISH:
                    return mPhrase.getEnglish();
                default:
                    return "";
            }
        }

        public SpeechSettings getSpeechSettings() {
            return mSpeechSettings;
        }
    }

}
