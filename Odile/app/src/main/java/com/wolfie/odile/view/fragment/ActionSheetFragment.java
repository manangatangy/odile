package com.wolfie.odile.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.wolfie.odile.R;
import com.wolfie.odile.util.KeyboardUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.support.design.widget.Snackbar.LENGTH_LONG;

/**
 * Subclasses should implement onCreateView, to specify their layout and bind their components.
 */
public abstract class ActionSheetFragment extends BaseFragment {

    @BindView(R.id.actionsheet_background_view)
    View mBackgroundView;               // This is GONE/VISIBLE

    @BindView(R.id.actionsheet_padding_view)
    View mPaddingView;                  // This has variable bottom padding

    @BindView(R.id.actionsheet_animating_view)
    RelativeLayout mAnimatingView;      // This animates open/close

    @BindView(R.id.actionsheet_holder_view)
    ScrollView mHolderView;             // This holds the content

    /**
     * Subclass to overide this and inflate their layout into the action_sheet_holder_view
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actionsheet, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    public abstract void onShowComplete();
    public abstract void onHideComplete();
    public abstract void onTouchBackground();

    public boolean isShowing() {
        return mBackgroundView != null && mBackgroundView.getVisibility() == View.VISIBLE;
    }

    public void hide() {
        if (isShowing()) {
            Animation bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.action_sheet_down);
            bottomDown.setAnimationListener(new ActionSheetFragment.SimpleListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mBackgroundView.setVisibility(View.GONE);
//                    mKeyboardVisibilityObserver = null;     // Stop observing the keyboard.
                    invokeHandlerOnUiThread();                // Callback on ui thread.
                }
            });
            mAnimatingView.startAnimation(bottomDown);
        }
    }

    public void show() {
        if (!isShowing()) {
            Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.action_sheet_up);
            bottomUp.setAnimationListener(new ActionSheetFragment.SimpleListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    invokeHandlerOnUiThread();            // Callback on ui thread.
                }
            });
            mAnimatingView.startAnimation(bottomUp);
            mBackgroundView.setVisibility(View.VISIBLE);


//            mKeyboardVisibilityObserver = new KeyboardVisibilityObserver(mPaddingView,
//                    new KeyboardVisibilityObserver.KeyboardVisibilityListener() {
//                        @Override
//                        public void onShow(int keyboardHeight) {
//                            mPaddingView.setPadding(0, 0, 0, keyboardHeight);
//                        }
//                        @Override
//                        public void onHide() {
//                            mPaddingView.setPadding(0, 0, 0, 0);
//                        }
//                    });
        }
    }

    public void dismissKeyboard(boolean andClose) {
        if (isKeyboardVisible()) {
            KeyboardUtils.dismissKeyboard(getActivity());
        }
        if (andClose) {
            hide();
        }
    }

//    // TODO determine the correct keyboard height
//    @Override
//    public void onKeyboardVisibilityChanged(BaseActivity.KeyboardVisibility keyboardVisibility, int keyboardHeight) {
////        int keyboardHeight = (keyboardVisibility == SHOWING) ? 200 : 0;
////        keyboardHeight = (keyboardVisibility == SHOWING) ? 838 : 0;
//        mPaddingView.setPadding(0, 0, 0, keyboardHeight);
//    }

    @OnClick(R.id.actionsheet_background_view)
    public void backGroundViewClicked() {
        // TODO add flags for when 3 states of animation and then change semantics of
        // onTouchBackground to return true if consumed and if false then to close
        onTouchBackground();
    }

    private void invokeHandlerOnUiThread() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isShowing()) {
                    onShowComplete();
                } else {
                    onHideComplete();
                }
            }
        });
    }

    public static class SimpleListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }
        @Override
        public void onAnimationEnd(Animation animation) {
        }
        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

}
