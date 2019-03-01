package com.example.juicekaaa.fireserver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.juicekaaa.fireserver.tcp.TCPSocket;
import com.example.juicekaaa.fireserver.util.EncodingConversionTools;
import com.example.juicekaaa.fireserver.util.GetMac;

import java.net.Socket;

public class MyService extends Service {
    private static TCPSocket tcpSocket;
    private Socket socket = new Socket();
    public String SERVICE_IP = "101.132.139.37";//10.101.208.78   10.101.80.134 10.101.80.100 10.101.208.157 10.101.208.157
    public int SERVICE_PORT = 23303;
    private String MAC = "";
    private final String TAG = "SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();
        getMac();
        socket = new Socket();
        initSeceive();
        Log.e(TAG, MAC);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tcpSocket.stopSocket();//关闭tcp通讯
    }

    /**
     * 初始化TCP通讯
     */
    private void initSeceive() {
        //用于接收命令
        tcpSocket = new TCPSocket(socket, SERVICE_IP, SERVICE_PORT, 2);
        tcpSocket.start();
        //用于发送心跳包
        TCPSocket sendHeart = new TCPSocket(EncodingConversionTools.HexString2Bytes(MAC));
        //tcpSocket.setPriority(Thread.NORM_PRIORITY + 3);
        sendHeart.start();
    }


    /**
     * 获取本地mac地址
     * 初始化socket
     */
    protected void getMac() {
        MAC = GetMac.getMacAddress().replaceAll(":", "");
        System.out.println("mac: " + MAC);
    }
}
