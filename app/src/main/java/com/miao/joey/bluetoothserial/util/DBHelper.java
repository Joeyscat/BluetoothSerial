package com.miao.joey.bluetoothserial.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.miao.joey.bluetoothserial.entity.Message;


public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";

    // 数据库版本号
    public static final int DATABASE_VERSION = 1;
    // 数据库名称
    public static final String DATABASE_NAME = "bluetooth_cm";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 创建数据表
        String CREATE_TABLE_BT_MSG = "CREATE TABLE " + Message.TABLE + "("
                + Message.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Message.KEY_content + " TEXT, "
                + Message.KEY_RECEIVE_TIME + " TEXT,"
                + Message.KEY_RECEIVE_DATE + " TEXT,"
                + Message.KEY_DEVICE_NAME + " TEXT"
                + ")";
        Log.i(TAG, "onCreate: " + CREATE_TABLE_BT_MSG);
        sqLiteDatabase.execSQL(CREATE_TABLE_BT_MSG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // 如果有旧表存在,删除
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Message.TABLE);
        Log.i(TAG, "onUpgrade: 删除表");
        // 新建表
        onCreate(sqLiteDatabase);
    }
}
