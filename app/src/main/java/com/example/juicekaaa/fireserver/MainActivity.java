package com.example.juicekaaa.fireserver;

import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

//import com.bjw.bean.ComBean;
//import com.bjw.utils.FuncUtil;
//import com.bjw.utils.SerialHelper;
import com.example.juicekaaa.fireserver.broadcast.Receiver;
import com.example.juicekaaa.fireserver.net.Advertisement;
import com.example.juicekaaa.fireserver.service.MyService;
import com.example.juicekaaa.fireserver.tcp.TCPManager;
import com.example.juicekaaa.fireserver.ui.OpenDoorActivity;
import com.example.juicekaaa.fireserver.utils.FullVideoView;
import com.example.juicekaaa.fireserver.utils.GlideImageLoader;
import com.example.juicekaaa.fireserver.utils.MessageEvent;
import com.example.juicekaaa.fireserver.utils.PayPasswordView;
import com.example.juicekaaa.fireserver.utils.SosDialog;
import com.example.juicekaaa.fireserver.utils.VideoUtil;
import com.example.juicekaaa.fireserver.utils.SerialUtils;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements OnBannerListener {

    @BindView(R.id.open_door)
    LinearLayout openDoor;
    private VideoUtil videoUtil;

    @BindView(R.id.video)
    FullVideoView video;
    @BindView(R.id.banner1)
    Banner bannerone;
    @BindView(R.id.banner2)
    Banner bannertwo;
    @BindView(R.id.smallprogram)
    LinearLayout smallprogram;
    @BindView(R.id.signout)
    LinearLayout signout;


    private Receiver receivera;
    private Receiver receiverb;
    private Receiver receiverc;
    private Receiver receiverd;

    private BottomSheetDialog bottomSheetDialog;
    private SerialUtils serial = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐藏虚拟按键
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        hideBottomUIMenu();
        videoUtil = new VideoUtil(this, video);
        Intent startIntent = new Intent(this, MyService.class);
        startService(startIntent);

        initView();
        //广播注册
        RegisteredBroadcasting();
    }


    /**
     * 播放视频广告
     */
    @Override
    protected void onResume() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //加载图片
//                Advertisement.addImage();
            }
        }).start();
        //加载本地視頻
        videoPlayback();
        super.onResume();
    }

    /**
     * 初始化
     */
    private void initView() {
        serial = new SerialUtils();
        try {
            serial.openSerialPort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //触发接收数据接口(不可以删，删掉你啥数据都接收不到了)
        TCPManager.getInstance().setOnReceiveDataListener(new TCPManager.OnReceiveDataListener() {
            @Override
            public void onReceiveData(String str) {
//                System.out.println("accept2:" + str);
            }
        });

//        //设备唯一标识（极光推送）
//        Context context = getWindow().getContext();
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        @SuppressLint("MissingPermission") String imei = tm.getDeviceId();
//        //绑定唯一标识
//        JPushInterface.setAlias(OpenDoorActivity.this, 1, imei);
    }


    /**
     * 接收EventBus返回数据
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void backData(MessageEvent messageEvent) {
        switch (messageEvent.getTAG()) {
            case MyApplication.TCP_BACK_DATA:
                String order = messageEvent.getMessage();
                order = order.replaceAll(" ", "");
                if (serial.isOpen()) {
                    Toast.makeText(this, "开门成功", Toast.LENGTH_SHORT).show();
                    serial.sendHex(order);
                } else {
                    Toast.makeText(this, "串口没打开", Toast.LENGTH_SHORT).show();
                }
                break;
            case MyApplication.MESSAGE:// 关闭提示框，播放本地视频
                videoUtil.setVideo();
                break;
            case MyApplication.MESSAGE_UPDATE://视频下载
                Advertisement.downLoad(MainActivity.this);
                break;
            case MyApplication.MESSAGE_OUT://退出
                finish();
                break;
            case MyApplication.MESSAGE_DISMISS://返回
                bottomSheetDialog.dismiss();
                break;
            case MyApplication.MESSAGE_BANNER://图片广告
                List<String> list_path = messageEvent.getListPath();
                List<String> list_paths = messageEvent.getListPaths();
                List<String> imageTitle = messageEvent.getImageTitle();
                List<String> imageTitles = messageEvent.getImageTitles();
                bannerone.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                bannerone.setImageLoader(new GlideImageLoader());
                bannerone.setBannerAnimation(Transformer.Default);
                bannerone.setDelayTime(3000);
                bannerone.setBannerTitles(imageTitle);
                bannerone.isAutoPlay(true);
                bannerone.setIndicatorGravity(BannerConfig.CENTER);
                bannerone.setImages(list_path).setOnBannerListener(MainActivity.this).start();
                bannertwo.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                bannertwo.setImageLoader(new GlideImageLoader());
                bannertwo.setBannerAnimation(Transformer.Default);
                bannertwo.setDelayTime(3000);
                bannertwo.setBannerTitles(imageTitles);
                bannertwo.isAutoPlay(true);
                bannertwo.setIndicatorGravity(BannerConfig.CENTER);
                bannertwo.setImages(list_paths).setOnBannerListener(MainActivity.this).start();
                break;

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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //如果不存在, 提示下载 获取视频路径；
                    Advertisement.downLoad(MainActivity.this);
                }
            }).start();
        }
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


    //广播注册
    private void RegisteredBroadcasting() {
        //视频更新广播注册
        receivera = new Receiver();
        IntentFilter intenta = new IntentFilter();
        intenta.addAction(MyApplication.BROADCAST_ACTION_DISC); // 只有持有相同的action的接受者才能接收此广
        registerReceiver(receivera, intenta, MyApplication.BROADCAST_PERMISSION_DISC, null);
        //退出通知广播注册
        receiverb = new Receiver();
        IntentFilter intentb = new IntentFilter();
        intentb.addAction(MyApplication.BROADCAST_ACTION_PASS); // 只有持有相同的action的接受者才能接收此广
        registerReceiver(receiverb, intentb, MyApplication.BROADCAST_PASS_DISC, null);
        //返回通知广播注册
        receiverc = new Receiver();
        IntentFilter intentc = new IntentFilter();
        intentc.addAction(MyApplication.BROADCAST_ACTION_DISMISS); // 只有持有相同的action的接受者才能接收此广
        registerReceiver(receiverc, intentc, MyApplication.BROADCAST_DISMISS_DISC, null);
        //视频下载完播放通知广播注册
        receiverd = new Receiver();
        IntentFilter intentd = new IntentFilter();
        intentd.addAction(MyApplication.BROADCAST_ACTION_VIDEO); // 只有持有相同的action的接受者才能接收此广
        registerReceiver(receiverd, intentd, MyApplication.BROADCAST_VIDEO_DISC, null);

    }


    //密码输入退出程序
    private void openPayPasswordDialog() {
        PayPasswordView payPasswordView = new PayPasswordView(this);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(payPasswordView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.show();
    }


    @OnClick({R.id.smallprogram, R.id.signout, R.id.open_door})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.smallprogram:
                SosDialog sosDialog = new SosDialog(MainActivity.this);
                sosDialog.show();
                break;
            case R.id.signout:
                openPayPasswordDialog();
                break;
            case R.id.open_door:
                Intent intent = new Intent(this, OpenDoorActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 轮播监听
     */
    @Override
    public void OnBannerClick(int position) {
        //Toast.makeText(this, "你点了第" + (position + 1) + "张轮播图", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serial.closeSerialPort();//关闭串口
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        unregisterReceiver(receivera);
        unregisterReceiver(receiverb);
        unregisterReceiver(receiverc);
        unregisterReceiver(receiverd);
    }


}