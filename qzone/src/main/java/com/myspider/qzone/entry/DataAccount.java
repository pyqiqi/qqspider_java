package com.myspider.qzone.entry;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @Document(collection="mongodb 对应 collection 名")

// 若未加 @Document ，该 bean save 到 mongo 的 user collection
// 若添加 @Document ，则 save 到 qzone_dataAccount collection
 */
@Document(collection = "qzone_dataAccount")
public class DataAccount {
    /** 数据id */
    @Id
    private String id;
    private String deviceId;
    /**qq账号**/
    private String uin;
    /**创建时间**/
    private long updateTime;
    /**更新时间**/
    private long createTime;
    private String nickName;
    private int age;
    /**公司所在地**/

    private String companyLocation;
    /**故乡**/
    private String hometown;
    /**现居地**/
    private String livePlace;
    /**公司**/
    private String company;
    /**职业**/
    private String career;

    private List<Visitor> visitors;


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
    public long getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    public long getCreateTime() {
        return createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getCompanyLocation() {
        return companyLocation;
    }
    public void setCompanyLocation(String companyLocation) {
        this.companyLocation = companyLocation;
    }
    public String getHometown() {
        return hometown;
    }
    public void setHometown(String hometown) {
        this.hometown = hometown;
    }
    public String getLivePlace() {
        return livePlace;
    }
    public void setLivePlace(String livePlace) {
        this.livePlace = livePlace;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public String getCareer() {
        return career;
    }
    public void setCareer(String career) {
        this.career = career;
    }
    public List<Visitor> getVisitors() {
        return visitors;
    }
    public void setVisitors(List<Visitor> visitors) {
        this.visitors = visitors;
    }

}
