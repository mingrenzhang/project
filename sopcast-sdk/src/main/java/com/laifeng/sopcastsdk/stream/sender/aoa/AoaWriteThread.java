package com.laifeng.sopcastsdk.stream.sender.aoa;

import android.text.InputType;
import android.util.Log;

import com.laifeng.sopcastsdk.entity.Frame;
import com.laifeng.sopcastsdk.stream.sender.aoa.packets.Video;
import com.laifeng.sopcastsdk.stream.sender.sendqueue.ISendQueue;
import com.laifeng.sopcastsdk.utils.ByteUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by zhangmr on 2021/09/14.
 */
public class AoaWriteThread extends Thread{

    private static final String TAG = "AoaWriteThread";
    private boolean running;
    private FileOutputStream fos;
    private ISendQueue iSendQueue;
    private OnAoaWriteListener mListener;

    private static final int AOA_MAX_BUFFER_BYTES = 16 * 1024;
    private byte[] mDataBuffer = new byte[AOA_MAX_BUFFER_BYTES];


    public AoaWriteThread(FileOutputStream outStream, ISendQueue sendQueue,OnAoaWriteListener listener){
        running = true;
        fos = outStream;
        iSendQueue = sendQueue;
        mListener = listener;

    }
    @Override
    public void run() {
        while (running) {
            Frame frame = iSendQueue.takeFrame();
            if (frame == null) {
                continue;
            }
            if (frame.data instanceof Video) {
                //body存储一帧视频流
                byte[] body = ((Video) frame.data).getData();
                //header存放一帧视频流长度
                int bodyLength = body.length;
                byte[] header = ByteUtil.int2Bytes(bodyLength);
                //header和视频流分开发送
                sendData(header);
                sendData(body);
            }
        }
    }

    /**
     * 终止线程
     */
    public void shutDown() {
        running = false;
        this.interrupt();
    }

    /**
     * AOA协议发送数据
     * @param data
     */
    public void sendData(byte[] data){
        int len = data.length;
        int ret = -1;
        int cnt = len;
        int readLen = -1;
        int dataLen = 0;
        try {
            if (fos == null) {
                Log.e(TAG, "mUsbDeviceConnection or mUsbEndpointIn is null");
                throw new IOException();
            }
            if (len <= AOA_MAX_BUFFER_BYTES) {
                //Log.e(TAG,"aoa send data"+Arrays.toString(data));
                fos.write(data, 0, len);
                fos.flush();
                dataLen = len;
            } else {
                //Log.e(TAG,"aoa send long data:"+data.length);
                //Log.e(TAG,"aoa send long data"+Arrays.toString(data));
                while (cnt > 0) {
                    readLen = cnt > AOA_MAX_BUFFER_BYTES ? AOA_MAX_BUFFER_BYTES : cnt;
                    System.arraycopy(data, dataLen, mDataBuffer, 0, readLen);
                    fos.write(mDataBuffer, 0, readLen);
                    fos.flush();
                    ret = readLen;
                    cnt -= ret;
                    dataLen += ret;
                }
            }

            if (dataLen != len) {
                Log.e(TAG, "bulkTransferOut error 3: dataLen = " + dataLen + ", len = " + len);
                ret = -1;
                throw new IOException();
            }
            //return dataLen;
        } catch (Exception e) {
            Log.e(TAG, "bulkTransferOut catch exception " + e.getMessage());
            e.printStackTrace();
            //return -1;
        }
    }

}
