package com.wb.util;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class V2exUtil {
    
    /**已发送通知的帖子id,防止重复通知*/
    public static List<String> replayedList = new ArrayList<String>();
    
    /**
     * 根据html源代码过滤出新帖标题至list
     * @param html 帖子源代码
     * @return 新帖标题list
     */
    public static List<String> getNewList(String html) {
        List<String> titleList = new ArrayList<String>();
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
        return titleList;
    }

}
