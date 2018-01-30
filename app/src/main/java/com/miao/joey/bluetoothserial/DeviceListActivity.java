package com.miao.joey.bluetoothserial;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Toast;

import com.miao.joey.bluetoothserial.adapter.DeviceAdapter;
import com.miao.joey.bluetoothserial.service.BluetoothService;
import com.miao.joey.bluetoothserial.view.NestedListView;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {

    private static final String TAG = "DeviceListActivity";
    public static final int ACCESS_LOCATION = 1001;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> nearbyDevices; // 已配对/附近设备
    private DeviceAdapter nearbyAdapter; // 适配器,将设备信息显示到ListView
    private NestedListView lv_nearbyDevice;
    private BluetoothService service;
    private MyServiceConn conn;
    private ContentReceiver mReceiver;
    private static Toast toast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "正在搜索蓝牙设备", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                doDiscovery();
            }
        });
        lv_nearbyDevice = findViewById(R.id.lv_nearby_device); // 显示附近设备信息
        nearbyDevices = new ArrayList<>();
        initBluetooth();
        showListView();

        // 绑定蓝牙功能服务
        conn = new MyServiceConn();
        bindService(new Intent(this, BluetoothService.class), conn, BIND_AUTO_CREATE);
        // 注册广播接收器
        doRegisterReceiver();
    }

    private void initBluetooth() {
        // 获得BluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            toast.makeText(DeviceListActivity.this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        doEnableBt();
    }

    private void doRegisterReceiver() {
        // 注册广播,用于接收BluetoothService消息
        mReceiver = new ContentReceiver();
        IntentFilter btmessage = new IntentFilter("com.miao.joey.bluetoothcallback.content");
        registerReceiver(mReceiver, btmessage);
        // 注册广播:找到设备.完成搜索
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化

//        filter.setPriority(Integer.MAX_VALUE);// 设置优先级
        registerReceiver(mReceiver, filter);
    }
    /*public void bluetooth_button(View view) {
        switch (view.getId()) {
            case R.id.btn_open:
                doEnableBt();
                break;
            case R.id.btn_close:
                service.stop();
                break;
            case R.id.btn_discovery:
                if (!doEnableBt()) {
                    return;
                }
                getPermission();
                nearbyDevices.clear();
                doDiscovery();
                break;
            case R.id.btn_flush:
                if (!doEnableBt()) {
                    return;
                }
                toast.makeText(DeviceListActivity.this, "正在刷新设备信息", Toast.LENGTH_SHORT).show();
                initView();
                break;

//            case R.id.btn_receive:
                openReceive();
                break;
            default:
                break;
        }
    }*/

    private boolean doEnableBt() {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        return mBluetoothAdapter.isEnabled();
    }

    private void openReceive() {
        Intent receive = new Intent(DeviceListActivity.this, ReceiveActivity.class);
        startActivity(receive);
    }

    private void doDiscovery() {
        getPermission();
        nearbyDevices.clear();
        doEnableBt();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        toast.makeText(DeviceListActivity.this, "正在搜索设备", Toast.LENGTH_SHORT).show();
        mBluetoothAdapter.startDiscovery();
    }

    public class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "onServiceConnected: ");
            service = ((BluetoothService.LocalBinder) iBinder).getService();
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
        unbindService(conn);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        mBluetoothAdapter.disable();
        toast.cancel();
    }

    public class ContentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if ("com.miao.joey.bluetoothcallback.content".equals(action)) {
                String content = intent.getStringExtra("content");
                if ("CONNECT_SUCCESSFUL".equals(content)) {
                    toast.makeText(DeviceListActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    openReceive();
                } else if ("CONNECT_FAILURE".equals(content)) {
                    toast.makeText(DeviceListActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) { // 发现设备
                Log.d(TAG, "onReceive: 搜索到设备");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (nearbyDevices == null || !nearbyDevices.contains(device)) {
                    if (nearbyDevices == null) {
                        nearbyDevices = new ArrayList<>();
                    }
                    nearbyDevices.add(device);
                }
                Log.d(TAG, "onReceive: nearbyDevices:" + (nearbyDevices == null ? "null" : nearbyDevices.toString()));
                showListView();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "onReceive: 搜索完成");
                toast.makeText(DeviceListActivity.this, "搜索完成", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "onReceive: Nothing");
            }
        }
    }

    private void showListView() {

        if (nearbyDevices != null) {
            showNearby(nearbyDevices);
        }
    }

    /**
     * 将未配对设备显示到UI并设置监听
     *
     * @param nearbyDevices 附近设备集合
     */
    private void showNearby(List<BluetoothDevice> nearbyDevices) {
        nearbyAdapter = new DeviceAdapter(this, nearbyDevices);
        lv_nearbyDevice.setAdapter(nearbyAdapter);

        lv_nearbyDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                BluetoothDevice selectedDevice = (BluetoothDevice) nearbyAdapter.getItem(position);
                service.connect(selectedDevice);
            }
        });
    }

    public void getPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            int permissionCheck;
            permissionCheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_LOCATION);
            } else {
                Log.d(TAG, "getPermission: 已经获得权限");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ACCESS_LOCATION:
                if (hasAllPermissionGranted(grantResults)) {
                    Log.d(TAG, "onRequestPermissionsResult: OK");
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: NOT OK");
                }
                break;
        }
    }

    private boolean hasAllPermissionGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
