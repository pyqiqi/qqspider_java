package com.myspider.qzone.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.myspider.qzone.dao.DataDao;
import com.myspider.qzone.entry.DataAccount;
import com.myspider.qzone.spider.utils.MultiPageStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class QzoneParseService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(QzoneParseService.class);
    @Autowired
    private DataDao dataDao;

    public void parse(String account, MultiPageStore multiPageStore){
//        //解析目标用户的基本信息
//        DataAccount dataAccount = parseDataAccount(account, multiPageStore);
//        dataDao.saveDataAccount(dataAccount);
//        JSONObject taotaoJson = parseTaotao(account, multiPageStore);
//        dataDao.savaTaotaoJson(taotaoJson);

        //存储原始的用户信息数据和说说数据
        JSONObject sourceInfo = sourceInfoJson(account,multiPageStore);
        dataDao.savaInfoJson(sourceInfo);
        JSONObject taotaoJson = sourceTaotao(account,multiPageStore);
        dataDao.savaTaotaoJson(taotaoJson);
    }


    /**
     * 原始qq空间基本信息json
     *
     * @param account
     * @param multiPageStore
     * @return
     */
    private JSONObject sourceInfoJson(String account, MultiPageStore multiPageStore) {
        String content = (String) multiPageStore.getFile("user/info.json");
        JSONObject contentJson = JSONObject.parseObject(content).getJSONObject("data");
        return contentJson;
    }

    /**
     * 原始qq空间说说json
     * @param account
     * @param multiPageStore
     * @return
     */
    private JSONObject sourceTaotao(String account, MultiPageStore multiPageStore) {
        Map<String, Object> reordMap = (Map<String,Object>) multiPageStore.getFile("user/msglist");
        if (null == reordMap || reordMap.isEmpty()){
            return null;
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("uin",account);
        resultJson.put("createTime",System.currentTimeMillis()/1000);
        JSONArray msglist = new JSONArray();
        for (Map.Entry<String,Object> entry : reordMap.entrySet()){
            Object value = entry.getValue();
            if (value instanceof Map){
                continue;
            } else if(value instanceof String){
                System.out.println("++++++++++++++++++原始空间说说json+++++++++++++++++++++++");
                JSONArray taotaoMsg = sourceTaotaoJson((String) value,multiPageStore);
                if (null != taotaoMsg && taotaoMsg.size() > 0){
                    msglist.addAll(taotaoMsg);
                }
//                System.out.println(value);
            }
        }
        resultJson.put("msglist",msglist);
        return resultJson;
    }

    /**
     * 解析原始空间原始json数据
     * @param account
     * @param multiPageStore
     * @return
     */
    private JSONArray sourceTaotaoJson(String content,MultiPageStore multiPageStore) {
        JSONArray msglist_new = null;
        String message = null;
        try {
            JSONObject jsonContent = JSONObject.parseObject(content);
            message = jsonContent.getString("message");
            JSONArray msglist = jsonContent.getJSONArray("msglist");
            msglist_new = new JSONArray();
            if (msglist != null && msglist.size() > 0) {
                for (int i = 0; i < msglist.size(); i++) {
                    JSONObject msgObj = msglist.getJSONObject(i);
                    //获取点赞情况
                    //获取tid
                    String tid = msgObj.getString("tid");
                    String likeContent = (String) multiPageStore.getFile("user/likelist/" + tid + ".json");
                    JSONObject likeContentObj = JSONObject.parseObject(likeContent);
                    String data = likeContentObj.getString("data");
                    data = data.substring(data.indexOf("[") + 1, data.lastIndexOf("]"));
                    JSONObject dataObj = JSONObject.parseObject(data);
                    msgObj.put("likedata",dataObj);

                    msglist_new.add(msgObj);
                }
            }

        }catch (Exception e) {
            LOG.error(message+e);
        }
        return msglist_new;
    }


    /**
     * 解析qq空间基础信息
     *
     * @param account
     * @param multiPageStore
     * @return
     */
    private DataAccount parseDataAccount(String account, MultiPageStore multiPageStore) {
        DataAccount dataAccount = null;
        String message = null;
        try {
            dataAccount = new DataAccount();
            dataAccount.setUin(account);
            String content = (String) multiPageStore.getFile("user/info.json");
            JSONObject contentJson = JSONObject.parseObject(content).getJSONObject("data");
            message = contentJson.getString("message");

            //现居地
            StringBuffer livePlace = new StringBuffer();
            String country = contentJson.getString("country");
            if(StringUtils.isNotEmpty(country)){
                livePlace.append(country).append(",");
            }

            //获取省份
            String province = contentJson.getString("province");
            if (StringUtils.isNotEmpty(province)){
                livePlace.append(province).append(",");
            }

            //获取城市
            String city = contentJson.getString("city");
            if(StringUtils.isNotEmpty(city)){
                livePlace.append(city);
            }

            dataAccount.setLivePlace(livePlace.toString());


            //获取故乡
            StringBuffer hometown = new StringBuffer();
            //国家
            String hco = contentJson.getString("hco");
            if(StringUtils.isNotEmpty(hco)){
                hometown.append(hco).append(",");
            }

            //省份
            String hp = contentJson.getString("hp");
            if (StringUtils.isNotEmpty(hp)){
                hometown.append(hp).append(",");
            }

            //城市
            String hc = contentJson.getString("hc");
            if(StringUtils.isNotEmpty(hc)){
                hometown.append(hc);
            }
            dataAccount.setHometown(hometown.toString());


            //公司所在地
            StringBuffer companyLocation = new StringBuffer();
            //国家
            String cco = contentJson.getString("cco");
            if (StringUtils.isNotEmpty(cco)){
                companyLocation.append(cco).append(",");
            }

            //省份
            String cp = contentJson.getString("cp");
            if (StringUtils.isNotEmpty(cp)){
                companyLocation.append(cp).append(",");
            }

            //城市
            String cc = contentJson.getString("cc");
            if (StringUtils.isNotEmpty(cc)){
                companyLocation.append(cc);
            }

            dataAccount.setCompanyLocation(companyLocation.toString());

            //公司
            String company = contentJson.getString("company");
            dataAccount.setCompany(company);
            //职业
            String career = contentJson.getString("career");
            dataAccount.setCareer(career);
            //年龄
            int age = contentJson.getIntValue("age");
            dataAccount.setAge(age);
            //昵称
            String nickName = contentJson.getString("nickname");
            dataAccount.setNickName(nickName);

        }catch (Exception e) {
            LOG.error("解析qq空间基础信息失败,result:" + message + ",error:" + e.toString());
        }
        return dataAccount;
    }

    /**
     * 解析空间访客信息
     *
     * @param JSONObject
     * @param multiPageStore
     * @return
     */
    private JSONObject parseTaotao(String account, MultiPageStore multiPageStore) {
        Map<String, Object> reordMap = (Map<String,Object>) multiPageStore.getFile("user/msglist");
        if (null == reordMap || reordMap.isEmpty()){
            return null;
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("uin",account);
        resultJson.put("createTime",System.currentTimeMillis()/1000);
        JSONArray msglist = new JSONArray();
        for (Map.Entry<String,Object> entry : reordMap.entrySet()){
            Object value = entry.getValue();
            if (value instanceof Map){
                continue;
            } else if(value instanceof String){
                System.out.println("++++++++++++++++++++++++++++++++++++++++");
                JSONArray taotaoMsg = parseTaotaoMsg((String) value,multiPageStore);
                if (null != taotaoMsg && taotaoMsg.size() > 0){
                    msglist.addAll(taotaoMsg);
                }
//                System.out.println(value);
            }
        }
        resultJson.put("msglist",msglist);
        return resultJson;

    }

    /**
     * 解析空间说说
     *
     * @param dataAccount
     * @param multiPageStore
     * @return
     */
    private JSONArray parseTaotaoMsg(String content,MultiPageStore multiPageStore) {
        JSONArray msglist_new = null;
        String message = null;
        try {
            JSONObject jsonContent = JSONObject.parseObject(content);
            message = jsonContent.getString("message");
            JSONArray msglist = jsonContent.getJSONArray("msglist");
            msglist_new = new JSONArray();
            if (msglist != null && msglist.size() > 0) {
                for (int i = 0; i < msglist.size(); i++) {
                    JSONObject msg = msglist.getJSONObject(i);
                    JSONObject msg_new = new JSONObject();
                    //说说内容
                    msg_new.put("content", msg.getString("content"));
                    //发表时间
                    msg_new.put("createTime", msg.getString("createTime"));
                    //设备名称
                    msg_new.put("sourceName", msg.getString("source_name"));
                    //定位信息
                    msg_new.put("lbs", msg.getJSONObject("lbs"));

                    //图片url
                    String picStr = "";
                    List<String> picUrlList = null;
                    try {
                        picStr = msg.getString("pic");
                        picUrlList = new ArrayList<String>();
                        if (picStr != "") {
                            String rule = "\"pic_id\":\".+?,";
                            Pattern p = Pattern.compile(rule);
                            Matcher m = p.matcher(picStr);
                            while (m.find()) {
                                String picUrl = m.group(0);
                                picUrl = picUrl.substring(10, picUrl.length() - 2);
                                picUrlList.add(picUrl);
                            }

                            System.out.println("图片url===>" + picUrlList);
                        }
                    } catch (Exception e) {
                        System.out.println("没有图片信息");
                    }

                    //图片url
                    msg_new.put("picUrls", picUrlList);

                    //获取点赞情况
                    //获取tid
                    String tid = msg.getString("tid");
                    String likeContent = (String) multiPageStore.getFile("user/likelist/" + tid + ".json");
                    JSONObject likeContentObj = JSONObject.parseObject(likeContent);

                    String data = likeContentObj.getString("data");
                    data = data.substring(data.indexOf("[") + 1, data.lastIndexOf("]"));
                    JSONObject dataObj = JSONObject.parseObject(data);
                    JSONObject current = dataObj.getJSONObject("current");
                    JSONObject likeData = current.getJSONObject("likedata");
                    JSONObject newData = current.getJSONObject("newdata");
                    //获取点赞好友列表
                    JSONArray likeList = likeData.getJSONArray("list");
                    msg_new.put("likeList",likeList);

                    //获取点赞数
                    msg_new.put("likeNum",likeList.size());

                    //获取评论数
                    int commentNum = newData.getIntValue("CS");
                    msg_new.put("commnetNum",commentNum);

                    //获取评论内容
                    JSONArray commentlist = msg.getJSONArray("commentlist");
                    JSONArray commentlist_new = new JSONArray();
                    if (commentlist != null && commentlist.size() > 0) {
                        for (int j = 0; j < commentlist.size(); j++) {
                            JSONObject commentJson = commentlist.getJSONObject(j);
                            JSONObject commentJson_new = new JSONObject();
                            commentJson_new.put("name", commentJson.getString("name"));
                            commentJson_new.put("content", commentJson.getString("content"));
                            commentJson_new.put("sourceName", commentJson.getString("source_name"));
                            commentJson_new.put("createTime", commentJson.getLongValue("create_time"));
                            commentJson_new.put("reply_num", commentJson.getString("reply_num"));
                            commentJson_new.put("tid", commentJson.getIntValue("tid"));
                            commentJson_new.put("uin", commentJson.getString("uin"));

                            JSONArray replylist = commentJson.getJSONArray("list_3");
                            JSONArray replylist_new = new JSONArray();
                            if (replylist != null && replylist.size() > 0) {
                                for (int k = 0; k < replylist.size(); k++) {
                                    JSONObject replyJson = replylist.getJSONObject(k);
                                    JSONObject replyJson_new = new JSONObject();
                                    replyJson_new.put("name", replyJson.getString("name"));
                                    replyJson_new.put("content", replyJson.getString("content"));
                                    replyJson_new.put("sourceName", replyJson.getString("source_name"));
                                    replyJson_new.put("createTime", replyJson.getLongValue("create_time"));
                                    replyJson_new.put("tid", replyJson.getIntValue("tid"));
                                    replyJson_new.put("uin", replyJson.getString("uin"));
                                    replylist_new.add(replyJson_new);
                                }
                            }
                            commentJson_new.put("replyList", replylist_new);
                            commentlist_new.add(commentJson_new);
                        }
                    }

                    msg_new.put("commentList",commentlist_new);
                    msglist_new.add(msg_new);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return msglist_new;
    }

}
