package com.miao.joey.bluetoothserial.entity;

/**
 * 蓝牙数据实体
 * 1.接收数据封装保存
 * 2.在数据管理页面查看
 * 3.界面可以选择是否显示时间(时分秒)
 * 4.按照当天日期保存到表
 */
public class Message {
    // 表名
    public static final String TABLE = "bluetooth_message";

    // 表的字段名
    public static final String KEY_ID = "id";
    public static final String KEY_content = "content";
    public static final String KEY_RECEIVE_TIME = "receive_time";
    public static final String KEY_RECEIVE_DATE = "receive_date";
    public static final String KEY_DEVICE_NAME = "device_name";

    private int message_id;
    private String content;
    private String receive_time;
    private String receive_date;
    private String device_name;

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public int getMessage_id() {
        return message_id;
    }

    public String getContent() {
        return content;
    }

    public String getReceive_time() {
        return receive_time;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setReceive_time(String receive_time) {
        this.receive_time = receive_time;
    }

    public String getReceive_date() {
        return receive_date;
    }

    public void setReceive_date(String receive_date) {
        this.receive_date = receive_date;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message_id=" + message_id +
                ", content='" + content + '\'' +
                ", receive_time='" + receive_time + '\'' +
                ", receive_date='" + receive_date + '\'' +
                ", device_name='" + device_name + '\'' +
                '}';
    }
}
