package com.wolfie.odile.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.internal.util.Predicate;
import com.wolfie.odile.model.DataSet;
import com.wolfie.odile.model.ImageEnum;
import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;
import com.wolfie.odile.model.loader.AsyncListeningTask;
import com.wolfie.odile.presenter.ListPresenter.ListUi;
import com.wolfie.odile.talker.TextToSpeechManager;
import com.wolfie.odile.view.BaseUi;
import com.wolfie.odile.view.activity.OdileActivity;
import com.wolfie.odile.view.fragment.EditFragment;
import com.wolfie.odile.view.fragment.TalkFragment;

import java.util.List;

public class ListPresenter extends BasePresenter<ListUi> implements
        AsyncListeningTask.Listener<DataSet>, OdileActivity.SearchListener {

    private final static String KEY_LIST_GROUPNAME = "KEY_LIST_GROUPNAME";

    // If non-null, then only show phrases from this group.
    @Nullable
    private String mGroupName;

    private TextToSpeechManager mTextToSpeechManager;

    // These values are not saved, but refreshed upon resume.
    // Note that mHeadings and mGroup are taken from here by
    // DrawerPresenter.resume() to reload the nav menu
    private DataSet mDataSet;
    private List<String> mHeadings;

    private int mImageIndex = 0;    // Index into the ImageEnum.

    public ListPresenter(ListUi listUi) {
        super(listUi);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void resume() {
        super.resume();
        // TODO - what about the searchCriterion ?
        loadPhrases();
        mTextToSpeechManager = new TextToSpeechManager(getContext());
    }

    @Override
    public void pause() {
        super.pause();
        mTextToSpeechManager.stop();
    }

    @Override
    public void onSaveState(Bundle outState) {
        outState.putString(KEY_LIST_GROUPNAME, mGroupName);
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        if (savedState != null) {
            mGroupName = savedState.getString(KEY_LIST_GROUPNAME);
        }
    }

    @Override
    public boolean backPressed() {
        return true;        // Means: not consumed here.
    }

    public void loadPhrases() {
        MainPresenter mainPresenter = getUi().findPresenter(null);
        if (mainPresenter != null) {
            mainPresenter.getPhraseLoader().read(this);
        }
        getUi().setAddEntryVisibility(true);
        getUi().hideNoFilteredPhrasesWarning();
    }

    /**
     * Set the DataSet.  Use the existing group name and display the (possibly
     * filtered) list on the ui. Then build a new list of group headings
     * which are passed to the DrawerPresenter.
     */
    @Override
    public void onCompletion(DataSet dataSet) {
        mDataSet = dataSet;
        mHeadings = PhraseGroup.buildHeadingsList(dataSet);
        setGroupName(mGroupName);
    }

    /**
     * Set the new group name for filtering, use it with the existing
     * (already loaded) DataSet to build a list of structured phrases,
     * and pass to the ui for display. Called by the DrawerPresenter.
     */
    public void setGroupName(@Nullable String groupName) {
        mGroupName = groupName;
        if (mDataSet != null) {
            List<PhraseGroup> groups = PhraseGroup.buildGroups(mGroupName, mDataSet);
            getUi().showPhrases(groups);
            mDisplayGroups = groups;
        }
    }

    public List<PhraseGroup> getDisplayGroups() {
        return mDisplayGroups;
    }

    @NonNull
    private List<PhraseGroup> mDisplayGroups;        // References the currently displayed groups.

    private List<PhraseGroup> mTempGroups;           // Contains all phrases.
    private List<PhraseGroup> mFilteredGroups;       // Contains phrases filtered from mTempGroups.

    @Override
    public void onQueryClick() {
        // For the duration of the query, the mGroupName and mDataSet are left untouched
        // and a new temporary filtered dataSet is used for display.
        // As the query text changes, filter the tempGroups --> filteredGroups
        // and pass it into showPhrases with the searchText

        mTempGroups = PhraseGroup.buildSingleGroup("Matching phrases", mDataSet);
        mFilteredGroups = PhraseGroup.buildSingleGroup("Matching phrases", null);
        getUi().showFilteredPhrases(mTempGroups, "");       // Will show all all phrases, since no search-filtering yet
        mDisplayGroups = mTempGroups;
        getUi().setAddEntryVisibility(false);               // Add function disabled.
    }

    @Override
    public void onQueryTextChange(String criteria) {
        // Use mTempGroups as a flag to indicate if onQueryClick has been called yet
        // because onQueryTextChange seems to get called first.
        if (mTempGroups != null) {
            getUi().hideNoFilteredPhrasesWarning();
            if (TextUtils.isEmpty(criteria)) {
                // Use unfiltered list
                getUi().showFilteredPhrases(mTempGroups, "");
                mDisplayGroups = mTempGroups;
            } else {
                // Filter
                List<Phrase> filteredEntries = mFilteredGroups.get(0).getPhrases();
                filteredEntries.clear();
                Predicate<Phrase> predicate = getFilterPredicate(criteria);
                for (Phrase phrase : mTempGroups.get(0).getPhrases()) {
                    if (predicate.apply(phrase)) {
                        filteredEntries.add(phrase);
                    }
                }
                getUi().showFilteredPhrases(mFilteredGroups, criteria);
                mDisplayGroups = mFilteredGroups;
                if (filteredEntries.size() == 0) {
                    getUi().showNoFilteredPhrasesWarning();
                }
            }
        }
    }

    @Override
    public void onQueryClose() {
        getUi().hideNoFilteredPhrasesWarning();
        // Refresh the list with the previous groupName/mDataSet
        onCompletion(mDataSet);
        getUi().setAddEntryVisibility(true);       // Add function re-enabled.
        mTempGroups = null;
    }

    protected Predicate<Phrase> getFilterPredicate(final String criteria) {
        return new Predicate<Phrase>() {
            @Override
            public boolean apply(Phrase phrase) {
                return phrase.getRussian().toLowerCase().contains(criteria.toLowerCase())
                        || phrase.getEnglish().toLowerCase().contains(criteria.toLowerCase());
            }
        };
    }

    public void onEditItemClick(Phrase selectedPhrase) {
        EditPresenter editPresenter = getUi().findPresenter(EditFragment.class);
        if (editPresenter != null) {
            if (selectedPhrase != null) {
                editPresenter.editPhrase(selectedPhrase);
            } else {
                // Set up a new Phrase for the currently selected group name.
                editPresenter.editNewPhrase(mGroupName);
            }
        }
    }

    public void onSpeakClick() {
        TalkPresenter talkPresenter = getUi().findPresenter(TalkFragment.class);
        if (talkPresenter != null) {
            talkPresenter.getUi().show();
        }
    }

    public void onRussianTextClick(Phrase selectedPhrase) {
        String errorMsg = mTextToSpeechManager.speak(selectedPhrase.getRussian());
        if (errorMsg != null) {
            getUi().showBanner(errorMsg);
        }
    }

    public void bumpImage() {
        if (++mImageIndex >= ImageEnum.values().length) {
            mImageIndex = 0;
        }
        getUi().setBackgroundImage(mImageIndex);
        getUi().hidePullToRefreshSpinner();

    }

    @Nullable
    public String getGroupName() {
        return mGroupName;
    }

    public List<String> getHeadings() {
        return mHeadings;
    }

    public interface ListUi extends BaseUi {

        void hidePullToRefreshSpinner();
        void setBackgroundImage(int enumIndex);

        // Show phrases from the list of groups.
        void showPhrases(@Nullable List<PhraseGroup> groups);
        // Show phrases, all expanded (with contract disabled) and search text highlighted in each phrase.
        // The sticky header is static, with the single group heading used.
        void showFilteredPhrases(@Nullable List<PhraseGroup> groups, @Nullable String searchText);

        void setAddEntryVisibility(boolean visible);
        void hideStickyHeader();

        void showNoFilteredPhrasesWarning();
        void hideNoFilteredPhrasesWarning();

    }

}
