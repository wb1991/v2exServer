package com.wb.util;

import java.util.HashMap;

import org.json.JSONObject;

import com.baidu.aip.ocr.AipOcr;

public class OCRUtil {
    //设置APPID/AK/SK
    public static AipOcr client = new AipOcr(Constants.APP_ID, Constants.API_KEY, Constants.SECRET_KEY);
    public static String getCaptcher(String path) {
        // 初始化一个AipOcr
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
        // 调用接口
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("language_type", "ENG");
        options.put("detect_direction", "true");
        options.put("detect_language", "false");
        options.put("probability", "true");
        JSONObject res = client.basicGeneral(path, options);
        String result = ((JSONObject)res.getJSONArray("words_result").get(0)).get("words").toString();
        System.out.println(result);
        return result;
    }
    
    public static void main(String[] args) {
        getCaptcher("D://cap/test3.png");
    }
}
