package org.unipi.mpsp2343.smartalert.app;

import android.content.Context;

import org.chromium.net.CronetEngine;
import org.unipi.mpsp2343.smartalert.Authentication;
import org.unipi.mpsp2343.smartalert.DbProvider;
import org.unipi.mpsp2343.smartalert.LocalStorage;

import java.nio.FloatBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppContainer {
    public Authentication authentication;
    public DbProvider dbProvider;
    public CronetEngine cronetEngine;
    Executor executor;
    public LocalStorage localStorage;
    //Initializes various "service" objects that will be shared across many activities of the app.
    //Also performs dependency injection between these objects.
    public AppContainer(Context context) {
        cronetEngine = new CronetEngine.Builder(context).build();
        executor = Executors.newSingleThreadExecutor();
        localStorage = new LocalStorage(context);
        authentication = new Authentication(cronetEngine, executor, localStorage);
        dbProvider = new DbProvider(cronetEngine, executor, authentication);
    }
}
