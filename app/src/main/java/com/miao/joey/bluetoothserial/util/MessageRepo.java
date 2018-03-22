package com.miao.joey.bluetoothserial.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.miao.joey.bluetoothserial.entity.Message;

import java.util.ArrayList;

/**
 * 针对蓝牙消息数据的操作类,包含增删查改
 */
public class MessageRepo {

    private static final String TAG = "MessageRepo";

    private DBHelper dbHelper;
    SQLiteDatabase databaseWrite;

    public MessageRepo(Context context) {
        dbHelper = new DBHelper(context);
        databaseWrite = dbHelper.getWritableDatabase();
    }

    public int insert(Message message) {
        // 打开连接, 写入数据
        ContentValues values = new ContentValues();
        values.put(Message.KEY_content, message.getContent());
        values.put(Message.KEY_RECEIVE_DATE, message.getReceive_date());
        values.put(Message.KEY_RECEIVE_TIME, message.getReceive_time());
        values.put(Message.KEY_DEVICE_NAME, message.getDevice_name());

        long message_Id = databaseWrite.insert(Message.TABLE, null, values);
        Log.i(TAG, "insert: " + message.toString());
        return (int) message_Id;
    }

    public void deleteByDate(String date) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Log.i(TAG, "delete: " + date);
        database.delete(Message.TABLE, Message.KEY_RECEIVE_DATE + "=?", new String[]{date});
        database.close();
    }

    public void update(Message message) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Message.KEY_content, message.getContent());
        values.put(Message.KEY_RECEIVE_TIME, message.getReceive_time());

        database.update(Message.TABLE, values, Message.KEY_ID + "=?",
                new String[]{String.valueOf(message.getMessage_id())});
        Log.i(TAG, "update: " + values.toString());
        database.close();
    }

    public ArrayList<Message> getListByDateAndDevice(String date, String device) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT " +
                Message.KEY_ID + "," +
                Message.KEY_content + "," +
                Message.KEY_RECEIVE_TIME + "," +
                Message.KEY_DEVICE_NAME +
                " FROM " + Message.TABLE +
                " WHERE " + Message.KEY_RECEIVE_DATE +
                "='" + date + "' and " + Message.KEY_DEVICE_NAME + "='" + device + "'";
        Log.i(TAG, "getMessageList: " + selectQuery);
        ArrayList<Message> messageList = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setMessage_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Message.KEY_ID))));
                message.setContent(cursor.getString(cursor.getColumnIndex(Message.KEY_content)));
                message.setReceive_time(cursor.getString(cursor.getColumnIndex(Message.KEY_RECEIVE_TIME)));
                messageList.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return messageList;
    }

    // 获取通过日期列表
    public ArrayList<Message> getDateList() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT " +
                Message.KEY_RECEIVE_DATE +
                "," + Message.KEY_DEVICE_NAME +
                " FROM " + Message.TABLE +
                " GROUP BY " + Message.KEY_RECEIVE_DATE + "," + Message.KEY_DEVICE_NAME;
        Log.i(TAG, "getDateList: " + selectQuery);
        ArrayList<Message> dateList = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setReceive_date(cursor.getString(cursor.getColumnIndex(Message.KEY_RECEIVE_DATE)));
                message.setDevice_name(cursor.getString(cursor.getColumnIndex(Message.KEY_DEVICE_NAME)));
                dateList.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return dateList;
    }

    public Message getMessageById(int messageId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT " +
                Message.KEY_ID + "," +
                Message.KEY_content + "," +
                Message.KEY_RECEIVE_TIME + " FROM " +
                Message.TABLE + " WHERE " +
                Message.KEY_ID + "=?";
        Log.i(TAG, "getMessageById: " + selectQuery);
        Message message = new Message();
        Cursor cursor = database.rawQuery(selectQuery, new String[]{String.valueOf(messageId)});
        if (cursor.moveToFirst()) {
            do {
                message.setMessage_id(cursor.getInt(cursor.getColumnIndex(Message.KEY_ID)));
                message.setContent(cursor.getString(cursor.getColumnIndex(Message.KEY_content)));
                message.setReceive_time(cursor.getString(cursor.getColumnIndex(Message.KEY_RECEIVE_TIME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return message;
    }

    public void closeDB() {
        try {
            if (databaseWrite != null) {
                databaseWrite.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            databaseWrite.close();
        }
    }

    public int deleteByDateAndDevice(String date, String deviceName) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Log.i(TAG, "delete: " + date);
        int deleteCount=database.delete(Message.TABLE, Message.KEY_RECEIVE_DATE + "=? AND "
                + Message.KEY_DEVICE_NAME + "=?", new String[]{date, deviceName});
        database.close();
        return deleteCount;
    }
}
