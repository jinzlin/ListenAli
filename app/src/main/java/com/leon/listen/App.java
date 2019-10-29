package com.leon.listen;

import android.app.Application;

/**
 * Author by ljz
 * PS:
 */
public class App extends Application {

    public static final String TAG = "ListenAli";

    @Override
    public void onCreate() {
        super.onCreate();
        SPUtils.init(this, "listenali");
    }
}
