package com.test.video_play.utils;

import android.util.Log;

import com.test.video_play.entity.Frame;

import java.util.Arrays;

/**
 * Created by zhangmr on 2021/09/18.
 *
 * @Desc 解析H264和AAC的Decoder
 */
public class DecodeUtils {
    private static final String TAG = "H264AacDecoder";
    // IDR关键帧
    public final static int IDR = 5;
    // SPS帧
    public final static int SPS = 7;
    // PPS帧
    public final static int PPS = 8;

    public final static int P = 1;

    public final static int AccessUnitDelimiter = 9;

    public final static int AUDIO = -2;

    private byte[] mPps;
    private byte[] mSps;

    public DecodeUtils() {
        Log.e(TAG,"DecodeUtils"+"new one"+this);
    }


    public void isCategory(byte[] frame) {
        boolean isKeyFrame = false;
        if (frame == null) {
            Log.e(TAG, "annexb not match.");
            return;
        }
        // ignore the nalu type aud(9)
        if (isAccessUnitDelimiter(frame)) {
            return;
        }
        // for pps
        if (isPps(frame)) {
            mPps = frame;
            //Log.e(TAG, "isCategory PPS: " + Arrays.toString(mPps) + "," + this);
            if (mPps != null && mSps != null) {
                //Log.e(TAG, "isCategory PPS IN: " + Arrays.toString(mPps));
                mListener.onSpsPps(mSps, mPps);
            }
            return;
        }
        // for sps
        if (isSps(frame)) {
            mSps = frame;
            //Log.e(TAG, "isCategory SPS: " + Arrays.toString(mSps) + "," + this);
            if (mPps != null && mSps != null) {
                //Log.e(TAG, "isCategory SPS IN: " + Arrays.toString(mSps) + "," + this);
                mListener.onSpsPps(mSps, mPps);
            }
            return;
        }
        if (isAudio(frame)) {
            byte[] temp = new byte[frame.length - 4];
            System.arraycopy(frame, 4, temp, 0, frame.length - 4);
            mListener.onVideo(temp, Frame.AUDIO_FRAME);
            return;
        }
        // for IDR frame
        if (isKeyFrame(frame)) {
            isKeyFrame = true;
        } else {
            isKeyFrame = false;
        }
        mListener.onVideo(frame, isKeyFrame ? Frame.KEY_FRAME : Frame.NORMAL_FRAME);
    }

    private boolean isAudio(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        return frame[4] == ((byte) 0xFF) && frame[5] == ((byte) 0xF9);
    }

    private boolean isSps(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == SPS;
    }

    private boolean isPps(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == PPS;
    }

    private boolean isKeyFrame(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == IDR;
    }

    private static boolean isAccessUnitDelimiter(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == AccessUnitDelimiter;
    }

    private static boolean isPFrame(byte[] frame){
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == P;
    }
    // TODO: 2018/6/7 自定义回调接口
    public OnVideoListener mListener;

    public void setOnVideoListener(OnVideoListener listener) {
        this.mListener = listener;
    }

    public interface OnVideoListener {
        void onSpsPps(byte[] sps, byte[] pps);

        void onVideo(byte[] video, int type);
    }
}
