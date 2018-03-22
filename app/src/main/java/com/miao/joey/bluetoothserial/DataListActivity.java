package com.miao.joey.bluetoothserial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.miao.joey.bluetoothserial.adapter.MessageAdapter;
import com.miao.joey.bluetoothserial.entity.Message;
import com.miao.joey.bluetoothserial.util.MessageRepo;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示某天的数据
 */
public class DataListActivity extends AppCompatActivity {
    private static final String TAG = "MessageListActivity";
    ListView message_list_view;
    private List<Message> messages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_list);
        message_list_view = findViewById(R.id.message_list);

        // 获得选中日期
        String date = getIntent().getStringExtra("date");
        String device = getIntent().getStringExtra("device");
        Log.d(TAG, date + "  " + device);

        MessageRepo messageRepo = new MessageRepo(this);
        ArrayList<Message> messages = messageRepo.getListByDateAndDevice(date,device);
        if (messages.size() != 0) {
            Log.d(TAG, "message[0]: " + messages.get(0));
            MessageAdapter adapter = new MessageAdapter(this, messages);
            message_list_view.setAdapter(adapter);
        }
    }
}
