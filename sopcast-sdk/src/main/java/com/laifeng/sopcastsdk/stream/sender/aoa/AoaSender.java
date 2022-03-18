package com.laifeng.sopcastsdk.stream.sender.aoa;

import android.content.Context;

import com.laifeng.sopcastsdk.configuration.VideoConfiguration;
import com.laifeng.sopcastsdk.entity.Frame;
import com.laifeng.sopcastsdk.stream.packer.aoa.AoaPacker;
import com.laifeng.sopcastsdk.stream.sender.OnSenderListener;
import com.laifeng.sopcastsdk.stream.sender.Sender;
import com.laifeng.sopcastsdk.stream.sender.aoa.packets.ContentData;
import com.laifeng.sopcastsdk.stream.sender.aoa.packets.Video;
import com.laifeng.sopcastsdk.stream.sender.sendqueue.ISendQueue;
import com.laifeng.sopcastsdk.stream.sender.sendqueue.NormalSendQueue;

/**
 * Created by zhangmr on 2021/09/14.
 */
public class AoaSender implements Sender {

    private static final String TAG = "AoaSender";
    private ISendQueue mSendQueue = new NormalSendQueue();
    private OnSenderListener sendListener;
    private AoaConnection mAoaConnection;
    private Context mContext;

    public AoaSender(Context context){
        this.mContext = context;
        mAoaConnection = new AoaConnection();
    }
    @Override
    public void start() {
        mSendQueue.start();
    }

    @Override
    public void onData(byte[] data, int type) {
        Frame<ContentData> frame = null;
        Video video = new Video();
        video.setData(data);
        if (type == AoaPacker.FIRST_VIDEO) {
            frame = new Frame(video, type, Frame.FRAME_TYPE_CONFIGURATION);
        } else if (type == AoaPacker.KEY_FRAME) {
            frame = new Frame(video, type, Frame.FRAME_TYPE_KEY_FRAME);
        } else if (type == AoaPacker.INTER_FRAME) {
            frame = new Frame(video, type, Frame.FRAME_TYPE_INTER_FRAME);
        }
        if (frame == null) return;
        mSendQueue.putFrame(frame);

    }

    @Override
    public void stop() {
        mSendQueue.stop();
    }

    public void setVideoParams(VideoConfiguration videoConfiguration) {
        mAoaConnection.setVideoParams(videoConfiguration);
    }
    public void connect() {
        mAoaConnection.setSendQueue(mSendQueue);
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectNotInUi();
            }
        }).start();

    }

    private synchronized void connectNotInUi() {
        //mAoaConnection.setConnectListener(mAoaListener);
        mAoaConnection.connect(mContext);
    }

    /**
     * 解决首次黑屏增加
     * @param spsPps
     */
    public void setSpsPps(byte[] spsPps) {
        if (mAoaConnection != null) mAoaConnection.setSpsPps(spsPps);
    }
}
