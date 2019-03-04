package com.example.juicekaaa.fireserver.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.juicekaaa.fireserver.R;
import com.example.juicekaaa.fireserver.net.NetCallBack;
import com.example.juicekaaa.fireserver.net.RequestUtils;
import com.example.juicekaaa.fireserver.net.URLs;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SOS确认提示
 *
 * @author qiaoshi
 */
public class SosDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private TextView notice;
    private TextView cancle;

    private EditText name;
    private EditText phone;
    private EditText identitycrad;
    private String type;

    private double lat;// 纬度
    private double lng;// 经度
    private String address;
    private LocationClient mLocationClient = null; // 定位对象
    private BDLocationListener myListener = new MyLocationListener(); // 定位监听

    public SosDialog(Context context) {
        super(context);
        this.context = context;
    }

    public SosDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        initMap();
        this.setContentView(R.layout.sosdialog);
        this.setCancelable(false);// 设置点击屏幕Dialog不消失
        notice = (TextView) findViewById(R.id.notice);
        cancle = (TextView) findViewById(R.id.cancle);
        name = (EditText) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        identitycrad = (EditText) findViewById(R.id.identitycrad);
        notice.setOnClickListener(this);
        cancle.setOnClickListener(this);
        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
        // 绑定一个匿名监听器
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                // 获取变更后的选中项的ID
                int radioButtonId = arg0.getCheckedRadioButtonId();
                // 根据ID获取RadioButton的实例
                RadioButton rb = (RadioButton) findViewById(radioButtonId);
                String s = rb.getText().toString();
                if (s.equals("火警")) {
                    type = "firecontrol";
                }
                if (s.equals("医疗")) {
                    type = "medicalcare";
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            // 确认求救
            case R.id.notice:
                CryForHelp();
                break;
            // 取消
            case R.id.cancle:
                dismiss();
                break;
            default:
                break;
        }
    }

    // 一键求救
    private void CryForHelp() {
        if (lat == 0.0 && lng == 0.0) {
           SVProgressHUD.showErrorWithStatus(context,"获取位置信息失败，请重新获取");
        } else if ("".equals(name.getText().toString().trim()) || "".equals(phone.getText().toString().trim()) || "".equals(identitycrad.getText().toString().trim()) || name.getText().toString().trim() == null || phone.getText().toString().trim() == null || identitycrad.getText().toString().trim() == null
                ||"".equals(type)||type==null) {
            Toast.makeText(context,"请填写完求救信息！",Toast.LENGTH_SHORT).show();
        } else {
            dismiss();
            RequestParams params = new RequestParams();
            params.put("lat", lat + "");
            params.put("lng", lng + "");
            params.put("name", name.getText().toString().trim());
            params.put("phone", phone.getText().toString().trim());
            params.put("identitycrad", identitycrad.getText().toString().trim());
            params.put("type", type);
            params.put("address", address);
            params.put("unitCode","");
            params.put("username", "admin");
            params.put("platformkey", "app_firecontrol_owner");
            RequestUtils.ClientPost(URLs.CryForHelp_URL, params, new NetCallBack() {
                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onMySuccess(String result) {
                    if (result == null || result.length() == 0) {
                        return;
                    }
                    System.out.println("数据请求成功" + result);
                    try {
                        JSONObject JSONObject = new JSONObject(result);
                        String msg = JSONObject.getString("msg");
                        if (msg.equals("申请成功")) {
                            SVProgressHUD.showSuccessWithStatus(context,"发送成功");
                        } else {
                            SVProgressHUD.showErrorWithStatus(context,msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMyFailure(Throwable arg0) {
                    SVProgressHUD.showErrorWithStatus(context,"物资信息加载失败");
                }
            });
        }
    }


    private void initMap() {
        mLocationClient = new LocationClient(context);
        // 声明LocationClient类 //配置定位SDK参数
        initLocation();
        mLocationClient.registerLocationListener(myListener);// 注册监听函数
        // 开启定位
        mLocationClient.start();
        // 图片点击事件，回到定位点
        mLocationClient.requestLocation();
    }

    //配置定位SDK参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span = 10000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        option.setOpenGps(true);// 打开gps
        // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程， 设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            address = location.getAddrStr();
            System.out.println("=====================" + address);
        }
    }
}
