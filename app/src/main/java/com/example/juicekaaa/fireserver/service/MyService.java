package com.example.juicekaaa.fireserver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.juicekaaa.fireserver.tcp.TCPManager;
import com.example.juicekaaa.fireserver.util.GetMac;

import java.net.Socket;

public class MyService extends Service {
    private Socket socket = new Socket();
    public String SERVICE_IP = "101.132.139.37";//10.101.208.78   10.101.80.134 10.101.80.100 10.101.208.157 10.101.208.157 101.132.139.37
    public int SERVICE_PORT = 23303;//23303
    private String MAC = "";
    private final String TAG = "SERVICE";

    @Override
    public void onCreate() {
        super.onCreate();
        getMac();
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
        TCPManager.getInstance().stopSocket();
    }

    /**
     * 初始化TCP通讯
     */
    private void initSeceive() {
        TCPManager.getInstance().initSocket(SERVICE_IP, String.valueOf(SERVICE_PORT)).setOnSocketStatusListener(new TCPManager.OnSocketStatusListener() {
            @Override
            public void onConnectSuccess() {
            }
        });
    }


    /**
     * 获取本地mac地址
     * 初始化socket
     */
    protected void getMac() {
        MAC = GetMac.getMacAddress().replaceAll(":", "");
        Log.i(TAG, MAC);
    }
}
