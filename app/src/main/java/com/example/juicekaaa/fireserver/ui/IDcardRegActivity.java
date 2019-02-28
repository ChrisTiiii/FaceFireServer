package com.example.juicekaaa.fireserver.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.POSD.controllers.IDcardController;
import com.example.juicekaaa.fireserver.R;
import com.example.juicekaaa.fireserver.api.FaceApi;
import com.example.juicekaaa.fireserver.db.DBManager;
import com.example.juicekaaa.fireserver.entity.ARGBImg;
import com.example.juicekaaa.fireserver.entity.Feature;
import com.example.juicekaaa.fireserver.entity.Group;
import com.example.juicekaaa.fireserver.entity.User;
import com.example.juicekaaa.fireserver.face.FaceDetectManager;
import com.example.juicekaaa.fireserver.utils.FeatureUtils;
import com.example.juicekaaa.fireserver.utils.GlobalSet;
import com.example.juicekaaa.fireserver.utils.LogUtil;
import com.example.juicekaaa.fireserver.utils.PreferencesUtil;
import com.synjones.idcard.IDCard;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class IDcardRegActivity extends AppCompatActivity implements
    View.OnClickListener  {

    private static final String TAG = IDcardRegActivity.class
            .getSimpleName();
    private static final int READ_ONCE_DONE = 0x01;

    private IDcardController controller;
    private IDCard idcard = null;
    private Bitmap bmp;
    private static Handler mHandler = null;
    private ReadCardThread ReadCardThreadhandler;

    private TextView tvTime;
    private TextView tvCount;
    private TextView tvSuccessCount;
    private TextView tvFailCount;

    private Button buttonReadCard;
    private Button BtnReadOnce;
    private Button buttonExit;
    private long StartTime;
    private boolean reading = false;
    private long ReadCount;
    private long SuccessCount;
    private long FailCount;
    private long eclipseTime;
    private boolean mIsShowFinish = false;

    private List<String> groupIds = new ArrayList<>();
    private String groupId = "";
    private Spinner groupIdSpinner;
    // 从相机识别时使用。
    private FaceDetectManager detectManager;
    // 注册时使用人脸图片路径。
    private String faceImagePath;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int port = 1;
        if (intent.hasExtra("PROT")) {
            port = intent.getIntExtra("PROT", 1);
        }
        controller = IDcardController.getInstance();
        controller.openIDcardController(this, port);

        setContentView(R.layout.activity_idcard_reg);

        tvCount = (TextView) findViewById(R.id.tvCount);
        tvSuccessCount = (TextView) findViewById(R.id.tvSuccessCount);
        tvFailCount = (TextView) findViewById(R.id.tvFailCount);
        tvTime = (TextView) findViewById(R.id.tvTime);
        buttonReadCard = (Button) findViewById(R.id.buttonReadCard);
        BtnReadOnce = (Button) findViewById(R.id.btnReadOnce);
        buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonReadCard.setOnClickListener(this);
        BtnReadOnce.setOnClickListener(this);
        buttonExit.setOnClickListener(this);

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case READ_ONCE_DONE:
                        mIsShowFinish = true;
                        showIDcardInfo();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        init();


    }

    private void init() {
        List<Group> groupList = DBManager.getInstance().queryGroups(0, 1000);
        for (Group group : groupList) {
            groupIds.add(group.getGroupId());
        }
        if (groupIds.size() > 0) {
            groupId = groupIds.get(0);
        }
    }

    public void onResume() {
        super.onResume();

        //自行连续读卡
        if (!reading) {
            ReadCount = 0;
            SuccessCount = 0;
            FailCount = 0;
            StartTime = 0;
            reading = true;
            mIsShowFinish = false;
            ReadCardThreadhandler = new ReadCardThread();
            ReadCardThreadhandler.start();
        } else {
            if (ReadCardThreadhandler != null) {
                ReadCardThreadhandler.stopRead();
                ReadCardThreadhandler = null;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onDestroy() {
        if (ReadCardThreadhandler != null) {
            ReadCardThreadhandler.stopRead();
            ReadCardThreadhandler = null;
        }
        if (controller != null) {
            // reader.EnterSavePowerMode();
            controller.closeIDcardController();
            controller = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, "on pause");

        if (ReadCardThreadhandler != null) {
            ReadCardThreadhandler.stopRead();
            ReadCardThreadhandler = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (ReadCardThreadhandler != null) {
                ReadCardThreadhandler.stopRead();
                ReadCardThreadhandler = null;
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonReadCard:
                if (!reading) {
                    ReadCount = 0;
                    SuccessCount = 0;
                    FailCount = 0;
                    StartTime = 0;
                    reading = true;
                    mIsShowFinish = false;
                    buttonReadCard.setText(getResources().getString(
                            R.string.idcard_controller_btn_stop));

                    ReadCardThreadhandler = new ReadCardThread();
                    ReadCardThreadhandler.start();
                } else {
                    if (ReadCardThreadhandler != null) {
                        ReadCardThreadhandler.stopRead();
                        ReadCardThreadhandler = null;
                    }
                    buttonReadCard.setText(getResources().getString(
                            R.string.idcard_controller_btn_readcard_2));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                break;

            case R.id.btnReadOnce:
                StartTime = System.currentTimeMillis();
                // 使用AsyncTask异步运行，防止ANR
                new RetrieveDataTask().execute();
                break;

            case R.id.buttonExit:
                if (ReadCardThreadhandler != null) {
                    ReadCardThreadhandler.stopRead();
                    ReadCardThreadhandler = null;
                }

                if (controller != null) {
                    // reader.EnterSavePowerMode();
                    controller.closeIDcardController();
                    controller = null;
                }
                IDcardRegActivity.this.finish();
                break;

            default:
                break;
        }
    }


    private void showIDcardInfo() {
        TextView tv;
        ReadCount++;
        LogUtil.d(TAG, "showIDcardInfo idcard: " + idcard);
        if (idcard != null) {
            SuccessCount++;
            tv = (TextView) findViewById(R.id.textViewName);
            tv.setText(getString(R.string.idcard_controller_tv_name)
                    + idcard.getName());
            tv = (TextView) findViewById(R.id.textViewSex);
            tv.setText(getString(R.string.idcard_controller_tv_sex)
                    + idcard.getSex());
            tv = (TextView) findViewById(R.id.textViewNation);
            tv.setText(getString(R.string.idcard_controller_tv_ethnic)
                    + idcard.getNation());
            tv = (TextView) findViewById(R.id.textViewBirthday);
            tv.setText(getString(R.string.idcard_controller_tv_birth)
                    + idcard.getBirthday().substring(0, 4) + "年"
                    + idcard.getBirthday().substring(4, 6) + "月"
                    + idcard.getBirthday().substring(6, 8) + "日");
            tv = (TextView) findViewById(R.id.textViewAddress);
            tv.setText(getString(R.string.idcard_controller_tv_address)
                    + idcard.getAddress());
            tv = (TextView) findViewById(R.id.textViewPIDNo);
            tv.setText(getString(R.string.idcard_controller_tv_idnumber)
                    + idcard.getIDCardNo());
            tv = (TextView) findViewById(R.id.textViewGrantDept);
            tv.setText(getString(R.string.idcard_controller_tv_authority)
                    + idcard.getGrantDept());
            tv = (TextView) findViewById(R.id.textViewUserLife);
            tv.setText(getString(R.string.idcard_controller_tv_period)
                    + idcard.getUserLifeBegin() + "-" + idcard.getUserLifeEnd());
            tv = (TextView) findViewById(R.id.textViewStatus);
            tv.setText(getString(R.string.sdtstatus));

            LogUtil.d(TAG, "指位信息:" + idcard.getFpName());
            ImageView imageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);

            try {
                bmp = idcard.getPhoto();
                if (bmp != null) {
                    imageViewPhoto.setImageBitmap(bmp);
                   saveBitmap(bmp);
                    String filePath = "/sdcard/faces/" + idcard.getIDCardNo() + "_" + idcard.getName() + ".jpg";

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    register(filePath);

                } else {
                    Resources res = getResources();
                    bmp = BitmapFactory.decodeResource(res, R.drawable.face);
                    imageViewPhoto.setImageBitmap(bmp);
                    LogUtil.d(TAG, "状态：照片解码错");
                    // imageViewPhoto.setImageResource(R.drawable.photo);
                    // tvMessage.setText("状态：照片解码错,错误号:" + result);
                }
                LogUtil.d(TAG, "decode wlt finish");
                System.gc();
//                Intent intent = new Intent(IDcardRegActivity.this,SelectActivity.class);
//                startActivity(intent);
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 1000, 1000);

            } catch (Exception ioe) {
                ioe.printStackTrace();
                LogUtil.d(TAG, "photo display error:" + ioe.getMessage());
                // tvMessage.setText("状态：照片显示错" + ioe.getMessage());
            }
        } else {
            FailCount++;
            tv = (TextView) findViewById(R.id.textViewName);
            tv.setText(getString(R.string.idcard_controller_tv_name));
            tv = (TextView) findViewById(R.id.textViewSex);
            tv.setText(getString(R.string.idcard_controller_tv_sex));
            tv = (TextView) findViewById(R.id.textViewNation);
            tv.setText(getString(R.string.idcard_controller_tv_ethnic));
            tv = (TextView) findViewById(R.id.textViewBirthday);
            tv.setText(getString(R.string.idcard_controller_tv_birth));
            tv = (TextView) findViewById(R.id.textViewAddress);
            tv.setText(getString(R.string.idcard_controller_tv_address));
            tv = (TextView) findViewById(R.id.textViewPIDNo);
            tv.setText(getString(R.string.idcard_controller_tv_idnumber));
            tv = (TextView) findViewById(R.id.textViewGrantDept);
            tv.setText(getString(R.string.idcard_controller_tv_authority));
            tv = (TextView) findViewById(R.id.textViewUserLife);
            tv.setText(getString(R.string.idcard_controller_tv_period));
            tv = (TextView) findViewById(R.id.textViewStatus);
            tv.setText(getString(R.string.sdtstatus) + " "
                    + Integer.toHexString(IDCard.SW1) + " "
                    + Integer.toHexString(IDCard.SW2) + " "
                    + Integer.toHexString(IDCard.SW3));
            ImageView imageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);
            imageViewPhoto.setImageResource(R.drawable.face);
        }

        tvCount.setText(getString(R.string.idcard_count) + ReadCount);
        tvFailCount
                .setText(getString(R.string.idcard_count_failed) + FailCount);
        tvSuccessCount.setText(getString(R.string.idcard_count_success)
                + SuccessCount);
        eclipseTime = (System.currentTimeMillis() - StartTime) / 1000;
        tvTime.setText("time：" + eclipseTime);
    }

    public void saveBitmap(Bitmap bitmap) {
        Log.e(TAG, "保存图片");
        File f = new File("/sdcard/faces/", idcard.getIDCardNo() + "_" + idcard.getName() + ".jpg");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

            Uri uri = Uri.fromFile(new File("/sdcard/faces/"));
            intent.setData(uri);
            getApplicationContext().sendBroadcast(intent);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            // stringBuilder.append("0x");
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }

            stringBuilder.append(hv.toUpperCase());
            // stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    class ReadCardThread extends Thread {

        @Override
        public void run() {
            Looper.prepare();
            StartTime = System.currentTimeMillis();
            long oldtime = 0;

            while (reading) {
                oldtime = System.currentTimeMillis();
                long n_time = oldtime;

                idcard = controller.readIDcardController();
                LogUtil.d(TAG, "ReadCardThread working");

                Message m = new Message();
                m.what = READ_ONCE_DONE;
                mHandler.sendMessage(m);

                if (idcard == null)
                    continue;
                mIsShowFinish = false;
                while (mIsShowFinish == false
                        && System.currentTimeMillis() - oldtime < 2000)
                    ;
            }
        }

        public void stopRead() {
            reading = false;
            mIsShowFinish = true;
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <功能描述> 异步收发数据，解析数据，更新UI，防止ANR
     *
     * @author Administrator
     */
    private class RetrieveDataTask extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // 运行在子线程
            idcard = controller.readIDcardController();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // 运行在主线程，更新UI
            showIDcardInfo();
        }
    }


    private void register(final String filePath) {

        final String username = idcard.getName();

        if (TextUtils.isEmpty(groupId)) {
            Toast.makeText(IDcardRegActivity.this, "分组groupId为空", Toast.LENGTH_SHORT).show();
            return;
        }
        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         *
         */
//        final String uid = UUID.randomUUID().toString();
        final String uid = idcard.getIDCardNo() + "_" + idcard.getName();
        final File file = new File(filePath);

        if (DBManager.getInstance().queryUser(groupId, uid) == null) {
            final User user = new User();
            user.setUserId(uid);
            user.setUserInfo(username);
            user.setGroupId(groupId);

            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    ARGBImg argbImg = FeatureUtils.getARGBImgFromPath(filePath);
                    byte[] bytes = new byte[512];
                    float ret = 0;
                    int type = PreferencesUtil.getInt(GlobalSet.TYPE_MODEL, GlobalSet.RECOGNIZE_LIVE);
                    if (type == GlobalSet.RECOGNIZE_LIVE) {
                        ret = FaceApi.getInstance().getFeature(argbImg, bytes);
                    } else if (type == GlobalSet.RECOGNIZE_ID_PHOTO) {
                        ret = FaceApi.getInstance().getFeatureForIDPhoto(argbImg, bytes);
                    }
                    if (ret == -1) {
                        toast("人脸太小（必须打于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
                    } else if (ret != -1) {
                        Feature feature = new Feature();
                        feature.setGroupId(groupId);
                        feature.setUserId(uid);
                        feature.setFeature(bytes);
                        feature.setImageName(file.getName());
                        user.getFeatureList().add(feature);

                        if (FaceApi.getInstance().userAdd(user)) {
                            toast("注册成功");
                            finish();
                        } else {
                            toast("注册失败");
                        }

                    } else {
                        toast("抽取特征失败");
                    }
                }
            });
        }
    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(IDcardRegActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper());
}
