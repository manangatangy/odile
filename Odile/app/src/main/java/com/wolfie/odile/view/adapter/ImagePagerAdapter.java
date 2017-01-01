package com.wolfie.odile.view.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wolfie.odile.R;
import com.wolfie.odile.model.ImageEnum;
import com.wolfie.odile.util.BitmapWorkerTask;

/**
 * Created by david on 29/10/16.
 * Ref: https://www.bignerdranch.com/blog/viewpager-without-fragments/
 */

public class ImagePagerAdapter extends PagerAdapter {

    private Context mContext;

    public ImagePagerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        final ImageEnum customPagerEnum = ImageEnum.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.view_image_page, collection, false);
        TextView textView = (TextView)layout.findViewById(R.id.page_text);
        textView.setText(customPagerEnum.getTitleResId());

        final ImageView imageView = (ImageView)layout.findViewById(R.id.page_image);
        // Delay image processing until layout done, so that width and height and determined.
        imageView.post(new Runnable() {
            @Override
            public void run() {
                int x = imageView.getWidth();
                int y = imageView.getHeight();
                Log.d("eskey", " post x = " + x + ", y = " + y);
                BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(imageView,
                        mContext.getResources(), customPagerEnum.getImageResId(), x, y);
            }
        });
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return ImageEnum.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        ImageEnum customPagerEnum = ImageEnum.values()[position];
        return mContext.getString(customPagerEnum.getTitleResId());
    }
}
