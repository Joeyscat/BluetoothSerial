package com.miao.joey.bluetoothserial;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.miao.joey.bluetoothserial.adapter.MDAdapter;
import com.miao.joey.bluetoothserial.entity.Message;
import com.miao.joey.bluetoothserial.util.FileUtil;
import com.miao.joey.bluetoothserial.util.MessageRepo;
import com.solo.library.ISlide;
import com.solo.library.OnClickSlideItemListener;

import java.io.File;
import java.util.ArrayList;

/**
 * 显示日期列表，点击某个列表项会打开该日期监测数据列表
 */
public class ManageDataActivity extends AppCompatActivity {
    private static final String TAG = "MessageDateListActivity";
    ListView message_list_view;
    private ArrayList<Message> messages;
    private FileUtil fileUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_data);

        message_list_view = findViewById(R.id.message_date_list);

        fileUtil = new FileUtil();
        final MessageRepo messageRepo = new MessageRepo(this);
        messages = messageRepo.getDateList();

        if (messages.size() != 0) {
            for (int i = 0; i < messages.size(); i++) {
                Log.d(TAG, "messageByDate: " + messages.get(i));
            }

            final MDAdapter adapter = new MDAdapter(messages, this);
            message_list_view.setAdapter(adapter);

            message_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // 获得选择的日期
                    String selectedDate = ((Message) adapter.getItem(position)).getReceive_date();
                    String selectedDevice = ((Message) adapter.getItem(position)).getDevice_name();
                    // 将日期发送到MessgeActivity
                    Intent showMessage = new Intent(ManageDataActivity.this, DataListActivity.class);
                    showMessage.putExtra("date", selectedDate);
                    showMessage.putExtra("device", selectedDevice);
                    startActivity(showMessage);
                }
            });
            adapter.setupListView(message_list_view);

            adapter.setOnClickSlideItemListener(new OnClickSlideItemListener() {
                @Override
                public void onItemClick(ISlide iSlide, View view, int position) {
                    String selectedDate = ((Message) adapter.getItem(position)).getReceive_date();
                    String selectedDevice = ((Message) adapter.getItem(position)).getDevice_name();
                    // 将日期发送到MessgeActivity
                    Intent showMessage = new Intent(ManageDataActivity.this, DataListActivity.class);
                    showMessage.putExtra("date", selectedDate);
                    showMessage.putExtra("device", selectedDevice);
                    startActivity(showMessage);
                }

                @Override
                public void onClick(ISlide iSlide, View view, int position) {
                    String date = messages.get(position).getReceive_date();
                    String device = messages.get(position).getDevice_name();
                    switch (view.getId()) {
                        case R.id.btn_output:
                            iSlide.close();
                            // 拼接文件名并新建文件  date+device
                            String fileName = date + device + ".txt";
                            Log.d(TAG, "filename: " + fileName);
                            File file = fileUtil.createNewFile(fileName);
                            // 获得数据库中的数据并保存到文件
                            if (!file.exists()) {
                                Toast.makeText(ManageDataActivity.this,
                                        "导出数据失败", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            boolean saved = fileUtil.saveDataToFile(file, messageRepo.getListByDateAndDevice(date, device));
                            Toast.makeText(ManageDataActivity.this,
                                    saved ? "数据已经保存到" + file.getAbsolutePath() : "导出数据失败", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.btn_del: // 需在adapter绑定
                            iSlide.close(); // 关闭当前按钮
                            // 从视图移除该item
                            messages.remove(position);
                            // 从数据库删除该item
                            int deleteCount = messageRepo.deleteByDateAndDevice(date, device);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(ManageDataActivity.this,
                                    "删除了" + deleteCount + "条数据", Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            break;
                    }
                }
            });
        }
    }
}
