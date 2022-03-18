package com.laifeng.sopcastsdk.stream.sender.aoa;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.laifeng.sopcastsdk.configuration.VideoConfiguration;
import com.laifeng.sopcastsdk.stream.sender.sendqueue.ISendQueue;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhangmr on 2021/09/14.
 */
public class AoaConnection implements OnAoaReadListener,OnAoaWriteListener{

    private static final String TAG = "AoaConnection";
    private UsbManager usbManager;
    private Context mContext;
    private ISendQueue mSendQueue;
    /**
     * AOA连接相关
     */
    private ParcelFileDescriptor fileDescriptor;
    private FileInputStream inStream;
    private FileOutputStream outStream;
    /**
     * 读写线程
     */
    private AoaReadThread mReader;
    private AoaWriteThread mWriter;
    /**
     * 视频流相关
     */
    private int width, height;
    private int maxBps;
    private int fps;
    private byte[] mSpsPps;

    private String REQUEST = "request";

    private AoaConnection.State state = AoaConnection.State.INIT;

    public enum State {
        INIT,
        CONNECTING,
        LIVING
    }

    public void setSendQueue(ISendQueue sendQueue) {
        mSendQueue = sendQueue;
    }

    /**
     * AOA连接
     * @param context
     */
    public void connect(Context context){
        mContext = context;
        usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        final UsbAccessory[] accessoryList = usbManager.getAccessoryList();

        if (accessoryList == null || accessoryList.length == 0) {
            Log.e(TAG,"no accessory found");
        } else {
            openAccessory(accessoryList[0]);
        }
    }

    /**
     * 打开配件模式
     * @param accessory
     */
    private void openAccessory(UsbAccessory accessory) {
        fileDescriptor = usbManager.openAccessory(accessory);
        if (fileDescriptor != null) {
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            inStream = new FileInputStream(fd);
            outStream = new FileOutputStream(fd);
            mWriter = new AoaWriteThread(outStream,mSendQueue,this);
            mWriter.start();
            mReader = new AoaReadThread(inStream,this);
            mReader.start();
            state = AoaConnection.State.LIVING;
            //mWriter.sendData(REQUEST.getBytes());//先发一个数据..用于让播放端相应,知道这是一个投屏请求
        } else {
            Log.e(TAG,"could not connect");
        }
    }
    public void setVideoParams(VideoConfiguration videoConfiguration) {
        width = videoConfiguration.width;
        height = videoConfiguration.height;
        fps = videoConfiguration.fps;
        maxBps = videoConfiguration.maxBps;
    }

    public void setSpsPps(byte[] spsPps) {
        this.mSpsPps = spsPps;
    }
    @Override
    public void aoaDisconnect() {

    }

    @Override
    public void connectSuccess() {
        Log.e(TAG, "connect success");
        //mWriter.start();
    }

    /**
     * 停止读写线程
     */
    public void stop() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (mWriter != null) mWriter.shutDown();
                if (mReader != null) mReader.shutDown();
                try {
                    if (outStream != null) outStream.close();
                    if (inStream != null) inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                closeAccessory();
            }
        }.start();
        state = AoaConnection.State.INIT;
    }

    public void closeAccessory() {
        try {
            if (fileDescriptor != null) {
                fileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            fileDescriptor = null;
        }
    }
    public AoaConnection.State getState() {
        return state;
    }
}
