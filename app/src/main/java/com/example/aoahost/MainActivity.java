package com.example.aoahost;

/**
 * Created by zhangmr on 2021/09/02.
 */

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.test.video_play.control.VideoPlayController;
import com.test.video_play.server.aoa.AoaServer;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int width = 720;
    private static final int height = 1280;

    private SurfaceView mSurface = null;
    private SurfaceHolder mSurfaceHolder;

    private VideoPlayController mController;
    private TextView tv_speednet;
    private RelativeLayout rl_detail;

    private AoaServer aoaServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSurface = findViewById(R.id.surfaceview);
        tv_speednet = findViewById(R.id.tv_main_speednet);
        rl_detail = findViewById(R.id.rl_content_detail);
        mSurfaceHolder = mSurface.getHolder();
        mController = new VideoPlayController();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mController.surfaceCreate(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.e(TAG, "surface destory");
                mController.surfaceDestrory();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initServer(){
        aoaServer = new AoaServer(mController);
        if (aoaServer.connect()){
            rl_detail.setVisibility(View.GONE);
            aoaServer.start();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        initServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (aoaServer != null){
            aoaServer.disConnect();
        }
    }
}