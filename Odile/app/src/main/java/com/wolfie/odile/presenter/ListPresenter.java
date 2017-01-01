package com.wolfie.odile.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.internal.util.Predicate;
import com.wolfie.odile.model.DataSet;
import com.wolfie.odile.model.Entry;
import com.wolfie.odile.model.EntryGroup;
import com.wolfie.odile.model.loader.AsyncListeningTask;
import com.wolfie.odile.presenter.ListPresenter.ListUi;
import com.wolfie.odile.view.BaseUi;
import com.wolfie.odile.view.activity.OdileActivity;
import com.wolfie.odile.view.fragment.EditFragment;

import java.util.List;


public class ListPresenter extends BasePresenter<ListUi> implements
        AsyncListeningTask.Listener<DataSet>, OdileActivity.SearchListener {

    private final static String KEY_LIST_GROUPNAME = "KEY_LIST_GROUPNAME";

    // If non-null, then only show entries from this group.
    @Nullable
    private String mGroupName;

    // These values are not saved, but refreshed upon resume.
    // Note that mHeadings and mGroupName are taken from here by
    // DrawerPresenter.resume() to reload the nav menu
    private DataSet mDataSet;
    private List<String> mHeadings;

    public ListPresenter(ListUi listUi) {
        super(listUi);
    }

    @Override
    public void resume() {
        super.resume();
        // TODO - what about the searchCriterion ?
        loadEntries();
    }

    @Override
    public void pause() {
        super.pause();
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

    public void loadEntries() {
        MainPresenter mainPresenter = getUi().findPresenter(null);
        if (mainPresenter != null) {
            mainPresenter.getEntryLoader().read(this);
        }
        getUi().setAddEntryVisibility(true);
        getUi().hideNoFilteredEntriesWarning();
    }

    /**
     * Set the DataSet.  Use the existing group name and display the (possibly
     * filtered) list on the ui. Then build a new list of group headings
     * which are passed to the DrawerPresenter.
     */
    @Override
    public void onCompletion(DataSet dataSet) {
        mDataSet = dataSet;
        mHeadings = EntryGroup.buildHeadingsList(dataSet);
        setGroupName(mGroupName);
    }

    /**
     * Set the new group name for filtering, use it with the existing
     * (already loaded) DataSet to build a list of structured entries,
     * and pass to the ui for display. Called by the DrawerPresenter.
     */
    public void setGroupName(@Nullable String groupName) {
        mGroupName = groupName;
        if (mDataSet != null) {
            List<EntryGroup> groups = EntryGroup.buildGroups(mGroupName, mDataSet);
            getUi().showEntries(groups);
        }
    }

    private List<EntryGroup> mTempGroups;           // Contains all entries.
    private List<EntryGroup> mFilteredGroups;       // Contains entries filtered from mTempGroups.

    @Override
    public void onQueryClick() {
        // For the duration of the query, the mGroupName and mDataSet are left untouched
        // and a new temporary filtered dataSet is used for display.
        // As the query text changes, filter the tempGroups --> filteredGroups
        // and pass it into showEntries with the searchText

        mTempGroups = EntryGroup.buildSingleGroup("Matching entries", mDataSet);
        mFilteredGroups = EntryGroup.buildSingleGroup("Matching entries", null);
        getUi().showFilteredEntries(mTempGroups, "");       // Will show all all entries, since no search-filtering yet
        getUi().setAddEntryVisibility(false);               // Add function disabled.
    }

    @Override
    public void onQueryTextChange(String criteria) {
        // Use mTempGroups as a flag to indicate if onQueryClick has been called yet
        // because onQueryTextChange seems to get called first.
        if (mTempGroups != null) {
            getUi().hideNoFilteredEntriesWarning();
            if (TextUtils.isEmpty(criteria)) {
                // Use unfiltered list
                getUi().showFilteredEntries(mTempGroups, "");
            } else {
                // Filter
                List<Entry> filteredEntries = mFilteredGroups.get(0).getEntries();
                filteredEntries.clear();
                Predicate<Entry> predicate = getFilterPredicate(criteria);
                for (Entry entry : mTempGroups.get(0).getEntries()) {
                    if (predicate.apply(entry)) {
                        filteredEntries.add(entry);
                    }
                }
                getUi().showFilteredEntries(mFilteredGroups, criteria);
                if (filteredEntries.size() == 0) {
                    getUi().showNoFilteredEntriesWarning();
                }
            }
        }
    }

    @Override
    public void onQueryClose() {
        getUi().hideNoFilteredEntriesWarning();
        // Refresh the list with the previous groupName/mDataSet
        onCompletion(mDataSet);
        getUi().setAddEntryVisibility(true);       // Add function re-enabled.
        mTempGroups = null;
    }

    protected Predicate<Entry> getFilterPredicate(final String criteria) {
        return new Predicate<Entry>() {
            @Override
            public boolean apply(Entry entry) {
                return entry.getEntryName().toLowerCase().contains(criteria.toLowerCase())
                        || entry.getContent().toLowerCase().contains(criteria.toLowerCase());
            }
        };
    }

    public void onListItemClick(Entry selectedEntry) {
        EditPresenter editPresenter = getUi().findPresenter(EditFragment.class);
        if (editPresenter != null) {
            if (selectedEntry != null) {
                editPresenter.editEntry(selectedEntry);
            } else {
                // Set up a new Entry for the currently selected groupname.
                editPresenter.editNewEntry(mGroupName);
            }
        }
    }

    @Nullable
    public String getGroupName() {
        return mGroupName;
    }

    public List<String> getHeadings() {
        return mHeadings;
    }

    public interface ListUi extends BaseUi {

        // Show entries from the list of groups.
        void showEntries(@Nullable List<EntryGroup> groups);
        // Show entries, all expanded (with contract disabled) and search text highlighted in each entry.
        // The sticky header is static, with the single group heading used.
        void showFilteredEntries(@Nullable List<EntryGroup> groups, @Nullable String searchText);

        void setAddEntryVisibility(boolean visible);
        void hideStickyHeader();

        void showNoFilteredEntriesWarning();
        void hideNoFilteredEntriesWarning();

    }

}
