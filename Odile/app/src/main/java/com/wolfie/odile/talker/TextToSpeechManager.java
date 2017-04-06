package com.wolfie.odile.talker;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechManager extends UtteranceProgressListener {

    // TODO support multi languages
    private LangSpec[] mLangSpecs = {
            new LangSpec(new Locale("ru", "RU"), "Russian"),
            new LangSpec(new Locale("en", "AU"), "English")
    };

    @SpeechParm.Language
    private int mCurrentLanguage = SpeechParm.Language.NONE;
    @SpeechParm.Rate
    private int mCurrentRate = SpeechParm.Rate.NONE;
    @SpeechParm.Pitch
    private int mCurrentPitch = SpeechParm.Pitch.NONE;

    // This member isn't used in the class. It's only held here for client convenience.
    private int mDelay;

    // https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html
    private TextToSpeech mTextToSpeech;
    private boolean mTextToSpeechReady = false;
    private SpeakerListener mSpeakerListener;

    public TextToSpeechManager(Context context) {
        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    mTextToSpeechReady = true;
                    for (LangSpec langSpec : mLangSpecs) {
                        langSpec.mIsAvailable =
                                (mTextToSpeech.isLanguageAvailable(langSpec.mLocale) != TextToSpeech.LANG_NOT_SUPPORTED);
                    }
                    mTextToSpeech.setOnUtteranceProgressListener(TextToSpeechManager.this);
                } else {
                    mTextToSpeech = null;
                }
            }
        });
    }

    public void stop() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
        }
    }

    /**
     * After this call the instance can no longer be used.
     */
    public void shutdown() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
    }

    /**
     * As well as speaking the phrase, this method fetches the associated delay from
     * the {@link Stepper.Step} and holds it locally, so that the client
     * of {@link TextToSpeechManager} may use it on the UTTERED event.
     * @param step
     * @return null if all went well, or an error string otherwise.
     */
    public String speak(Stepper.Step step) {
        if (!mTextToSpeechReady) {
            return "Error: TextToSpeech not yet initialised";
        } else if (mTextToSpeech == null) {
            return "Error: TextToSpeech failed to initialise";
        }

        @SpeechParm.Language
        int language = step.getSpeechParms().getLanguage();
        @SpeechParm.Rate
        int rate = step.getSpeechParms().getRate();
        @SpeechParm.Pitch
        int pitch = step.getSpeechParms().getPitch();

        // Dont speak, just issue the UTTERED callback
        if (step.getSpeechParms().getSilenceMode() == SpeechParm.SilenceMode.STAY_SILENT) {
            hasUttered(false);
            return null;
        }
        // Only set language/rate/pitch if different to current settings.
        if (mCurrentLanguage != language) {
            if (!mLangSpecs[language].mIsAvailable) {
                return "Error: TextToSpeech " + mLangSpecs[language].mName + " not available";
            }
            mCurrentLanguage = language;
            long start = System.nanoTime();
            mTextToSpeech.setLanguage(mLangSpecs[language].mLocale);
            long finish = System.nanoTime();
            Log.d("TextToSpeechManager", "setLangauge " + mCurrentLanguage + ", elapsed millis = " + (finish - start)/1000000);
        }
        if (mCurrentRate != rate) {
            mCurrentRate = rate;
            Log.d("TextToSpeechManager", "setRate " + mCurrentRate);
        }
        if (mCurrentPitch != pitch) {
            mCurrentPitch = pitch;
            Log.d("TextToSpeechManager", "setPitch " + mCurrentPitch);
        }

        HashMap<String, String> params = new HashMap();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "an-utterance-id");
        mTextToSpeech.speak(step.getText(), TextToSpeech.QUEUE_FLUSH, params);
        // Store delay for use until the nextStep step() call.
        mDelay = step.getSpeechParms().getDelay();
        return null;
    }

    public int getDelayFromLastStep() {
        return mDelay;
    }

    /**
     * @return null if ok, or error string otherwise.
     */
    public String speak(@SpeechParm.Language int language, String text) {
        if (!mTextToSpeechReady) {
            return "Error: TextToSpeech not yet initialised";
        } else if (!mLangSpecs[language].mIsAvailable) {
            return "Error: TextToSpeech " + mLangSpecs[language].mName + " not available";
        } else if (mTextToSpeech == null) {
            return "Error: TextToSpeech failed to initialise";
        } else {
            // Only set language if different to current setting.
            if (mCurrentLanguage != language) {
                mCurrentLanguage = language;
                long start = System.nanoTime();
                mTextToSpeech.setLanguage(mLangSpecs[language].mLocale);
                long finish = System.nanoTime();
                Log.d("TextToSpeechManager", "setLangauge elapsed millis = " + (finish - start)/1000000);
            }
            HashMap<String, String> params = new HashMap();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "an-utterance-id");
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
            return null;
        }
    }

    @Override
    public void onStart(String utteranceId) {
    }

    @Override
    public void onDone(String utteranceId) {
        hasUttered(false);
    }

    @Override
    public void onError(String utteranceId) {
        hasUttered(true);
    }

    private void hasUttered(boolean error) {
        if (mSpeakerListener != null) {
            mSpeakerListener.onDoneUttering(error);
        }
    }

    /*
    https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener.html#onDone(java.lang.String)
    https://developer.android.com/reference/android/speech/tts/TextToSpeech.html#speak(java.lang.CharSequence, int, android.os.Bundle, java.lang.String)
     tts.setSpeechRate(1.0f);
     http://stackoverflow.com/questions/11409177/unable-to-detect-completion-of-tts-callback-android



    Map<String,String> ttsParams = new HashMap<String, String>();
    ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,  MainActivity.this.getPackageName());
    mTts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsParams);

    https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html

    http://stackoverflow.com/questions/15718592/tts-tone-android

    https://www.ivona.com/us/
     */

    public void setSpeakerListener(@Nullable SpeakerListener speakerListener) {
        mSpeakerListener = speakerListener;
    }

    public interface SpeakerListener {
        void onDoneUttering(boolean error);       // Speaker has finished uttering the text.
    }

    class LangSpec {
        Locale mLocale;
        String mName;
        boolean mIsAvailable;

        public LangSpec(Locale locale, String name) {
            mLocale = locale;
            mName = name;
        }
    }

}
