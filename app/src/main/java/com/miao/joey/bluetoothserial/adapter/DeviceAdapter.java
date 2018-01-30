package com.miao.joey.bluetoothserial.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.miao.joey.bluetoothserial.R;

import java.util.List;

/**
 * Created by Joey on 2017/12/4.
 */

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<BluetoothDevice> list;

    public DeviceAdapter(Context context, List<BluetoothDevice> list) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_device, null);
            holder = new ViewHolder();
            holder.item_name = convertView.findViewById(R.id.item_name);
            holder.item_address = convertView.findViewById(R.id.item_address);
            holder.item_rssi = convertView.findViewById(R.id.rssi);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.item_name.setText(list.get(position).getName());
        holder.item_address.setText(list.get(position).getAddress());
        if (list.get(position).getBondState() == 10) {
            holder.item_rssi.setText("未配对");
        }else if (list.get(position).getBondState() == 12){
            holder.item_rssi.setText("已配对");
        }
        return convertView;
    }

    static class ViewHolder {
        TextView item_name, item_address, item_rssi;
    }
}
