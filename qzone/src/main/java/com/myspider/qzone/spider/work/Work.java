package com.myspider.qzone.spider.work;

import com.myspider.qzone.dao.DataDao;
import com.myspider.qzone.entry.QQAccount;
import com.myspider.qzone.parse.QzoneParseService;
import com.myspider.qzone.service.FileSaveService;
import com.myspider.qzone.spider.utils.CreateFileUtil;
import com.myspider.qzone.spider.utils.MultiPageStore;
import com.myspider.qzone.spider.utils.PoolManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.myspider.qzone.spider.utils.QzoneLogin;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.rmi.runtime.Log;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.myspider.qzone.spider.utils.FetchUtils.get;
import static com.myspider.qzone.spider.utils.FetchUtils.getImageFromNetByUrl;

@Service
public class Work {
    private static final Logger LOG = Logger.getLogger(Work.class);
    @Autowired
    private DataDao dataDao;
    @Autowired
    private FileSaveService fileSaveService;
    @Autowired
    private QzoneParseService qzoneParseService;

    private MyQueue queue;
    private ExecutorService executor = null;

    @PostConstruct
    public void start(){
        //获取一个连接
        PoolManager.getInstance();
        queue = new MyQueue(10);
        executor = Executors.newFixedThreadPool(2);
        executor.execute(new StartSpider());
//        executor.execute(new FilterQQ());
//        executor.execute();
    }

    class FilterQQ implements Runnable{

        @Override
        public void run(){
            long startTime = 111111111;
            long endTime = 11111111;
            //通过时间段查找qq数据
            List<JSONObject> qqDataJsons = dataDao.findQQData(startTime,endTime);
            for (JSONObject qqDataJson : qqDataJsons){
                String deviceId = qqDataJson.getString("deviceId");
                JSONArray qqDataJsonArray = qqDataJson.getJSONArray("qqData");
                if(qqDataJsonArray != null && qqDataJsonArray.size() > 0){
                    for(int i = 0;i < qqDataJsonArray.size(); i++){
                        String qq = qqDataJsonArray.getJSONObject(i).getString("qq");
                        QQAccount qa = new QQAccount();
                        qa.setUin(qq);
                        qa.setDeviceId(deviceId);
                        qa.setLogTime(System.currentTimeMillis()/1000);
                        dataDao.savaQQ(qa);
                        LOG.info("insert qq:"+ qq);
                    }
                }
            }
        }
    }

    class GetQQAccount implements Runnable{

        @Override
        public void run() {
            while (true){
                try {
                    LOG.info("开始获取qq数据 ，并更新执行状态");
                    QQAccount qqAccount = dataDao.findAndModifyQQ(QQAccount.EXECUTINGSTATUS_NOT_START , QQAccount.EXECUTINGSTATUS_EXECUTING);
                    LOG.info("获取到qq数据："+qqAccount);
                    if (qqAccount != null){
                        queue.putObj(qqAccount);
                    }
                } catch (InterruptedException e) {
                    LOG.error("获取qq数据时发生错误："+e.toString());
                }
            }
        }
    }

