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

    public MessageRepo(Context context) {
        dbHelper = new DBHelper(context);
    }

    public int insert(Message message) {
        // 打开连接, 写入数据
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Message.KEY_content, message.getContent());
        values.put(Message.KEY_time, message.getReceive_time());

        long message_Id = database.insert(Message.TABLE, null, values);
        Log.i(TAG, "insert: "+message.toString());
        database.close(); // TODO 数据写入频率高,在此关闭是否合适
        return (int) message_Id;
    }

    public void delete(int messageId){
        SQLiteDatabase database=dbHelper.getWritableDatabase();
        Log.i(TAG, "delete: "+messageId);
        database.delete(Message.TABLE, Message.KEY_ID+"=?",new String[]{String.valueOf(messageId)});
        database.close();
    }

    public void update(Message message){
        SQLiteDatabase database=dbHelper.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put(Message.KEY_content,message.getContent());
        values.put(Message.KEY_time,message.getReceive_time());

        database.update(Message.TABLE,values, Message.KEY_ID+"=?",new String[]{String.valueOf(message.getMessage_id())});
        Log.i(TAG, "update: "+values.toString());
        database.close();
    }

    public ArrayList<Message> getMessageList() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT " +
                Message.KEY_ID + "," +
                Message.KEY_content + "," +
                Message.KEY_time + " FROM " + Message.TABLE;
        Log.i(TAG, "getMessageList: "+selectQuery);
        ArrayList<Message> messageList= new ArrayList<>();
        Cursor cursor=database.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()){
            do {
                Message message=new Message();
                message.setMessage_id(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Message.KEY_ID))));
                message.setContent(cursor.getString(cursor.getColumnIndex(Message.KEY_content)));
                message.setReceive_time(cursor.getString(cursor.getColumnIndex(Message.KEY_time)));
                messageList.add(message);
            }while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return messageList;
    }

    public Message getMessageById(int messageId){
        SQLiteDatabase database=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Message.KEY_ID+","+
                Message.KEY_content+","+
                Message.KEY_time+" FROM "+
                Message.TABLE+" WHERE "+
                Message.KEY_ID+"=?";
        Log.i(TAG, "getMessageById: "+selectQuery);
        Message message=new Message();
        Cursor cursor=database.rawQuery(selectQuery,new String[]{String.valueOf(messageId)});
        if (cursor.moveToFirst()){
            do {
                message.setMessage_id(cursor.getInt(cursor.getColumnIndex(Message.KEY_ID)));
                message.setContent(cursor.getString(cursor.getColumnIndex(Message.KEY_content)));
                message.setReceive_time(cursor.getString(cursor.getColumnIndex(Message.KEY_time)));
            }while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return message;
    }
}
