package com.lh.sms.message.service;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.alibaba.fastjson.JSONObject;
import com.lh.sms.MainActivity;
import com.lh.sms.enums.ResultCodeEnum;
import com.lh.sms.message.SendReceiver;
import com.lh.sms.util.ClassUtil;

import java.util.List;

import androidx.core.app.ActivityCompat;

public class SmsService {
    /**
     * @do 发送信息
     * @author liuhua
     * @date 2020/3/20 7:48 PM
     */
    public JSONObject sendSms(String phone, String iccId, String msg) {
        JSONObject result = new JSONObject();
        if(phone==null||iccId==null||msg==null){
            result.put("code", ResultCodeEnum.ERROR.getValue());
            result.put("msg","参数错误");
            return result;
        }
        //注册消息通知
        //必须先注册广播接收器,否则接收不到发送结果
        Context context = ClassUtil.get(MainActivity.class);
        SendReceiver receiver = new SendReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SendReceiver.ACTION);
        context.registerReceiver(receiver, filter);
        SubscriptionManager sManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            result.put("code", ResultCodeEnum.ERROR.getValue());
            result.put("msg","没有发送短信权限");
            return result;
        }
        int subscriptionId = -1;
        List<SubscriptionInfo> mList = sManager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : mList) {
            if(iccId.equals(subscriptionInfo.getIccId())){
                subscriptionId = subscriptionInfo.getSubscriptionId();
            }
        }
        if(subscriptionId<0){
            result.put("code", ResultCodeEnum.ERROR.getValue());
            result.put("msg","没有找到对应sim卡");
            return result;
        }
        SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
        Intent intent = new Intent();
        intent.putExtra("phone",phone);
        intent.putExtra("msg",msg);
        intent.putExtra("iccId",iccId);
        intent.setAction(SendReceiver.ACTION);
        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            smsManager.sendTextMessage(phone,null,msg,sentIntent,sentIntent);
            result.put("code", ResultCodeEnum.OK.getValue());
            result.put("msg","已发送");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", ResultCodeEnum.ERROR.getValue());
            result.put("msg",e.getMessage());
            return result;
        }
    }
}
