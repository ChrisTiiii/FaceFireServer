package com.example.juicekaaa.fireserver.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.example.juicekaaa.fireserver.R;


/**
 * Created by Sai on 15/8/15.
 */
public class SVProgressHUD {
    private Context context;
    private static SVProgressHUD mSVProgressHUD;
    private static final long DISMISSDELAYED = 2000;
    private SVProgressHUDMaskType mSVProgressHUDMaskType;

    public enum SVProgressHUDMaskType {
        None,  // 鍏佽閬僵涓嬮潰鎺т欢鐐瑰嚮
        Clear,     // 涓嶅厑璁搁伄缃╀笅闈㈡帶浠剁偣鍑￿
        Black,     // 涓嶅厑璁搁伄缃╀笅闈㈡帶浠剁偣鍑伙紝鑳屾櫙榛戣壊鍗婇�忔槿
        Gradient,   // 涓嶅厑璁搁伄缃╀笅闈㈡帶浠剁偣鍑伙紝鑳屾櫙娓愬彉鍗婇�忔槿
        ClearCancel,     // 涓嶅厑璁搁伄缃╀笅闈㈡帶浠剁偣鍑伙紝鐐瑰嚮閬僵娑堝み
        BlackCancel,     // 涓嶅厑璁搁伄缃╀笅闈㈡帶浠剁偣鍑伙紝鑳屾櫙榛戣壊鍗婇�忔槑锛岿偣鍑婚伄缃╂秷澶￿
        GradientCancel   // 涓嶅厑璁搁伄缃╀笅闈㈡帶浠剁偣鍑伙紝鑳屾櫙娓愬彉鍗婇�忔槑锛岿偣鍑婚伄缃╂秷澶￿
        ;
    }

    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
    );
    private ViewGroup decorView;//activity鐨勬牴View
    private ViewGroup rootView;// mSharedView 鐨� 鏍筕iew
    private SVProgressDefaultView mSharedView;

    private Animation outAnim;
    private Animation inAnim;
    private int gravity = Gravity.CENTER;

    private static final SVProgressHUD getInstance(Context context) {
        if (mSVProgressHUD == null) {
            mSVProgressHUD = new SVProgressHUD();
            mSVProgressHUD.context = context;
            mSVProgressHUD.gravity = Gravity.CENTER;
            mSVProgressHUD.initViews();
            mSVProgressHUD.initDefaultView();
            mSVProgressHUD.initAnimation();
        }
        if (context != null && context != mSVProgressHUD.context ){
            mSVProgressHUD.context = context;
            mSVProgressHUD.initViews();
        }

        return mSVProgressHUD;
    }

    protected void initViews() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_svprogresshud, null, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }
    protected void initDefaultView(){
        mSharedView = new SVProgressDefaultView(context);
        params.gravity = gravity;
        mSharedView.setLayoutParams(params);
    }

    protected void initAnimation() {
        if(inAnim == null)
            inAnim = getInAnimation();
        if(outAnim == null)
            outAnim = getOutAnimation();
    }

    /**
     * show鐨勬椂鍊欒皟鐢�
     */
    private void onAttached() {
        decorView.addView(rootView);
        if(mSharedView.getParent()!=null)((ViewGroup)mSharedView.getParent()).removeView(mSharedView);
        rootView.addView(mSharedView);
    }

    /**
     * 娣诲姞杩欎釜View鍒癆ctivity鐨勬牴瑙嗗浘
     */
    private void svShow() {

        mHandler.removeCallbacksAndMessages(null);
        if (!isShowing()) {
            onAttached();
        }

        mSharedView.startAnimation(inAnim);

    }

    public static void show(Context context) {
        getInstance(context).setMaskType(SVProgressHUDMaskType.Black);
        getInstance(context).mSharedView.show();
        getInstance(context).svShow();
    }

    public static void showWithMaskType(Context context,SVProgressHUDMaskType maskType) {
        //鍒ゆ柇maskType
        getInstance(context).setMaskType(maskType);
        getInstance(context).mSharedView.show();
        getInstance(context).svShow();
    }

    public static void showWithStatus(Context context,String string) {
        getInstance(context).setMaskType(SVProgressHUDMaskType.Black);
        getInstance(context).mSharedView.showWithStatus(string);
        getInstance(context).svShow();
    }

    public static void showWithStatus(Context context,String string, SVProgressHUDMaskType maskType) {
        getInstance(context).setMaskType(maskType);
        getInstance(context).mSharedView.showWithStatus(string);
        getInstance(context).svShow();
    }

    public static void showInfoWithStatus(Context context,String string) {
        getInstance(context).setMaskType(SVProgressHUDMaskType.Black);
        getInstance(context).mSharedView.showInfoWithStatus(string);
        getInstance(context).svShow();
        getInstance(context).scheduleDismiss();
    }

    public static void showInfoWithStatus(Context context,String string, SVProgressHUDMaskType maskType) {
        getInstance(context).setMaskType(maskType);
        getInstance(context).mSharedView.showInfoWithStatus(string);
        getInstance(context).svShow();
        getInstance(context).scheduleDismiss();
    }

    public static void showSuccessWithStatus(Context context,String string) {
        getInstance(context).setMaskType(SVProgressHUDMaskType.Black);
        getInstance(context).mSharedView.showSuccessWithStatus(string);
        getInstance(context).svShow();
        getInstance(context).scheduleDismiss();
    }

    public static void showSuccessWithStatus(Context context,String string, SVProgressHUDMaskType maskType) {
        getInstance(context).setMaskType(maskType);
        getInstance(context).mSharedView.showSuccessWithStatus(string);
        getInstance(context).svShow();
        getInstance(context).scheduleDismiss();
    }

    public static void showErrorWithStatus(Context context,String string) {
        getInstance(context).setMaskType(SVProgressHUDMaskType.Black);
        getInstance(context).mSharedView.showErrorWithStatus(string);
        getInstance(context).svShow();
        getInstance(context).scheduleDismiss();
    }

    public static void showErrorWithStatus(Context context,String string, SVProgressHUDMaskType maskType) {
        getInstance(context).setMaskType(maskType);
        getInstance(context).mSharedView.showErrorWithStatus(string);
        getInstance(context).svShow();
        getInstance(context).scheduleDismiss();
    }

    public static void showWithProgress(Context context,String string, SVProgressHUDMaskType maskType) {
        getInstance(context).setMaskType(maskType);
        getInstance(context).mSharedView.showWithProgress(string);
        getInstance(context).svShow();
    }

    public static SVCircleProgressBar getProgressBar(Context context){
        return getInstance(context).mSharedView.getCircleProgressBar();
    }
    public static void setText(Context context,String string){
        getInstance(context).mSharedView.setText(string);
    }

    private void setMaskType(SVProgressHUDMaskType maskType) {
        mSVProgressHUDMaskType = maskType;
        switch (mSVProgressHUDMaskType) {
            case None:
                configMaskType(android.R.color.transparent, false, false);
                break;
            case Clear:
                configMaskType(android.R.color.transparent, true, false);
                break;
            case ClearCancel:
                configMaskType(android.R.color.transparent, true, true);
                break;
            case Black:
                configMaskType(R.color.bgColor_overlay, true, false);
                break;
            case BlackCancel:
                configMaskType(R.color.bgColor_overlay, true, true);
                break;
            case Gradient:
                //TODO 璁剧疆鍗婇�忔槑娓愬彉鑳屾櫙
                configMaskType(R.drawable.bg_overlay_gradient, true, false);
                break;
            case GradientCancel:
                //TODO 璁剧疆鍗婇�忔槑娓愬彉鑳屾櫙
                configMaskType(R.drawable.bg_overlay_gradient, true, true);
                break;
            default:
                break;
        }
    }

    private void configMaskType(int bg, boolean clickable, boolean cancelable) {
        rootView.setBackgroundResource(bg);
        rootView.setClickable(clickable);
        setCancelable(cancelable);
    }

    /**
     * 妫�娴嬭View鏄笉鏄凡缁忔坊鍔犲埌鏍硅鍥￿
     *
     * @return 濡傛灉瑙嗗浘宸茬粡翛樺湪璇iew杩斿洖true
     */
    public boolean isShowing() {
        return rootView.getParent() != null;
    }

    public static boolean isShowing(Context context) {
        return getInstance(context).rootView.getParent() != null;
    }

    public static void dismiss(Context context) {
        getInstance(context).dismiss();
    }

    public void dismiss() {
        //娑堝け鍔ㄧ敾
        outAnim.setAnimationListener(outAnimListener);
        mSharedView.startAnimation(outAnim);
    }

    public void dismissImmediately() {
        mSharedView.dismiss();
        rootView.removeView(mSharedView);
        decorView.removeView(rootView);
        context = null;
    }

    public Animation getInAnimation() {
        int res = SVProgressHUDAnimateUtil.getAnimationResource(this.gravity, true);
        return AnimationUtils.loadAnimation(context, res);
    }

    public Animation getOutAnimation() {
        int res = SVProgressHUDAnimateUtil.getAnimationResource(this.gravity, false);
        return AnimationUtils.loadAnimation(context, res);
    }

    private void setCancelable(boolean isCancelable) {
        View view = rootView.findViewById(R.id.sv_outmost_container);

        if (isCancelable) {
            view.setOnTouchListener(onCancelableTouchListener);
        } else {
            view.setOnTouchListener(null);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dismiss();
        }
    };

    private void scheduleDismiss() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(0, DISMISSDELAYED);
    }

    /**
     * Called when the user touch on black overlay in order to dismiss the dialog
     */
    private final View.OnTouchListener onCancelableTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dismiss();
                setCancelable(false);
            }
            return false;
        }
    };

    Animation.AnimationListener outAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            dismissImmediately();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
}
