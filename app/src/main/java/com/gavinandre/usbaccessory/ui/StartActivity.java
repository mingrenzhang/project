package com.gavinandre.usbaccessory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gavinandre.usbaccessory.R;
import com.laifeng.sopcastsdk.configuration.VideoConfiguration;
import com.laifeng.sopcastsdk.stream.packer.aoa.AoaPacker;
import com.laifeng.sopcastsdk.stream.sender.OnSenderListener;
import com.laifeng.sopcastsdk.stream.sender.aoa.AoaSender;


public class StartActivity extends com.laifeng.sopcastsdk.screen.ScreenRecordActivity implements OnSenderListener{
    private AppCompatButton btn_start;
    private VideoConfiguration mVideoConfiguration;
    private AoaSender aoaSender;
    private AoaPacker packer;
    private final static String TAG = "StartActivity";
    private boolean isRecord = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialView();
    }

    private void initialView() {
        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecord) {
                    requestRecording();
                    btn_start.setText("开始录屏");
                } else {
                    stopRecording();
                    btn_start.setText("停止录屏");
                }
            }
        });

        Button btn = findViewById(R.id.btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startScreenRecording();
                Intent intent = new Intent(StartActivity.this,OneActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void requestRecordSuccess(int requestCode, int resultCode,Intent data) {
        super.requestRecordSuccess(requestCode,resultCode,data);
        isRecord = true;
        initConnect();
        startRecord();
    }

    @Override
    protected void requestRecordFail() {
        super.requestRecordFail();
    }

    /**
     * 打开AOA通道，初始化aoaPacker，aoaSender
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
        Toast.makeText(StartActivity.this,"连接成功",Toast.LENGTH_SHORT);
        Log.i(TAG,"连接成功");
    }
    private void startRecord() {
        startRecording();
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

}
