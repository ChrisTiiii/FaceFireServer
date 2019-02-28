package com.example.juicekaaa.fireserver.api;

/**
 * Author: create by ZhongMing
 * Time: 2019/2/28 0028 10:17
 * Description:
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


import com.baidu.idl.facesdk.model.BDFaceSDKAttribute;
import com.baidu.idl.facesdk.model.BDFaceSDKEmotions;
import com.baidu.idl.facesdk.model.FaceInfo;
import com.example.juicekaaa.fireserver.db.DBManager;
import com.example.juicekaaa.fireserver.entity.ARGBImg;
import com.example.juicekaaa.fireserver.entity.Feature;
import com.example.juicekaaa.fireserver.entity.Group;
import com.example.juicekaaa.fireserver.entity.IdentifyRet;
import com.example.juicekaaa.fireserver.entity.User;
import com.example.juicekaaa.fireserver.manager.FaceSDKManager;
import com.example.juicekaaa.fireserver.utils.FeatureUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class FaceApi {

    public static final int FACE_FILE = 1;
    public static final int FACE_TOKEN = 2;
    private HashMap<String, HashMap<String, byte[]>> group2Facesets = new HashMap<>();
    private static FaceApi instance;

    private int faceFeaturelen = 512;

    private FaceApi() {

    }

    public static synchronized FaceApi getInstance() {
        if (instance == null) {
            instance = new FaceApi();
        }
        return instance;
    }

    public boolean groupAdd(Group group) {
        if (group == null || TextUtils.isEmpty(group.getGroupId())) {
            return false;
        }
//        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
//        Matcher matcher = pattern.matcher(group.getGroupId());
//        if (!matcher.matches()) {
//            return false;
//        }
        boolean ret = DBManager.getInstance().addGroup(group);

        return ret;
    }

    public boolean userAdd(User user) {
        if (user == null || TextUtils.isEmpty(user.getGroupId()) || user.getFeatureList().size() == 0) {
            return false;
        }
//        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
//        Matcher matcher = pattern.matcher(user.getUserId());
//        if (!matcher.matches()) {
//            return false;
//        }
        boolean ret = DBManager.getInstance().addUser(user);

        return ret;
    }

    public boolean userDelete(String userId, String groupId) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(groupId)) {
            return false;
        }

        boolean ret = DBManager.getInstance().deleteUser(userId, groupId);
        return ret;
    }

    public boolean userUpdate(User user, int mode) {
        if (user == null) {
            return false;
        }

        boolean ret = DBManager.getInstance().updateUser(user, mode);
        return ret;
    }

    public boolean userFaceDelete(String userId, String groupId, String faceToken) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(groupId) || TextUtils.isEmpty(faceToken)) {
            return false;
        }
        boolean ret = DBManager.getInstance().deleteFeature(userId, groupId, faceToken);
        return ret;
    }

    public boolean groupDelete(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return false;
        }
        boolean ret = DBManager.getInstance().deleteGroup(groupId);
        return ret;
    }

    public User getUserInfo(String groupId, String userId) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
            return null;
        }
        User user = DBManager.getInstance().queryUser(groupId, userId);
        List<Feature> featureList = DBManager.getInstance().queryFeature(groupId, userId);
        user.setFeatureList(featureList);
        return user;
    }

    public List<User> getUserList(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        List<User> userList = DBManager.getInstance().queryUserByGroupId(groupId);
        return userList;
    }

    public List<Group> getGroupList(int start, int length) {
        if (start < 0 || length < 0) {
            return null;
        }
        if (length > 1000) {
            length = 1000;
        }
        List<Group> groupList = DBManager.getInstance().queryGroups(start, length);
        return groupList;
    }

    public byte[] getFeature(String faceToken) {
        if (TextUtils.isEmpty(faceToken)) {
            return null;
        }
        byte[] feature = DBManager.getInstance().queryFeature(faceToken);
        return feature;
    }

    public float getFeature(ARGBImg argbImg, byte[] feature) {
        if (argbImg == null) {
            return -1;
        }
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(argbImg.data, argbImg.width, argbImg.height);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            ret = FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbImg.data, argbImg.height, argbImg.width, feature, faceInfo.landmarks);
        }
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
        return ret;
    }

    public float getFeature(Bitmap bitmap, byte[] feature) {
        if (bitmap == null) {
            return -1;
        }
        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(argbImg.data, argbImg.width, argbImg.height);
//        Log.e("gangzi", "track --after-- h 2 " + argbImg.height + "  w: " + argbImg.width);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            ret = FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbImg.data, argbImg.height, argbImg.width, feature, faceInfo.landmarks);
        }
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
        return ret;
    }

    public float getFeatureForIDPhoto(ARGBImg argbImg, byte[] feature) {
        if (argbImg == null) {
            return -1;
        }
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(argbImg.data, argbImg.width, argbImg.height);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            ret = FaceSDKManager.getInstance().getFaceFeature().extractFeatureForIDPhoto(argbImg.data, argbImg.height, argbImg.width, feature, faceInfo.landmarks);
        }
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
        return ret;
    }

    public float getFeatureForIDPhoto(Bitmap bitmap, byte[] feature) {
        if (bitmap == null) {
            return -1;
        }
        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(argbImg.data, argbImg.width, argbImg.height);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            ret = FaceSDKManager.getInstance().getFaceFeature().extractFeatureForIDPhoto(argbImg.data, argbImg.height, argbImg.width, feature, faceInfo.landmarks);
        }
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
        return ret;
    }


    public float match(String image1, String image2, int type, Context context) {
        if (TextUtils.isEmpty(image1) || TextUtils.isEmpty(image2)) {
            return -1;
        }
        float ret = -1;
        if (type == FACE_FILE) {
            Uri uri1 = Uri.parse(image1);
            Uri uri2 = Uri.parse(image1);
            ret = match(uri1, uri2, context);
        } else if (type == FACE_TOKEN) {
            byte[] firstFeature = DBManager.getInstance().queryFeature(image1);
            byte[] secondFeature = DBManager.getInstance().queryFeature(image2);
            ret = FaceSDKManager.getInstance().getFaceFeature()
                    .featureCompare(firstFeature, secondFeature);
        }
        return ret;
    }

    public float match(Uri image1, Uri image2, Context context) {
        if (image1 == null) {
            return -100;
        }
        if (image2 == null) {
            return -101;
        }
        float ret = -1;
        try {
            byte[] firstFeature = new byte[512];
            byte[] secondFeature = new byte[512];
            final Bitmap bitmap1 = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(image1));
            ARGBImg argbImg1 = FeatureUtils.getImageInfo(bitmap1);
            FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(argbImg1.data, argbImg1.width, argbImg1.height);
            float ret1 = -1;
            if (faceInfos != null && faceInfos.length > 0) {
                FaceInfo faceInfo = faceInfos[0];
                ret1 = FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbImg1.data, argbImg1.height, argbImg1.width, firstFeature, faceInfo.landmarks);
            }
            FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();

            final Bitmap bitmap2 = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(image2));
            ARGBImg argbImg2 = FeatureUtils.getImageInfo(bitmap2);
            float ret2 = -1;
            if (faceInfos != null && faceInfos.length > 0) {
                FaceInfo faceInfo = faceInfos[0];
                ret1 = FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbImg2.data, argbImg2.height, argbImg2.width, secondFeature, faceInfo.landmarks);
            }
            FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
            if (ret1 != 128) {
                return -102;
            }
            if (ret2 != 128) {
                return -103;
            }
            ret = FaceSDKManager.getInstance().getFaceFeature().featureCompare(firstFeature, secondFeature);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }


    public float match(final byte[] photoFeature, int[] argbData, int rows, int cols, int[] landmarks) {
        if (photoFeature == null || argbData == null || landmarks == null) {
            return -1;
        }
        byte[] imageFrameFeature = new byte[512];
        FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbData, rows, cols,
                imageFrameFeature, landmarks);

        final float score = FaceSDKManager.getInstance().getFaceFeature()
                .featureCompare(imageFrameFeature,
                        photoFeature);
        return score;
    }

    public float matchIDPhoto(final byte[] photoFeature, int[] argbData, int rows, int cols, int[] landmarks) {
        if (photoFeature == null || argbData == null || landmarks == null) {
            return -1;
        }
        byte[] imageFrameFeature = new byte[512];
        FaceSDKManager.getInstance().getFaceFeature()
                .extractFeatureForIDPhoto(argbData, rows, cols,
                        imageFrameFeature, landmarks);

        final float score = FaceSDKManager.getInstance()
                .getFaceFeature().featureIDCompare(imageFrameFeature,
                        photoFeature);
        return score;
    }

    public float matchIDPhoto(final byte[] feature1, final byte[] feature2) {
        if (feature1 == null || feature2 == null) {
            return -1;
        }
        final float score = FaceSDKManager.getInstance()
                .getFaceFeature().featureIDCompare(feature1, feature2);
        return score;
    }

    public float match(final byte[] feature1, final byte[] feature2) {
        if (feature1 == null || feature2 == null) {
            return -1;
        }
        final float score = FaceSDKManager.getInstance().getFaceFeature()
                .featureCompare(feature1, feature2);
        return score;
    }


    public IdentifyRet identity(String image, int type, String groupId, Context context) {
        if (TextUtils.isEmpty(image) || TextUtils.isEmpty(groupId)) {
            return null;
        }
        byte[] imageFrameFeature = new byte[512];
        if (type == FACE_FILE) {
            try {
                Uri uri = Uri.parse(image);
                final Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(argbImg.data, argbImg.width, argbImg.height);
                float ret = -1;
                if (faceInfos != null && faceInfos.length > 0) {
                    FaceInfo faceInfo = faceInfos[0];
                    ret = FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbImg.data, argbImg.height, argbImg.width, imageFrameFeature, faceInfo.landmarks);
                }
                FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (type == FACE_TOKEN) {
            imageFrameFeature = DBManager.getInstance().queryFeature(image);
        }
        if (imageFrameFeature == null || groupId == null) {
            return null;
        }
        HashMap<String, byte[]> userId2Feature = group2Facesets.get(groupId);
        String userIdOfMaxScore = "";
        float identifyScore = 0;
        Iterator iterator = userId2Feature.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry<String, byte[]>) iterator.next();
            byte[] feature = entry.getValue();
            final float score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                    feature, imageFrameFeature);
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = entry.getKey();
            }
        }
        return new IdentifyRet(userIdOfMaxScore, identifyScore);
    }

    public IdentifyRet identity(String image, int type, String groupId, String userId, Context context) {
        if (TextUtils.isEmpty(image) || TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
            return null;
        }
        byte[] imageFrameFeature = new byte[512];
        if (type == FACE_FILE) {
            try {
                Uri uri = Uri.parse(image);
                final Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().trackMaxFace(argbImg.data, argbImg.width, argbImg.height);
                float ret = -1;
                if (faceInfos != null && faceInfos.length > 0) {
                    FaceInfo faceInfo = faceInfos[0];
                    ret = FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbImg.data, argbImg.height, argbImg.width, imageFrameFeature, faceInfo.landmarks);
                }
                FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (type == FACE_TOKEN) {
            imageFrameFeature = DBManager.getInstance().queryFeature(image);
        }
        if (imageFrameFeature == null || groupId == null || userId == null) {
            return null;
        }
        HashMap<String, byte[]> userId2Feature = group2Facesets.get(groupId);
        byte[] feature = userId2Feature.get(userId);
        float score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                feature, imageFrameFeature);
        return new IdentifyRet(userId, score);
    }


    public IdentifyRet identity(int[] argbData, int rows, int cols, int[] landmarks, String groupId) {
        if (argbData == null || landmarks == null || groupId == null || TextUtils.isEmpty(groupId)) {
            return null;
        }
        HashMap<String, byte[]> userId2Feature = group2Facesets.get(groupId);
        long startExtraTime = System.currentTimeMillis();
        byte[] imageFrameFeature = new byte[512];
        FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbData, rows, cols,
                imageFrameFeature, landmarks);
        // Log.i("identity", "extractFeature duration->" + (System.currentTimeMillis() - startExtraTime));
        String userIdOfMaxScore = "";
        float identifyScore = 0;
        Iterator iterator = userId2Feature.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry<String, byte[]>) iterator.next();
            byte[] feature = entry.getValue();
            final float score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                    imageFrameFeature, feature);
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = entry.getKey();
            }
        }
        return new IdentifyRet(userIdOfMaxScore, identifyScore);
    }


    public IdentifyRet identityForIDPhoto(int[] argbData, int rows, int cols, int[] landmarks, String groupId) {
        if (argbData == null || landmarks == null || groupId == null || TextUtils.isEmpty(groupId)) {
            return null;
        }
        HashMap<String, byte[]> userId2Feature = group2Facesets.get(groupId);
        long startExtraTime = System.currentTimeMillis();
        byte[] imageFrameFeature = new byte[512];
        FaceSDKManager.getInstance().getFaceFeature().extractFeatureForIDPhoto(argbData, rows, cols,
                imageFrameFeature, landmarks);
        // Log.i("identity", "extractFeature duration->" + (System.currentTimeMillis() - startExtraTime));
        String userIdOfMaxScore = "";
        float identifyScore = 0;
        Iterator iterator = userId2Feature.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry<String, byte[]>) iterator.next();
            byte[] feature = entry.getValue();
            final float score = FaceSDKManager.getInstance().getFaceFeature().featureIDCompare(
                    imageFrameFeature, feature);
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = entry.getKey();
            }
        }
        return new IdentifyRet(userIdOfMaxScore, identifyScore);
    }

    public void loadFacesFromDB(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        List<Feature> featureList = DBManager.getInstance().queryFeatureByGroupId(groupId);

        HashMap<String, byte[]> userId2Feature = new HashMap<String, byte[]>();
        for (Feature feature : featureList) {
            userId2Feature.put(feature.getUserId(), feature.getFeature());
            Log.i("wtf", " loadFeature2Memery feature " + feature.getFeature());
        }
        group2Facesets.put(groupId, userId2Feature);
    }


    public float extractVisFeature(Bitmap bitmap, byte[] feature) {
        if (bitmap == null) {
            return -1;
        }
        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        float ret = FaceSDKManager.getInstance().getFacefeaturesImage()
                .extractFeature(argbImg.data, argbImg.height, argbImg.width, feature);
        return ret;
    }

    public float extractIdPhotoFeature(Bitmap bitmap, byte[] feature) {
        if (bitmap == null) {
            return -1;
        }
        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        float ret = FaceSDKManager.getInstance().getFacefeaturesImage()
                .extractIdFeature(argbImg.data, argbImg.height, argbImg.width, feature);
        return ret;
    }


    public float matchIdFeaturePhoto(final byte[] feature1, final byte[] feature2) {
        if (feature1 == null || feature2 == null) {
            return -1;
        }
        final float score = FaceSDKManager.getInstance()
                .getFacefeaturesImage().featureIdCompare(feature1, feature2);
        return score;
    }

    public float matchVisFeaturePhoto(final byte[] feature1, final byte[] feature2) {
        if (feature1 == null || feature2 == null) {
            return -1;
        }
        final float score = FaceSDKManager.getInstance()
                .getFacefeaturesImage().featureCompare(feature1, feature2);
        return score;
    }

    public BDFaceSDKAttribute attribute(int[] imageData, int height, int width, int[] landmarks) {
        return FaceSDKManager.getInstance().getFaceAttribute().attribute(imageData, height, width, landmarks);
    }

    public BDFaceSDKEmotions emotions(int[] imageData, int height, int width, int[] landmarks) {
        return FaceSDKManager.getInstance().getFaceAttribute().emotions(imageData, height, width, landmarks);
    }

    public HashMap<String, HashMap<String, byte[]>> getGroup2Facesets() {
        return group2Facesets;
    }

}
