package com.example.juicekaaa.fireserver.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.juicekaaa.fireserver.MyApplication;
import com.example.juicekaaa.fireserver.utils.MessageEvent;

import org.greenrobot.eventbus.EventBus;

public class Receiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(MyApplication.BROADCAST_ACTION_DISC)) {
            String str = intent.getStringExtra("str_test");
            EventBus.getDefault().post(new MessageEvent(MyApplication.MESSAGE_UPDATE));
        }
        if (action.equals(MyApplication.BROADCAST_ACTION_PASS)) {
            EventBus.getDefault().post(new MessageEvent(MyApplication.MESSAGE_OUT));
        }
        if (action.equals(MyApplication.BROADCAST_ACTION_DISMISS)) {
            EventBus.getDefault().post(new MessageEvent(MyApplication.MESSAGE_DISMISS));
        }
        if (action.equals(MyApplication.BROADCAST_ACTION_VIDEO)) {
            EventBus.getDefault().post(new MessageEvent(MyApplication.MESSAGE));
        }
    }
}
