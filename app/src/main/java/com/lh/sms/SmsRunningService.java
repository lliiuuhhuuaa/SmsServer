package com.lh.sms;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.lh.sms.message.service.MsgService;
import com.lh.sms.socket.service.SocketService;
import com.lh.sms.util.AlertUtil;
import com.lh.sms.util.ClassUtil;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SmsRunningService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        readyConnect();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startFG();
        }
    }
    /**
     * @do 创建通知前台运行
     * @author liuhua
     * @date 2020/3/21 5:29 PM
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startFG() {
        String CHANNEL_ONE_ID = "CHANNEL_ONE_ID";
        String CHANNEL_ONE_NAME= "CHANNEL_ONE_ID";
        NotificationChannel notificationChannel= null;
        //进行8.0的判断
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel= new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification= new Notification.Builder(this).setChannelId(CHANNEL_ONE_ID)
                .setTicker("Nature")
                .setSmallIcon(R.mipmap.sms)
                .setLargeIcon(Icon.createWithResource(ClassUtil.get(MainActivity.class),R.mipmap.logo))
                .setContentTitle("短信发送服务")
                .setContentIntent(pendingIntent)
                .setContentText("等待发送监听服务运行中...")
                .setOngoing(true)
                .build();
        notification.flags|= Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    /**
     * @do 准备连接
     * @author liuhua
     * @date 2020/3/21 10:10 AM
     */
    private void readyConnect() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        SubscriptionManager sManager = (SubscriptionManager) this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> mList = sManager.getActiveSubscriptionInfoList();
        if (mList == null || mList.size() < 1) {
            ClassUtil.get(MsgService.class).push("未检测到有效sim卡,请检查后重试");
            return;
        }
        String iccId = null;
        if (mList.size() < 2) {
            iccId = mList.get(0).getIccId();
        } else {
            SharedPreferences smsInfo = getSharedPreferences("smsInfo", MODE_PRIVATE);
            int sim = smsInfo.getInt("sim", -1);
            if (sim < 0) {
                StringBuffer sb = new StringBuffer();
                sb.append("卡1:").append(mList.get(0).getIccId()).append("<br/>");
                sb.append("卡2:").append(mList.get(1).getIccId());
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
                sweetAlertDialog.setTitle("请选择使用那张sim卡发送短信");
                sweetAlertDialog.setContentText(sb.toString());
                sweetAlertDialog.setCancelText("使用卡1").setCancelClickListener(sweetAlertDialog12 -> {
                    smsInfo.edit().putInt("sim", 0).apply();
                    sweetAlertDialog.cancel();
                    ClassUtil.get(SocketService.class).connect(mList.get(0).getIccId());
                }).setConfirmText("使用卡2").setConfirmClickListener(sweetAlertDialog1 -> {
                    smsInfo.edit().putInt("sim", 0).apply();
                    sweetAlertDialog.cancel();
                    ClassUtil.get(SocketService.class).connect(mList.get(1).getIccId());
                });
                AlertUtil.alertOther(sweetAlertDialog);
            } else {
                iccId = mList.get(sim).getIccId();
            }

        }
        if (iccId != null) {
            //连接socket
            ClassUtil.get(SocketService.class).connect(iccId);
        }
    }
}
