package com.miao.joey.bluetoothserial.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 蓝牙服务类(Broadcast发送消息):
 * 1. 创建、断开连接
 * 2. 接收、发送数据
 * 3. 获得连接状态
 */
public class BluetoothService extends Service {

    private static final String TAG = "ContentService";
    public static int CONNECT_STATUS = 0;
    public static String device_connected = ""; // 当前 连接设备名称

    private final BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public BluetoothService() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
    }

    public final class LocalBinder extends Binder {
        public BluetoothService getService() {
            Log.i(TAG, "getService: " + "获取蓝牙服务");
            return BluetoothService.this;
        }
    }

    /**
     * 发送广播
     *
     * @param content 广播内容
     */
    protected void sendContentBroadcast(String content) {
        Intent intent = new Intent();
        intent.setAction("com.miao.joey.bluetoothcallback.content");
        intent.putExtra("content", content);
        sendBroadcast(intent);
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // 取消任何尝试建立连接的线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 取消当前正在运行连接的任何线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        // 取消任何尝试建立连接的线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // 取消当前正在运行连接的任何线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // 开始线程连接给定的设备
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        Log.d(TAG, "connected");
        // 取消连接线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // 在创建新的线程前重置该线程(只需要一个)
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // 启动线程来管理连接并执行传输
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // 将连接的设备的名称发送回UI界面
        device_connected = device.getName();
//        sendContentBroadcast(device.getName());
    }

    /**
     * 停止所有线程
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        CONNECT_STATUS = 0;
        sendContentBroadcast("STOP");
    }

    /**
     * 调用此方法向设备发送指令
     *
     * @param out
     */
    public void write(byte[] out) {
        // 创建临时对象
        ConnectedThread r;
        // 同步ConnectedThread的副本
        synchronized (this) {
            if (mConnectedThread == null) {
                return;
            }
            r = mConnectedThread;
        }
        // 执行写入（不同步）
        r.write(out);
    }

    /**
     * 此线程在尝试与设备建立传出连接时运行。 它贯穿始终; 连接成功或失败。
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    class ConnectThread extends Thread {
        private static final String TAG = "ConnectThread";
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(
                        MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            mAdapter.cancelDiscovery();

            // 连接BluetoothSocket
            try {
                mmSocket.connect();// 阻塞方法
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                sendContentBroadcast("CONNECT_FAILURE");
                return;
            }

            // 连接成功时重置连接线程
            synchronized (this) {
                mConnectThread = null;
            }
            sendContentBroadcast("CONNECT_SUCCESSFUL");
            CONNECT_STATUS = 1;
            // 开启Connected线程
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
            CONNECT_STATUS = 0;
        }
    }

    /**
     * 此线程在与远程设备连接期间运行。
     * 它处理所有传入和传出的传输。
     */
    class ConnectedThread extends Thread {
        private static final String TAG = "ConnectedThread";
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private BufferedReader bufferedReader;
        private InputStreamReader inputStreamReader;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            bufferedReader = null;
            inputStreamReader = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "获取流失败", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            while (true) {
                try {
                    // 读取输入流并发送广播
                    inputStreamReader = new InputStreamReader(mmInStream);
                    bufferedReader = new BufferedReader(inputStreamReader);
                    String str = bufferedReader.readLine();
                    sendContentBroadcast(str);
                } catch (IOException e) {
                    sendContentBroadcast("接收终止");
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }
}
