package com.test.video_play.server.aoa;

import android.util.Log;

/**
 * Created by zhangmr on 2021/09/18.
 */
public class AoaWriterThread extends Thread{

    private static final String TAG = "AoaWriterThread";


    @Override
    public void run() {
        try {
            //AOAHostSetup.getInstance().bulkTransferOut((byte[]) msg.obj,((byte[]) msg.obj).length);
        } catch (final Exception e) {
            Log.e(TAG,"USB Send Failed " + e.toString() + "\n");
        }
    }

    public void shutDown(){
        //running = false;
        this.interrupt();
    }
}
