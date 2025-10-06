package com.wornux.chatzam;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ChatZamApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
}