    class StartSpider implements Runnable{
        @Override
        public void run() {
            QzoneLogin qzoneLogin = new QzoneLogin("账号","xx");
            MultiPageStore pageStore = new MultiPageStore();
            int gtk;
            String url;
            String p_skey;
            String content;
            //赵敏
//            String targetqq = "1534032123";
            //wcl
//            String targetqq = "303358288";
            //蒋林利
//            String targetqq ="1026887893";
            String targetqq = "1014237050";
            String selfqq = "448118837";
            Integer taotaoNum = 0;
            String cookie = "pgv_pvi=9052879872; RK=meGyhyxfOg; pac_uid=0_80dcc66b6873a; __Q_w_s__QZN_TodoMsgCnt=1; QZ_FE_WEBP_SUPPORT=1; cpu_performance_v8=32; pgv_pvid=7959719265; zzpaneluin=; zzpanelkey=; pgv_si=s8816517120; _qpsvr_localtk=0.7386915807086845; pgv_info=ssid=s943463033; ptui_loginuin=448118837; ptisp=cm; ptcz=f80bc866a574db3cdf31faa16dbe619310b6b88eb3949bfdc2569aee0b7cbe3b; uin=o0448118837; skey=@oYMgWwfx8; pt2gguin=o0448118837; p_uin=o0448118837; pt4_token=b93cz0*YYUvysTUwRIez3mw9WZhbidfUFQoN5Kqakko_; p_skey=5-P6t4Tww*xihxFZOElclDba6Dzq5fQhqD3G575J6Cs_; Loading=Yes";
            p_skey = qzoneLogin.p_skey(cookie);
            gtk = qzoneLogin.g_tk(p_skey);
//        String url = "https://h5.qzone.qq.com/proxy/domain/ic2.qzone.qq.com/cgi-bin/feeds/feeds_html_act_all?uin=951109140&hostuin=8274350&scope=0&filter=all&flag=1&refresh=0&firstGetGroup=0&mixnocache=0&scene=0&begintime=undefined&icServerTime=&start=0&count=10&sidomain=qzonestyle.gtimg.cn&useutf8=1&outputhtmlfeed=1&refer=2&r=0.36911631942767964&g_tk=" + qzoneLogin.g_tk(cookie);

            BasicCookieStore cookieStore = new BasicCookieStore();
            CloseableHttpClient client = PoolManager.getHttpClient(cookieStore);

            //设置头
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Cookie",cookie);



            //获取空间个人资料
            url = "https://h5.qzone.qq.com/proxy/domain/base.qzone.qq.com/cgi-bin/user/cgi_userinfo_get_all?uin=" + targetqq + "&fupdate=1&g_tk=" + gtk;
            content = get(client, url,"UTF-8",null,headers);
            content = content.substring(content.indexOf("(") + 1, content.lastIndexOf(")"));
            JSONObject userInfoJson = JSONObject.parseObject(content);
            content = JSON.toJSONString(userInfoJson, true);
            System.out.println("=========================================获取个人资料============================================");
            //保存到文件中
            content = JSON.toJSONString(JSONObject.parseObject(content), true);
            pageStore.addFile("user/info.json", content);
            System.out.println("=============================================================================================");

            //获取说说
            url = "https://h5.qzone.qq.com/proxy/domain/taotao.qq.com/cgi-bin/emotion_cgi_msglist_v6?uin="+targetqq+"&pos=1000000&num=20&g_tk=" + gtk;
            content = get(client, url,"UTF-8",null,headers);
            content = content.substring(content.indexOf("(") + 1, content.lastIndexOf(")"));
            JSONObject taotaoNumContent = JSONObject.parseObject(content);
            content = JSON.toJSONString(taotaoNumContent, true);
            System.out.println("=========================================说说数量============================================");
//            System.out.println(content);
            taotaoNum = Integer.parseInt(taotaoNumContent.getJSONObject("usrinfo").getString("msgnum"));
            System.out.println(taotaoNum);

            //获取说说内容
             int index = 0;
             System.out.println("==========================================获取说说内容============================================");
             while (index < taotaoNum) {
                 url = "https://h5.qzone.qq.com/proxy/domain/taotao.qq.com/cgi-bin/emotion_cgi_msglist_v6?uin=" + targetqq + "&pos=" + index + "&num=20&g_tk=" + gtk;
                 content = get(client, url,"UTF-8",null,headers);
                 content = content.substring(content.indexOf("(") + 1, content.lastIndexOf(")"));

                 JSONObject taotaoContent = JSONObject.parseObject(content);
                 //保存说说列表内容到文件中
                 content = JSON.toJSONString(taotaoContent, true);
//                 CreateFileUtil.writeToFile("D://QQ/"+targetqq+"/taotaoList/"+index/20+".json",content);
                 pageStore.addFile("user/msglist/"+index/20+".json", content);

                 try {
                     String msgListStr = taotaoContent.getString("msglist");
                     JSONArray msgArray = JSONArray.parseArray(msgListStr);

                     //遍历说说消息列表
                     for (int i = 0; i < msgArray.size(); i++){
                         JSONObject msgObj = msgArray.getJSONObject(i);
                         //获取id
                         String tid = msgObj.getString("tid");

                         //获取点赞和评论情况
                         String unikey = "http://user.qzone.qq.com/" + targetqq + "/mood/" + tid;
                         url = "https://h5.qzone.qq.com/proxy/domain/r.qzone.qq.com/cgi-bin/user/qz_opcnt2?_stp=&unikey=" + unikey + "&g_tk=" + gtk;
                         content = get(client, url,"UTF-8",null,headers);
                         content = content.substring(content.indexOf("(") + 1, content.lastIndexOf(")"));
                         JSONObject contentObj = JSONObject.parseObject(content);
                         //保存到文件中
                         content = JSON.toJSONString(contentObj, true);
                         pageStore.addFile("user/likelist/"+tid+".json", content);

//                         dataDao.findAndModifyQQ(qq,QQAccount.EXECUTINGSTATUS_FINISHED);
                     }



                 }catch (Exception e){
                     System.out.println("获取msglist错误"+e);
                 }
                 index = index + 20;
             }

            System.out.println("==========================================获取图片内容============================================");
            url = "http://h5.qzone.qq.com/proxy/domain/shalist.photo.qq.com/fcgi-bin/fcg_list_album_v3?g_tk="+ gtk+"&hostUin="+targetqq+"&uin="+selfqq+"&inCharset=utf-8&outCharset=utf-8";
            content = get(client, url,"UTF-8",null,headers);

            content = content.substring(content.indexOf("(") + 1, content.lastIndexOf(")"));
            JSONObject albumContentObj = JSONObject.parseObject(content);

            JSONObject data = albumContentObj.getJSONObject("data");
            JSONArray albumArray = data.getJSONArray("albumListModeSort");
            System.out.println(albumContentObj);
            System.out.println(albumArray);
            if (albumArray != null && albumArray.size()>0){
                for (int i = 0 ; i < albumArray.size() ; i++){
                    JSONObject albumObj = albumArray.getJSONObject(i);
                    System.out.println(albumObj);
                    //获取相册名
                    String albumName = albumObj.getString("name");
                    System.out.println("相册名"+albumName);
                    //获取相片数量
                    int photoNum = albumObj.getIntValue("total");
                    System.out.println("相册数量"+photoNum);
                    //获取时间
                    int createtime = albumObj.getIntValue("createtime");
                    System.out.println("发表时间"+createtime);
                    //获取相册id
                    String id = albumObj.getString("id");
                    System.out.println("id"+id);

                    //获取每张图片
                    url = "http://h5.qzone.qq.com/proxy/domain/shplist.photo.qzone.qq.com/fcgi-bin/cgi_list_photo?g_tk="+gtk+"&hostUin="+targetqq+"&topicId="+id+"&uin="+selfqq+"&pageStart=0&pageNum="+photoNum+"&inCharset=utf-8&outCharset=utf-8";
                    content = get(client, url,"UTF-8",null,headers);
                    System.out.println("**************************************");
                    System.out.println(url);
                    content = content.substring(content.indexOf("(") + 1, content.lastIndexOf(")"));
                    JSONObject photoObj = JSONObject.parseObject(content);
                    JSONObject photoDataObj = photoObj.getJSONObject("data");
                    JSONArray photoListArr = photoDataObj.getJSONArray("photoList");
                    if (photoListArr != null && photoListArr.size()>0){
                        for (int i1 = 0; i1 < photoListArr.size(); i1++ ){
                            JSONObject photo = (JSONObject) photoListArr.get(i1);
                            //相片名字
                            String photoName = photo.getString("name");
                            //获取上传时间
                            String uploadTime = photo.getString("uploadtime");
                            //获取相片url
                            String photoUrl = photo.getString("url");
                            //获取相片内容
//                            content = get(client, photoUrl,"UTF-8",null,headers);
                            byte[] photoContent = null;
                            try {
                                photoContent = getImageFromNetByUrl(photoUrl);
                            }catch (Exception e){
                                System.out.println("获取图片错误:"+e);
                            }
                            if (photoContent != null) {
                                pageStore.addFile("user/albumlist/" + albumName + "//" + uploadTime + ".jpg", photoContent);
                            }else {
                                System.out.println("获取图片错误");
                            }
                            System.out.println("相片名===> "+photoName);
                            System.out.println("上传时间===> "+uploadTime);
                            System.out.println("相片url===> "+photoUrl);
                            System.out.println("相片内容===> "+photoContent);
                        }
                    }
                    System.out.println(photoListArr);
                    System.out.println("**************************************");
                }
            }


            //保存文件
            fileSaveService.save(targetqq, "qzone", pageStore);
//            //解析数据，并存储到数据库
            qzoneParseService.parse(targetqq, pageStore);
            System.out.println("=============================================================================================");


            //获取访客
//        url = "https://h5.qzone.qq.com/proxy/domain/g.qzone.qq.com/cgi-bin/friendshow/cgi_get_visitor_simple?uin=" + selfqq + "&mask=2&page=1&fupdate=1&g_tk=" + gtk;
//        content = get(client, url,"UTF-8",null,headers);
//        System.out.println(content);
        }
    }

}

class MyQueue{
    //LinkedBlockingQueue
    private LinkedBlockingQueue<Object> queue;

    public MyQueue(int num){
        queue = new LinkedBlockingQueue<Object>(num);
    }

    public Object getObj() throws InterruptedException{
        return queue.take();
    }

    public void putObj(Object obj) throws InterruptedException{
        queue.put(obj);
    }

    ///test test

}


