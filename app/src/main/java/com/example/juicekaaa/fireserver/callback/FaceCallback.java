package com.example.juicekaaa.fireserver.callback;

/**
 * Author: create by ZhongMing
 * Time: 2019/2/28 0028 10:16
 * Description:
 */
public interface FaceCallback {
    /**
     *  回调函数 code 0 : 成功；code 1 加载失败
     * @param code
     * @param response
     */
    void onResponse(int code, String response);
}
