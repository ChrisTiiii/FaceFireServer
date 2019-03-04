package com.example.juicekaaa.fireserver.tcp;

/**
 * Author: create by ZhongMing
 * Time: 2019/3/4 0004 14:13
 * Description:
 */

import android.util.Log;

import com.example.juicekaaa.fireserver.MyApplication;
import com.example.juicekaaa.fireserver.util.EncodingConversionTools;
import com.example.juicekaaa.fireserver.util.GetMac;
import com.example.juicekaaa.fireserver.util.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

public class TCPManager {
    private String MAC = "";
    private static final String TAG = TCPManager.class.getSimpleName();
    /*socket*/
    private Socket socket;
    /*连接线程*/
    private Thread connectThread;
    /* 发送输出流*/
    private OutputStream outputStream;
    /* 读写输入流*/
    private InputStream inputStream;
    private DataInputStream dis;
    /* 线程状态，安全结束线程*/
    private boolean threadStatus = false;
    /* 读取保存进数组*/
    byte buff[] = new byte[1024 * 1024 * 2];
    private String ip;
    private String port;
    /*默认重连*/
    private boolean isReConnect = true;
    /*倒计时Timer发送心跳包*/
    private Timer timer;
    private TimerTask task;

    /* 心跳周期(s)*/
    private int heartCycle = 300;
    /*接收数据长度*/
    private int rcvLength;
    /*接收数据*/
    private String rcvMsg;

    private TCPManager() {
    }

    private static TCPManager instance;

    public static synchronized TCPManager getInstance() {
        if (instance == null) {
            synchronized (TCPManager.class) {
                instance = new TCPManager();
            }
        }
        return instance;
    }

    public TCPManager initSocket(final String ip, final String port) {
        this.ip = ip;
        this.port = port;
        getMac();
        /* 开启读写线程*/
        threadStatus = true;
        new ReadThread().start();

        if (socket == null && connectThread == null) {
            connectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    socket = new Socket();
                    try {
                        /*超时时间为2秒*/
                        socket.connect(new InetSocketAddress(ip, Integer.valueOf(port)), 2000);
                        /*连接成功的话  发送心跳包*/
                        if (socket.isConnected()) {
                            inputStream = socket.getInputStream();
                            dis = new DataInputStream(inputStream);
                            /*因为Toast是要运行在主线程的  这里是子线程  所以需要到主线程哪里去显示toast*/
                            Log.e(TAG, "服务连接成功");
                            /*发送连接成功的消息*/
                            if (onSocketStatusListener != null)
                                onSocketStatusListener.onConnectSuccess();
                            /*发送心跳数据*/
                            sendBeatData();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof SocketTimeoutException) {
                            Log.e(TAG, "连接超时，正在重连");
                            releaseSocket();
                        } else if (e instanceof NoRouteToHostException) {
                            Log.e(TAG, "该地址不存在，请检查");
                        } else if (e instanceof ConnectException) {
                            Log.e(TAG, "连接异常或被拒绝，请检查");
                        }
                    }
                }
            });
            /*启动连接线程*/
            connectThread.start();
        }
        return this;
    }

    /*定时发送数据*/
    private void sendBeatData() {
        if (timer == null) {
            timer = new Timer();
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        outputStream = socket.getOutputStream();
                        Log.i(TAG, "发送心跳包:" + EncodingConversionTools.HexString2Bytes(MAC));
                        /*这里的编码方式根据你的需求去改*/
                        outputStream.write(EncodingConversionTools.HexString2Bytes(MAC));
                        outputStream.flush();
                    } catch (Exception e) {
                        /*发送失败说明socket断开了或者出现了其他错误*/
                        Log.e(TAG, "连接断开，正在重连");
                        /*重连*/
                        releaseSocket();
                        e.printStackTrace();
                    }
                }
            };
        }
        timer.schedule(task, 0, 1000 * heartCycle);
    }

    /*释放资源*/
    private void releaseSocket() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
        if (dis != null) {
            try {
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dis = null;
        }
        if (socket != null) {
            try {
                socket.close();

            } catch (IOException e) {
            }
            socket = null;
        }
        if (connectThread != null) {
            connectThread = null;
        }
        /*重新初始化socket*/
        if (isReConnect) {
            initSocket(ip, port);
        }
    }

    /**
     * 读取数据线程
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (threadStatus) {
                if (inputStream != null) {
                    try {
                        rcvLength = dis.read(buff);
                        if (rcvLength > 0) {
                            rcvMsg = new String(buff, 0, rcvLength, "GBK");
                            rcvMsg = EncodingConversionTools.str2HexStr(rcvMsg);
                            //接收到数据，切换主线程，显示数据
                            if (onReceiveDataListener != null) {
                                onReceiveDataListener.onReceiveData(rcvMsg);
                                System.out.println("accept:" + rcvMsg);
                                EventBus.getDefault().post(new MessageEvent(MyApplication.TCP_BACK_DATA, rcvMsg));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "接收总控数据异常");
                    }
                }

            }
        }
    }

    public interface OnSocketStatusListener {
        void onConnectSuccess();
    }

    public OnSocketStatusListener onSocketStatusListener;

    public void setOnSocketStatusListener(OnSocketStatusListener onSocketStatusListener) {
        this.onSocketStatusListener = onSocketStatusListener;
    }

    public interface OnReceiveDataListener {
        void onReceiveData(String str);
    }

    public OnReceiveDataListener onReceiveDataListener;

    public void setOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        this.onReceiveDataListener = onReceiveDataListener;
    }

    /**
     * 获取本地mac地址
     * 初始化socket
     */
    public void getMac() {
        MAC = GetMac.getMacAddress().replaceAll(":", "");
        System.out.println("mac: " + MAC);
    }

    public void stopSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
