package com.wb.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.wb.util.AutoLoginUtil;
import com.wb.util.HttpClientKeepSession;
import com.wb.util.MailUtil;
import com.wb.util.V2exUtil;

/**
 * 
 * @author wb
 *
 */
public class V2exFree implements Job{
    
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
            titleList = V2exUtil.getNewList(html);
            //发送通知邮件
            MailUtil.sendV2exEmail(titleList);
        } catch (Exception e) {
            AutoLoginUtil.IS_LOGIN = false;
            e.printStackTrace();
        }
    }
    
    /**
     * 获取二手交易帖子列表
     */
    public void  getSecondHandTitle() {
        try {
            List<String> titleList = new ArrayList<String>();
            
            // 登录后页面
            HttpGet httpGet = new HttpGet("https://www.v2ex.com/go/all4all");
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
            titleList = V2exUtil.getNewList(html);
            //发送通知邮件
            MailUtil.sendV2exSecondHandEmail(titleList);
        } catch (Exception e) {
            AutoLoginUtil.IS_LOGIN = false;
            e.printStackTrace();
        }
    }
    
    @Test
    public void test() {
        getFreeTitle();
    }

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        getFreeTitle();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getSecondHandTitle();
    }
    
}
