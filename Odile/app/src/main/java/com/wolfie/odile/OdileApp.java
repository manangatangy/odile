package com.wolfie.odile;

import android.app.Application;

public class OdileApp extends Application {

    public static final String TAG = OdileApp.class.getSimpleName();

//    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

//        initAppComponent();
    }

//    private void initAppComponent() {
//        appComponent = DaggerFactory.getAppComponent(this);
//    }

//    public AppComponent getAppComponent() {
//        return appComponent;
//    }
}
