package com.test.video_play.server.aoa;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.test.video_play.utils.ByteUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by zhangmr on 2021/09/02.
 */
public class AOAHostSetup {
    private static final String TAG = "AOAHostSetup";
    private static AOAHostSetup mInstance = null;

    /**
     * AOA连接相关常量
     */
    private static final int AOA_GET_PROTOCOL = 51;
    private static final int AOA_SEND_IDENT = 52;
    private static final int AOA_START_ACCESSORY = 53;
    private static final int TIME_OUT = 0;
    private static final String AOA_MANUFACTURER = "pateo";
    private static final String AOA_MODEL_NAME = "QingLink";
    private static final String AOA_DESCRIPTION = "QingLink AOA";
    private static final String AOA_VERSION = "1.0.0";
    private static final String AOA_URI = "http://www.abc.com.cn/";
    private static final String AOA_SERIAL_NUMBER = "12345678";
    private static final int AOA_MAX_BUFFER_BYTES = 16 * 1024;
    private byte[] mDataBuffer = new byte[AOA_MAX_BUFFER_BYTES];

    /**
     * USB连接模式相关变量
     */
    private UsbDevice mUsbDevice = null;
    private UsbDeviceConnection mUsbDeviceConnection = null;
    private UsbManager mUsbManager = null;
    private Context mContext = null;
    private PendingIntent mPermissionIntent = null;
    private UsbInterface mUsbInterface = null;
    private UsbEndpoint mUsbEndpointIn = null;
    private UsbEndpoint mUsbEndpointOut = null;

