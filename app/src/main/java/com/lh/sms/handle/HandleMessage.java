package com.lh.sms.handle;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.lh.sms.MainActivity;
import com.lh.sms.enums.CountDownMsgTypeEnum;
import com.lh.sms.util.ClassUtil;

import java.lang.reflect.Method;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HandleMessage extends Handler {
    public HandleMessage() {
        super();
    }

    @Override
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        if(msg.what== CountDownMsgTypeEnum.ALERT_TOAST.getValue()){
            //弹系统提示
            Bundle data = msg.getData();
            Toast.makeText(ClassUtil.get(MainActivity.class),data.getString("msg"),data.getInt("length")).show();
        }else if(msg.what==CountDownMsgTypeEnum.ALERT_SWEET.getValue()){
            //弹自定义提示框
            ((SweetAlertDialog)msg.obj).show();
        }else if(msg.what == CountDownMsgTypeEnum.CALL_BACK.getValue()){
            //回调指定类方法
            Bundle data = msg.getData();
            String methodName = data.getString("method");
            Object[] objects= (Object[]) msg.obj;
            Object service = ClassUtil.get((Class<? extends Object>) objects[0]);
            if(service==null){
                return;
            }
            try {
                assert methodName != null;
                if(objects.length>1) {
                    Method method = service.getClass().getMethod(methodName, objects[1].getClass());
                    method.invoke(service,objects[1]);
                }else{
                    Method method = service.getClass().getMethod(methodName);
                    method.invoke(service);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
