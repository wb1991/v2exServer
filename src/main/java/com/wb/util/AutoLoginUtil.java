package com.wb.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

/**
 * 自动登录v2ex
 * 
 * @author wb
 *
 */
public class AutoLoginUtil {

    /**验证码图片保存路径*/
//    private static final String IMAGE_PATH = "D:\\cap\\";
    /**是否已登录*/
    public static boolean IS_LOGIN = false;
    
    static {
        
    }
    
    /**
     * 获取已登录状态的httpclient
     * @return
     * @throws InterruptedException 
     */
    public static CloseableHttpClient getLoginClient() throws InterruptedException {
        if (IS_LOGIN) {
            return HttpClientKeepSession.httpClient;
        }
        //因验证码识别有概率出错，且频繁请求会导致封ip，所以多次间歇尝试登录
        for (int i = 0; i < 5; i++) {
            try {
                autoLogin();
            } catch (Exception e) {
                //TODO 发送通知邮件||切换ip地址
                e.printStackTrace();
                try {
                    MailUtil.sendTextEmail("v2消息", e.getMessage()+"请及时处理！");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                break;
            }
            System.out.println("==============第"+i+"次尝试登录==============");
            if (IS_LOGIN) {
                System.out.println("==============登录成功==============");
                return HttpClientKeepSession.httpClient;
            }
            Thread.sleep(1000*30);
        }
        return null;
    }
    
    /**
     * 自动登录
     */
    public static void autoLogin() {

        try {
            String html = null;
            //step1:请求登录页获取源代码
            HttpGet httpGet = new HttpGet("https://www.v2ex.com/signin");
            CloseableHttpResponse response = HttpClientKeepSession.httpClient.execute(httpGet,
                    HttpClientKeepSession.context);
            HttpClientKeepSession.printCookies("初次访问");
            HttpEntity entity = response.getEntity();
            html = EntityUtils.toString(entity, "UTF-8");
            //step2:获取登录参数once
            System.out.println(html);
            String once = getOnce(html);;
            System.out.println(once);
            //step3:获取验证码
            String captcherStr = getCaptcher(once);
            System.out.println(captcherStr);
            //step4:获取动态参数名
            List<String> params = getInput(html);
            //step5:登录提交
            login(params, captcherStr, once);
        }catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("ip地址已被封禁");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param params
     * @param captcherStr
     * @param once
     * @return
     */
    public static void login(List<String> params, String captcherStr, String once) {
        try {
            HttpPost httpPost2 = new HttpPost("https://www.v2ex.com/signin");
            String formData = params.get(0) + "=" + Constants.V2EX_USER_NAME +"&" + params.get(1) + "="+ Constants.V2EX_USER_PASSWORD +"&" + params.get(2) + "="
                    + captcherStr + "&once=" + once + "&next=/";
            List<NameValuePair> nvps = HttpClientKeepSession.toNameValuePairList(formData);
            httpPost2.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36"); // 设置请求头消息User-Agent
            httpPost2.setHeader("referer", "https://www.v2ex.com/signin"); // 设置请求头消息User-Agent
            httpPost2.setHeader("origin", "https://www.v2ex.com"); // 设置请求头消息User-Agent
            httpPost2.setHeader("upgrade-insecure-requests", "1"); // 设置请求头消息User-Agent
            httpPost2.setHeader("content-type", "application/x-www-form-urlencoded"); // 设置请求头消息User-Agent
            httpPost2.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            CloseableHttpResponse response3 = HttpClientKeepSession.httpClient.execute(httpPost2,
                    HttpClientKeepSession.context);
            HttpClientKeepSession.printCookies("登录提交后");
            HttpEntity entity = response3.getEntity();
            String html = EntityUtils.toString(entity, "UTF-8");
            if (html != null && html.contains("创作新主题")) {
                IS_LOGIN = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * 识别验证码
     * @param once 请求参数
     * @return
     */
    public static String getCaptcher(String once) {
        String captcherStr = null;
        try {
            for (int i = 0; i < 10; i++) {
                // 拿到验证码图片
                HttpGet httpGet2 = new HttpGet("https://www.v2ex.com/_captcha?once=" + once);
                httpGet2.setHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0"); // 设置请求头消息User-Agent
                CloseableHttpResponse response2;
                    response2 = HttpClientKeepSession.httpClient.execute(httpGet2,
                            HttpClientKeepSession.context);
                HttpClientKeepSession.printCookies("获取验证码图片" + i);
                HttpEntity entity = response2.getEntity();
                HttpClientKeepSession.printResponse(response2);
                InputStream inputStream = entity.getContent();
                FileOutputStream fos = new FileOutputStream(Constants.CAPTCHA_PATH + "test" + i + ".png");
                byte[] data = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(data)) != -1) {
                    fos.write(data, 0, len);
                }
                fos.close();
            }
            for (int i = 0; i < 10; i++) {
                try {
                    String result = OCRUtil.getCaptcher(Constants.CAPTCHA_PATH + "test" + i + ".png");
                    if (result != null && result.matches("[a-zA-Z]+") && result.length() == 8) {
                        captcherStr = result;
                        System.out.println("识别到test" + i + ".png 验证码为" + captcherStr);
                        break;
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (Exception e) {
             System.err.println(e.getMessage());
        }
        return captcherStr;
    }

    /**
     * 获取登录所需的动态的参数名.
     * 
     * @param html
     *            登录页面源代码.
     * @return 登录状态
     */
    public static List<String> getInput(String html) {
        List<String> paramList = new ArrayList<String>();
        Document doc = Jsoup.parse(html);
        Elements inputs = doc.select("input");
        for (Element element : inputs) {
            String name = element.attr("name");
            if (name != null && name.length() > 10) {
                paramList.add(name);
            }
        }
        return paramList;
    }

    /**
     * 获取登录所需once参数
     * 
     * @param html
     *            登录网页源代码
     * @return
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public static String getOnce(String html) {
        Document doc = Jsoup.parse(html);
        Element element = doc.getElementsByTag("table").get(1) // 第二个tabel
                .getElementsByTag("tr").get(2) // 验证码所在tr
                .getElementsByTag("div").get(0);
        String style = element.attr("style");
        String once = style.split(";")[0].split(":")[1].split("=")[1].split("'")[0];
        return once;
    }
    
    @Test
    public void test() {
        autoLogin();
    }

}
