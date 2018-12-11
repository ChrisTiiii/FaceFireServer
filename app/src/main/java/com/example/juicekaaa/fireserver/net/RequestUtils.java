package com.example.juicekaaa.fireserver.net;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

public class RequestUtils {
    public static AsyncHttpClient client = new AsyncHttpClient();

    static {
        client.setTimeout(60000);// 设置超时的时间
    }

    public static void ClientGet(String url, NetCallBack cb) {
        client.get(url, cb);
    }

    public static void ClientPost(String url, RequestParams params,
                                  NetCallBack cb) {
        client.post(url, params, cb);
    }
}
