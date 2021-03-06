package com.example.juicekaaa.fireserver.ui;/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juicekaaa.fireserver.utils.ImageFrame;
import com.example.juicekaaa.fireserver.R;
import com.example.juicekaaa.fireserver.api.FaceApi;
import com.example.juicekaaa.fireserver.entity.ARGBImg;
import com.example.juicekaaa.fireserver.face.CameraImageSource;
import com.example.juicekaaa.fireserver.face.FaceDetectManager;
import com.example.juicekaaa.fireserver.face.FaceFilter;
import com.example.juicekaaa.fireserver.face.PreviewView;

import com.baidu.idl.facesdk.model.FaceInfo;
import com.example.juicekaaa.fireserver.face.camera.CameraView;
import com.example.juicekaaa.fireserver.face.camera.ICameraControl;
import com.example.juicekaaa.fireserver.manager.FaceSDKManager;
import com.example.juicekaaa.fireserver.utils.FeatureUtils;
import com.example.juicekaaa.fireserver.utils.GlobalSet;
import com.example.juicekaaa.fireserver.utils.ImageUtils;
import com.example.juicekaaa.fireserver.utils.PreferencesUtil;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Time: 2018/11/12
 * @Author: v_chaixiaogang
 * @Description: RGB可见光视频VS图片
 */
public class RgbVideoMatchImageActivity extends Activity implements View.OnClickListener {

    private static final int PICK_PHOTO = 100;
    // 预览View;
    private PreviewView previewView;
    // textureView用于绘制人脸框等。
    private TextureView textureView;
    // 用于检测人脸。
    private FaceDetectManager faceDetectManager;

    private Button imageFeatureBtn;
    private TextView tipTv;
    private TextView detectDurationTv;
    private TextView rgbLivenssDurationTv;
    private TextView rgbLivenessScoreTv;
    private TextView matchScoreTv;
    private ImageView photoIv;

