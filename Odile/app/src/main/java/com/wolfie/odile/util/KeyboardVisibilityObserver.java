package com.wolfie.odile.util;

/**
 * Created by david on 22/09/16.
 */

import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.wolfie.odile.R;

public class KeyboardVisibilityObserver implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = "KeyboardVisibility";
    private View mView;
    private KeyboardVisibilityListener mListener;
    private @KeyboardState int mCurrentState = KeyboardState.NONE;

    public KeyboardVisibilityObserver(View view, KeyboardVisibilityListener listener) {
        mView = view;
        mListener = listener;
        mView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        View rootView = mView.getRootView();
        View toolBar = rootView.findViewById(R.id.toolbar);
        int toolBarHeight = (toolBar == null) ? 0 : toolBar.getHeight();
        // Ref http://stackoverflow.com/a/26152562
        // 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard
        final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;

        Rect visibleRect = new Rect();
        rootView.getWindowVisibleDisplayFrame(visibleRect);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();

        // heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top)
        // This is the height of the root view that is not visible
        int heightDiff = rootView.getBottom() - visibleRect.bottom;
        // Threshold size: dp to pixels, multiply with display density
        boolean isKeyboardShown = heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density;

        @KeyboardState int newState = isKeyboardShown ? KeyboardState.SHOWING : KeyboardState.HIDDEN;

        Log.d(TAG, "isKeyboardShown: " + newState + ", (was: " + mCurrentState +
                "), heightDiff:" + heightDiff + ", density:" + dm.density +
                ", visibleRect:" + visibleRect +
                ", toolbarHeight=" + toolBarHeight);
        Log.d(TAG, "root view height:" + rootView.getHeight() + ", top=" + rootView.getTop() + ", bottom=" + rootView.getBottom() );
        if (mCurrentState != newState) {
            mCurrentState = newState;
            if (mListener != null) {
                if (isKeyboardShown) {
                    mListener.onShow(heightDiff - toolBarHeight);
                } else {
                    mListener.onHide();
                }
            }
        }

    }

    public interface KeyboardVisibilityListener {
        void onShow(int keyboardHeight);
        void onHide();
    }

    @IntDef({KeyboardState.NONE, KeyboardState.HIDDEN, KeyboardState.SHOWING})
    public @interface KeyboardState {
        int NONE = 0;
        int HIDDEN = 1;
        int SHOWING = 2;
    }
}
