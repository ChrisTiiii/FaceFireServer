package com.example.juicekaaa.fireserver.callback;

import com.example.juicekaaa.fireserver.entity.LivenessModel;

/**
 * Author: create by ZhongMing
 * Time: 2019/2/28 0028 10:19
 * Description:
 */
public interface ILivenessCallBack {
    public void onCallback(LivenessModel livenessModel);

    public void onTip(int code, String msg);

    public void onCanvasRectCallback(LivenessModel livenessModel);
}