    // 为了方便调式。
    private ImageView testView;
    private Handler handler = new Handler();
    private byte[] photoFeature = new byte[512];
    private volatile boolean matching = false;
    private ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedio_match_image);
        findView();
        init();
        addListener();

    }

    private void findView() {
        testView = (ImageView) findViewById(R.id.test_view);
        previewView = (PreviewView) findViewById(R.id.preview_view);
        textureView = (TextureView) findViewById(R.id.texture_view);
        imageFeatureBtn = (Button) findViewById(R.id.image_feature_btn);
        tipTv = (TextView) findViewById(R.id.tip);

        detectDurationTv = (TextView) findViewById(R.id.detect_duration_tv);
        rgbLivenssDurationTv = (TextView) findViewById(R.id.rgb_liveness_duration_tv);
        rgbLivenessScoreTv = (TextView) findViewById(R.id.rgb_liveness_score_tv);
        matchScoreTv = (TextView) findViewById(R.id.match_score_tv);
        photoIv = (ImageView) findViewById(R.id.pick_from_album_iv);
    }

    private void init() {
        faceDetectManager = new FaceDetectManager(getApplicationContext());
        // 从系统相机获取图片帧。
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        cameraImageSource.getCameraControl().setPreferredPreviewSize(640, 480);

        // 设置预览
        cameraImageSource.setPreviewView(previewView);
        // 设置图片源
        faceDetectManager.setImageSource(cameraImageSource);

        textureView.setOpaque(false);
        // 不需要屏幕自动变黑。
        textureView.setKeepScreenOn(true);
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            // 相机横屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }

        setCameraType(cameraImageSource);
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        // TODO 选择使用前置摄像头
//         cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);

        // TODO 选择使用usb摄像头
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
//        // 如果不设置，人脸框会镜像，显示不准
        previewView.getTextureView().setScaleX(-1);

        // TODO 选择使用后置摄像头
/*        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
        previewView.getTextureView().setScaleX(-1);*/
    }

    private void addListener() {
        imageFeatureBtn.setOnClickListener(this);
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(FaceInfo[] infos, ImageFrame frame) {
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断
                final Bitmap bitmap =
                        Bitmap.createBitmap(frame.getArgb(), frame.getWidth(), frame.getHeight(),
                                Bitmap.Config.ARGB_8888);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        testView.setVisibility(View.VISIBLE);
                        testView.setImageBitmap(bitmap);
                    }
                });
                checkFace(infos, frame);
                showFrame(frame, infos);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开始检测
        faceDetectManager.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 结束检测。
        faceDetectManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceDetectManager.stop();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.image_feature_btn) {
            // 检测相册的图片人脸时不能同时检测视频流的人脸，视频流关闭检测
            faceDetectManager.setUseDetect(false);
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_PHOTO);
        }
    }

    private void checkFace(FaceInfo[] faceInfos, ImageFrame frame) {
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            String tip = filter(faceInfo, frame);
            displayTip(tip);
        } else {
            String tip = checkFaceCode(faceInfos);
            displayTip(tip);
        }
    }


    private String filter(FaceInfo faceInfo, ImageFrame imageFrame) {

        String tip = "";
        if (faceInfo.mConf < 0.6) {
            tip = "人脸置信度太低";
            return tip;
        }

        float[] headPose = faceInfo.headPose;
        if (Math.abs(headPose[0]) > 20 || Math.abs(headPose[1]) > 20 || Math.abs(headPose[2]) > 20) {
            tip = "人脸置角度太大，请正对屏幕";
            return tip;
        }

        int width = imageFrame.getWidth();
        int height = imageFrame.getHeight();
        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
        float ratio = (float) faceInfo.mWidth / (float) height;
        Log.i("liveness_ratio", "ratio=" + ratio);
        if (ratio > 0.6) {
            tip = "人脸离屏幕太近，请调整与屏幕的距离";
            return tip;
        } else if (ratio < 0.2) {
            tip = "人脸离屏幕太远，请调整与屏幕的距离";
            return tip;
        } else if (faceInfo.mCenter_x > width * 3 / 4) {
            tip = "人脸在屏幕中太靠右";
            return tip;
        } else if (faceInfo.mCenter_x < width / 4) {
            tip = "人脸在屏幕中太靠左";
            return tip;
        } else if (faceInfo.mCenter_y > height * 3 / 4) {
            tip = "人脸在屏幕中太靠下";
            return tip;
        } else if (faceInfo.mCenter_x < height / 4) {
            tip = "人脸在屏幕中太靠上";
            return tip;
        }

        int liveType = PreferencesUtil.getInt(GlobalSet.TYPE_LIVENSS, GlobalSet.TYPE_NO_LIVENSS);
        if (liveType == GlobalSet.TYPE_NO_LIVENSS) {
            asyncMath(photoFeature, faceInfo, imageFrame);
        } else if (liveType == GlobalSet.TYPE_RGB_LIVENSS) {
            float rgbLivenessScore = rgbLiveness(imageFrame, faceInfo);
            if (rgbLivenessScore > 0.9) {
                asyncMath(photoFeature, faceInfo, imageFrame);
            } else {
//                toast("rgb活体分数过低");
            }
        }


        return tip;
    }

    private String checkFaceCode(FaceInfo[] faceInfos) {
        String tip = "";
        if (faceInfos == null || faceInfos.length <= 0) {
            tip = "未检测到人脸";
        }
        return tip;
    }

    private float rgbLiveness(ImageFrame imageFrame, FaceInfo faceInfo) {

        long starttime = System.currentTimeMillis();
        final float rgbScore = FaceSDKManager.getInstance().getFaceLiveness().rgbLiveness(imageFrame.getArgb(), imageFrame
                .getWidth(), imageFrame.getHeight(), faceInfo.landmarks);
        final long duration = System.currentTimeMillis() - starttime;

        displayTip("RGB活体分数：" + rgbScore, rgbLivenessScoreTv);
        displayTip("RGB活体耗时：" + duration, rgbLivenssDurationTv);

        return rgbScore;
    }

    private void asyncMath(final byte[] photoFeature, final FaceInfo faceInfo, final ImageFrame imageFrame) {
        if (matching) {
            return;
        }
        es.submit(new Runnable() {
            @Override
            public void run() {
                match(photoFeature, faceInfo, imageFrame);
            }
        });
    }

    private void match(final byte[] photoFeature, FaceInfo faceInfo, ImageFrame imageFrame) {

        if (faceInfo == null) {
            return;
        }

        float raw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        //人脸的三个角度大于20不进行识别  角度越小，人脸越正，比对时分数越高
        if (raw > 20 || patch > 20 || roll > 20) {
            return;
        }

        matching = true;
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;
        int type = PreferencesUtil.getInt(GlobalSet.TYPE_MODEL, GlobalSet.RECOGNIZE_ID_PHOTO);
        float score = 0;
        if (type == GlobalSet.RECOGNIZE_LIVE) {
            score = FaceApi.getInstance().match(photoFeature, argb, rows, cols, landmarks);
        } else if (type == GlobalSet.RECOGNIZE_ID_PHOTO) {
            score = FaceApi.getInstance().matchIDPhoto(photoFeature, argb, rows, cols, landmarks);
        }
        matching = false;
        displayTip("比对得分：" + score, matchScoreTv);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == 0) {
            return;
        }

        if (requestCode == PICK_PHOTO && (data != null && data.getData() != null)) {
            Uri uri = ImageUtils.geturi(data, this);
            pickPhotoFeature(uri);
        }
    }

    private void pickPhotoFeature(final Uri imageUri) {
        faceDetectManager.setUseDetect(false);
//        Executors.newSingleThreadExecutor().submit(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });
        try {
            final Bitmap bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(imageUri));
            ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
            if ((argbImg.width * argbImg.height) > GlobalSet.pictureSize) {
                clearTip();
                toast("图片尺寸超过了限制");
                return;
            }
            int type = PreferencesUtil.getInt(GlobalSet.TYPE_MODEL,
                    GlobalSet.RECOGNIZE_LIVE);
            float ret = 0;
            if (type == GlobalSet.RECOGNIZE_LIVE) {
                ret = FaceApi.getInstance().getFeature(argbImg, photoFeature);
            } else if (type == GlobalSet.RECOGNIZE_ID_PHOTO) {
                ret = FaceApi.getInstance().getFeatureForIDPhoto(argbImg, photoFeature);
            }
            if (ret == -1) {
                clearTip();
                toast("未检测到人脸，可能原因：人脸太小（必须大于最小检测人脸minFaceSize）" +
                        "，或者人脸角度太大，人脸不是朝上");
            } else if (ret != 128) {
                clearTip();
                toast("抽取特征失败");
            } else if (ret == 128) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        photoIv.setVisibility(View.VISIBLE);
                        textureView.setVisibility(View.VISIBLE);
                    }
                });
                faceDetectManager.setUseDetect(true);

            } else {
                clearTip();
                toast("未检测到人脸");
            }
            Log.i("wtf", "photoFeature from image->" + ret);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    photoIv.setImageBitmap(bitmap);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void clearTip() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                photoIv.setVisibility(View.GONE);
                textureView.setVisibility(View.GONE);
                rgbLivenessScoreTv.setVisibility(View.GONE);
                rgbLivenssDurationTv.setVisibility(View.GONE);
                matchScoreTv.setVisibility(View.GONE);
                testView.setVisibility(View.GONE);
            }
        });
    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RgbVideoMatchImageActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tipTv.setText(tip);
            }
        });
    }

    private void displayTip(final String tip, final TextView textView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.VISIBLE);
                textView.setText(tip);
            }
        });
    }


    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
    }

    RectF rectF = new RectF();

    /**
     * 绘制人脸框。
     */
    private void showFrame(ImageFrame imageFrame, FaceInfo[] faceInfos) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        if (faceInfos == null || faceInfos.length == 0) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        FaceInfo faceInfo = faceInfos[0];

        rectF.set(getFaceRect(faceInfo, imageFrame));

        // 检测图片的坐标和显示的坐标不一样，需要转换。
        previewView.mapFromOriginalRect(rectF);

        float yaw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        if (yaw > 20 || patch > 20 || roll > 20) {
            // 不符合要求，绘制黄框
            paint.setColor(Color.YELLOW);

            String text = "请正视屏幕";
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);

        } else {
            // 符合检测要求，绘制绿框
            paint.setColor(Color.GREEN);
        }
        paint.setStyle(Paint.Style.STROKE);
        // 绘制框
        canvas.drawRect(rectF, paint);
        textureView.unlockCanvasAndPost(canvas);
    }

    /**
     * 绘制人脸框。
     *
     * @param model 追踪到的人脸
     */
    private void showFrame(FaceFilter.TrackedModel model) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            return;
        }
        // 清空canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (model != null) {
            model.getImageFrame().retain();
            rectF.set(model.getFaceRect());

            // 检测图片的坐标和显示的坐标不一样，需要转换。
            previewView.mapFromOriginalRect(rectF);
            if (model.meetCriteria()) {
                // 符合检测要求，绘制绿框
                paint.setColor(Color.GREEN);
            } else {
                // 不符合要求，绘制黄框
                paint.setColor(Color.YELLOW);

                String text = "请正视屏幕";
                float width = paint.measureText(text) + 50;
                float x = rectF.centerX() - width / 2;
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(text, x + 25, rectF.top - 20, paint);
                paint.setColor(Color.YELLOW);
            }
            paint.setStyle(Paint.Style.STROKE);
            // 绘制框
            canvas.drawRect(rectF, paint);
        }
        textureView.unlockCanvasAndPost(canvas);
    }

    /**
     * 获取人脸框区域。
     *
     * @return 人脸框区域
     */
    // TODO padding?
    public Rect getFaceRect(FaceInfo faceInfo, ImageFrame frame) {
        Rect rect = new Rect();
        int[] points = new int[8];
        faceInfo.getRectPoints(points);

        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];

        //            int width = (right - left) * 4 / 3;
        //            int height = (bottom - top) * 4 / 3;
        //
        //            left = getInfo().mCenter_x - width / 2;
        //            top = getInfo().mCenter_y - height / 2;
        //
        //            rect.top = top;
        //            rect.left = left;
        //            rect.right = left + width;
        //            rect.bottom = top + height;

        //            int width = (right - left) * 4 / 3;
        //            int height = (bottom - top) * 5 / 3;
        int width = (right - left);
        int height = (bottom - top);

        //            left = getInfo().mCenter_x - width / 2;
        //            top = getInfo().mCenter_y - height * 2 / 3;
        left = (int) (faceInfo.mCenter_x - width / 2);
        top = (int) (faceInfo.mCenter_y - height / 2);


        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width);
        rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);

        return rect;
    }

}
