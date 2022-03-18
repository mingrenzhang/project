package com.test.video_play.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by zhangmr on 2021/09/18.
 */
public class ApplicationUtil extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    public static Context getContext() {
        return context;
    }
}
