package com.example.juicekaaa.fireserver.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.View;
import android.widget.MediaController;

import com.example.juicekaaa.fireserver.net.Advertisement;

import java.io.File;

public class VideoUtil {
    private Context context;
    private FullVideoView video;
    public VideoUtil(Context context, FullVideoView video) {
        this.context = context;
        this.video = video;
    }

    //播放本地视频，判断本地是否存在视频，没有视频就下载视频
    public void videoPlayback() {
        // 创建文件夹，在存储卡下
        String dirName = "/sdcard/FireVideo/";
        File file = new File(dirName);
        // 文件夹不存在时创建
        if (!file.exists()) {
            file.mkdir();
        }
        // 下载后的文件名
        String fileName = dirName + "1542178640266.mp4";
        File file1 = new File(fileName);
        if (file1.exists()) {
            // 如果已经存在, 就不下载了, 去播放
            setVideo();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //如果不存在, 提示下载 获取视频路径；
                    Advertisement.downLoad(context);
                }
            }).start();
        }
    }


    /**
     * 设置视频参数
     */
    public void setVideo() {
        MediaController mediaController = new MediaController(context);
        mediaController.setVisibility(View.GONE);//隐藏进度条
        video.setMediaController(mediaController);
        File file = new File(Environment.getExternalStorageDirectory() + "/" + "FireVideo", "1542178640266.mp4");
        video.setVideoPath(file.getAbsolutePath());
        video.start();
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
            }
        });
    }
}
