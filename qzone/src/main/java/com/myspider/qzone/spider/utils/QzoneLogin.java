package com.myspider.qzone.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.myspider.qzone.service.FileSaveService;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.myspider.qzone.spider.utils.FetchUtils.get;

public class QzoneLogin {
    private String qqAccount;
    private String password;
    public QzoneLogin(String qqAccount,String password){
        this.qqAccount = qqAccount;
        this.password = password;
    }


    public static void main(String[] args) throws IOException {
        QzoneLogin qzoneLogin = new QzoneLogin("账号","xx");
        MultiPageStore pageStore = new MultiPageStore();
        FileSaveService fileSaveService = new FileSaveService();
        int gtk;
        String url;
        String p_skey;
        String content;
        String targetqq = "448118837";
        String selfqq = "1014237050";
        Integer taotaoNum = 0;
        String cookie = "pgv_pvi=9052879872; RK=meGyhyxfOg; pac_uid=0_80dcc66b6873a; __Q_w_s__QZN_TodoMsgCnt=1; pgv_pvid=7959719265; QZ_FE_WEBP_SUPPORT=1; cpu_performance_v8=32; _qpsvr_localtk=0.7367530326681497; pgv_si=s8071211008; pgv_info=ssid=s5087590640; ptui_loginuin=448118837; ptisp=cm; ptcz=f80bc866a574db3cdf31faa16dbe619310b6b88eb3949bfdc2569aee0b7cbe3b; uin=o0448118837; skey=@oYMgWwfx8; pt2gguin=o0448118837; p_uin=o0448118837; pt4_token=Pgfr9FaBA7bzT2by5EnjPLFL1h6rd63EVV-0vTzjdZQ_; p_skey=h20NY8O7z9EpbxBkp7CjcLDI0L7oMqwJjG2x7kd1WEk_; fnc=2; Loading=Yes";
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

        //获取说说的数量
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
                for (int i = 0; i < msgArray.size(); i++ ){
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
//                    content = JSON.toJSONString(contentObj, true);
//                    pageStore.addFile("user/likelist/"+tid+".json", content);
//
//                    //保存文件
//                    fileSaveService.save(targetqq, "qzone", pageStore);

//                         dataDao.findAndModifyQQ(qq,QQAccount.EXECUTINGSTATUS_FINISHED);

                }
            }catch (Exception e){
                System.out.println("获取msglist"+e);
            }
            index = index + 20;
        }
        System.out.println("=============================================================================================");


    }

    /**
     *  获取skey
     */
    public String skey(String cookie){
        String stringSkey = null;

        String ruleSkey = "; skey=.+?;";
        Pattern patternSkey = Pattern.compile(ruleSkey);
        Matcher matcherSkey = patternSkey.matcher(cookie);
        while (matcherSkey.find()){
            stringSkey = matcherSkey.group(0);
            stringSkey = stringSkey.substring(7,stringSkey.length()-1);
            System.out.println("skey===>"+stringSkey);
        }
        return stringSkey;
    }

    /**
     *  获取p_skey
     */
    public String p_skey(String cookie) {
        String stringPskey = null;
        String rulePskey = "p_skey=.+?;";
        Pattern patternPskey = Pattern.compile(rulePskey);
        Matcher matcherPskey = patternPskey.matcher(cookie);
        while (matcherPskey.find()) {
            stringPskey = matcherPskey.group(0);
            stringPskey = stringPskey.substring(7, stringPskey.length() - 1);
            System.out.println("p_skey===>" + stringPskey);
        }
        return stringPskey;
    }

    public int g_tk(String skey){
        int hash = 5381;
        for (int i = 0, len = skey.length();i < len;++i) {
            hash += (hash << 5) + skey.charAt(i);
        }
        return hash & 2147483647;
    }

    /**
     * cookie字符串转map
     * @param cookies
     * @return
     */
    public Map<String,String> getCookieParams(String cookies){
        Map<String,String> cookieParams = new HashMap<String, String>();
        System.out.println("================>"+cookies);
        cookies = cookies.replaceAll(" ","");
        System.out.println("=================> "+cookies);

        String[] cookieList = cookies.split(";");
        System.out.println("=================> "+cookieList);
        for(String cookie :cookieList){
            String[] s = cookie.split("=");
            String key = s[0];
            String value = s[1];
            System.out.println(cookie);
            System.out.println(key);
            System.out.println(value);
            cookieParams.put(key,value);
        }
        System.out.println(cookieParams);
        return cookieParams;
    }


}




