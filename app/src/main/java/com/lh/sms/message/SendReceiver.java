package com.lh.sms.message;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSONObject;
import com.lh.sms.enums.ResultCodeEnum;
import com.lh.sms.message.service.MsgService;
import com.lh.sms.message.service.SmsService;
import com.lh.sms.socket.entity.SocketMessage;
import com.lh.sms.socket.enums.MsgHandleTypeEnum;
import com.lh.sms.socket.enums.SmsSocketCodeEnum;
import com.lh.sms.socket.service.SocketService;
import com.lh.sms.util.ClassUtil;

public class SendReceiver extends BroadcastReceiver {
    public static final String ACTION = "action.send.sms";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION.equals(action)) {
            int resultCode = getResultCode();
            if (resultCode == Activity.RESULT_OK) {
                SocketMessage socketMessage = new SocketMessage();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("iccId",intent.getStringExtra("iccId"));
                socketMessage.setBody(jsonObject);
                socketMessage.setCode(SmsSocketCodeEnum.SEND.getValue());
                // 发送成功
                ClassUtil.get(MsgService.class).push(String.format("发送成功:手机号[%s]->内容[%s]",
                        intent.getStringExtra("phone"),intent.getStringExtra("msg")));
                ClassUtil.get(SocketService.class).sendMessage(MsgHandleTypeEnum.SMS.getValue(),socketMessage,null);
            } else {
                // 发送失败
                ClassUtil.get(MsgService.class).push(String.format("发送失败:手机号[%s]->内容[%s]",
                        intent.getStringExtra("phone"),intent.getStringExtra("msg")));
            }
        }
        context.unregisterReceiver(this);
    }


}