package com.wb.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.wb.util.AutoLoginUtil;
import com.wb.util.HttpClientKeepSession;
import com.wb.util.MailUtil;

/**
 * 
 * @author wb
 *
 */
public class V2exFree implements Job{
    /**已发送通知的帖子id,防止重复通知*/
    static List<String> replayedList = new ArrayList<String>();
    public void  getFreeTitle() {
        try {
            List<String> titleList = new ArrayList<String>();
            
            // 登录后页面
            HttpGet httpGet = new HttpGet("https://www.v2ex.com/go/free");
            CloseableHttpClient httpClient = AutoLoginUtil.getLoginClient();
            if (httpClient == null) {
                System.out.println("========自动登录失败============");
                return;
            }
            CloseableHttpResponse response5 = httpClient.execute(httpGet,
                    HttpClientKeepSession.context);
            HttpClientKeepSession.printCookies("登录提交后访问首页");
            HttpEntity entity = response5.getEntity();
            String html = EntityUtils.toString(entity, "UTF-8");
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementById("TopicsNode").getElementsByTag("table");
            for (Element element : elements) {
                try {
                    Element e = element.getElementsByTag("td").get(2).tagName("a");
                    //标题
                    String title = e.getElementsByClass("item_title").tagName("a").toString();
                    //处理
                    document = Jsoup.parse(title);
                    //回复数
                    String replyCount = (document.getElementsByTag("a").get(1).attr("href").split("#")[1]).replaceAll("reply", "");
                    //帖子id
                    String href = document.getElementsByTag("a").get(1).attr("href").split("#")[0];
                    if (Integer.parseInt(replyCount) <= 5 && !replayedList.contains(href)) {
                        String text = document.getElementsByTag("a").get(1).text();
                        titleList.add(text);
                        replayedList.add(href);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (String title: titleList) {
                System.out.println(title);
            }
            //发送通知邮件
            MailUtil.sendV2exEmail(titleList);
        } catch (Exception e) {
            AutoLoginUtil.IS_LOGIN = false;
            e.printStackTrace();
        }
    }
    
    /**
     * 判断标题中是否包含特定的关键词
     * @param title 标题
     * @return
     */
//    private boolean isContainsKeywords(String title) {
//        boolean isContains = false;
//        for (String keyword : keywords) {
//            if (title.contains(keyword)) {
//                isContains = true;
//                break;
//            }
//        }
//        return isContains;
//    }
    
    @Test
    public void test() {
        getFreeTitle();
    }

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        getFreeTitle();
    }
    
}
