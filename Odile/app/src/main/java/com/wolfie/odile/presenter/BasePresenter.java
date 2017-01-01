package com.wolfie.odile.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import com.wolfie.odile.view.BaseUi;
import com.wolfie.odile.view.activity.BaseActivity.KeyboardVisibility;

public abstract class BasePresenter<T extends BaseUi> implements Presenter {

    // This field can be protected once the inject mechanism is completed (it should be @Inject)
    public T mUi;

    protected boolean mPaused;

    protected T getUi() {
        return mUi;
    }

    public BasePresenter(T ui) {
        this.mUi = ui;
    }

    @Override
    public boolean backPressed() {
        return true;        // Means: not consumed here.
    }

    @Override
    public boolean homeAsUpPressed() {
        return true;        // Means: not consumed here.
    }

    @Override
    public void onKeyboardVisibilityChanged(KeyboardVisibility keyboardVisibility) {
    }

    /**
     * The frag may have been paused, while waiting for the callback to complete.
     * @return true if the associated fragment is in paused state.
     */
    protected boolean isPaused() {
        return mPaused;
    }

    public Context getContext() {
        return getUi().getContext();
    }

    @CallSuper
    @Override
    public void pause() {
        mPaused = true;
//        getUi().hideSoftKeyboard();
    }

    @CallSuper
    @Override
    public void resume() {
        mPaused = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onSaveState(Bundle outState) {
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
    }

}
