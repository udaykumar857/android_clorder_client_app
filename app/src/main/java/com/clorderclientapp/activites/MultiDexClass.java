package com.clorderclientapp.activites;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import io.realm.Realm;

public class MultiDexClass extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        androidx.multidex.MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this); //initialize other plugins

    }
}
