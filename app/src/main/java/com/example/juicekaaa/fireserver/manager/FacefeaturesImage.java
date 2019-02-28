package com.example.juicekaaa.fireserver.manager;

import android.content.Context;

import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.FaceFeatureImage;
import com.baidu.idl.facesdk.callback.Callback;
import com.example.juicekaaa.fireserver.callback.FaceCallback;

/**
 * Time: 2018/12/4
 * Author: v_chaixiaogang
 * Description:
 */
public class FacefeaturesImage {

    private FaceFeatureImage mFaceFeatureImage;

    public void initModel(Context context, String visDetectModel, String alignModel,
                          String idPhotoModel, String visFeatureModel, final FaceCallback faceCallback) {
        if (mFaceFeatureImage == null) {
            mFaceFeatureImage = new FaceFeatureImage();
            mFaceFeatureImage.initMdoel(context, visDetectModel, alignModel, idPhotoModel,
                    visFeatureModel, new Callback() {
                        @Override
                        public void onResponse(int code, String response) {
                            faceCallback.onResponse(code, response);
                        }
                    });
        } else {
            mFaceFeatureImage.initMdoel(context, visDetectModel, alignModel, idPhotoModel,
                    visFeatureModel, new Callback() {
                        @Override
                        public void onResponse(int code, String response) {
                            faceCallback.onResponse(code, response);
                        }
                    });
        }
    }

    public float extractFeature(int[] argb, int height, int width, byte[] feature) {
        return mFaceFeatureImage.feature(FaceFeature.FeatureType.FEATURE_VIS, argb, height, width, feature);
    }

    public float extractIdFeature(int[] argb, int height, int width, byte[] feature) {
        return mFaceFeatureImage.feature(FaceFeature.FeatureType.FEATURE_ID_PHOTO, argb, height, width, feature);
    }

    public float featureCompare(byte[] feature1, byte[] feature2) {
        return mFaceFeatureImage.featureCompare(FaceFeature.FeatureType.FEATURE_VIS, feature1, feature2);
    }

    public float featureIdCompare(byte[] feature1, byte[] feature2) {
        return mFaceFeatureImage.featureCompare(FaceFeature.FeatureType.FEATURE_ID_PHOTO, feature1, feature2);
    }
}
