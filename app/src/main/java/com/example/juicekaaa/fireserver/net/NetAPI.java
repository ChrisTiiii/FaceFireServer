package com.example.juicekaaa.fireserver.net;


import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

public interface NetAPI {
    @GET("/")
    Observable<ResponseBody> getCall(@Query("mac") String mac);//接收网络请求数据的方法

    //文件Retrofit下载
    @Streaming
    @GET
    Observable<ResponseBody> downFile(@Query("username") String username, @Query("page") Integer page, @Query("pageSize") Integer pageSize, @Query("publicitytype") Integer publicitytype, @Query("platformkey") String platformkey, @Url String fileUrl);

    @GET("getAllGoodsType.do")
    Observable<String> getGoodsType(@Query("from") String app, @Query("token") String token);//获得商品分类

}
