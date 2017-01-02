package com.wolfie.odile.view.adapter;

import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wolfie.odile.R;
import com.wolfie.odile.model.Phrase;
import com.wolfie.odile.model.PhraseGroup;
import com.wolfie.odile.view.adapter.viewholder.BaseViewHolder;
import com.wolfie.odile.view.adapter.viewholder.HeadingViewHolder;
import com.wolfie.odile.view.adapter.viewholder.ItemViewHolder;

import java.util.ArrayList;
import java.util.List;

public class GroupingRecyclerAdapter extends PlaceholderRecyclerAdapter<BaseViewHolder> {

    private static final int VIEW_TYPE_TITLE = 0;
    private static final int VIEW_TYPE_ENTRY = 1;

    private OnItemInListClickedListener mOnItemInListClickedListener;
    private List<PhraseGroup> mGroups = new ArrayList<>();

    private @Nullable String mHighlightText;
    private @AdapterMode int mMode;

    public GroupingRecyclerAdapter(@AdapterMode int mode) {
        mMode = mode;
    }

    public @AdapterMode int getMode() {
        return mMode;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -1) {
            return null;
        }
        View view;
        final BaseViewHolder viewHolder;
        switch (viewType) {
            case VIEW_TYPE_TITLE:
                view = inflateView(parent, R.layout.view_list_heading);
                viewHolder = new HeadingViewHolder(view);
                break;
            case VIEW_TYPE_ENTRY:
            default:
                // For FIXED_EXPANDED mode, inflate the already expanded layout and
                // don't set the listener for toggling.
                view = inflateView(parent,
                        (mMode == AdapterMode.FIXED_EXPANDED)
                                ? R.layout.view_list_item_expanded : R.layout.view_list_item_contracted);
                viewHolder = new ItemViewHolder(view, mOnItemInListClickedListener);
                if  (mMode == AdapterMode.EXPANDING_CONTRACTING) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((ItemViewHolder)viewHolder).toggleDetailView();
                        }
                    });
                }
                break;
        }
        return viewHolder;
    }

    private View inflateView(ViewGroup parent, @LayoutRes int layout) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    /**
     * Update the viewHolder with the contents of the item at the given position in the data set.
     * If searchText is not empty then the relevant searchable fields in the item will be highlighted.
     * If searchText is not null, then the views will be expanded (contraction disable).
     */
    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        Object item = getItemAt(position);
        if (holder != null && item != null) {
            holder.bind(item, mHighlightText);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = -1;
        Object item = getItemAt(position);
        if (item != null) {
            if (item instanceof String) {
                viewType = VIEW_TYPE_TITLE;
            } else {
                viewType = VIEW_TYPE_ENTRY;
            }
        }
        return viewType;
    }

    /**
     * The data in mGroups is mapped to the adapter one to one, plus one item for the
     * heading of each group.  A PhraseGroup must not have null fields.
     */
    @Override
    public int getItemCount() {
        int count = 0;
        for (PhraseGroup group : mGroups) {
            ++count;
            count += group.getPhrases().size();
        }
        return count;
    }

    /**
     * @return either the String (heading) or Phrase at the specified position in the adapter.
     */
    public Object getItemAt(int position) {
        int count = 0;
        for (PhraseGroup group : mGroups) {
            // Check if the heading is at the position.
            if (count++ == position) {
                return group.getHeading();
            }
            // Skip this part if there's no items in the group
            // Only iterate through the list if we know the position falls within this list
            if (count + group.getPhrases().size() > position) {
                for (Phrase phrase : group.getPhrases()) {
                    if (count++ == position) {
                        return phrase;
                    }
                }
            } else {
                count += group.getPhrases().size();
            }
        }
        return null;
    }

    /**
     * Load the specified PhraseGroups into the adapter.
     * @param groups Lists of phrases to display.
     * @param highlightText Used for binding the viewHolders, for details
     *                      refer to {@link ItemViewHolder#bind(Object, String)}
     */
    public void setGroups(List<PhraseGroup> groups, @Nullable String highlightText) {
        mGroups.clear();
        mGroups.addAll(groups);
        mHighlightText = highlightText;
        notifyDataSetChanged();
    }

    public void clearItems() {
        mGroups.clear();
        notifyDataSetChanged();
    }

    public void setOnItemInListClickerListener(OnItemInListClickedListener listener) {
        mOnItemInListClickedListener = listener;
    }

    public interface OnItemInListClickedListener {
        void onRussianTextClick(Phrase selectedPhrase);     // Click on the Russian text (expanded or contracted).
        void onEditItemClick(Phrase selectedPhrase);        // Click on the edit icon (only visible when expanded)
    }

    @IntDef({AdapterMode.EXPANDING_CONTRACTING, AdapterMode.FIXED_EXPANDED})
    public @interface AdapterMode {
        int EXPANDING_CONTRACTING = 0;
        int FIXED_EXPANDED = 1;
    }

}
