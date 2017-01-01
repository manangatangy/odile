package com.wolfie.odile.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wolfie.odile.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavMenuRecyclerAdapter extends RecyclerView.Adapter<NavMenuRecyclerAdapter.MenuItemViewHolder> {

    private static final String ALL_GROUPS = "All groups";

    private Context mContext;
    private List<Item> mItemList = new ArrayList<>();
    private Item mSelectedItem = null;
    private MenuItemViewHolder mSelectedViewHolder = null;
    private OnNavMenuItemClickListener mNavMenuItemClickListener;

    private class Item {
        public Item(String text, boolean isSelected) {
            mText = text;
            mIsSelected = isSelected;
        }
        public String mText;
        public boolean mIsSelected;
    }

    public NavMenuRecyclerAdapter(Context context) {
        mContext = context;
    }

    /**
     * Build the nav menu items, placing the ALL_GROUPS at the top of the menu
     * then adding in the specified groupList.  If the groupList is null, then
     * the menu will consist of just ALL_GROUPS.
     * ALL_GROUPS is initially selected.
     * @param groupList
     */
    public void setMenuItems(List<String> groupList) {
        mItemList.clear();
        mItemList.add(new Item(ALL_GROUPS, false));
        if (groupList != null) {
            for (String group : groupList) {
                mItemList.add(new Item(group, false));
            }
        }
        notifyDataSetChanged();
//        notifyListener(ALL_GROUPS, true);
    }

    public int getAdapterPositionForItem(String group) {
        for (int position = 0; position < mItemList.size(); position++) {
            Item item = mItemList.get(position);
            if (item.mText.equals(group)) {
                return position;
            }
        }
        return -1;      // Not found.
    }

    @Override
    public MenuItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_menu_item, null);
        MenuItemViewHolder viewHolder = new MenuItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MenuItemViewHolder menuItemViewHolder, int i) {
        Item item = mItemList.get(i);
        menuItemViewHolder.bind(item);
    }

    @Override
    public int getItemCount() {
        return (mItemList == null) ? 0 : mItemList.size();
    }


    public class MenuItemViewHolder extends RecyclerView.ViewHolder {
        private View mItemView;
        private Item mItem;

        @BindView(R.id.navigation_item_text)
        TextView mTextView;

        @BindView(R.id.navigation_item_top_separator)
        View mTopSeparator;

        @BindView(R.id.navigation_item_chevron)
        ImageView mChevron;

        public MenuItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mItemView = view;
        }

        public void bind(Item item) {
            mItem = item;
            mTextView.setText(mItem.mText);
            setSelectedChevronVisibility(mItem.mIsSelected);
            mTopSeparator.setVisibility(ALL_GROUPS.equals(mItem.mText) ? View.INVISIBLE : View.VISIBLE);
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSelected(true);
                }
            });
        }

        /**
         * This viewHolder is now the selected one.
         * The previously selected viewHolder and item is de-selected.
         * Use the reference to the item to indicate which is selected.
         */
        public void setSelected(boolean notify) {
            boolean hasChanged = (mSelectedItem != this.mItem);

            if (mSelectedViewHolder != null) {
                mSelectedViewHolder.setSelectedChevronVisibility(false);
            }
            if (mSelectedItem != null) {
                mSelectedItem.mIsSelected = false;
            }
            mSelectedViewHolder = this;
            mSelectedItem = this.mItem;
            mSelectedViewHolder.setSelectedChevronVisibility(true);
            mSelectedItem.mIsSelected = true;

            if (notify) {
                notifyListener(mItem.mText, hasChanged);
            }
        }

        public void setSelectedChevronVisibility(boolean selected) {
            mChevron.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
        public boolean isSelected() {
            return (mChevron.getVisibility() == View.VISIBLE);
        }

        public Item getItem() {
            return mItem;
        }
    }

    private void notifyListener(String groupName, boolean hasChanged) {
        if (mNavMenuItemClickListener != null) {
            if (ALL_GROUPS.equals(groupName)) {
                groupName = null;
            }
            mNavMenuItemClickListener.onNavMenuItemClick(groupName, hasChanged);
        }
    }

    public void setNavMenuItemClickListener(OnNavMenuItemClickListener listener) {
        mNavMenuItemClickListener = listener;
    }

    public interface OnNavMenuItemClickListener {
        void onNavMenuItemClick(String groupName, boolean hasChanged);
    }

}


