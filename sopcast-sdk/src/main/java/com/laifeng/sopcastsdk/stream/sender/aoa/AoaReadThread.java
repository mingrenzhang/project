package com.laifeng.sopcastsdk.stream.sender.aoa;

import android.util.Log;

import java.io.FileInputStream;

/**
 * Created by zhangmr on 2021/09/14.
 */
public class AoaReadThread extends Thread{

    private static final String TAG = "AoaReadThread";
    private boolean running;
    private FileInputStream fis;
    private OnAoaReadListener mListener;

    public AoaReadThread(FileInputStream inputStream,OnAoaReadListener listener){
        running = true;
        fis = inputStream;
        mListener = listener;
    }

    @Override
    public void run() {
        while (running) {
            byte[] msg = new byte[16*1024];
            try {
                //Handle incoming messages
                int len = fis.read(msg); //等待消息时会阻塞在这里
                while (fis != null && len > 0 && running) {
                    //receive(msg, len);
                    Thread.sleep(10);
                    len = fis.read(msg);
                }
            } catch (final Exception e) {
                Log.e(TAG,"USB Receive Failed " + e.toString() + "\n");
                if (mListener != null)  mListener.aoaDisconnect();
            }
        }
    }

    public void shutDown(){
        running = false;
        this.interrupt();
    }
}
