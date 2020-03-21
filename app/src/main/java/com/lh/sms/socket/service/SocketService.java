package com.lh.sms.socket.service;

import android.os.Bundle;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;
import com.lh.sms.MainActivity;
import com.lh.sms.enums.CountDownMsgTypeEnum;
import com.lh.sms.enums.ResultCodeEnum;
import com.lh.sms.handle.HandleMessage;
import com.lh.sms.message.service.MsgService;
import com.lh.sms.message.service.SmsService;
import com.lh.sms.socket.enums.MsgHandleTypeEnum;
import com.lh.sms.util.ClassUtil;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketService {
    //socket连接
    private Socket socket;
    /**
     * @do 开始连接socket
     * @author liuhua
     * @date 2020/3/14 11:05 PM
     * @param iccId
     */
    public void connect(String iccId){
        try{
            if(socket!=null){
                //先断开之前连接
                socket.disconnect();
            }
            sendShowMsg(true,"开始连接服务器...");
            socket = IO.socket(String.format("http://118.190.102.137?iccId=%s&type=sms",iccId));
            //连接超时事件
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
                sendShowMsg(false,"服务器连接超时");
            });
            //连接成功事件
            socket.on(Socket.EVENT_CONNECT, args -> {
                sendShowMsg(true,"服务器连接成功");
            });
            socket.on(Socket.EVENT_ERROR,args -> {
                sendShowMsg(false,"服务器连接错误");
            });
            socket.on("connect_failed", args -> {
                if(!(args[0] instanceof Exception)){
                    JSONObject jsonObject = JSONObject.parseObject(args[0].toString());
                    sendShowMsg(false,jsonObject.getString("msg"));
                }
            });
            socket.on(MsgHandleTypeEnum.SMS.getValue(), args -> {
                JSONObject jsonObject = JSONObject.parseObject(args[0].toString());
                if(!ResultCodeEnum.OK.getValue().equals(jsonObject.getInteger("code"))){
                    sendShowMsg(false,jsonObject.getString("msg"));
                    return;
                }
                jsonObject = jsonObject.getJSONObject("body");
                jsonObject = ClassUtil.get(SmsService.class).sendSms(jsonObject.getString("phone"), jsonObject.getString("iccId"), jsonObject.getString("text"));
                if(!ResultCodeEnum.OK.getValue().equals(jsonObject.getInteger("code"))) {
                    sendShowMsg(false, jsonObject.toJSONString());
                }
            });
            socket.on(Socket.EVENT_ERROR,args -> {
                sendShowMsg(false,"服务器连接错误");
            });
            socket.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @do 发送消息
     * @author liuhua
     * @date 2020/3/15 1:06 PM
     */
    public void sendMessage(String handle, Object obj, Ack ack){
        if(socket!=null){
            socket.emit(handle,JSONObject.toJSONString(obj),ack);
        }
    }
    /**
     * @do 发送显示消息
     * @author liuhua
     * @date 2020/3/20 7:36 PM
     */
    private void sendShowMsg(boolean success,String msg){
        HandleMessage handleMessage = ClassUtil.get(HandleMessage.class);
        Message message = Message.obtain(handleMessage, CountDownMsgTypeEnum.CALL_BACK.getValue());
        Bundle data = message.getData();
        message.obj = new Object[]{MsgService.class,msg};
        data.putString("method","push");
        data.putBoolean("success",success);
        data.putString("msg", msg);
        handleMessage.sendMessage(message);
    }
}
