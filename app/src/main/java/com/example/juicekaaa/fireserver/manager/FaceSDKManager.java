package com.example.juicekaaa.fireserver.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.baidu.idl.facesdk.FaceAuth;
import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.callback.Callback;
import com.example.juicekaaa.fireserver.callback.FaceCallback;
import com.example.juicekaaa.fireserver.ui.Activation;
import com.example.juicekaaa.fireserver.utils.PreferencesUtil;

public class FaceSDKManager {

    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_FAIL = 5;
    public static final int SDK_SUCCESS = 6;
    public static final String licenseFileName = "idl-license.face-android";
    private FaceDetector faceDetector;
    private FaceFeatures faceFeature;
    private FaceAttribute faceAttribute;
    private FaceLiveness faceLiveness;
    private FacefeaturesImage mFacefeaturesImage;
    private Context context;
    public static volatile int initStatus = SDK_UNACTIVATION;
    private Handler handler = new Handler(Looper.getMainLooper());

    private Activation activation;
    private FaceAuth mFaceAuth;

    private FaceEnvironment faceEnvironment;

    private FaceSDKManager() {
        faceDetector = new FaceDetector();
        faceFeature = new FaceFeatures();
        faceAttribute = new FaceAttribute();
        faceLiveness = new FaceLiveness();
        mFacefeaturesImage = new FacefeaturesImage();
        faceEnvironment = new FaceEnvironment();
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public int initStatus() {
        return initStatus;
    }

    public FaceDetector getFaceDetector() {
        return faceDetector;
    }

    public FaceFeatures getFaceFeature() {
        return faceFeature;
    }

    public FaceAttribute getFaceAttribute() {
        return faceAttribute;
    }

    public FaceLiveness getFaceLiveness() {
        return faceLiveness;
    }

    public FacefeaturesImage getFacefeaturesImage() {
        return mFacefeaturesImage;
    }

    /**
     * FaceSDK 初始化，用户可以根据自己的需求实例化FaceTracker 和 FaceRecognize
     *
     * @param context
     */
    public void init(final Context context, final SdkInitListener sdkInitListener) {
        this.context = context;
        PreferencesUtil.initPrefs(context.getApplicationContext());
        String licenseKey = PreferencesUtil.getString("activate_key", "");
        if (licenseKey.equals("") || TextUtils.isEmpty(licenseKey)) {
            showActivation(sdkInitListener);
            return;
        } else {
            if (sdkInitListener != null) {
                sdkInitListener.initStart();
            }
            Log.e("FaceSDK", "初始化授权");
            check(context, licenseKey, new FaceCallback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        Log.e("FaceSDK", "授权成功");
                        if (sdkInitListener != null) {
                            initStatus = SDK_SUCCESS;
                            sdkInitListener.initSuccess();
                            initModel();
                        }
                        return;
                    } else {
                        Log.e("FaceSDK", "授权失败:" + response);
                        if (sdkInitListener != null) {
                            sdkInitListener.initFail(code, "授权失败:" + response);
                        }
                        showActivation(sdkInitListener);
                    }
                }
            });
        }
    }

    private void initModel() {
        faceDetector.initModel(context, "detect_rgb_anakin_2.0.0.bin",
                "detect_nir_2.0.0.model",
                "align_2.0.0.anakin.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        toast(code, response);
                    }
                });
        faceDetector.initQuality(context, "blur_2.0.0.binary",
                "occlu_2.0.0.binary", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        toast(code, response);
                    }
                });
        faceDetector.loadConfig(getFaceEnvironmentConfig());
        faceFeature.initModel(context, "recognize_rgb_idcard_anakin_2.0.0.bin",
                "recognize_rgb_live_anakin_2.0.0.bin",
                "", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        toast(code, response);
                    }
                });
        faceLiveness.initModel(context, "liveness_rgb_anakin_2.0.0.bin",
                "liveness_nir_anakin_2.0.0.bin",
                "liveness_depth_anakin_2.0.0.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        toast(code, response);
                    }
                });
        faceAttribute.initModel(context, "attribute_anakin_2.0.0.bin",
                "emotion_anakin_2.0.0.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        toast(code, response);
                    }
                });
        mFacefeaturesImage.initModel(context, "detect_rgb_anakin_2.0.0.bin",
                "align_2.0.0.anakin.bin",
                "recognize_rgb_idcard_anakin_2.0.0.bin",
                "recognize_rgb_live_anakin_2.0.0.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        toast(code, response);
                    }
                });
    }

    public FaceEnvironment getFaceEnvironmentConfig() {
        faceEnvironment.setMinFaceSize(30);
        faceEnvironment.setMaxFaceSize(-1);
        faceEnvironment.setDetectInterval(1000);
        faceEnvironment.setTrackInterval(500);
        faceEnvironment.setNoFaceSize(0.5f);
        faceEnvironment.setPitch(30);
        faceEnvironment.setYaw(30);
        faceEnvironment.setRoll(30);
        faceEnvironment.setCheckBlur(false);
        faceEnvironment.setOcclusion(false);
        faceEnvironment.setIllumination(false);
        faceEnvironment.setDetectMethodType(FaceDetect.DetectType.DETECT_VIS);
        return faceEnvironment;
    }

    public void check(Context context, String licenseKey, final FaceCallback faceCallback) {
        mFaceAuth = new FaceAuth();
        mFaceAuth.setAnakinThreadsConfigure(4, 0);
        mFaceAuth.setActiveLog(FaceAuth.BDFaceLogInfo.BDFACE_LOG_ALL_MESSAGE);
        mFaceAuth.initLicense(context, licenseKey, licenseFileName, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                faceCallback.onResponse(code, response);
            }
        });
    }

    public void showActivation(final SdkInitListener sdkInitListener) {
        activation = new Activation(context);
        activation.show();
        activation.setActivationCallback(new Activation.ActivationCallback() {
            @Override
            public void callback(int code, String response, String licenseKey) {
                if (code == 0) {
                    Log.e("FaceSDK", "授权成功");
                    if (sdkInitListener != null) {
                        PreferencesUtil.putString("activate_key", licenseKey);
                        initStatus = SDK_SUCCESS;
                        dissDialog();
                        sdkInitListener.initSuccess();
                        initModel();
                    }
                    return;
                } else {
                    Log.e("FaceSDK", "授权失败:" + response);
                    if (sdkInitListener != null) {
                        dissDialog();
                        sdkInitListener.initFail(code, "授权失败:" + response);
                    }
                }
            }
        });

    }

    private void dissDialog() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                activation.dismissActivationDialog();
            }
        });
    }

    private void toast(final int code, final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,
                        code + "  " + text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }


    public interface SdkInitListener {

        public void initStart();

        public void initSuccess();

        public void initFail(int errorCode, String msg);
    }


}