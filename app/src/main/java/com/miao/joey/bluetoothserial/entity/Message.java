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
    public static final String KEY_time = "receive_time";

    private int message_id;
    private String content;
    private String receive_time;

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

    @Override
    public String toString() {
        return "Message{" +
                "message_id=" + message_id +
                ", content='" + content + '\'' +
                ", receive_time='" + receive_time + '\'' +
                '}';
    }
}
