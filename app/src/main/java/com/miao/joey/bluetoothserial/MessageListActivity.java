package com.miao.joey.bluetoothserial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.miao.joey.bluetoothserial.adapter.MessageAdapter;
import com.miao.joey.bluetoothserial.entity.Message;
import com.miao.joey.bluetoothserial.util.MessageRepo;

import java.util.ArrayList;
import java.util.List;

public class MessageListActivity extends AppCompatActivity {
    private static final String TAG = "MessageListActivity";
    ListView message_list_view;
    private List<Message> messages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        message_list_view=findViewById(R.id.message_list);

        MessageRepo messageRepo=new MessageRepo(this);
        ArrayList<Message> messages=messageRepo.getMessageList();
        if (messages.size()!=0){
            MessageAdapter adapter=new MessageAdapter(this,messages);
            message_list_view.setAdapter(adapter);
        }
    }
}
