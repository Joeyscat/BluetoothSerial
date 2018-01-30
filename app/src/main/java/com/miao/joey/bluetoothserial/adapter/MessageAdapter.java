package com.miao.joey.bluetoothserial.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.miao.joey.bluetoothserial.R;
import com.miao.joey.bluetoothserial.entity.Message;

import java.util.List;

/**
 * Created by Joey on 2017/12/4.
 */

public class MessageAdapter extends BaseAdapter {

    private Context context;
    private List<Message> list;

    public MessageAdapter(Context context, List<Message> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message, null);
            holder = new ViewHolder();
            holder.item_content = convertView.findViewById(R.id.item_content);
            holder.item_time = convertView.findViewById(R.id.item_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.item_content.setText(list.get(position).getContent());
        holder.item_time.setText(list.get(position).getReceive_time());
        return convertView;
    }

    static class ViewHolder {
        TextView item_content, item_time;
    }
}
