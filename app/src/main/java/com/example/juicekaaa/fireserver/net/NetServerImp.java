package com.example.juicekaaa.fireserver.net;

import android.content.Context;
import android.content.Intent;

import com.example.juicekaaa.fireserver.MyApplication;
import com.example.juicekaaa.fireserver.utils.DownLoadManager;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NetServerImp {
    private Retrofit retrofit;
    private static final String BASE_URL = "10.101.80.134:23303";
    public static String HOST_IP = "47.100.192.169:8080";
    private Context context;
    private String mac;
    OkHttpClient client;
    private NetAPI netAPI;

//    public NetServerImp(Context context, String mac) {
//        this.context = context;
//        this.mac = mac;
//    }

    /**
     * 初始化retrofit netApi
     *
     * @param
     */
    public NetServerImp() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        client = httpBuilder.readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS) //设置超时
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(HOST_IP)//基础URL 建议以 / 结尾
                .addConverterFactory(GsonConverterFactory.create())//设置 Json 转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//RxJava 适配器
                .client(client)
                .build();
        netAPI = retrofit.create(NetAPI.class);

    }

    public void downFile() {
        netAPI.downFile("hailiang", 1, 1, 3, "app_firecontrol_owner", URLs.Article_URL)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ResponseBody>() {
            @Override
            public void onCompleted() {
                //下载完播放视频,发送广播通知播放
                Intent myintent = new Intent(MyApplication.BROADCAST_ACTION_VIDEO);
                context.sendBroadcast(myintent, MyApplication.BROADCAST_VIDEO_DISC);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
                //下载成功，写入文件
                boolean bl = false;
                String dirName = "/sdcard/FireVideo/";
                File file = new File(dirName);
                //不存在创建
                if (!file.exists()) {
                    file.mkdir();
                }
                DownLoadManager.writeResponseBodyToDisk(file, responseBody);

            }
        });
    }
//        netAPI.downFile("hailiang", 1, 1, 3, "app_firecontrol_owner").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<ResponseBody>() {
//                                                                                                                                                           @Override
//                                                                                                                                                           public void onCompleted() {
//
//                                                                                                                                                           }
//
//                                                                                                                                                           @Override
//                                                                                                                                                           public void onError(Throwable e) {
//
//                                                                                                                                                           }
//
//                                                                                                                                                           @Override
//                                                                                                                                                           public void onNext(ResponseBody responseBody) {
//
//                                                                                                                                                           }
//                                                                                                                                                       }
//
//
////    public void sendMac() {
////        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
////        NetAPI netAPI = retrofit.create(NetAPI.class);
////        Call<ResponseBody> call = netAPI.getCall(mac);
////        call.enqueue(new Callback<ResponseBody>() {
////            @Override
////            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
////                try {
////                    System.out.println(response.body().bytes());
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////
////            @Override
////            public void onFailure(Call<ResponseBody> call, Throwable t) {
////
////            }
////        });
////    }
}
