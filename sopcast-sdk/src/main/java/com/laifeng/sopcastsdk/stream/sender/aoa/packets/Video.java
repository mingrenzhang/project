package com.laifeng.sopcastsdk.stream.sender.aoa.packets;

/**
 * Created by zhangmr on 2021/09/14.
 * 视频数据封装类
 *
 */
public class Video extends ContentData {

    public Video() {
        //super(new ChunkHeader(ChunkType.TYPE_0_FULL, SessionInfo.VIDEO_CHANNEL, MessageType.VIDEO));
    }

    @Override
    public String toString() {
        return "AOA Video";
    }
}
