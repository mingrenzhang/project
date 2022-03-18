package com.gavinandre.usbaccessory.ui;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.AppCompatButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.gavinandre.usbaccessory.R;
import com.gavinandre.usbaccessory.ScreenPresentation;
import com.laifeng.sopcastsdk.configuration.VideoConfiguration;
import com.laifeng.sopcastsdk.controller.video.OnSendListener;
import com.laifeng.sopcastsdk.controller.video.ScreenRecordService2;
import com.laifeng.sopcastsdk.stream.packer.aoa.AoaPacker;
import com.laifeng.sopcastsdk.stream.sender.OnSenderListener;
import com.laifeng.sopcastsdk.stream.sender.aoa.AoaSender;

import java.nio.ByteBuffer;

public class OneActivity extends com.laifeng.sopcastsdk.screen.ScreenRecordActivity implements OnSenderListener, OnSendListener {
    private AppCompatButton btn_start;
    private VideoConfiguration mVideoConfiguration;
    private AoaSender aoaSender;
    private AoaPacker packer;
    private final static String TAG = "OneActivity";
    private boolean isRecord = false;

    private DisplayManager displayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);
        initialView();
    }

    private void initialView() {
        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecord) {
                    requestRecording();
                    //btn_start.setText("start record");
                } else {
                    //stopRecording();
                    //btn_start.setText("stop record");
                }
            }
        });


        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                Log.e("lhf", "onDisplayAdded");
                Display display = displayManager.getDisplay(displayId);
                if (display != null) {
                    showSecond(display);
                }
            }

            @Override
            public void onDisplayRemoved(int displayId) {
                Log.e("lhf", "onDisplayRemoved");
            }

            @Override
            public void onDisplayChanged(int displayId) {
                Log.e("lhf", "onDisplayChanged");
            }
        }, null);
    }


    @Override
    protected void requestRecordSuccess(int requestCode, int resultCode,Intent data) {
        super.requestRecordSuccess(requestCode,resultCode,data);
        isRecord = true;
        initConnect();
        //connect(this);
        //startRecord();
        packer.start();
        aoaSender.start();
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                getScreenBaseInfo();
                MediaProjectionManager mMediaProjectionManage = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                MediaProjection mMediaProjection = mMediaProjectionManage.getMediaProjection(resultCode, data);

                screenRecordService2 = new ScreenRecordService2(mWidth, mHeight, 800000, mScreenDensity,
                        mMediaProjection, Environment.getExternalStorageDirectory() + "/" + "sssss.mp4",null,this);
                screenRecordService2.start();
            } else {
                Toast.makeText(this, "取消录屏", Toast.LENGTH_LONG).show();
                Log.i(TAG, "User cancelled");
            }
        }
    }

    @Override
    protected void requestRecordFail() {
        super.requestRecordFail();
    }

    /**
     * 打开AOA通道，初始化aoaPacker，aoaSender，点击事件触发初始化有问题？？？？？？
     */
    private void initConnect(){
        packer = new AoaPacker();
        mVideoConfiguration = new VideoConfiguration.Builder().build();
        setVideoConfiguration(mVideoConfiguration);
        setRecordPacker(packer);

        aoaSender = new AoaSender(this);
        //aoaSender.setSenderListener(this);
        aoaSender.setVideoParams(mVideoConfiguration);
        aoaSender.connect();
        setRecordSender(aoaSender);
        Toast.makeText(OneActivity.this,"连接成功",Toast.LENGTH_SHORT);
        Log.i(TAG,"连接成功");
    }

    @Override
    public void onConnecting() {
        Log.e(TAG, "onConnecting ...");
    }

    @Override
    public void onConnected() {
        Log.e(TAG, "onConnected");
    }

    @Override
    public void onDisConnected() {
        Log.e(TAG, "onDisConnected");
    }

    @Override
    public void onPublishFail() {
        Log.e(TAG, "onPublishFail");
    }

    @Override
    public void onNetGood() {

    }

    @Override
    public void onNetBad() {

    }


    @Override
    protected void onDestroy() {
        aoaSender.stop();
        super.onDestroy();
    }


    private int mWidth = 720;
    private int mHeight = 1080;

    private ScreenPresentation screenPresentation;
    private ScreenRecordService2 screenRecordService2;
    private void showSecond(Display display) {
        screenPresentation = new ScreenPresentation(this, display);
        screenPresentation.show();
    }
    private int mScreenDensity;
    /**
     * 获取屏幕相关数据
     */
    private void getScreenBaseInfo() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
    }


    @Override
    public void sendData(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        packer.onVideoData(bb,bi);
    }


}
