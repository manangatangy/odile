package com.wolfie.odile.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.wolfie.odile.R;
import com.wolfie.odile.model.Entry;
import com.wolfie.odile.model.EntryGroup;
import com.wolfie.odile.presenter.ListPresenter;
import com.wolfie.odile.util.DefaultLayoutManager;
import com.wolfie.odile.view.adapter.GroupingRecyclerAdapter;
import com.wolfie.odile.view.adapter.GroupingRecyclerAdapter.AdapterMode;
import com.wolfie.odile.view.adapter.ScrollListeningRecyclerView;
import com.wolfie.odile.presenter.ListPresenter.ListUi;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ListFragment extends BaseFragment implements
        ListUi,
        ScrollListeningRecyclerView.ItemScrollListener,
        GroupingRecyclerAdapter.OnItemInListClickedListener {

    @BindView(R.id.sticky_header)
    View mStickyHeaderFrame;

    @BindView(R.id.heading_divider_top)
    View mStickyHeaderDividerTop;

    @BindView(R.id.heading_text_view)
    TextView mStickyHeaderText;

    @BindView(R.id.recycler_view)
    ScrollListeningRecyclerView mRecyclerView;

    @BindView(R.id.add_entry_fab)
    View mAddEntryButton;

    @BindView(R.id.no_filtered_results_text_view)
    TextView mNoFilteredEntriesWarning;

    @OnClick(R.id.add_entry_fab)
    public void onAddEntryClick() {
        mListPresenter.onListItemClick(null);
    }

    private ListPresenter mListPresenter;

    private boolean mAutoUpdateStickyHeader;

    @Override
    public ListPresenter getPresenter() {
        return mListPresenter;
    }

    public ListFragment() {
        mListPresenter = new ListPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setLayoutManager(new DefaultLayoutManager(getContext()));
        mRecyclerView.setItemScrollListener(this);
    }

    /**
     * Display the entries in an expanding/contracting list view, with no highlighting.
     * @param groups Lists of entries to display.  If null, clear the list. Otherwise
     *               also scroll to the top of the list.
     */
    @Override
    public void showEntries(@Nullable List<EntryGroup> groups) {
        mAutoUpdateStickyHeader = true;
        if (groups == null) {
            getAdapter(AdapterMode.EXPANDING_CONTRACTING).clearItems();
        } else {
            getAdapter(AdapterMode.EXPANDING_CONTRACTING).setGroups(groups, null);
            // Scroll back to the top of the list.  If the list is short, no scrolling
            // will occur and so we also have to trigger the sticky header refresh.
            mRecyclerView.scrollToPosition(0);
            onItemAlignedToTop(0);
        }
    }

    /**
     * Display the entries in an fixed expanded list view, with highlighting.
     * Shows all the entries expanded, fixes the sticky header to a static text,
     * rather than the auto-updating value (depending on which entry is at the top).
     * @param groups Lists of entries to display.  If null, clear the list. Otherwise
     *               also scroll to the top of the list.
     * @param searchText If not blank, then use to highlight the fields in the viewHolders.
     */
    public void showFilteredEntries(@NonNull List<EntryGroup> groups, @NonNull String searchText) {
        mAutoUpdateStickyHeader = false;
        int matches = groups.get(0).getEntries().size();        // Must be one EntryGroup
        mStickyHeaderText.setText(groups.get(0).getHeading() + ":  " + matches);

        getAdapter(AdapterMode.FIXED_EXPANDED).setGroups(groups, searchText);
        // Scroll back to the top of the list.  If the list is short, no scrolling
        // will occur and so we also have to trigger the sticky header refresh.
        mRecyclerView.scrollToPosition(0);
        onItemAlignedToTop(0);
    }

    /**
     * @param mode specifies if the list items should be shown as initially contracted or
     *             expanded. If this mode is different to the current adapter's mode, then
     *             create a new one with the required mode.
     * @return the GroupingRecyclerAdapter which should be used for supplying the recyclerView
     */
    private GroupingRecyclerAdapter getAdapter(@GroupingRecyclerAdapter.AdapterMode int mode) {
        GroupingRecyclerAdapter adapter = (GroupingRecyclerAdapter)mRecyclerView.getAdapter();
        if (adapter == null || adapter.getMode() != mode) {
            adapter = new GroupingRecyclerAdapter(mode);
            adapter.setOnItemInListClickerListener(this);
            mRecyclerView.setAdapter(adapter);
        }
        return adapter;
    }

    @Override
    public void onItemAlignedToTop(int position) {
        if (mAutoUpdateStickyHeader) {
            // This method is called by the ScrollListeningRecyclerView.  At this stage,
            // showEntries or showFilteredEntries should have already been called to populate
            // the list, so there should be an adapter instance already associated with the
            // recyclerView (and we don't need to specify the AdapterMode).
            if (mRecyclerView.getAdapter() != null) {
                GroupingRecyclerAdapter adapter = (GroupingRecyclerAdapter)mRecyclerView.getAdapter();
                Object item = adapter.getItemAt(position);
                String headerText;
                if (item instanceof Entry) {
                    Entry entry = (Entry) item;
                    headerText = entry.getGroupName();
//            if (headerText == null || headerText.length() == 0) {
//                return;
//            }
                } else {
                    headerText = (String) item;
                }
                mStickyHeaderText.setText(headerText);
                mStickyHeaderFrame.setVisibility(View.VISIBLE);
                mStickyHeaderDividerTop.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showNoFilteredEntriesWarning() {
        mNoFilteredEntriesWarning.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoFilteredEntriesWarning() {
        mNoFilteredEntriesWarning.setVisibility(View.GONE);
    }

    @Override
    public void setAddEntryVisibility(boolean visible) {
        mAddEntryButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void hideStickyHeader() {
        mStickyHeaderFrame.setVisibility(View.GONE);
    }

    @Override
    public void onListItemClick(Entry selectedEntry) {
        mListPresenter.onListItemClick(selectedEntry);
    }

}
