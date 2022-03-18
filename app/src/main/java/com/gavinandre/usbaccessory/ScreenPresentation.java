package com.gavinandre.usbaccessory;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.TextView;


public class ScreenPresentation extends Presentation {
    private Context context;

    private TextView aaa;
    int i = 0;

    public ScreenPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        this.context = outerContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        aaa = findViewById(R.id.tv_aaa);
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            i++;
            aaa.setText(Integer.toString(i));
            Log.e("lhf", "handleMessage  " + i);
            sendEmptyMessageDelayed(0, 30);
        }
    };
}