package com.example.juicekaaa.fireserver.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class FullVideoView extends VideoView {
    public FullVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // 必须得注掉 不然的话就会默认的返回布局中的宽高
//    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取设备中的的总宽高
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 将这些值返回到布局当中使得可以使用
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}
