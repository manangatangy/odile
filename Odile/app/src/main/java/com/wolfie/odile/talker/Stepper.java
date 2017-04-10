package com.wolfie.odile.talker;

import android.util.Log;

import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;

import java.util.List;

/**
 * Code to iterate through a list of {@link Phrase}s and a list of {@link SpeechParm}s.
 * The indexes are held in the Step returned from next and previous.
 */
public class Stepper {

    private List<Phrase> mPhrases;
    private List<SpeechParm> mSpeechParms;
//    private int mPhraseIndex;
//    private int mSpeechParmIndex;

    public void init(List<PhraseGroup> phraseGroups, List<SpeechParm> speechParms) {
        mPhrases = PhraseGroup.getAllPhrases(phraseGroups);
        mSpeechParms = speechParms;
//        reset();
    }

//    public void reset() {
//        mPhraseIndex = 0;
//        mSpeechParmIndex = -1;
//    }

//    public boolean isAtStart() {
//        return (mSpeechParmIndex < 0);
//    }

    public int getPhrasesSize() {
        return (mPhrases == null) ? 0 : mPhrases.size();
    }

    public int getSpeechParmsSize() {
        return (mSpeechParms == null) ? 0 : mSpeechParms.size();
    }

//    public int getPhraseIndex() {
//        return mPhraseIndex;
//    }
//
//    public int getSpeechParmIndex() {
//        return mSpeechParmIndex;
//    }

    public boolean empty() {
        // Both lists have to be non-empty, in order to have any Steps.
        return (getSpeechParmsSize() == 0 || getPhrasesSize() == 0);
    }

    /**
     * Using the parameter as the reference, return a step created form the next speechParm
     * in the list. If the list rolls over or the wholePhrase flag is set, use the first
     * speechParm in the next phrase. Returns null at end of list.
     * For the special case where the parameter is null (means at the pre-start of list)
     * then return the first item (speechParm/phrase).
     * Store the indexes in the step for subsequent iteration.
     */
    public Step next(Step step, boolean wholePhrase) {
        if (empty()) {
            return null;
        }
        if (step == null) {
            return new Step(0, 0);
        }
        int phraseIndex = step.getPhraseIndex();
        int speechParmIndex = step.getSpeechParmIndex();

        if (++speechParmIndex >= mSpeechParms.size() || wholePhrase) {
            speechParmIndex = 0;
            phraseIndex++;
        }
        if (phraseIndex >= mPhrases.size()) {
            // End of phrases list; return nothing.
            return null;
        }
        Log.d("Stepper", "next -> " + speechParmIndex + " / " + phraseIndex);
        return new Step(phraseIndex, speechParmIndex);
    }

    /**
     * Using the parameter as the reference, return a step created from the previous speechParm
     * in the list. If the list rolls under, set the last speechParm in the previous phrase.
     * If the wholePhrase flag is set, use the first speechParm in the previous phrase.
     * Returns null at start of list.
     * Store the indexes in the step for subsequent iteration.
     */
    public Step previous(Step step, boolean wholePhrase) {
        if (step == null || empty()) {
            return null;
        }
        int phraseIndex = step.getPhraseIndex();
        int speechParmIndex = step.getSpeechParmIndex();

        if (--speechParmIndex < 0 || wholePhrase) {
            speechParmIndex = wholePhrase ? 0 : (mSpeechParms.size() - 1);
            phraseIndex--;
        }
        if (phraseIndex < 0) {
            // Start of phrases list; return nothing.
            return null;
        }
        Log.d("Stepper", "previous -> " + speechParmIndex + " / " + phraseIndex);
        return new Step(phraseIndex, speechParmIndex);
    }

    /**
     * This class represent the application of a {@link SpeechParm} to a {@link Phrase}.
     * It holds all the data needed by {@link TextToSpeechManager#speak(Step)}.
     */
    public class Step {
        private Phrase mPhrase;                     // What should be spoken.
        private SpeechParm mSpeechParm;             // How it should be spoken.
        private int mPhraseIndex;
        private int mSpeechParmIndex;

        public Step(Phrase phrase, SpeechParm speechParm, int phraseIndex, int speechParmIndex) {
            mPhrase = phrase;
            mSpeechParm = speechParm;
            mPhraseIndex = phraseIndex;
            mSpeechParmIndex = speechParmIndex;
        }

        public Step(int phraseIndex, int speechParmIndex) {
            this(mPhrases.get(phraseIndex), mSpeechParms.get(speechParmIndex), phraseIndex, speechParmIndex);
        }

        public SpeechParm getSpeechParm() {
            return mSpeechParm;
        }

        public Phrase getPhrase() {
            return mPhrase;
        }

        public int getPhraseIndex() {
            return mPhraseIndex;
        }

        public int getSpeechParmIndex() {
            return mSpeechParmIndex;
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

        public String toString() {
            return "phrase:" + mPhraseIndex + " parm:" + mSpeechParmIndex;
        }

    }

}
