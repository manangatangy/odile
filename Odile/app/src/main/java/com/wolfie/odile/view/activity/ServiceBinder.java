/**
 * (C) 2017. National Australia Bank [All rights reserved]. This product and related documentation are protected by
 * copyright restricting its use, copying, distribution, and decompilation. No part of this product or related
 * documentation may be reproduced in any form by any means without prior written authorization of National Australia
 * Bank. Unless otherwise arranged, third parties may not have access to this product or related documents.
 */

package com.wolfie.odile.view.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.wolfie.odile.talker.TalkService;

/**
 * Binds a service and notifies a listener on bind/unbind.
 * Protocol is for user to first set a {@link ServiceBinderListener} and then to invoke
 * {@link #bindService(OdileActivity, Context)} or {@link #unbindService(OdileActivity)}.
 * Note that once bindService() is called, then callbacks to {@link ServiceConnection}
 * may occur repeatedly (which will also notify the ServiceBinderListener). Only after
 * unbindService() is called, will the callbacks cease.
 * Ref http://developer.android.com/reference/android/app/Service.html#LocalServiceSample
 */
public class ServiceBinder implements ServiceConnection {

    private boolean mServiceIsBound = false;
    private TalkService mBoundTalkService;
    private ServiceBinderListener mServiceBinderListener;

    public void setServiceBinderListener(ServiceBinderListener serviceBinderListener) {
        mServiceBinderListener = serviceBinderListener;
    }

    public void bindService(OdileActivity odileActivity, Context packageContext) {
        odileActivity.bindService(new Intent(packageContext, TalkService.class), this, Context.BIND_AUTO_CREATE);
        mServiceIsBound = true;
    }

    public void unbindService(OdileActivity odileActivity) {
        if (mServiceIsBound) {
            odileActivity.unbindService(this);       // Does not cause a callback.
            doUnbind();
            mServiceIsBound = false;
        }
    }

    private void doUnbind() {
        // Release reference to service and notify listener.
        // It seems that the mBoundTalkService is sometimes null
        if (mServiceBinderListener != null && mBoundTalkService != null) {
            mServiceBinderListener.onServiceUnBound(mBoundTalkService);
        }
        mBoundTalkService = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Called back from Activity.bindService().
        mBoundTalkService = ((TalkService.LocalBinder)service).getService();
        if (mServiceBinderListener != null && mBoundTalkService != null) {
            mServiceBinderListener.onServiceBound(mBoundTalkService);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // May be called if service is killed, independent of call to unbindService().
        doUnbind();
    }

    public interface ServiceBinderListener {
        void onServiceBound(TalkService mBoundTalkService);
        void onServiceUnBound(TalkService mBoundTalkService);
    }

}
