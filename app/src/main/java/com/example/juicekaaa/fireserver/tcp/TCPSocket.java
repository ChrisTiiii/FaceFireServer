package com.example.juicekaaa.fireserver.tcp;

import android.util.Log;

import com.example.juicekaaa.fireserver.MyApplication;
import com.example.juicekaaa.fireserver.utils.EncodingConversionTools;
import com.example.juicekaaa.fireserver.utils.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TCPSocket extends Thread {
    private static final long HEARTBEAT_MESSAGE_DURATION = 120 * 1000; //50
    private static Socket connectsocket;
    private static String IPAddress;
    private static int Port;
    private boolean isSuccess = false;
    private String lastResult;
    private byte[] senddata;
    private int callbacktype;

    private static boolean first = true;
    Timer timer = null;

    private static long INTERVAL_TIME = 30 * 1000;//15


    TimerTask timerTask;

    public TCPSocket(byte[] senddata) {
        this.callbacktype = 1;
        this.senddata = senddata;
    }

    public TCPSocket(Socket socket, String IPAddress, int port, int callbacktype, byte[] senddata) {
        this.senddata = senddata;
        this.connectsocket = socket;
        this.IPAddress = IPAddress;
        this.Port = port;
        this.callbacktype = callbacktype;
    }


    @Override
    public void run() {
        switch (callbacktype) {
            case 0:
                onebyone();
                break;
            case 1:
                moretomore();
                break;
            case 2:
                equitupload();
                break;
        }
    }


    /**
     * 一应一答模式（所谓的短连接模式）
     **/
    public void onebyone() {
        buildConnect();   //建立与服务器的连接
        if (isSuccess) {
//            senddata(senddata);
        }
    }

    /**
     * 循环发送请求和接收响应
     **/
    public void moretomore() {
        buildConnect();   //建立与服务器的连接
        while (true) {
            try {
                if (connectsocket.isClosed()) {
                    break;
                }
                if (isSuccess) {
                    startHeartbeatTimer();
                }
                Thread.sleep(INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 循环接收服务器传过来的数据
     **/
    public void equitupload() {
        buildConnect();   //建立与服务器的连接
        if (isSuccess) {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("Data", "接收数据");
                if (!connectsocket.isClosed()) {
                    acceptdata(connectsocket);
                    Log.i("Data", "接收响应数据结束++++++++++++++++");
                } else {
                    break;
                }

            }
        }

    }

    /**
     * 建立与服务器的连接并发送请求
     **/
    public void buildConnect() {
        try {
            Log.i("Data", " socket begin connect ++++++++++");
            Log.i("Data", "tcp connect ,ip = " + IPAddress + "Port = " + Port);
            connectsocket.setSoTimeout(10 * 1000);   //设置连接超时时间
            Log.i("Data", "connectsocket.isConnected=" + connectsocket.isConnected());   //是否关闭
            Log.i("Data", "connectsocket.isClosed=" + connectsocket.isClosed());   //是否连接
            Log.i("Data", "connectsocket.isInputShutdown=" + connectsocket.isInputShutdown());   //是否关闭输入流
            Log.i("Data", "connectsocket.isOutputShutdown=" + connectsocket.isOutputShutdown());   //是否关闭输出流
            Log.i("Data", "连接请求已发送");
            if (connectsocket.isConnected() && (!connectsocket.isClosed())) {  //表明当前连接成功
                Log.i("Data", "连接成功，准备发送信息");
                isSuccess = true;
            } else {
                try {
                    Log.i("Data", "断开连接，重新连接");
                    Thread.sleep(1000);
                    connectsocket = new Socket(IPAddress, Port);
                    if (connectsocket.isConnected() && (!connectsocket.isClosed())) {
                        Log.i("Data", "尝试重新连接成功");
                        isSuccess = true;
                        //第一次重连
                        if (first) {
                            Log.i("first", "success");
                            senddata(senddata);
                            first = false;
                        }
                    } else {
                        Log.i("Data", "尝试重新连接失败");
                        isSuccess = false;
                        return;
                    }

                } catch (InterruptedException e) {
                    isSuccess = false;
                    e.printStackTrace();
                }
                return;
            }

        } catch (IOException e) {
            isSuccess = false;
            e.printStackTrace();
        }

    }

    /**
     * 发送数据
     **/
    public void senddata(byte[] senddata) {
        synchronized (this) {
            try {
                Log.i("Data", "向服务器发送数据,发送时间=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                System.out.println("========================88888888888" + "向服务器发送数据,发送时间=");
                if (connectsocket.isClosed()) {
                    connectsocket.close();
                } else {
                    connectsocket.getOutputStream().write(senddata);//发送数据
                    Log.i("Data", "获取输出流成功");
                    System.out.println("========================9999999999" + "获取输出流成功");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 接收数据
     **/
    public byte[] acceptdata(Socket connectsocket) {
        byte[] acceptdata1 = null;
        try {
            InputStream istream = null;
            //Thread.sleep(1000);
            if (connectsocket.isConnected()) {
                if (!connectsocket.isInputShutdown()) {
                    istream = connectsocket.getInputStream();
                    int datalength = istream.available();
                    Log.i("Data", "接收到的字节数" + datalength);
                    if (datalength > 0) {
                        acceptdata1 = new byte[datalength];
                        istream.read(acceptdata1);
                        if (acceptdata1 != null) {
                            isSuccess = true;
                            lastResult = EncodingConversionTools.byte2HexStr(acceptdata1);
                            if (lastResult != null)
                                EventBus.getDefault().post(new MessageEvent(MyApplication.TCP_BACK_DATA, lastResult));
                            Log.i("Data", "the lastresult=" + lastResult);
                        }
                    }
                } else {
                    isSuccess = false;
                    Log.i("Data", "connectsocket 失败 isInputShutdown=false");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return acceptdata1;

    }


    /**
     * 启动心跳，timer 间隔60秒
     */

    public void startHeartbeatTimer() {
        if (timer == null) {
            timer = new Timer();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                senddata(senddata);
            }
        };
        timer.schedule(timerTask, 0, HEARTBEAT_MESSAGE_DURATION);
    }

    public void stopSocket() {
        try {
            connectsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
