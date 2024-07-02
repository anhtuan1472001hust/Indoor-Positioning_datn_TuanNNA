package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning;

import android.app.Application;

import io.realm.Realm;

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
