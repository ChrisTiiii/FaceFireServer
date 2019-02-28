package com.example.juicekaaa.fireserver.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.juicekaaa.fireserver.R;
import com.example.juicekaaa.fireserver.api.FaceApi;
import com.example.juicekaaa.fireserver.db.DBManager;
import com.example.juicekaaa.fireserver.entity.Group;
import com.example.juicekaaa.fireserver.manager.FaceSDKManager;
import com.example.juicekaaa.fireserver.utils.GlobalSet;
import com.example.juicekaaa.fireserver.utils.PreferencesUtil;


import java.util.List;

public class OpenDoorActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnUserGroupManager;
    Button btnDistinguish;
    Button btnImage;
    Button btnActivation;
    Button btnReadCard;

    private AlertDialog alertDialog;
    private String[] items;
    static int COUNTS = 5;//点击次数
    static long DURATION = 3 * 1000;//规定有效时间
    long[] mHits = new long[COUNTS];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_door_main);

        btnUserGroupManager = (Button) findViewById(R.id.btnUserGroupManager);
        btnDistinguish = (Button) findViewById(R.id.btnDistinguish);
        btnImage = (Button) findViewById(R.id.btnImage);
        btnActivation = (Button) findViewById(R.id.btnActivation);
        btnReadCard = (Button) findViewById(R.id.btnReadCard);
        btnUserGroupManager.setOnClickListener(this);
        btnDistinguish.setOnClickListener(this);
        btnImage.setOnClickListener(this);
        btnActivation.setOnClickListener(this);
        btnReadCard.setOnClickListener(this);


        PreferencesUtil.initPrefs(this);
        // 使用人脸1：n时使用
        DBManager.getInstance().init(this);
        //单目RGB活体, 请选用普通USB摄像头
        PreferencesUtil.putInt(GlobalSet.TYPE_LIVENSS, GlobalSet.TYPE_RGB_LIVENSS);
        //生活照
        PreferencesUtil.putInt(GlobalSet.TYPE_MODEL, GlobalSet.RECOGNIZE_LIVE);
        FaceSDKManager.getInstance().init(this, new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                toast("开始初始化SDK");
            }

            @Override
            public void initSuccess() {
                toast("SDK初始化成功");
            }

            @Override
            public void initFail(int errorCode, String msg) {
                toast("SDK初始化失败:" + msg);
            }
        });

        //强制加入一个分组
        List<Group> groupList = FaceApi.getInstance().getGroupList(0, 1000);
        if (groupList.size() < 1) {
            Group group = new Group();
            group.setGroupId("SLKJ");
            FaceApi.getInstance().groupAdd(group);
        }

    }

    private Handler handler = new Handler(Looper.getMainLooper());

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(OpenDoorActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnActivation) {
            fiveClick(1);
        }
        if (FaceSDKManager.getInstance().initStatus() != FaceSDKManager.SDK_SUCCESS) {
            toast("设备还未激活，请先激活");
            return;
        }
        if (v == btnImage) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager
                    .PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
                return;
            }
            Intent intent = new Intent(OpenDoorActivity.this, RgbVideoMatchImageActivity.class);
            startActivity(intent);
        } else if (v == btnDistinguish) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager
                    .PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
                return;
            }
            showSingleAlertDialog();
        } else if (v == btnUserGroupManager) {
            fiveClick(2);
        } else if (v == btnReadCard) {
            Intent intent = new Intent(this, IDcardRegActivity.class);
            startActivity(intent);
        }

    }

    public void showSingleAlertDialog() {
        List<Group> groupList = FaceApi.getInstance().getGroupList(0, 1000);
        if (groupList.size() <= 0) {
            Toast.makeText(this, "还没有分组，请创建分组并添加用户", Toast.LENGTH_SHORT).show();
            return;
        }
        items = new String[groupList.size()];
        for (int i = 0; i < groupList.size(); i++) {
            Group group = groupList.get(i);
            items[i] = group.getGroupId();
        }

/*        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择分组groupID");
        alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int index) {
                Toast.makeText(OpenDoorActivity.this, "当前活体策略：单目RGB活体", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(OpenDoorActivity.this, RgbVideoIdentityActivity.class);
                intent.putExtra("group_id", items[index]);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

        alertDialog = alertBuilder.create();
        alertDialog.show();*/

        Intent intent = new Intent(OpenDoorActivity.this, RgbVideoIdentityActivity.class);
        intent.putExtra("group_id", items[0]);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * 连续点五次
     */
    public void fiveClick(int type) {

        switch (type) {
            case 1://设备激活
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    System.out.println("您已在[" + DURATION + "]ms内连续点击【" + mHits.length + "】次了！！！");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                            .PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                        return;
                    }
                    FaceSDKManager.getInstance().showActivation(new FaceSDKManager.SdkInitListener() {
                        @Override
                        public void initStart() {
                            toast("开始初始化SDK");
                        }

                        @Override
                        public void initSuccess() {
                            toast("SDK初始化成功");
                        }

                        @Override
                        public void initFail(int errorCode, String msg) {
                            toast("SDK初始化失败:" + msg);
                        }
                    });
                    return;
                }
                break;
            case 2://人员管理
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    System.out.println("您已在[" + DURATION + "]ms内连续点击【" + mHits.length + "】次了！！！");
                    Intent intent = new Intent(this, UserGroupManagerActivity.class);
                    startActivity(intent);
                }

                break;
        }

    }
}
