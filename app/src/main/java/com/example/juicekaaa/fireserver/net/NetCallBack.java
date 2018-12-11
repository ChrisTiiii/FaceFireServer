package com.example.juicekaaa.fireserver.net;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;

public abstract class NetCallBack extends AsyncHttpResponseHandler {

    @Override
    public void onStart() {
        Log.i("info", "请求开始，弹出进度条框");
        super.onStart();
    }

    @Override
    public void onSuccess(String arg0) {
        Log.i("info", "请求成功，隐藏进度条框：" + arg0);
        onMySuccess(arg0);
        super.onSuccess(arg0);
    }

    @Override
    public void onFailure(Throwable arg0) {
        Log.i("info", "请求失败，隐藏进度条框：" + arg0);
        super.onFailure(arg0);
        onMyFailure(arg0);
    }

    public abstract void onMySuccess(String result);

    public abstract void onMyFailure(Throwable arg0);
}
