package com.wolfie.odile.talker;

import android.util.Log;

import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;

import java.util.List;

/**
 * Code to iterate through a list of {@link Phrase}s and a list of {@link SpeechParm}s.
 */
public class Stepper {

    private List<Phrase> mPhrases;
    private List<SpeechParm> mSpeechParms;
    private int mPhraseIndex;
    private int mSpeechParmIndex;

    public void init(List<PhraseGroup> phraseGroups, List<SpeechParm> speechParms) {
        mPhrases = PhraseGroup.getAllPhrases(phraseGroups);
        mSpeechParms = speechParms;
        mPhraseIndex = 0;
        mSpeechParmIndex = 0;
    }

    public void reset() {
        mPhraseIndex = 0;
        mSpeechParmIndex = 0;
    }

    public int getPhrasesSize() {
        return (mPhrases == null) ? 0 : mPhrases.size();
    }

    public int getSpeechParmsSize() {
        return (mSpeechParms == null) ? 0 : mSpeechParms.size();
    }

    public int getPhraseIndex() {
        return mPhraseIndex;
    }

    public int getSpeechParmIndex() {
        return mSpeechParmIndex;
    }

    /**
     * @return the phrase/Parms (currently pointed to), or null if we've reached end of lists.
     * Advance the pointers to point at the item to be returned on the nextStep invocation.
     *
     */
    public Step nextStep() {
        if (getSpeechParmsSize() == 0 || getPhrasesSize() == 0) {
            // Empty lists; can't process anything.
            return null;
        }
        if (mPhraseIndex >= mPhrases.size()) {
            // End of phrases list; return nothing.
            mPhraseIndex = 0;
            return null;
        }
        Log.d("Stepper", "nextStep at " + mSpeechParmIndex + " / " + mPhraseIndex);
        SpeechParm speechParm = mSpeechParms.get(mSpeechParmIndex);
        Phrase phrase = mPhrases.get(mPhraseIndex);
        // Point to nextStep speechParm/phrase
        if (++mSpeechParmIndex >= mSpeechParms.size()) {
            // End of Parms list; repeat the speech Parms iteration for the nextStep phrase.
            mSpeechParmIndex = 0;
            mPhraseIndex++;
        }
        return new Step(phrase, speechParm);
    }

    /**
     * Alters the current pointers to the first SpeechParm, in the most recently
     * returned Phrase.  This has the effect that the next call to nextStep() will
     * return the first SpeechParm in the most recently returned Phrase.
     * @param firstStep if true, resets to the first {@link SpeechParm} in the
     *                  {@link Phrase}, else to the one just issued.
     */
    public void resetToPhrase(boolean firstStep) {
        if (mPhraseIndex == 0 && mSpeechParmIndex == 0) {
            // Haven't yet had a call to nextStep(); nothing to reset.
            return;
        }
        if (mSpeechParmIndex > 0) {
            mSpeechParmIndex--;
        } else {
            // The previous call to nextStep() caused roll-over of SpeechParm, go back one.
            mSpeechParmIndex = getSpeechParmsSize() - 1;
            mPhraseIndex--;
        }
        if (firstStep) {
            mSpeechParmIndex = 0;
        }
    }

    /**
     * This class represent the application of a {@link SpeechParm} to a {@link Phrase}.
     * It holds all the data needed by {@link TextToSpeechManager#speak(Step)}.
     */
    public class Step {
        private Phrase mPhrase;                     // What should be spoken.
        private SpeechParm mSpeechParm;     // How it should be spoken.

        public Step(Phrase phrase, SpeechParm speechParm) {
            mPhrase = phrase;
            mSpeechParm = speechParm;
        }

        public String getText() {
            switch (mSpeechParm.getLanguage()) {
                case SpeechParm.Language.RUSSIAN:
                    return mPhrase.getRussian();
                case SpeechParm.Language.ENGLISH:
                    return mPhrase.getEnglish();
                default:
                    return "";
            }
        }

        public SpeechParm getSpeechParms() {
            return mSpeechParm;
        }
    }

}
