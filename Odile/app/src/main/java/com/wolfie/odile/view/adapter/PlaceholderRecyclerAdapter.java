package com.wolfie.odile.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Observes the adapter dataset, and when on changed, if there are no items sets the the
 * placeHolderView to visible, otherwise sets it gone.
 */
public abstract class PlaceholderRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private View mPlaceholderView;

    public PlaceholderRecyclerAdapter() {
        super();
        this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIfAdapterIsEmpty();
            }
        });
    }

    private void checkIfAdapterIsEmpty() {
        setPlaceholderVisibility(getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    public void setPlaceholderVisibility(int visibility) {
        if (mPlaceholderView != null) {
            mPlaceholderView.setVisibility(visibility);
        }
    }

    public void setPlaceholderView(View placeholderView) {
        mPlaceholderView = placeholderView;
    }
}
