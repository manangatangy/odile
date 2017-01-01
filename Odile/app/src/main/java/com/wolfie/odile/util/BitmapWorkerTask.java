package com.wolfie.odile.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by david on 14/09/16.
 */

/**
 * https://developer.android.com/training/displaying-bitmaps/process-bitmap.html
 */
public class BitmapWorkerTask extends AsyncTask<BitmapWorkerTask.Parms, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;

    public BitmapWorkerTask(ImageView imageView, Resources resources, int resourceId,
                            int requiredWidth, int requiredHeight) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        Parms mParms = new Parms();
        mParms.resources = resources;
        mParms.resourceId = resourceId;
        mParms.requiredWidth = requiredWidth;
        mParms.requiredHeight = requiredHeight;
        execute(mParms);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Parms... params) {
        Parms parms = params[0];
        return decodeSampledBitmapFromResource(
                parms.resources, parms.resourceId, parms.requiredWidth, parms.requiredHeight);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public class Parms {
        Resources resources;
        int resourceId;
        int requiredWidth;
        int requiredHeight;
    }
    /**
     * https://developer.android.com/training/displaying-bitmaps/load-bitmap.html#decodeSampledBitmapFromResource
     *
     * eg;
     * mImageView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.id.myimage, 100, 100));
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}