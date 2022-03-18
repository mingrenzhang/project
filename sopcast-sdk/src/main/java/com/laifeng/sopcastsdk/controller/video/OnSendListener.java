package com.laifeng.sopcastsdk.controller.video;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * Created by zhangmr on 2021/09/17.
 */
public interface OnSendListener {
    public void sendData(ByteBuffer bb, MediaCodec.BufferInfo bi);
}
