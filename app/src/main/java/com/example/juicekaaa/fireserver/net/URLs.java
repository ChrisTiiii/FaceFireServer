package com.example.juicekaaa.fireserver.net;

/**
 * @author qiaoshi
 */
public class URLs {
    public static String HOST_IP = "47.100.192.169";
    public static String HOST_PORT = "8080";

    public static String HOST = "http://" + HOST_IP + ":" + HOST_PORT;
    //加载图片资源
    public static String IMAGEE_URL = HOST + "/kspf/app/publicityedu/banner?platformkey=app_firecontrol_owner";
    //加载视频资源
    public static String Article_URL = HOST + "/kspf/app/publicityedu/list";
    // 一键求救
    public static String CryForHelp_URL = HOST + "/kspf/app/stationassistant/add";
}
