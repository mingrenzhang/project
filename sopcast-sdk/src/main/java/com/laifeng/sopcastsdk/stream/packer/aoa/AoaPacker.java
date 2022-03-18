package com.laifeng.sopcastsdk.stream.packer.aoa;

import android.media.MediaCodec;
import android.util.Log;
import android.widget.TabHost;

import com.laifeng.sopcastsdk.stream.packer.AnnexbHelper;
import com.laifeng.sopcastsdk.stream.packer.Packer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by zhangmr on 2021/09/13.
 *
 * @Desc add
 */

public class AoaPacker implements Packer, AnnexbHelper.AnnexbNaluListener {
    private static final String TAG = "AoaPacker";
    public static final int HEADER = 0;
    public static final int METADATA = 1;
    public static final int FIRST_VIDEO = 2;
    public static final int AUDIO = 4;
    public static final int KEY_FRAME = 5;
    public static final int INTER_FRAME = 6;

    private OnPacketListener packetListener;
    private boolean isHeaderWrite;
    private boolean isKeyFrameWrite;

    private int mAudioSampleRate, mAudioSampleSize;
    private boolean mIsStereo;
    private boolean mSendAudio = false;

    private AnnexbHelper mAnnexbHelper;


    public AoaPacker() {
        mAnnexbHelper = new AnnexbHelper();
    }

    public void setPacketListener(OnPacketListener listener) {
        packetListener = listener;
    }


    @Override
    public void start() {
        mAnnexbHelper.setAnnexbNaluListener(this);
    }

    @Override
    public void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        mAnnexbHelper.analyseVideoDataonlyH264(bb, bi);
    }

    @Override
    public void stop() {
        isHeaderWrite = false;
        isKeyFrameWrite = false;
        mAnnexbHelper.stop();
    }

    private byte[] mSpsPps;
    private byte[] header = {0x00, 0x00, 0x00, 0x01};   //H264的头文件

    @Override
    public void onSpsPps(byte[] sps, byte[] pps) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(sps.length + 4);
        byteBuffer.put(header);
        byteBuffer.put(sps);
        mSpsPps = byteBuffer.array();

        packetListener.onPacket(mSpsPps, FIRST_VIDEO);
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(pps.length + 4);
        byteBuffer1.put(header);
        byteBuffer1.put(pps);
        packetListener.onPacket(byteBuffer1.array(), FIRST_VIDEO);
        isHeaderWrite = true;
    }


    @Override
    public void onVideo(byte[] video, boolean isKeyFrame) {
        if (packetListener == null || !isHeaderWrite) {
            return;
        }
        int packetType = INTER_FRAME;
        if (isKeyFrame) {
            isKeyFrameWrite = true;
            packetType = KEY_FRAME;
        }
        //确保第一帧是关键帧，避免一开始出现灰色模糊界面
        if (!isKeyFrameWrite) {
            return;
        }
        ByteBuffer bb;
        if (isKeyFrame) {
            bb = ByteBuffer.allocate(video.length);
            bb.put(video);
        } else {
            bb = ByteBuffer.allocate(video.length);
            bb.put(video);
        }
        packetListener.onPacket(bb.array(), packetType);
    }
}
