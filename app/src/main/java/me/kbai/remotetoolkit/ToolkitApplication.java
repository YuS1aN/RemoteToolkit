package me.kbai.remotetoolkit;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ToolkitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Constants.filesDir = getFilesDir();
    }
}
