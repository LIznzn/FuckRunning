package com.acgmiao.dev.fuckrunning;

import android.app.Application;


public class MyLocationApplication extends Application {

    private static MyLocationApplication mApplication;

    @Override
    public void onCreate() {
        mApplication = this;
        super.onCreate();
    }

    public static MyLocationApplication getApplication() {
        return mApplication;
    }


}
