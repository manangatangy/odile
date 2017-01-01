package com.wolfie.odile.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.wolfie.odile.view.activity.BaseActivity.KeyboardVisibility;

/**
 * Presenter can be owned by Activity or Fragment.
 */

public interface Presenter {

    void onCreate(Bundle savedInstanceState);

    void resume();

    void pause();

    void onDestroy();

    void onSaveState(Bundle outState);

    void onRestoreState(@Nullable Bundle savedState);

    /**
     * @return false if the back press has been consumed
     */
    boolean backPressed();

    /**
     * @return false if the home press has been consumed
     */
    boolean homeAsUpPressed();

    void onKeyboardVisibilityChanged(KeyboardVisibility keyboardVisibility);

}
