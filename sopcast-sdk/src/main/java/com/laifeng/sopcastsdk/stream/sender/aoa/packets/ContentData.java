package com.laifeng.sopcastsdk.stream.sender.aoa.packets;

import com.laifeng.sopcastsdk.stream.sender.aoa.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zhangmr on 2021/09/14.
 * 基础数据类
 *
 */
public abstract class ContentData {

    protected byte[] data;

    public ContentData() {
        //super();
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
