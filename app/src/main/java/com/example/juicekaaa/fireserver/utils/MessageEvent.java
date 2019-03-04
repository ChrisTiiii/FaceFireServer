package com.example.juicekaaa.fireserver.utils;

import java.util.List;

/**
 * author: ZhongMing
 * DATE: 2018/11/16 0016
 * Description:
 **/
public class MessageEvent {
    private int TAG;
    private String message;
    private List<String> listPath;
    private List<String> listPaths;
    private List<String> imageTitle;
    private List<String> imageTitles;

    public MessageEvent(int TAG) {
        this.TAG = TAG;
    }

    public MessageEvent(int TAG, String message) {
        this.TAG = TAG;
        this.message = message;
    }


    public List<String> getListPath() {
        return listPath;
    }

    public void setListPath(List<String> listPath) {
        this.listPath = listPath;
    }

    public List<String> getListPaths() {
        return listPaths;
    }

    public void setListPaths(List<String> listPaths) {
        this.listPaths = listPaths;
    }

    public List<String> getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(List<String> imageTitle) {
        this.imageTitle = imageTitle;
    }

    public List<String> getImageTitles() {
        return imageTitles;
    }

    public void setImageTitles(List<String> imageTitles) {
        this.imageTitles = imageTitles;
    }

    public int getTAG() {
        return TAG;
    }

    public void setTAG(int TAG) {
        this.TAG = TAG;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
