package com.example.juicekaaa.fireserver.utils;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.juicekaaa.fireserver.R;


public class SVCircleProgressBar extends View {
    private Context mContext;
    /**
     * 閻㈣崵鐟�电钖勯惃鍕穿閿燂拿
     */
    private Paint paint;

    /**
     * 閸﹀棛骞嗛惃鍕杹閿燂拷
     */
    private int roundColor;

    /**
     * 閸﹀棛骞嗘潻娑樺閻ㄥ嫰顤侀敓锟￿
     */
    private int roundProgressColor;

    /**
     * 閸﹀棛骞嗛惃鍕啍閿燂拷
     */
    private float roundWidth;

    /**
     * 閿燂拷婢堆嗙箻閿燂拷
     */
    private int max;

    /**
     * 瑜版挸澧犳潻娑樺
     */
    private int progress;

    /**
     * 鏉╂稑翹抽惃鍕棢閺嶇》绱濈�圭偛绺鹃幋鏍垫嫹?閿熺晫鈹栭敓锟�
     */
    private int style;

    public static final int STROKE = 0;
    public static final int FILL = 1;

    public SVCircleProgressBar(Context context) {
        this(context, null);
        this.mContext = context;

    }

    public SVCircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
    }

    public SVCircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;

        paint = new Paint();

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.SVCircleProgressBar);

        // 閼惧嘲褰囬懛顏勭暰娑斿鐫橀广褍鎷版妯款吇閿燂拷
        roundColor = mTypedArray.getColor(R.styleable.SVCircleProgressBar_roundColor, Color.BLUE);
        roundProgressColor = mTypedArray.getColor(R.styleable.SVCircleProgressBar_roundProgressColor,
                Color.GRAY);
        roundWidth = mTypedArray.getDimension(R.styleable.SVCircleProgressBar_roundWidth, 5);
        max = mTypedArray.getInteger(R.styleable.SVCircleProgressBar_max, 100);
        style = mTypedArray.getInt(R.styleable.SVCircleProgressBar_style, 0);

        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 閻㈢粯娓舵径鏍х湴閻ㄥ嫬銇囬崷鍡欏箿
         */
        int centre = getWidth() / 2; // 閼惧嘲褰囬崷鍡楃妇閻ㄥ墡閸ф劖鐖�
        int radius = (int) (centre - roundWidth / 2); // 閸﹀棛骞嗛惃鍕磹閿燂拷
        paint.setAntiAlias(true); // 濞戝牓娅庨柨顖炲▿
        paint.setColor(roundColor); // 鐠佸墽鐤嗛崷鍡欏箚閻ㄥ嫰顤侀敓锟￿
        paint.setStyle(Paint.Style.STROKE); // 鐠佸墽鐤嗙粚鍝勭妿
        paint.setStrokeWidth(roundWidth); // 鐠佸墽鐤嗛崷鍡欏箚閻ㄥ嫬顔旈敓锟￿
        canvas.drawCircle(centre, centre, radius, paint); // 閻㈣鍤崷鍡欏箿


        /**
         * 閻㈣娓鹃敓锟� 閿涘瞼鏁鹃崷鍡欏箚閻ㄥ嫯绻橀敓锟￿
         */

        // 鐠佸墽鐤嗘潻娑樺閺勵垰鐤勮箛鍐箷閺勵垳鈹栭敓锟�
        paint.setStrokeWidth(roundWidth); // 鐠佸墽鐤嗛崷鍡欏箚閻ㄥ嫬顔旈敓锟￿
        paint.setColor(roundProgressColor); // 鐠佸墽鐤嗘潻娑樺閻ㄥ嫰顤侀敓锟￿
        RectF oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius); // 閻€劋绨�规矮绠熼惃鍕妇瀵呮畱瑜般垻濮搁崪灞姐亣鐏忓繒娈戦悾宀勬

        switch (style) {
            case STROKE: {
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(oval, 270, 360 * progress / max, false, paint); // 閺嶈宓佹潻娑樺閻㈣娓鹃敓锟￿
                break;
            }
            case FILL: {
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (progress != 0)
                    canvas.drawArc(oval, 270, 360 * progress / max, true, paint); // 閺嶈宓佹潻娑樺閻㈣娓鹃敓锟￿
                break;
            }
        }

    }

    public synchronized int getMax() {
        return max;
    }

    /**
     * 鐠佸墽鐤嗘潻娑樺閻ㄥ嫭娓舵径褝鎷�??
     *
     * @param max
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 閼惧嘲褰囨潻娑樺.閿燂拷鐟曚礁鎮撻敓锟￿
     *
     * @return
     */
    public synchronized int getProgress() {
        return progress;
    }

    /**
     * 鐠佸墽鐤嗘潻娑樺閿涘本顒濇稉铏瑰殠缁嬪鐣ㄩ崗銊﹀付娴犺绱濋悽鍙樼艾閼板啳妾绘径姘卞殠閻ㄥ嫰妫舵０姗堢礉閿燂拷鐟曚礁鎮撻敓锟￿
     * 閸掗攱鏌婇悾宿勬桨鐠嬪啰鏁ostInvalidate()閼宠棄婀棃婵絀缁捐法鈻奸崚閿嬫煿
     *
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        if (progress <= max) {
            this.progress = progress;
            postInvalidate();
        }

    }

    public int getCricleColor() {
        return roundColor;
    }

    public void setCricleColor(int cricleColor) {
        this.roundColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return roundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.roundProgressColor = cricleProgressColor;
    }

    public float getRoundWidth() {
        return roundWidth;
    }

    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
    }

}