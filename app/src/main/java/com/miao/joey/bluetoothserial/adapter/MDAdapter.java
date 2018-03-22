package com.miao.joey.bluetoothserial.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miao.joey.bluetoothserial.R;
import com.miao.joey.bluetoothserial.entity.Message;
import com.solo.library.SlideBaseAdapter;
import com.solo.library.SlideTouchView;

import java.util.List;

/**
 * 该适配器用于可进行滑动操作的slide_touch_view
 * Created by Joey on 2018/3/21.
 */

public class MDAdapter extends SlideBaseAdapter {
    private List<Message> list;
    private Context context;

    public MDAdapter(List list) {
        this.list = list;
    }

    public MDAdapter(List<Message> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int[] getBindOnClickViewsIds() {
        return new int[]{R.id.btn_del}; //
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_touch_view, null);
            viewHolder.tv_date = convertView.findViewById(R.id.item_date);
            viewHolder.tv_device = convertView.findViewById(R.id.item_device_connected);
            viewHolder.slideTouchView = convertView.findViewById(R.id.mSlide);
            convertView.setTag(viewHolder);

            bindSlideState(viewHolder.slideTouchView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        bindSlidePosition(viewHolder.slideTouchView,position);

        viewHolder.tv_date.setText(String.valueOf(list.get(position).getReceive_date()));
        viewHolder.tv_device.setText(String.valueOf(list.get(position).getDevice_name()));
        return convertView;
    }

    static class ViewHolder {
        SlideTouchView slideTouchView;
        TextView tv_date,tv_device;
    }
}
