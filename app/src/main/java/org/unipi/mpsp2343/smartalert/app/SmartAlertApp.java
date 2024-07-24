package org.unipi.mpsp2343.smartalert.app;

import android.app.Application;

public class SmartAlertApp extends Application {
    public AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }
}
