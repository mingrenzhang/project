package com.test.video_play.server.aoa;

import android.os.SystemClock;
import android.util.Log;

import com.test.video_play.control.VideoPlayController;
import com.test.video_play.entity.Frame;
import com.test.video_play.utils.ByteUtil;
import com.test.video_play.utils.DecodeUtils;

import java.util.Arrays;

/**
 * Created by zhangmr on 2021/09/18.
 */
public class AoaReaderThread extends Thread{

    private static final String TAG = "AoaReaderThread";
    private boolean running;
    private static final int AOA_MAX_BUFFER_SIZE = 16*1024;
    private static final int AOA_HEAD_BUFFER_SIZE = 4;
    private byte[] msg = new byte[AOA_MAX_BUFFER_SIZE];
    private byte[] msgHead = new byte[AOA_HEAD_BUFFER_SIZE];

    private DecodeUtils mDecodeUtils;
    /**
     * VideoPlayController类用于将数据放入队列
     */
    private VideoPlayController mController;


    public AoaReaderThread(DecodeUtils decodeUtils,VideoPlayController controller){

        //decodeUtils用于区分sps,pps,idr,及p帧
        this.mDecodeUtils = decodeUtils;
        this.mController = controller;
        decodeUtils.setOnVideoListener(new DecodeUtils.OnVideoListener() {
            @Override
            public void onSpsPps(byte[] sps, byte[] pps) {
                Frame spsPpsFrame = new Frame();
                spsPpsFrame.setType(Frame.SPSPPS);
                spsPpsFrame.setSps(sps);
                spsPpsFrame.setPps(pps);
                Log.d("AcceptH264MsgThread", "sps pps ...");
                //放入队列
                mController.setPlayQueue(spsPpsFrame);
            }

            @Override
            public void onVideo(byte[] video, int type) {
                Frame frame = new Frame();
                //将读取的视频流封装在frame中，放入队列
                switch (type) {
                    case Frame.KEY_FRAME:
                        frame.setType(Frame.KEY_FRAME);
                        frame.setBytes(video);
                        //放入队列
                        mController.setPlayQueue(frame);
                        Log.d("AcceptH264MsgThread", "key frame ...");

                        break;
                    case Frame.NORMAL_FRAME:
                        frame.setType(Frame.NORMAL_FRAME);
                        frame.setBytes(video);
                        Log.d("AcceptH264MsgThread", "normal frame ...");
                        //放入队列
                        mController.setPlayQueue(frame);
                        break;
                    case Frame.AUDIO_FRAME:
                        frame.setType(Frame.AUDIO_FRAME);
                        frame.setBytes(video);
                        Log.d("AcceptH264MsgThread", "audio frame ...");
                        //放入队列
                        mController.setPlayQueue(frame);
                        break;
                    default:
                        Log.e("AcceptH264MsgThread", "other video...");
                        break;
                }

            }
        });
    }
    @Override
    public void run() {
        running = true;

        while (running) {
            try {
                //先接收header
                int ret = AOAHostSetup.getInstance().bulkTransferIn(msgHead, AOA_HEAD_BUFFER_SIZE);
                if (ret < 0) {
                    Log.e(TAG, "bulkTransferIn fail 1");
                    break;
                } else if (ret == 0) {
                    continue;
                }
                int lenMsg = ByteUtil.bytesToInt(new byte[] { msgHead[0], msgHead[1], msgHead[2], msgHead[3] });
                //Log.e(TAG,"receive header:"+ lenMsg);
                //一帧数据长度大于AOA传输最大值,扩容接收数据数组
                if (lenMsg > AOA_MAX_BUFFER_SIZE) {
                    msg = new byte[lenMsg];
                }else{
                    msg = new byte[AOA_MAX_BUFFER_SIZE];
                }
                int len = AOAHostSetup.getInstance().bulkTransferIn(msg,msg.length);
                if (len>0 && running){
                    byte[] actual = new byte[len];
                    System.arraycopy(msg,0,actual,0,len);
                    //Log.e(TAG,"receive data:"+ Arrays.toString(actual));
                    mDecodeUtils.isCategory(actual);
                    SystemClock.sleep(10);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                Log.e(TAG,"USB Receive Failed " + e.toString() + "\n");
                //AOAHostSetup.getInstance().uninitUsbDevice();
            }
        }

    }


    public void shutDown(){
        running = false;
        this.interrupt();
    }

}
