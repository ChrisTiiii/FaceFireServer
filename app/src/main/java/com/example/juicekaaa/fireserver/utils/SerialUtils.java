package com.example.juicekaaa.fireserver.utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;


/**
 * Author: create by ZhongMing
 * Time: 2019/2/28 0028 14:45
 * Description:
 */
public class SerialUtils {
    private static final String CHUAN = "/dev/ttyS2";//ttymxc2
    private static final int BOTE = 9600;

    private final String TAG = "SerialPortUtils";
    public boolean serialPortStatus = false; //是否打开串口标志
    public String data_;
    public boolean threadStatus; //线程状态，为了安全终止线程
    private SerialPort serialPort = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;

    /**
     * 打开串口
     *
     * @return serialPort串口对象
     */
    public SerialPort openSerialPort() {
        try {
            serialPort = new SerialPort(new File(CHUAN), BOTE, 0);
            this.serialPortStatus = true;
            threadStatus = false; //线程状态
            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "openSerialPort: 打开串口异常：" + e.toString());
            return serialPort;
        }
        Log.d(TAG, "openSerialPort: 打开串口");
        return serialPort;
    }


    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        try {
            inputStream.close();
            outputStream.close();

            this.serialPortStatus = false;
            this.threadStatus = true; //线程状态
            serialPort.close();
        } catch (IOException e) {
            Log.e(TAG, "closeSerialPort: 关闭串口异常：" + e.toString());
            return;
        }
        Log.d(TAG, "closeSerialPort: 关闭串口成功");
    }

    /**
     * 发送串口指令（字符串）
     *
     * @param data String数据指令
     */
    public void sendHex(String sHex) {
        byte[] bOutArray = HexToByteArr(sHex);
        this.send(bOutArray);
    }

    public void send(byte[] bOutArray) {
        try {
            this.outputStream.write(bOutArray);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public static byte[] HexToByteArr(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {
            ++hexlen;
            result = new byte[hexlen / 2];
            inHex = "0" + inHex;
        } else {
            result = new byte[hexlen / 2];
        }

        int j = 0;

        for(int i = 0; i < hexlen; i += 2) {
            result[j] = HexToByte(inHex.substring(i, i + 2));
            ++j;
        }

        return result;
    }

    public static int isOdd(int num) {
        return num & 1;
    }

    public static byte HexToByte(String inHex) {
        return (byte)Integer.parseInt(inHex, 16);
    }

//    public void sendSerialPort(String data) {
//        System.out.println("before:" + data);
//        Log.d(TAG, "sendSerialPort: 发送数据");
//        byte[] sendData = EncodingConversionTools.HexString2Bytes(data);
//        System.out.println("after" + sendData);
//        try {
////            byte[] sendData = data.getBytes(); //string转byte[]
////            this.data_ = new String(sendData); //byte[]转string
////            byte[] sendData = EncodingConversionTools.HexString2Bytes(data);
//            if (data.length() > 0) {
//                outputStream.write(sendData);
//                outputStream.write('\n');
//                outputStream.flush();
//                Log.d(TAG, "sendSerialPort: 串口数据发送成功 " + sendData);
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "sendSerialPort: 串口数据发送失败：" + e.toString());
//        }
//
//    }

    public boolean isOpen() {
        return serialPortStatus;
    }

}
