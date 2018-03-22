package com.miao.joey.bluetoothserial.util;

import android.os.Environment;
import android.util.Log;

import com.miao.joey.bluetoothserial.entity.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by Joey on 2018/3/23.
 * 用于创建文件，写入内容
 */

public class FileUtil {
    private static final String TAG = "FileUtil";
    private String fileName;
    private String parentPath = File.separator + "监测数据";
    private File parent;
    private String content;

    public FileUtil() {
        parent = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)) + parentPath);
        if (!parent.exists() || !parent.isDirectory()) {
            parent.mkdirs();
        }
    }

    public File createNewFile(String fileName) {
        File file = new File(parent, fileName);
        Log.d(TAG, "createNewFile: " + file.getAbsolutePath());
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public boolean saveDataToFile(File file, List<Message> messages) {
        // 格式化数据
        StringBuilder builder = new StringBuilder();
        builder.append("\t环境温度\t\t\t记录时间\r\n");
        for (int i = 0; i < messages.size(); i++) {
            builder.append("\t").append(messages.get(i).getContent()).append("\t\t\t").append(messages.get(i).getReceive_time()).append("\r\n");
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(String.valueOf(builder));
            Log.d(TAG, "saveDataToFile: " + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
