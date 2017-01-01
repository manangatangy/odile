package com.wolfie.odile.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by david on 22/09/16.
 */

public final class KeyboardUtils {

    private static final int DELAY_MILLIS_SHOW_KEYBOARD_ON_RESUME = 200;

    /**
     * Display the IME keyboard for the specified view after a short delay.
     */
    public static void showKeyboard(final View view) {
        view.requestFocus();
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) view.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, DELAY_MILLIS_SHOW_KEYBOARD_ON_RESUME);
    }

    /**
     * Use this method when the focused view is known (e.g. inside OnEditorActionListener).
     * @return true if keyboard was dismissed, otherwise false
     */
    public static boolean dismissKeyboard(View view) {
        boolean result = false;
        if (view != null && view.getContext() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) view.getContext()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            result = inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return result;
    }

    /**
     * Use this method only when the currently focused view is unknown.
     * @return true if keyboard was dismissed, otherwise false
     */
    public static boolean dismissKeyboard(Activity activity) {
        boolean result = false;
        if (activity != null) {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                result = inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        return result;
    }

    private KeyboardUtils() { }
}
