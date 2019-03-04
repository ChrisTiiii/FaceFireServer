package com.example.juicekaaa.fireserver.net;

import android.content.Context;
import android.content.Intent;

import com.example.juicekaaa.fireserver.MyApplication;
import com.example.juicekaaa.fireserver.utils.MessageEvent;
import com.loopj.android.http.RequestParams;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * 广告视频
 */
public class Advertisement {
    static String mypath;
    public static void downLoad(final Context context) {
        RequestParams params = new RequestParams();
        params.put("username", "hailiang");
        params.put("page", "1");
        params.put("pageSize", "1");
        params.put("publicitytype", "3");
        params.put("platformkey", "app_firecontrol_owner");
        RequestUtils.ClientPost(URLs.Article_URL, params,
                new NetCallBack() {
                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onMySuccess(String result) {
                        if (result == null || result.length() == 0) {
                            return;
                        }
                        System.out.println("数据请求成功" + result);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String msg = jsonObject.getString("msg");
                            if (msg.equals("获取成功")) {
                                String data = jsonObject.getString("data");
                                JSONObject objects = new JSONObject(data);
                                String mylist = objects.getString("list");
                                JSONArray array = new JSONArray(mylist);
                                JSONObject object;
                                for (int i = 0; i < array.length(); i++) {
                                    object = (JSONObject) array.get(i);
                                    String record_url = object.optString("record_url");
                                    mypath = URLs.HOST + record_url;
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            //"http://10.101.80.113:8080/RootFile/Platform/20181114/1542178640266.mp4"
                                            URL url = new URL(mypath);
                                            //打开连接
                                            URLConnection conn = url.openConnection();
                                            //打开输入流
                                            InputStream is = conn.getInputStream();
                                            //获得长度
                                            int contentLength = conn.getContentLength();
                                            //创建文件夹 MyDownLoad，在存储卡下
                                            String dirName = "/sdcard/FireVideo/";
                                            File file = new File(dirName);
                                            //不存在创建
                                            if (!file.exists()) {
                                                file.mkdir();
                                            }
                                            // 下载后的文件名
                                            int i = mypath.lastIndexOf("/"); // 取的最后一个斜杠后的字符串为名
                                            String fileName = dirName + mypath.substring(i, mypath.length());
                                            File file1 = new File(fileName);
                                            if (file1.exists()) {
                                                file1.delete();
                                            }
                                            //创建字节流
                                            byte[] bs = new byte[1024];
                                            int len;
                                            OutputStream os = new FileOutputStream(fileName);
                                            //写数据
                                            while ((len = is.read(bs)) != -1) {
                                                os.write(bs, 0, len);
                                            }
                                            //完成后关闭流
                                            os.close();
                                            is.close();
                                            //下载完播放视频,发送广播通知播放
                                            Intent myintent = new Intent(MyApplication.BROADCAST_ACTION_VIDEO);
                                            context.sendBroadcast(myintent, MyApplication.BROADCAST_VIDEO_DISC);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onMyFailure(Throwable arg0) {
                    }
                });
    }

    //广告图片信息
    public static void addImage() {
        final ArrayList<String> list_path = new ArrayList<>();
        final ArrayList<String> imageTitle = new ArrayList<>();
        final ArrayList<String> list_paths = new ArrayList<>();
        final ArrayList<String> imageTitles = new ArrayList<>();

        RequestParams params = new RequestParams();
        params.put("username", "hailiang");
        RequestUtils.ClientPost(URLs.IMAGEE_URL, params,
                new NetCallBack() {
                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onMySuccess(String result) {
                        if (result == null || result.length() == 0) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            String data = jsonObject.getString("data");
                            JSONArray array = new JSONArray(data);
                            JSONObject object;
                            for (int i = 0; i < array.length(); i++) {
                                object = (JSONObject) array.get(i);
                                String url = object.getString("url");
                                list_path.add(URLs.HOST + url);
                                imageTitle.add("");
                            }
                            for (int i = 2; i < array.length(); i++) {
                                object = (JSONObject) array.get(i);
                                String url = object.getString("url");
                                list_paths.add(URLs.HOST + url);
                                imageTitles.add("");
                            }
                            MessageEvent messageEvent = new MessageEvent(MyApplication.MESSAGE_BANNER);
                            messageEvent.setListPath(list_path);
                            messageEvent.setListPaths(list_paths);
                            messageEvent.setImageTitle(imageTitle);
                            messageEvent.setImageTitles(imageTitles);
                            EventBus.getDefault().post(messageEvent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onMyFailure(Throwable arg0) {
                    }
                });
    }

}
