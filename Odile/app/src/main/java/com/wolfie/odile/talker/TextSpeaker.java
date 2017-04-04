package com.wolfie.odile.talker;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;

public class TextSpeaker extends UtteranceProgressListener {

    // TODO support multi languages
    // TODO support completion notification
    private Locale mLanguageLocale = new Locale("ru", "RU");
    // https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html
    private TextToSpeech mTextToSpeech;
    private boolean mTextToSpeechReady = false;
    private boolean mTextToSpeechLanguageAvailable = false;

    private SpeakerListener mSpeakerListener;

    public TextSpeaker(Context context) {
        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    mTextToSpeechReady = true;
                    if (mTextToSpeech.isLanguageAvailable(mLanguageLocale) != TextToSpeech.LANG_NOT_SUPPORTED) {
                        mTextToSpeech.setLanguage(mLanguageLocale);
                        mTextToSpeechLanguageAvailable = true;
                        mTextToSpeech.setOnUtteranceProgressListener(TextSpeaker.this);
                    }
                } else {
                    mTextToSpeech = null;
                }
            }
        });
    }

    public void stop() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
            mTextToSpeech = null;
        }
    }

    /**
     * @return null if ok, or error string otherwise.
     */
    public String speak(String text) {
        if (!mTextToSpeechReady) {
            return "Error: TextToSpeech not yet initialised";
        } else if (!mTextToSpeechLanguageAvailable) {
            return "Error: TextToSpeech Russian not available";
        } else if (mTextToSpeech == null) {
            return "Error: TextToSpeech failed to initialise";
        } else {
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
     */

    public void setSpeakerListener(@Nullable SpeakerListener speakerListener) {
        mSpeakerListener = speakerListener;
    }

    public interface SpeakerListener {
        void onDoneUttering(boolean error);       // Speaker has finished uttering the text.
    }

}
