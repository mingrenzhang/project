package com.test.video_play.server.aoa;

import android.content.Context;

import com.test.video_play.control.VideoPlayController;
import com.test.video_play.utils.ApplicationUtil;
import com.test.video_play.utils.DecodeUtils;

/**
 * Created by zhangmr on 2021/09/18.
 */
public class AoaServer {

    private static final String TAG = "AoaServer";



    /**
     * 读写线程
     */
    private AoaWriterThread mWriter;
    private AoaReaderThread mReader;
    private Context context;

    public AoaServer(VideoPlayController controller){
        DecodeUtils decodeUtils = new DecodeUtils();
        context = ApplicationUtil.getContext();
        //初始化读写线程
        mReader = new AoaReaderThread(decodeUtils,controller);
        mWriter = new AoaWriterThread();
    }
    public boolean connect(){
        AOAHostSetup.getInstance().init(context);
        //检测是否有设备接入
        boolean flag = AOAHostSetup.getInstance().scanUsbDevices();
        return flag;
    }

    public void start(){
        //启动读写线程
        mReader.start();
        //mWriter.start();
    }

    public void disConnect(){
        //停止读写线程
        if (mReader != null){
            mReader.shutDown();
        }
        if (mWriter != null){
            mWriter.shutDown();
        }
        //终止AOA通信
        AOAHostSetup.getInstance().uninitUsbDevice();
    }

}
