package com.wolfie.odile.view.adapter.viewholder;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;

import static android.text.TextUtils.isEmpty;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    protected ViewGroup mItemView;

    public BaseViewHolder(View itemView) {
        super(itemView);
        mItemView = (ViewGroup)itemView;
    }

    public abstract void bind(Object item, @Nullable String highlightText);

    public static Spannable highlight(@NonNull String targetText, String highlightText) {
        Spannable targetSpannable = Spannable.Factory.getInstance().newSpannable(targetText);
        if (!isEmpty(highlightText) && targetText.toLowerCase().contains(highlightText.toLowerCase())) {
            int start = targetSpannable.toString().toLowerCase().indexOf(highlightText.toLowerCase());
            int end = start + highlightText.length();
            targetSpannable.setSpan(new BackgroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            targetSpannable.setSpan(new ForegroundColorSpan(Color.WHITE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return targetSpannable;
    }

}
