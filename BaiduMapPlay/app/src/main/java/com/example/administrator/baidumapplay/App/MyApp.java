package com.example.administrator.baidumapplay.App;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Administrator on 2017/11/7.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
    }
}
