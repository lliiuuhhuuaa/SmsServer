package com.lh.sms.message.service;

import android.widget.BaseAdapter;
import android.widget.ListView;

import com.lh.sms.MainActivity;
import com.lh.sms.message.util.ListViewUtil;
import com.lh.sms.util.ClassUtil;

import java.util.ArrayList;
import java.util.List;
/**
 * @do 消息显示
 * @author liuhua
 * @date 2020/3/20 8:23 PM
 */
public class MsgService {
    private List<String> list = new ArrayList<>();
    private ListView listView = null;
    public  MsgService(){
        listView = ListViewUtil.handleResult(ClassUtil.get(MainActivity.class), list);
    }
    /**
     * @do 显示消息
     * @author liuhua
     * @date 2020/3/20 8:33 PM
     */
    public void push(String msg){
        list.add(msg);
        if(list.size()>100){
            list.remove(0);
        }
        ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
    }

}
