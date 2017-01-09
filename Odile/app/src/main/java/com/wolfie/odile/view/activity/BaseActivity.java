package com.wolfie.odile.view.activity;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;

import com.wolfie.odile.presenter.Presenter;
import com.wolfie.odile.view.fragment.BaseFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inject would normally be called here
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Don't delete this method. It is needed so fragment.onSaveInstanceState() is called.
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addKeyboardVisibilityListener();
        if(getPresenter() != null) {
            getPresenter().resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeKeyboardVisibilityListener();
        if (getPresenter() != null) {
            getPresenter().pause();
        }
    }

    /**
     * BaseActivity defaults to having no presenter, override this is yo need one.
     */
    @Nullable
    public Presenter getPresenter() {
        return null;
    }


    private List<WeakReference<BaseFragment>> mAttachedFrags = new ArrayList<>();
    @Override

    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof BaseFragment) {
            mAttachedFrags.add(new WeakReference<>((BaseFragment) fragment));
        }
    }

    public List<BaseFragment> getActiveBaseFragments() {
        ArrayList<BaseFragment> ret = new ArrayList<>();
        for (WeakReference<BaseFragment> ref : mAttachedFrags) {
            BaseFragment baseFragment = ref.get();
            if (baseFragment != null && baseFragment.isAdded()) {
                ret.add(baseFragment);
            }
        }
        return ret;
    }

    public <T extends BaseFragment> T findFragment(Class<T> fragClass) {
        List<BaseFragment> fragmentList = getActiveBaseFragments();
        for (BaseFragment fragment : fragmentList) {
            if (fragment != null) {
                Class c = fragment.getClass();
                if (c.equals(fragClass)) {
                    // noinspection unchecked
                    return (T)fragment;
                }
            }
        }
        return null;
    }

    // TODO - save/restore this member
    private Map<Integer, BaseFragment> mPermissionsFragmentMap= new HashMap<>();

    public void requestPermissions(final @NonNull BaseFragment baseFragment,
                                   final @NonNull String[] permissions, final int requestCode) {
        mPermissionsFragmentMap.put(requestCode, baseFragment);
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        BaseFragment baseFragment = mPermissionsFragmentMap.get(requestCode);
        if (baseFragment != null) {
            baseFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        mPermissionsFragmentMap.remove(requestCode);
    }

    /**
     * Can be used for inter-presenter communication.
     */
    @Nullable
    public <F extends BaseFragment, P extends Presenter> P findPresenter(Class<F> fragClass) {
        F frag = findFragment(fragClass);
        // noinspection unchecked
        return (frag == null) ? null : (P)frag.getPresenter();
    }

    @Override
    public void onBackPressed() {
        boolean shouldPerformBackPressed = true;
        for (BaseFragment baseFragment : getActiveBaseFragments()) {
            shouldPerformBackPressed = shouldPerformBackPressed & baseFragment.onBackPressed();
        }
        // If the back press isn't handled by any of the child fragments,
        // then it is passed to the Activity's presenter (if there is one).
        if (shouldPerformBackPressed) {
            if (getPresenter() != null) {
                getPresenter().backPressed();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void addKeyboardVisibilityListener() {
        final View activityRootView = getActivityRootView();
        if (activityRootView != null) {
            activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    }

    private void removeKeyboardVisibilityListener() {
        final View activityRootView = getActivityRootView();
        if (activityRootView != null) {
            activityRootView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    }

    @NonNull
    private KeyboardVisibility mKeyboardVisibility = KeyboardVisibility.UNKNOWN;

    public enum KeyboardVisibility {
        UNKNOWN, HIDDEN, SHOWING
    }

    final ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            KeyboardVisibility keyboardVisibility = getKeyboardVisibility();
            if (keyboardVisibility != mKeyboardVisibility) {
                mKeyboardVisibility = keyboardVisibility;
                onKeyboardVisibilityChanged(mKeyboardVisibility);
            }
        }
    };

    /**
     * Notify interested fragments when software keyboard visibility changes.
     */
    public void onKeyboardVisibilityChanged(KeyboardVisibility keyboardVisibility) {
        for (BaseFragment baseFragment : getActiveBaseFragments()) {
            baseFragment.onKeyboardVisibilityChanged(keyboardVisibility);
        }
    }

    /**
     * Check if software keyboard is visible or not, based on window height and root view height.
     */
    public KeyboardVisibility getKeyboardVisibility() {
        KeyboardVisibility visibility = KeyboardVisibility.UNKNOWN;
        if (getWindowHeight() > 0 && getActivityRootViewHeight() > 0) {
            // if root view takes less space than the allowed by the window - most likely keyboard is displayed
            if (getWindowHeight() - getActivityRootViewHeight() >= getApproxKeyboardViewHeight()) {
                visibility = KeyboardVisibility.SHOWING;
            } else {
                visibility = KeyboardVisibility.HIDDEN;
            }
        }
        return visibility;
    }

    int getActivityRootViewHeight() {
        int activityRootViewHeight = -1;
        final View activityRootView = getActivityRootView();
        if (activityRootView != null) {
            Rect r = new Rect();
            //r will be populated with the coordinates of your view that area still visible.
            activityRootView.getWindowVisibleDisplayFrame(r);
            activityRootViewHeight = (r.bottom - r.top);
        }
        return activityRootViewHeight;
    }

    public abstract View getActivityRootView();

    private static int mWindowHeight = -1;              // window height, doesn't include buttons and status bar
    private static int mApproxKeyboardViewHeight = -1;  // assume it's 1/5th of the sWindowHeight

    int getApproxKeyboardViewHeight() {
        if (mWindowHeight <= 0 || mApproxKeyboardViewHeight <= 0) {
            getWindowHeight();
        }
        return mApproxKeyboardViewHeight;
    }

    int getWindowHeight() {
        if (mWindowHeight <= 0) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mWindowHeight = size.y;
            mApproxKeyboardViewHeight = mWindowHeight / 5;
        }
        return mWindowHeight;
    }
}
