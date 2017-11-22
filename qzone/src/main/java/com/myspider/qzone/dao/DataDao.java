package com.myspider.qzone.dao;

import com.alibaba.fastjson.JSONObject;
import com.myspider.qzone.entry.DataAccount;
import com.myspider.qzone.entry.QQAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 数据库操作
 */
//@Repository用于标注数据访问组件
@Repository
public class DataDao {

    @Autowired
    private MongoTemplate thirdpartyMongoTemplate;

    private MongoTemplate applogsMongoTemplate;

    public void saveDataAccount(DataAccount dataAccount){
        //为数据添加创建时间
        dataAccount.setCreateTime(System.currentTimeMillis()/1000);
        //为数据添加上传时间
        dataAccount.setUpdateTime(dataAccount.getCreateTime());
        System.out.println("插入数据库中");
        thirdpartyMongoTemplate.insert(dataAccount);
    }

    //获取心情插入数据
    public void savaTaotaoJson(JSONObject json){
        //获取QQ空间的心情json文件
        thirdpartyMongoTemplate.insert(json,"qzone_taotao");
    }


    //获取心情插入数据
    public void savaInfoJson(JSONObject json){
        //获取QQ空间的心情json文件
        thirdpartyMongoTemplate.insert(json,"qzone_info");
    }

    //保存QQ，保存之前先在数据库中查找是否已有数据
    public void savaQQ(QQAccount qqAccount){
        QQAccount qa = thirdpartyMongoTemplate.findOne(Query.query(Criteria.where("uin").is(qqAccount.getUin())), QQAccount.class);
        if (null == qa){
            thirdpartyMongoTemplate.save(qqAccount);
        }
    }

    //修改QQ数据，根据QQ执行的状态查找并更新执行状态
    //0:未执行，1：执行中，2：已完成
    public QQAccount findAndModifyQQ(int executingStatus, int newExecutingStatus) {
        return thirdpartyMongoTemplate
                .findAndModify(Query.query(Criteria.where("executingStatus").is(executingStatus)), new Update().set("executingStatus", newExecutingStatus), QQAccount.class);
    }

    //修改QQ数据，根据QQ号查找并更新执行状态
    public QQAccount findAndModifyQQ(String qq,int newExecutingStatus) {
        return thirdpartyMongoTemplate
                .findAndModify(Query.query(Criteria.where("uin").is(qq)), new Update().set("executingStatus", newExecutingStatus), QQAccount.class);
    }


    //根据时间查寻QQ数据
    public List<JSONObject> findQQData(long startTime, long endTime) {
        return applogsMongoTemplate
                .find(Query.query(Criteria.where("logTime").gte(startTime).lte(endTime)).with(new Sort(Sort.Direction.DESC, "logTime")), JSONObject.class,"QQData");
    }
}
