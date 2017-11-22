package com.myspider.qzone.entry;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="qq_account")
public class QQAccount {
    public final static int EXECUTINGSTATUS_NOT_START=0;
    public final static int EXECUTINGSTATUS_EXECUTING=1;
    public final static int EXECUTINGSTATUS_FINISHED=2;

    //自增长注解，id增长器
    @Id
    private String id;
    /**qq账号**/
    private String uin;
    /**设备id**/
    private String deviceId;
    private long logTime;
    //0:未執行，1：執行中，2：已完成
    private int executingStatus;


    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUin() {
        return uin;
    }
    public void setUin(String uin) {
        this.uin = uin;
    }
    public long getLogTime() {
        return logTime;
    }
    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }
    public int getExecutingStatus() {
        return executingStatus;
    }
    public void setExecutingStatus(int executingStatus) {
        this.executingStatus = executingStatus;
    }


}
