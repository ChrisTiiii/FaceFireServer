package com.example.juicekaaa.fireserver.ui;/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.juicekaaa.fireserver.utils.ImageFrame;
import com.example.juicekaaa.fireserver.R;
import com.example.juicekaaa.fireserver.api.FaceApi;
import com.example.juicekaaa.fireserver.db.DBManager;
import com.example.juicekaaa.fireserver.entity.ARGBImg;
import com.example.juicekaaa.fireserver.entity.Feature;
import com.example.juicekaaa.fireserver.entity.Group;
import com.example.juicekaaa.fireserver.entity.User;
import com.example.juicekaaa.fireserver.face.FaceCropper;
import com.example.juicekaaa.fireserver.face.FaceDetectManager;

import com.baidu.idl.facesdk.model.FaceInfo;
import com.example.juicekaaa.fireserver.face.FileImageSource;
import com.example.juicekaaa.fireserver.utils.FeatureUtils;
import com.example.juicekaaa.fireserver.utils.FileUitls;
import com.example.juicekaaa.fireserver.utils.GlobalSet;
import com.example.juicekaaa.fireserver.utils.ImageUtils;
import com.example.juicekaaa.fireserver.utils.PreferencesUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;


/**
 * @Time: 2018/11/12
 * @Author: v_chaixiaogang
 * @Description: 该类提供人脸注册功能，注册的人脸可以通个自动检测和选自相册两种方式获取。
 */

public class RegActivity extends Activity implements View.OnClickListener {

    public static final int SOURCE_REG = 1;
    private static final int REQUEST_CODE_PICK_IMAGE = 1000;
    private static final int REQUEST_CODE_AUTO_DETECT = 100;
    private EditText usernameEt;
    // private EditText groupIdEt;
    private Spinner groupIdSpinner;
    private ImageView avatarIv;
    private Button autoDetectBtn;
    private Button fromAlbumButton;
    private Button submitButton;

    // 注册时使用人脸图片路径。
    private String faceImagePath;

    // 从相机识别时使用。
    private FaceDetectManager detectManager;
    private List<String> groupIds = new ArrayList<>();
    private String groupId = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detectManager = new FaceDetectManager(getApplicationContext());
        setContentView(R.layout.activity_reg);

        usernameEt = (EditText) findViewById(R.id.username_et);
        // groupIdEt = (EditText) findViewById(R.id.group_id_et);
        groupIdSpinner = (Spinner) findViewById(R.id.spinner);
        avatarIv = (ImageView) findViewById(R.id.avatar_iv);
        autoDetectBtn = (Button) findViewById(R.id.auto_detect_btn);
        fromAlbumButton = (Button) findViewById(R.id.pick_from_album_btn);
        submitButton = (Button) findViewById(R.id.submit_btn);
        submitButton.setVisibility(View.GONE);