    private Boolean running;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    /**
     * 获取AOAHostSetup实例
     * @return
     */
    public static AOAHostSetup getInstance() {
        if (null == mInstance) {
            synchronized (AOAHostSetup.class) {
                if (null == mInstance) {
                    mInstance = new AOAHostSetup();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        Log.d(TAG, "init");
        mContext = context;
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        mPermissionIntent =
                PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

    }

    /**
     * 初始化usb设备
     * @param device
     * @return
     */
    public boolean initUsbDevice(UsbDevice device) {
        Log.d(TAG, "initUsbDevice");
        if (device == null) {
            Log.e(TAG, "device is null, initUsbDevice fail");
            return false;
        }
        if (mUsbManager == null) {
            Log.e(TAG, "mUsbManager is null, initUsbDevice fail");
            return false;
        }
        mUsbDevice = device;
        changeToAccessoryMode(mUsbDevice);
        mUsbInterface = mUsbDevice.getInterface(0);
        try {
            mUsbDeviceConnection.claimInterface(mUsbInterface, true);
        } catch (Exception ex) {
            Log.e(TAG, "initUsbDevice fail");
            return false;
        }

        int endpointCount = mUsbInterface.getEndpointCount();
        for (int i = 0; i < endpointCount; i++) {
            UsbEndpoint usbEndpoint = mUsbInterface.getEndpoint(i);
            if (usbEndpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    mUsbEndpointOut = usbEndpoint;
                } else if (usbEndpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    mUsbEndpointIn = usbEndpoint;
                }
            }
        }
        //mUsbEndpointIn = mUsbInterface.getEndpoint(BULK_ENDPOINT_IN_ADDRESS);
        //mUsbEndpointOut = mUsbInterface.getEndpoint(BULK_ENDPOINT_OUT_ADDRESS);

        return true;
    }

    /**
     * 切换到
     * @param usbDevice
     * @return
     */
    public boolean changeToAccessoryMode(UsbDevice usbDevice) {
        mUsbDeviceConnection = mUsbManager.openDevice(usbDevice);

        Log.d(TAG, "changeToAccessoryMode");
        if (usbDevice == null) {
            return false;
        }
        if (!getProtocolVersion()) {
            Log.e(TAG, "Change Accessory Mode getProtocolVersion Fail");
            return false;
        }
        if (!sendIdentityStrings()) {
            Log.e(TAG, "Change Accessory Mode sendIdentityStrings Fail");
            return false;
        }
        if (!startAccessoryMode()) {
            Log.e(TAG, "Change Accessory Mode startAccessoryMode Fail");
            return false;
        }
        Log.e(TAG, "Change Accessory Mode Success");
        return true;
    }

    /**
     * 检查usb设备是否支持AOA
     * @return
     */
    private boolean getProtocolVersion() {
        byte[] buffer = new byte[2];
        if (controlTransferIn(AOA_GET_PROTOCOL, 0, 0, buffer) < 0) {
            Log.d(TAG, "get protocol version fail");
            return false;
        }
        int version = buffer[1] << 8 | buffer[0];
        if (version < 1 || version > 2) {
            Log.e(TAG, "usb device not capable of AOA 1.0 or 2.0, version = " + version);
            return false;
        }
        Log.e(TAG, "usb device AOA version is " + version);
        return true;
    }

    /**
     * 校验device设备相关参数是否符合
     * @return
     */
    private boolean sendIdentityStrings() {
        if (controlTransferOut(AOA_SEND_IDENT, 0, 0, AOA_MANUFACTURER.getBytes()) < 0) {
            Log.d(TAG, "send identity AOA_MANUFACTURER fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 1, AOA_MODEL_NAME.getBytes()) < 0) {
            Log.d(TAG, "send identity AOA_MODEL_NAME fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 2, AOA_DESCRIPTION.getBytes()) < 0) {
            Log.d(TAG, "send identity AOA_DESCRIPTION fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 3, AOA_VERSION.getBytes()) < 0) {
            Log.d(TAG, "send identity AOA_VERSION fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 4, AOA_URI.getBytes()) < 0) {
            Log.d(TAG, "send identity AOA_URI fail");
            return false;
        }
        if (controlTransferOut(AOA_SEND_IDENT, 0, 5, AOA_SERIAL_NUMBER.getBytes()) < 0) {
            Log.d(TAG, "send identity AOA_SERIAL_NUMBER fail");
            return false;
        }
        Log.e(TAG, "send indentity string success");
        return true;
    }

    /**
     * 开启配件模式
     * @return
     */
    private boolean startAccessoryMode() {
        if (controlTransferOut(AOA_START_ACCESSORY, 0, 0, null) < 0) {
            Log.d(TAG, "start accessory mode fail");
            return false;
        }
        Log.e(TAG, "start accessory mode success");
        return true;
    }
    private int controlTransferOut(int request, int value, int index, byte[] buffer) {
        if (mUsbDeviceConnection == null) {
            return -1;
        }
        return mUsbDeviceConnection.controlTransfer(UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_VENDOR, request,
                value, index, buffer, buffer == null ? 0 : buffer.length, TIME_OUT);
    }
    private int controlTransferIn(int request, int value, int index, byte[] buffer) {
        if (mUsbDeviceConnection == null) {
            return -1;
        }
        return mUsbDeviceConnection.controlTransfer(UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_VENDOR, request,
                value, index, buffer, buffer == null ? 0 : buffer.length, TIME_OUT);
    }

    /**
     * 检测已连接USB设备，并进行设备初始化
     * @return
     */
    public boolean scanUsbDevices() {
        if (mContext == null || mUsbManager == null || mPermissionIntent == null) {
            Log.e(TAG, "scanUsbDevices fail");
            return false;
        }

        //获取已连接设备列表
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.d(TAG, "device count = " + deviceList.size());
        if (deviceList.size() == 0) {
            return false;
        }
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device == null) {
                continue;
            }

            Log.d(TAG, device.toString());
            //请求权限
            mUsbManager.requestPermission(device, mPermissionIntent);
            //初始化usb设备
            if (initUsbDevice(device)) {
                Log.d(TAG, "init device success");
                return true;
            }
        }
        return false;
    }
    /**
     * AOA连接后读取流
     * @param data
     * @param len
     * @return
     */
    public int bulkTransferIn(byte[] data, int len) {
        int ret = -1;
        int cnt = len;
        int readLen = -1;
        int dataLen = 0;
        try {
            if (mUsbDeviceConnection == null || mUsbEndpointIn == null) {
                Log.e(TAG, "mUsbDeviceConnection or mUsbEndpointIn is null");
                throw new IOException();
            }

            //该帧长度小于AOA最大传输数据大小
            if (len <= AOA_MAX_BUFFER_BYTES) {
                ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, data, len, TIME_OUT);
                if (ret < 0) {
                    //Log.e(TAG, "bulkTransferIn error 1: ret = " + ret);
                    //throw new IOException();
                    return -1;
                } else if (ret == 0) {
                    return 0;
                }
                dataLen = ret;
            } else {
                //该帧长度大于AOA最大传输数据大小
                while (cnt > 0) {
                    readLen = cnt > AOA_MAX_BUFFER_BYTES ? AOA_MAX_BUFFER_BYTES : cnt;
                    ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, mDataBuffer, readLen, TIME_OUT);
                    if (ret < 0) {
                        Log.e(TAG, "bulkTransferIn error 2: ret = " + ret);
                        throw new IOException();
                    } else if (ret == 0) {
                        continue;
                    }
                    System.arraycopy(mDataBuffer, 0, data, dataLen, ret);
                    //Log.i(TAG,"mDataBuffer"+Arrays.toString(mDataBuffer));
                    //Log.i(TAG,"mDataBufferLength:"+mDataBuffer.length+",cnt:"+cnt+"ret:"+ret);
                    cnt -= ret;
                    dataLen += ret;
                }
            }

            /*len为byte数组定义长度,dataLen为byte数组实际使用长度,
             但是如何保证decice设备写入数据与host读出的数据长度一致?
             */
            /*if (dataLen != len) {
                Log.e(TAG, "bulkTransferIn error 3: dataLen = " + dataLen + ", len = " + len);
                ret = -1;
                throw new IOException();
            }*/
            return dataLen;
        } catch (Exception e) {
            Log.e(TAG, "bulkTransferIn catch exception");
            e.printStackTrace();
            return -1;
        }

    }

    /**
     * AOA连接后输出流
     * @param data
     * @param len
     * @return
     */
    public int bulkTransferOut(byte[] data, int len) {
        int ret = -1;
        int cnt = len;
        int readLen = -1;
        int dataLen = 0;
        try {
            if (mUsbDeviceConnection == null || mUsbEndpointOut == null) {
                Log.e(TAG, "mUsbDeviceConnection or mUsbEndpointIn is null");
                throw new IOException();
            }

            if (len <= AOA_MAX_BUFFER_BYTES) {
                ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, data, len, TIME_OUT);
                if (ret <= 0) {
                    Log.e(TAG, "bulkTransferOut error 1: ret = " + ret);
                    throw new IOException();
                }
                dataLen = ret;
            } else {
                while (cnt > 0) {
                    readLen = cnt > AOA_MAX_BUFFER_BYTES ? AOA_MAX_BUFFER_BYTES : cnt;
                    System.arraycopy(data, dataLen, mDataBuffer, 0, readLen);
                    ret = mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, mDataBuffer, readLen, TIME_OUT);
                    if (ret <= 0) {
                        Log.e(TAG, "bulkTransferOut error 2: ret = " + ret);
                        throw new IOException();
                    }
                    cnt -= ret;
                    dataLen += ret;
                }
            }

            if (dataLen != len) {
                Log.e(TAG, "bulkTransferOut error 3: dataLen = " + dataLen + ", len = " + len);
                ret = -1;
                throw new IOException();
            }
            return dataLen;
        } catch (Exception e) {
            Log.e(TAG, "bulkTransferOut catch exception");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 终止device通信
     */
    public void uninitUsbDevice() {
        Log.d(TAG, "uninitUsbDevice");
        try {
            if (mUsbDeviceConnection != null) {
                mUsbDeviceConnection.releaseInterface(mUsbInterface);
                mUsbDeviceConnection.close();
            }

            mUsbDevice = null;
            mUsbEndpointIn = null;
            mUsbEndpointOut = null;
            mUsbDeviceConnection = null;
        } catch (Exception ex) {
            Log.e(TAG, "uninitUsbDevice fail");
        }
    }

}
