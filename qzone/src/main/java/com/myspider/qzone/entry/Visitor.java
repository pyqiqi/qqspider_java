package com.myspider.qzone.entry;

public class Visitor {
    private String uin;
    private String nickName;
    private int qzoneLevel;
    private long time;

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public String getUin() {
        return uin;
    }
    public void setUin(String uin) {
        this.uin = uin;
    }
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public int getQzoneLevel() {
        return qzoneLevel;
    }
    public void setQzoneLevel(int qzoneLevel) {
        this.qzoneLevel = qzoneLevel;
    }

}
