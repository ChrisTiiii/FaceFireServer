package com.example.juicekaaa.fireserver;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

public class MyApplication extends Application {
    public static final String BROADCAST_PERMISSION_DISC = "com.permissions.MYF_BROADCAST";
    public static final String BROADCAST_PASS_DISC = "com.permissions.MYP_BROADCAST";
    public static final String BROADCAST_DISMISS_DISC = "com.permissions.MYD_BROADCAST";
    public static final String BROADCAST_VIDEO_DISC = "com.permissions.MYV_BROADCAST";
    public static final String BROADCAST_ACTION_DISC = "com.permissions.myf_broadcast";
    public static final String BROADCAST_ACTION_PASS = "com.permissions.myp_broadcast";
    public static final String BROADCAST_ACTION_DISMISS = "com.permissions.myd_broadcast";
    public static final String BROADCAST_ACTION_VIDEO = "com.permissions.myv_broadcast";

    public static final int MESSAGE = 0x345334;
    public static final int MESSAGE_UPDATE = 0x345333;
    public static final int MESSAGE_OUT = 0x756467;
    public static final int MESSAGE_DISMISS = 0x756468;
    public static final int MESSAGE_BANNER = 0x756499;


    public static final int TCP_BACK_DATA = 0x213;

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}

