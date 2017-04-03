package com.wolfie.odile.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.LayoutRes;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.wolfie.odile.R;
import com.wolfie.odile.presenter.DrawerPresenter;
import com.wolfie.odile.presenter.ListPresenter;
import com.wolfie.odile.presenter.MainPresenter;
import com.wolfie.odile.talker.TalkerService;
import com.wolfie.odile.view.activity.ServiceBinder.ServiceBinderListener;
import com.wolfie.odile.view.fragment.EditFragment;
import com.wolfie.odile.view.fragment.FileFragment;
import com.wolfie.odile.view.fragment.ListFragment;
import com.wolfie.odile.view.fragment.DrawerFragment;
import com.wolfie.odile.view.fragment.TalkFragment;

import butterknife.BindView;

public class OdileActivity extends SimpleActivity {

    public static final int REQUEST_TTS_DATA_CHECK = 345;
    public static final int REQUEST_DRIVE_RESOLUTION = 346;
    public static final int REQUEST_DRIVE_OPENER = 347;

    @BindView(R.id.layout_activity_drawer)
    public DrawerLayout mDrawer;

    @BindView(R.id.progress_overlay)
    View mProgressOverlayView;

    private MainPresenter mMainPresenter;

    @Override
    public MainPresenter getPresenter() {
        return mMainPresenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainPresenter = new MainPresenter(null, getApplicationContext());

        // Set the initial values for some settings.  May be changed later by SettingsPresenter
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        int sessionTimeout = prefs.getInt(SettingsPresenter.PREF_SESSION_TIMEOUT, TimeoutMonitor.DEFAULT_TIMEOUT);
//        mMainPresenter.setTimeout(sessionTimeout, false);       // No need to start; we are not yet logged in
//        int enumIndex = prefs.getInt(SettingsPresenter.PREF_SESSION_BACKGROUND_IMAGE, 0);
        setBackgroundImage(0);

        // Create the main content fragment into it's container.
        setupFragment(ListFragment.class.getName(), R.id.fragment_container_activity_simple, null);

        // Create the drawer fragment into it's container.
        setupFragment(DrawerFragment.class.getName(), R.id.fragment_container_activity_drawer, null);

        // Create the entry edit (activity sheet) fragment into it's container.
        setupFragment(EditFragment.class.getName(), R.id.fragment_container_edit, null);

        // Create the file (activity sheet) fragment into it's container.
        setupFragment(FileFragment.class.getName(), R.id.fragment_container_file, null);

        // Create the talk (activity sheet) fragment into it's container.
        setupFragment(TalkFragment.class.getName(), R.id.fragment_container_talk, null);

        // Create the settings (activity sheet) fragment into it's container.
//        setupFragment(SettingsFragment.class.getName(), R.id.fragment_container_settings, null);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, REQUEST_TTS_DATA_CHECK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TTS_DATA_CHECK:
                if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
                break;
            case REQUEST_DRIVE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    mMainPresenter.restoreFromGoogleDrive();
                } else {
                    Toast.makeText(this, "Can't resolve Google Drive connection", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_DRIVE_OPENER:
                DriveId driveId = null;         // Null means no file selected.
                if (resultCode == RESULT_OK) {
                    driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                }
                mMainPresenter.retrieveFileContents(driveId);
//                } else {
//                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    @LayoutRes
    public int getLayoutResource() {
        // Specify the layout to use for the OdileActivity.  This layout include
        // the activity_simple layout, which contains the toolbar and the
        // fragment_container_activity_simple container (for ListFragment) as
        // well as fragment_container_activity_drawer for the DrawerFragment
        return R.layout.activity_drawer;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        ListPresenter listPresenter = findPresenter(ListFragment.class);
        SearchViewHandler searchViewHandler = new SearchViewHandler(listPresenter);

        // Retrieve the SearchView and setup the callbacks.
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(searchViewHandler);
        searchView.setOnSearchClickListener(searchViewHandler);
        MenuItemCompat.setOnActionExpandListener(searchItem, searchViewHandler);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainPresenter.setActivity(this);       // Hacky.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void closeMenuDrawer() {
        DrawerPresenter drawerPresenter = findPresenter(DrawerFragment.class);
        drawerPresenter.closeDrawer();
    }

    /**
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     * Ref: http://stackoverflow.com/a/29542951
     */
    private void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = (toVisibility == View.VISIBLE);
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate().setDuration(duration).alpha(show ? toAlpha : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(toVisibility);
            }
        });
    }

    public void showLoadingOverlay() {
        animateView(mProgressOverlayView, View.VISIBLE, 0.4f, 200);
    }

    public void hideLoadingOverlay() {
        animateView(mProgressOverlayView, View.GONE, 0, 200);
    }

    /**
     * The behaviour of the SearchView is as follows:
     * When the view is open, then the cross in the right hand side will only appear if there is
     * some text in the field.  Clicking this cross will then clear the text but won't close (which
     * is called iconify in the SearchView code) the searchView.  To iconify, must either click
     * back-press (after the keyboard is first hidden), or must click the left arrow in the top
     * left of the app bar.  When either of these is done, then the onMenuItemActionCollapse
     * is called.
     * ref: http://stackoverflow.com/a/18186164
     */
    private class SearchViewHandler implements
            MenuItemCompat.OnActionExpandListener,
            SearchView.OnQueryTextListener,
            View.OnClickListener {

        private SearchListener mSearchListener;

        public SearchViewHandler(SearchListener searchListener) {
            mSearchListener = searchListener;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            mSearchListener.onQueryClose();
            return true;  // Return true to collapse action view
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            return true;  // Return true to expand action view
        }

        @Override
        public void onClick(View v) {
            mSearchListener.onQueryClick();
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mSearchListener.onQueryTextChange(newText);
            return true;
        }
    }

    public interface SearchListener {
        void onQueryClick();
        void onQueryTextChange(String newText);
        void onQueryClose();
    }

}
