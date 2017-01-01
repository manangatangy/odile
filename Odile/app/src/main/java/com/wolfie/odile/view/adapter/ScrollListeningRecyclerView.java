package com.wolfie.odile.view.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class ScrollListeningRecyclerView extends RecyclerView {

    private ItemScrollListener mItemScrollListener;

    public ScrollListeningRecyclerView(Context context) {
        super(context);
    }

    public ScrollListeningRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollListeningRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setItemScrollListener(ItemScrollListener itemScrollListener) {
        mItemScrollListener = itemScrollListener;
        setupScrollListener();
    }

    public ItemScrollListener removeItemScrollListener() {
        ItemScrollListener temp = mItemScrollListener;
        mItemScrollListener = null;
        this.removeOnScrollListener(mScrollListener);
        return temp;
    }

    private OnScrollListener mScrollListener;

    private void setupScrollListener() {
        mScrollListener = new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy != 0) {
                    handleItemScrollEvent(recyclerView);
                }
            }
        };
        this.addOnScrollListener(mScrollListener);
    }

    private void handleItemScrollEvent(RecyclerView recyclerView) {
        if (mItemScrollListener != null) {
            View view = recyclerView.findChildViewUnder(0, 0);
            int position = recyclerView.getChildAdapterPosition(view);
            mItemScrollListener.onItemAlignedToTop(position);
        }
    }

    public interface ItemScrollListener {
        // Will be fired when a list item aligns to the top edge of the recycler view.
        void onItemAlignedToTop(int position);
    }

}