        autoDetectBtn.setOnClickListener(this);
        fromAlbumButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);

        groupIdSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (arg2 < groupIds.size()) {
                    groupId = groupIds.get(arg2);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

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
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, groupIds);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupIdSpinner.setAdapter(arrayAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v == autoDetectBtn) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest
                    .permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, 100);
                return;
            }
            avatarIv.setImageResource(R.mipmap.avatar);
            faceImagePath = null;
            int type = PreferencesUtil.getInt(GlobalSet.TYPE_MODEL, GlobalSet.RECOGNIZE_LIVE);
            if (type == GlobalSet.TYPE_NO_LIVENSS || type == GlobalSet.TYPE_RGB_LIVENSS) {
                Intent intent = new Intent(RegActivity.this, RgbDetectActivity.class);
                intent.putExtra("source", SOURCE_REG);
                startActivityForResult(intent, REQUEST_CODE_AUTO_DETECT);
            }
        } else if (v == fromAlbumButton) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        100);
                return;
            }
            avatarIv.setImageResource(R.mipmap.avatar);
            faceImagePath = null;
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);

        } else if (v == submitButton) {
            register(faceImagePath);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AUTO_DETECT && data != null) {
            faceImagePath = data.getStringExtra("file_path");
            Bitmap bitmap = BitmapFactory.decodeFile(faceImagePath);
            avatarIv.setImageBitmap(bitmap);
            submitButton.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
//            String filePath = getRealPathFromURI(uri);
                String filePath = imageUriToFile(uri);
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                File faceDir = FileUitls.getFaceDirectory();
                String imageName = UUID.randomUUID().toString();
                File file = new File(faceDir, imageName);
                // 压缩人脸图片至300 * 300，减少网络传输时间
                ImageUtils.resize(bitmap, file, 900, 900);
                filePath = file.getAbsolutePath();
                if ((bitmap.getWidth() * bitmap.getHeight()) > GlobalSet.pictureSize) {
                    Toast.makeText(RegActivity.this, "图片尺寸超过了限制",
                            Toast.LENGTH_SHORT).show();
                } else {
                    detect(filePath);
                }
            }
        }

    }


    // 从相册检测。
    private void detect(final String filePath) {

        FileImageSource fileImageSource = new FileImageSource();
        fileImageSource.setFilePath(filePath);
        detectManager.setImageSource(fileImageSource);
        detectManager.setUseDetect(true);
        detectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(FaceInfo[] faces, ImageFrame frame) {
                if (faces != null && faces.length > 0) {
                    final Bitmap cropBitmap = FaceCropper.getFace(frame.getArgb(), faces[0], frame.getWidth());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            avatarIv.setImageBitmap(cropBitmap);
                        }
                    });

                    // File file = File.createTempFile(UUID.randomUUID().toString() + "", ".jpg");
                    File faceDir = FileUitls.getFaceDirectory();
                    if (faceDir != null) {
                        String imageName = UUID.randomUUID().toString();
                        File file = new File(faceDir, imageName);
                        // 压缩人脸图片至300 * 300，减少网络传输时间
                        ImageUtils.resize(cropBitmap, file, 300, 300);
                        RegActivity.this.faceImagePath = file.getAbsolutePath();
                        submitButton.setVisibility(View.VISIBLE);
                    } else {
                        toast("注册人脸目录未找到");
                    }
                } else {
                    toast("未检测到人脸，可能原因：人脸太小（必须大于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
                }
            }
        });
        detectManager.start();
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }


    public String imageUriToFile(final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA},
                    null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    private File target;

    private void register(final String filePath) {

        final String username = usernameEt.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(RegActivity.this, "userid不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
//        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
//        Matcher matcher = pattern.matcher(username);
//        if (!matcher.matches()) {
//            Toast.makeText(RegActivity.this, "userid由数字、字母、下划线中的一个或者多个组合", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // final String groupId = groupIdEt.getText().toString().trim();
        if (TextUtils.isEmpty(groupId)) {
            Toast.makeText(RegActivity.this, "分组groupId为空", Toast.LENGTH_SHORT).show();
            return;
        }
//        matcher = pattern.matcher(username);
//        if (!matcher.matches()) {
//            Toast.makeText(RegActivity.this, "groupId由数字、字母、下划线中的一个或者多个组合", Toast.LENGTH_SHORT).show();
//            return;
//        }
        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         *
         */
        final String uid = UUID.randomUUID().toString();
        // String uid = 修改为自己用户系统中用户的id;

        if (TextUtils.isEmpty(faceImagePath)) {
            Toast.makeText(RegActivity.this, "人脸文件不存在", Toast.LENGTH_LONG).show();
            return;
        }
        final File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(RegActivity.this, "人脸文件不存在", Toast.LENGTH_LONG).show();
            return;
        }


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

//                   target = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/chaixiaogangFeature2");
//                   Utils.saveToFile(target,"feature2.txt",bytes);

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


    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper());


    // 写入文件
    private void writeFile(String filePath, String fileName) {
        File file = new File(filePath, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write("asdasdas".getBytes());
            Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("1507", "error: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
