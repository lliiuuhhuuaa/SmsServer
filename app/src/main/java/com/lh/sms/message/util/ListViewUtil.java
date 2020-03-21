package com.lh.sms.message.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lh.sms.MainActivity;
import com.lh.sms.R;

import java.util.List;

public class ListViewUtil {

    /**
     * 显示结果
     * @param context
     * @param list
     */
    public static ListView handleResult(MainActivity context, List<String> list) {
        ListView listView = context.findViewById(R.id.resultListView);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = context.getLayoutInflater();
                View view = convertView==null?inflater.inflate(R.layout.list_item, null):convertView;
                String text = list.get(position);
                TextView textView = view.findViewById(R.id.list_item);
                textView.setText(text);
                return view;
            }
        });
        return listView;
    }
}
