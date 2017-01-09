package com.wolfie.odile.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wolfie.odile.view.BaseUi;
import com.wolfie.odile.presenter.DrawerPresenter.DrawerUi;
import com.wolfie.odile.view.fragment.FileFragment;
import com.wolfie.odile.view.fragment.ListFragment;

import java.util.List;

public class DrawerPresenter extends BasePresenter<DrawerUi> {

    private final static String KEY_DRAWER_SHOWING = "KEY_DRAWER_SHOWING";
    private boolean mIsOpen;

    public DrawerPresenter(DrawerUi drawerUi) {
        super(drawerUi);
    }

    @Override
    public void resume() {
        super.resume();
        Log.d(MainPresenter.TAG, "DrawerPresenter.resume, mIsOpen=" + mIsOpen);
        if (!mIsOpen) {
            getUi().closeDrawer();
        } else {
            getUi().openDrawer();
        }
    }

    @Override
    public void pause() {
        super.pause();
        mIsOpen = !getUi().isDrawerClosed();
        Log.d(MainPresenter.TAG, "DrawerPresenter.pause, mIsOpen=" + mIsOpen);
    }

    @Override
    public void onSaveState(Bundle outState) {
        outState.putBoolean(KEY_DRAWER_SHOWING, mIsOpen);
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        mIsOpen = savedState.getBoolean(KEY_DRAWER_SHOWING, false);
    }

    public void onDrawerOpened() {
        // Fetch the headings and selected from ListPresenter and set to the view
        ListPresenter listPresenter = getUi().findPresenter(ListFragment.class);
        if (listPresenter != null) {
            List<String> headings = listPresenter.getHeadings();
            String selected = listPresenter.getGroupName();
            getUi().refreshListWithHeadings(headings);
            getUi().selectListItem(selected);
        }
    }

    public void closeDrawer() {
        getUi().closeDrawer();
    }

    public void onItemSelected(String groupName, boolean hasChanged) {
        getUi().closeDrawer();
        if (hasChanged) {
            // Inform ListPresenter of a new filtered group value
            ListPresenter listPresenter = getUi().findPresenter(ListFragment.class);
            if (listPresenter != null) {
                listPresenter.setGroupName(groupName);
            }
        }
    }

    @Override
    public boolean backPressed() {
        if (getUi().isDrawerClosed()) {
            return true;        // Means: not consumed here
        }
        getUi().closeDrawer();
        return false;
    }

    public void onMenuSettingsClick() {
        getUi().closeDrawer();
//        SettingsPresenter settingsPresenter = getUi().findPresenter(SettingsFragment.class);
//        settingsPresenter.show();
    }
    public void onMenuHelp() {
        getUi().closeDrawer();
//        HelpPresenter helpPresenter = getUi().findPresenter(HelpFragment.class);
//        helpPresenter.show();
    }
    public void onMenuExportClick() {
        getUi().closeDrawer();
        FilePresenter filePresenter = getUi().findPresenter(FileFragment.class);
        filePresenter.exporting();
    }
    public void onMenuImportClick() {
        getUi().closeDrawer();
        FilePresenter filePresenter = getUi().findPresenter(FileFragment.class);
        filePresenter.importing();
    }
    public void onMenuBackup() {
        getUi().closeDrawer();
//        FilePresenter filePresenter = getUi().findPresenter(FileFragment.class);
//        filePresenter.backup();
    }
    public void onMenuRestore() {
//        getUi().closeDrawer();
        MainPresenter mainPresenter = getUi().findPresenter(null);
        mainPresenter.restoreFromGoogleDrive();
    }

    public interface DrawerUi extends BaseUi {
//        boolean isDrawerOpen();
        boolean isDrawerClosed();
        void closeDrawer();
        void refreshListWithHeadings(List<String> headings);
        void selectListItem(@Nullable String selected);
        void openDrawer();
    }

}
