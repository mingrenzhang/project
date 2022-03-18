package com.laifeng.sopcastsdk.stream.sender.aoa;

/**
 * Created by xu.wang
 * Date on  2017/11/23 19:23:36.
 *
 * @Desc 从tcp read thread中的回调
 */

public interface OnAoaReadListener {

    void aoaDisconnect();    //断开连接

    void connectSuccess();  //连接成功.
}
