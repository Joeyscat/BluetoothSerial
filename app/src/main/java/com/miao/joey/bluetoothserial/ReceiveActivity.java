package com.miao.joey.bluetoothserial;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import com.miao.joey.bluetoothserial.entity.Message;
import com.miao.joey.bluetoothserial.service.BluetoothService;
import com.miao.joey.bluetoothserial.util.MessageRepo;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class ReceiveActivity extends AppCompatActivity {
    private static final String TAG = "ReceiveActivity";
    private BluetoothService service;
    private TextView mContent;
    private EditText mCommand;
    private MyServiceConn conn;
    private ContentReceiver mReceiver;
    private MessageRepo messageRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        mCommand = findViewById(R.id.et_command);
        mContent = findViewById(R.id.tv_content);

        conn = new MyServiceConn();
        Log.i(TAG, "onCreate: 绑定蓝牙服务");
        bindService(new Intent(ReceiveActivity.this, BluetoothService.class), conn, BIND_AUTO_CREATE);

        doRegisterReceiver();
    }

    private void doRegisterReceiver() {
        mReceiver = new ContentReceiver();
        IntentFilter filter = new IntentFilter("com.miao.joey.bluetoothcallback.content");
        registerReceiver(mReceiver, filter);
    }

    public void bluetooth_button(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                service.stop();
                break;
            case R.id.bt_file_manage:
                Intent receive = new Intent(ReceiveActivity.this, MessageListActivity.class);
                startActivity(receive);
                break;
            case R.id.bt_send: // 发送按钮: ! 停止符 $ 开始符
                String endSign="\n"; // 结束标志
                String commandStr= String.valueOf(mCommand.getText())+endSign;
                byte[] command=commandStr.getBytes();
                Log.i(TAG, "bluetooth_button: "+ Arrays.toString(command));
                service.write(command);
                break;
            default:
                break;
        }
    }

    public final class MyServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "onServiceConnected: ");
            service = ((BluetoothService.LocalBinder) iBinder).getService();
            // 创建数据库
            if (service != null) {
                messageRepo=new MessageRepo(ReceiveActivity.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected: ");
            service = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: 解绑服务");
        unbindService(conn);
        if (mReceiver != null) {
            Log.i(TAG, "onDestroy: 注销广播");
            unregisterReceiver(mReceiver);
        }
    }

    public class ContentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action:" + action);
            if ("com.miao.joey.bluetoothcallback.content".equals(action)) {
                String content = intent.getStringExtra("content");
                Log.i(TAG, "广播中接收: " + content);
                SimpleDateFormat d = new SimpleDateFormat("HH:mm:ss");
                String data = d.format(new Date());
                showMessage(content);

                //  保存到数据库
                Message message=new Message();
                message.setContent(content);
                message.setReceive_time(data);
                messageRepo.insert(message);
            }
        }
    }

    // TODO
    public void showMessage(String message) {
        Log.i(TAG, "显示到UI: "+message);
        mContent.append(message+"\n");
    }
}
