package com.example.juicekaaa.fireserver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

import com.bjw.bean.ComBean;
import com.bjw.utils.FuncUtil;
import com.bjw.utils.SerialHelper;
import com.bumptech.glide.Glide;
import com.example.juicekaaa.fireserver.dialog.SosDialog;
import com.example.juicekaaa.fireserver.firebox.FireBox;
import com.example.juicekaaa.fireserver.net.NetCallBack;
import com.example.juicekaaa.fireserver.net.RequestUtils;
import com.example.juicekaaa.fireserver.net.URLs;
import com.example.juicekaaa.fireserver.tcp.TCPSocket;
import com.example.juicekaaa.fireserver.util.EncodingConversionTools;
import com.example.juicekaaa.fireserver.util.FullVideoView;
import com.example.juicekaaa.fireserver.util.GetMac;
import com.example.juicekaaa.fireserver.util.MessageEvent;
import com.example.juicekaaa.fireserver.util.PayPasswordView;
import com.loopj.android.http.RequestParams;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPortFinder;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.android.api.JPushInterface;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class MainActivity extends AppCompatActivity implements OnBannerListener {
    //    private static final int PORT = 12342;//接收客户端的监听端口
    private static TCPSocket tcpSocket;
    public String SERVICE_IP = "101.132.139.37";//10.101.208.78   10.101.80.134 10.101.80.100 10.101.208.157 10.101.208.157
    public int SERVICE_PORT = 23303;
    private Socket socket = new Socket();
    private String MAC = "";

    private ArrayList<String> list_path;
    private ArrayList<String> list_paths;
    private List<Integer> bannerList = new ArrayList();

    @BindView(R.id.video)
    FullVideoView video;
    @BindView(R.id.banner1)
    Banner banner1;
    @BindView(R.id.banner2)
    Banner banner2;

    private FireBox fireBox;
    public static final String SHEBEI_IP = "10.101.208.101";
    public static final String SHEBEI_PORT = "28327";
    private static final String CHUAN = "/dev/ttymxc2";
    private static final String BOTE = "9600";
    //        private static final String CHUAN = "/dev/ttyS0";
    private static final int TCP_BACK_DATA = 0x213;

    private SerialPortFinder serialPortFinder;
    private SerialHelper serialHelper;

    private ArrayList<String> imageTitle;
    private ArrayList<String> imageTitles;
    String path = null; // 下载地址
    public static final int MESSAGE = 345334;
    public static final int MESSAGE_UPDATE = 345333;
    public static final int MESSAGE_OUT = 756467;
    public static final int MESSAGE_DISMISS = 756468;

    private Receiver receiver;
    private Receivers receivers;
    private Receiverdismiss receiverdismiss;
    private static final String BROADCAST_PERMISSION_DISC = "com.permissions.MYF_BROADCAST";
    private static final String BROADCAST_ACTION_DISC = "com.permissions.myf_broadcast";
    private static final String BROADCAST_PASS_DISC = "com.permissions.MYP_BROADCAST";
    private static final String BROADCAST_ACTION_PASS = "com.permissions.myp_broadcast";
    private static final String BROADCAST_DISMISS_DISC = "com.permissions.MYD_BROADCAST";
    private static final String BROADCAST_ACTION_DISMISS = "com.permissions.myd_broadcast";
    private SweetAlertDialog pDialog;
    private LinearLayout smallprogram;
    private LinearLayout signout;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐藏虚拟按键
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        initView();
        initSeceive();
        hideBottomUIMenu();
    }

    //初始化TCP通讯
    private void initSeceive() {
        //用于接收命令
        tcpSocket = new TCPSocket(socket, SERVICE_IP, SERVICE_PORT, 2);
        tcpSocket.start();
        //用于发送心跳包
        TCPSocket sendHeart = new TCPSocket(EncodingConversionTools.HexString2Bytes(MAC));
//        tcpSocket.setPriority(Thread.NORM_PRIORITY + 3);
        sendHeart.start();

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void backData(MessageEvent messageEvent) {
        switch (messageEvent.getTAG()) {
            case TCP_BACK_DATA:
                String order = messageEvent.getMessage();
                order = order.replaceAll(" ", "");
                    Toast.makeText(MainActivity.this, "收到信息啦！" + order, Toast.LENGTH_LONG).show();
                if (serialHelper.isOpen()) {
                    Toast.makeText(this, "开门成功", Toast.LENGTH_LONG).show();
                    serialHelper.sendHex(order);
                } else
                    Toast.makeText(this, "串口没打开", Toast.LENGTH_LONG).show();
                break;

        }

    }

    private void initView() {
        smallprogram = findViewById(R.id.smallprogram);
        smallprogram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SosDialog sosDialog = new SosDialog(MainActivity.this);
                sosDialog.show();
            }
        });
        signout = findViewById(R.id.signout);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPayPasswordDialog();
            }
        });
        initData();
        //设备唯一标识
        Context context = getWindow().getContext();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imei = tm.getDeviceId();
        //绑定唯一标识
        JPushInterface.setAlias(MainActivity.this, 1, imei);
        //视频更新广播注册
        RegisteredBroadcasting();
        //加载图片
        addImage();
        //初始化串口
        initSerial();
    }

    @Override
    protected void onResume() {
        //加载本地視頻
        videoPlayback();
        super.onResume();
    }

    /**
     * 初始化串口
     */
    private void initSerial() {
        serialPortFinder = new SerialPortFinder();
        serialHelper = new SerialHelper() {
            @Override
            protected void onDataReceived(final ComBean comBean) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //接收设备回传数据
                        Toast.makeText(getBaseContext(), FuncUtil.ByteArrToHex(comBean.bRec), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

//        try {
////           设置串口信息
//            serialHelper.setBaudRate(BOTE);
//            serialHelper.setPort(CHUAN);
//            serialHelper.open();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


    /**
     * 设置视频参数
     */
    private void setVideo() {
        MediaController mediaController = new MediaController(this);
        mediaController.setVisibility(View.GONE);//隐藏进度条
        video.setMediaController(mediaController);
        File file = new File(Environment.getExternalStorageDirectory() + "/" + "FireVideo", "1542178640266.mp4");
        video.setVideoPath(file.getAbsolutePath());
        video.start();
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
            }
        });
    }


    //静态初始化数据
    void initData() {
        MAC = GetMac.getMacAddress().replaceAll(":", "");
        System.out.println("mac: " + MAC);
        socket = new Socket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialHelper.close();//关闭串口
        tcpSocket.stopSocket();//关闭tcp通讯
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

        unregisterReceiver(receiver);
        unregisterReceiver(receivers);
        unregisterReceiver(receiverdismiss);

    }

    //加载网络图片资源
    private void addImage() {
        list_path = new ArrayList<>();
        list_paths = new ArrayList<>();
        imageTitle = new ArrayList<>();
        imageTitles = new ArrayList<>();
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
                        System.out.println("数据请求成功" + result);
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
                            banner1.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                            banner1.setImageLoader(new MyLoader());
                            banner1.setBannerAnimation(Transformer.Default);
                            banner1.setDelayTime(3000);
                            banner1.setBannerTitles(imageTitle);
                            banner1.isAutoPlay(true);
                            banner1.setIndicatorGravity(BannerConfig.CENTER);
                            banner1.setImages(list_path).setOnBannerListener(MainActivity.this).start();

                            banner2.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                            banner2.setImageLoader(new MyLoader());
                            banner2.setBannerAnimation(Transformer.Default);
                            banner2.setDelayTime(3000);
                            banner2.setBannerTitles(imageTitles);
                            banner2.isAutoPlay(true);
                            banner2.setIndicatorGravity(BannerConfig.CENTER);
                            banner2.setImages(list_paths).setOnBannerListener(MainActivity.this).start();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onMyFailure(Throwable arg0) {
                    }
                });
    }

    /**
     * 轮播监听
     *
     * @param position
     */
    @Override
    public void OnBannerClick(int position) {
//        Toast.makeText(this, "你点了第" + (position + 1) + "张轮播图", Toast.LENGTH_SHORT).show();
    }

    /**
     * 网络加载图片
     * 使用了Glide图片加载框架
     */
    private class MyLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context.getApplicationContext()).load((String) path).into(imageView);
        }
    }

    //播放本地视频，判断本地是否存在视频，没有视频就下载视频
    private void videoPlayback() {
        // 创建文件夹，在存储卡下
        String dirName = "/sdcard/FireVideo/";
        File file = new File(dirName);
        // 文件夹不存在时创建
        if (!file.exists()) {
            file.mkdir();
        }
        // 下载后的文件名
        String fileName = dirName + "1542178640266.mp4";
        File file1 = new File(fileName);
        if (file1.exists()) {
            // 如果已经存在, 就不下载了, 去播放
            setVideo();
        } else {
            //如果不存在, 提示下载 获取视频路径；
            addVideo();
        }
    }

    // 下载具体操作
    private void DownLoad(String path) {
        try {
            //"http://10.101.80.113:8080/RootFile/Platform/20181114/1542178640266.mp4"
            URL url = new URL(path);
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
            int i = path.lastIndexOf("/"); // 取的最后一个斜杠后的字符串为名
            String fileName = dirName + path.substring(i, path.length());
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
            //下载完播放视频
            m_handler.sendEmptyMessage(MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //加载网络视频资源
    private void addVideo() {
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
                        pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                        pDialog.setTitleText("获取视频中......");
                        pDialog.setCancelable(false);
                        pDialog.show();
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
                                    path = URLs.HOST + record_url;
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DownLoad(path);
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

    //收到视频下载完的消息，播放视频
    @SuppressLint("HandlerLeak")
    private Handler m_handler = new Handler() {
        @SuppressWarnings("deprecation")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE:
                    // 关闭提示框，播放本地视频
                    pDialog.dismiss();
                    setVideo();
                    break;
                case MESSAGE_UPDATE:
                    //视频下载
                    addVideo();
                    break;
                case MESSAGE_OUT:
                    //退出
                    finish();
                case MESSAGE_DISMISS:
                    //返回
                    bottomSheetDialog.dismiss();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    //广播注册
    private void RegisteredBroadcasting() {
        //视频更新广播注册
        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION_DISC); // 只有持有相同的action的接受者才能接收此广
        registerReceiver(receiver, intentFilter, BROADCAST_PERMISSION_DISC, null);
        //退出事件广播注册
        receivers = new Receivers();
        IntentFilter intentFilters = new IntentFilter();
        intentFilters.addAction(BROADCAST_ACTION_PASS); // 只有持有相同的action的接受者才能接收此广
        registerReceiver(receivers, intentFilters, BROADCAST_PASS_DISC, null);
        //返回事件广播注册
        receiverdismiss = new Receiverdismiss();
        IntentFilter intentFilterdismiss = new IntentFilter();
        intentFilterdismiss.addAction(BROADCAST_ACTION_DISMISS); // 只有持有相同的action的接受者才能接收此广
        registerReceiver(receiverdismiss, intentFilterdismiss, BROADCAST_DISMISS_DISC, null);

    }


    // 视频更新广播通知处理
    public class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BROADCAST_ACTION_DISC)) {
                String str = intent.getStringExtra("str_test");
                m_handler.sendEmptyMessage(MESSAGE_UPDATE);
            }
        }
    }

    // 退出通知处理
    public class Receivers extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BROADCAST_ACTION_PASS)) {
                m_handler.sendEmptyMessage(MESSAGE_OUT);
            }
        }
    }

    // 返回通知处理
    public class Receiverdismiss extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BROADCAST_ACTION_DISMISS)) {
                m_handler.sendEmptyMessage(MESSAGE_DISMISS);
            }
        }
    }

    private void openPayPasswordDialog() {
        PayPasswordView payPasswordView = new PayPasswordView(this);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(payPasswordView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.show();
    }

    @Override
    public void onBackPressed() {
        return;//在按返回键时的操作
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}