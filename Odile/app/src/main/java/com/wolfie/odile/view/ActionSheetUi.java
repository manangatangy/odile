package com.wolfie.odile.view;

/**
 * Created by david on 23/10/16.
 */

public interface ActionSheetUi extends BaseUi {

    // The following are implemented in ActionSheetFragment
    void dismissKeyboard(boolean andClose);
    boolean isKeyboardVisible();
    void show();
    void hide();
    boolean isShowing();

}
