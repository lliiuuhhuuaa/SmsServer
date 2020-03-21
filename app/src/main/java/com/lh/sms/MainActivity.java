package com.lh.sms;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.lh.sms.handle.HandleMessage;
import com.lh.sms.message.service.MsgService;
import com.lh.sms.message.service.SmsService;
import com.lh.sms.socket.service.SocketService;
import com.lh.sms.util.ClassUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);//宽高最大
        //初始化对象
        init();
        //请求权限
        requestPermission();
    }

    /**
     * @do 初始化对象
     * @author liuhua
     * @date 2020/3/20 8:30 PM
     */
    private void init() {
        ClassUtil.push(this);
        ClassUtil.push(new HandleMessage(),new SmsService(),new SocketService(),new MsgService());
    }

    /**
     * @do 显示消息
     * @author liuhua
     * @date 2020/3/20 8:17 PM
     */
    public void showMsg(Bundle data){
        ClassUtil.get(MsgService.class).push(data.getString("msg"));
    }
    /**
     * @do 请求权限
     * @author liuhua
     * @date 2020/3/13 5:23 PM
     */
    private void requestPermission() {
        List<PermissionItem> permissionItems = new ArrayList<>();
        permissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, "手机信息", R.drawable.permission_ic_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.SEND_SMS, "发送短信", R.drawable.permission_ic_sms));
       // permissionItems.add(new PermissionItem(Manifest.permission.READ_SMS, "读取短信", R.drawable.permission_ic_sms));
        HiPermission.create(MainActivity.this)
                .title("权限申请")
                .msg("为了拥有更好的体验,请允许以下权限")
                .permissions(permissionItems)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onFinish() {
                        //启动service
                        Intent intent = new Intent(MainActivity.this, SmsRunningService.class);
                        startService(intent);
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                        Log.i(TAG, "onDeny");
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                        Log.i(TAG, "onGuarantee");
                    }
                });
    }
}
