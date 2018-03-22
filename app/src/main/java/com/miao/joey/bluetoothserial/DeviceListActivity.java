package com.miao.joey.bluetoothserial;

import android.Manifest;
import android.app.ProgressDialog;
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
//import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

import com.miao.joey.bluetoothserial.adapter.DeviceAdapter;
import com.miao.joey.bluetoothserial.entity.Message;
import com.miao.joey.bluetoothserial.service.BluetoothService;
import com.miao.joey.bluetoothserial.util.MessageRepo;
import com.miao.joey.bluetoothserial.view.NestedListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 思路:
 * 打开应用自动搜索获取设备(已配对设备与附近设备)
 * 点击设备列表项进行连接,连接成功自动打开数据接收界面
 * 未连接设备时,点击悬浮按钮进行设备搜索
 * 已连接设备时,点击悬浮按钮打开数据接收界面
 */
public class DeviceListActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "DeviceListActivity";
    public static final int ACCESS_LOCATION = 1001;
    private BluetoothAdapter mBluetoothAdapter; // 系统蓝牙适配器,用来开启/搜索/关闭蓝牙功能
    private List<BluetoothDevice> nearbyDevices; // 已配对/附近设备
    private DeviceAdapter nearbyAdapter; // 适配器,将设备信息显示到ListView
    private NestedListView lv_nearbyDevice; // 设备列表视图
    private BluetoothService service; // 蓝牙服务
    private MyServiceConn conn; // 蓝牙服务连接
    private ContentReceiver mReceiver;  // 广播接收器
    private static Toast toast = null;
    private ProgressDialog connectingDialog;
    private SwipeRefreshLayout refreshDevice;
    private View footerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        connectingDialog = new ProgressDialog(DeviceListActivity.this);
        refreshDevice = findViewById(R.id.refresh_device);
        footerView = getLayoutInflater().inflate(R.layout.refresh_layout, null);
        lv_nearbyDevice = findViewById(R.id.lv_nearby_device);
        lv_nearbyDevice.addFooterView(footerView);
        lv_nearbyDevice.setOnScrollListener(this);

        nearbyDevices = new ArrayList<>();
        initBluetooth();
        showListView();

        // 绑定蓝牙功能服务
        conn = new MyServiceConn();
        bindService(new Intent(this, BluetoothService.class), conn, BIND_AUTO_CREATE);
        doRegisterReceiver();
        doDiscovery();

        refreshDevice.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        refreshDevice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doDiscovery();
            }
        });
    }

    /**
     * 初始化蓝牙设备(获取BluetoothAdapter并开启蓝牙)
     */
    private void initBluetooth() {
        // 获得BluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            toast.makeText(DeviceListActivity.this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean enable = doEnableBt();
    }

    /**
     * ；连接等待的dialog，屏蔽其他控件的交互能力
     */
    private void showWaitingDialog() {
        connectingDialog.setTitle("蓝牙连接");
        connectingDialog.setMessage("蓝牙连接中...");
        connectingDialog.setIndeterminate(true);
        connectingDialog.setCancelable(false); // 不可取消
        connectingDialog.show();
    }

    /**
     * 注册广播接收器
     */
    private void doRegisterReceiver() {
        mReceiver = new ContentReceiver();
        IntentFilter btMessage = new IntentFilter("com.miao.joey.bluetoothcallback.content");
        registerReceiver(mReceiver, btMessage);
        // 注册广播:找到设备.完成搜索
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//发现设备
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索完成
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化
//        filter.setPriority(Integer.MAX_VALUE);// 设置优先级
        registerReceiver(mReceiver, filter);
    }

    /**
     * 打开蓝牙
     *
     * @return true-打开成功  false-打开失败
     */
    private boolean doEnableBt() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 打开数据接收界面
     */
    private void openReceive() {
        Intent receive = new Intent(DeviceListActivity.this, ReceiveActivity.class);
        startActivity(receive);
    }

    /**
     * 搜索附近设备
     *
     * @return
     */
    private boolean doDiscovery() {
        getPermission();
        nearbyDevices.clear();
        doEnableBt();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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
        unbindService(conn); // 解绑蓝牙服务
        if (mReceiver != null) {
            unregisterReceiver(mReceiver); // 注销广播接收器
        }
        mBluetoothAdapter.disable(); // 关闭蓝牙
        if (toast != null)
            toast.cancel();
    }

    /**
     * 广播接收器,接收以下广播:
     * 连接状态, 搜索状态, 发现的设备
     */
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
                connectingDialog.cancel();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) { // 发现设备
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
                if (nearbyDevices.isEmpty())
                    toast.makeText(DeviceListActivity.this, "搜索完成", Toast.LENGTH_SHORT).show();
                if (refreshDevice.isRefreshing()) {
                    refreshDevice.setRefreshing(false);//设置不刷新
                }
            } else {
                Log.i(TAG, "onReceive: Nothing");
            }
        }
    }

    /**
     * 显示附近设备
     */
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
                if (BluetoothService.CONNECT_STATUS != 0) {
                    service.stop();
                }
                BluetoothDevice selectedDevice = (BluetoothDevice) nearbyAdapter.getItem(position);
                service.connect(selectedDevice);
                // 启动连接时开启dialog屏蔽用户操作
                showWaitingDialog();
            }
        });
    }

    /**
     * 获取权限
     */
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

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_message_view) {
            if (BluetoothService.CONNECT_STATUS != 1) {
                toast.makeText(DeviceListActivity.this, "未连接可用设备", Toast.LENGTH_SHORT).show();
                return true;
            }
            openReceive();
            return true;
        } else if (id == R.id.action_file_manage) {
            Intent receive = new Intent(DeviceListActivity.this, MessageDateListActivity.class);
            startActivity(receive);
            return true;
        } else if (id == R.id.action_nothing) {
            MessageRepo messageRepo = new MessageRepo(DeviceListActivity.this);
            String[] devices = {"温度采集", "盐度采集"};
            String deviceName = devices[(int) ((Math.random() * 2))];
            SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat t = new SimpleDateFormat("HH:mm:ss");
            long now = System.currentTimeMillis();
            String date = d.format(new Date((long) (Math.random() * now)));
            Message message = new Message();
            message.setReceive_date(date);
            message.setDevice_name(deviceName);
            final int[] wendu = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

            for (int i = 0; i <= 20; i++) {
                //  保存到数据库
                String time = t.format(new Date());
                message.setContent(wendu[(int) (Math.random()*3)] + ""+wendu[(int) (Math.random()*10)] + "℃ --");
                message.setReceive_time(time);
                messageRepo.insert(message);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}