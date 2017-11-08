package WebLibrary.Utils;

import WebLibrary.Model.NewsItem;
import WebLibrary.Model.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.*;

@Service
public class HTMLParser{

    private final String BASE_DOMAIN = "http://gov.cap.ru";
    private final String SITE_1 = "/?gov_id=61";
    private final String SITE_2 = "/?gov_id=588";

    private ArrayList<NewsItem> mainNews;
    private ArrayList<NewsItem> news;
    private ArrayList<NewsItem> declarations;

    public void parse(String site) {
        String curSite = "";
        if (site.equals("site_1")) {
            curSite = SITE_1;
        }else curSite = SITE_2;

        mainNews = getLast8("div#ContentBody_MainNews div.ListItem_Mini a",BASE_DOMAIN + curSite);
        news = getLast8("div#ContentBody_News div.ListItem_Mini a",BASE_DOMAIN + curSite);
        declarations = getLast8("div#ContentBody_Declarations div.ListItem_Mini a",BASE_DOMAIN + curSite);
    }

    public ArrayList<Post> getPosts(String type) {
        ArrayList<Post> posts = new ArrayList<>();
        ArrayList<NewsItem> list = new ArrayList<>();
        switch (type) {
            case "main": {
                list = mainNews;
                break;
            }
            case "news": {
                list = news;
                break;
            }
            case "declarations": {
                list = declarations;
                break;
            }

        }
        for (int i = 0; i < list.size(); i++) {
            posts.add(getPost(list.get(i)));
        }
        return posts;
    }

    private ArrayList<NewsItem> getLast8(String cssQuery, String domain) {
        Document doc = getDocument( domain);
        assert doc != null;
        Elements last8 = doc.select(cssQuery);
        ArrayList<NewsItem> newsTitle = new ArrayList<>();
        String href;
        for (int i = 0; i < 8; i++) {
            href = last8.get(i).attr("href");

            NewsItem item = new NewsItem();

            item.setTitle(last8.get(i).text());
            item.setLink(href);
            if (href.contains("type")) {
                item.setType(href.substring(href.indexOf("type=") + 5, href.indexOf('&')));
            }else item.setType("declarations");
            newsTitle.add(item);
        }

        return newsTitle;
    }

    public Post getPost(NewsItem news) {
        Post post = new Post();

        Document doc = getDocument(BASE_DOMAIN + news.getLink());
        assert doc != null;

        Elements textNodes = doc.select("div.ItemInfo p");
        StringBuilder text = new StringBuilder();
        for (int i = 2; i < textNodes.size() - 3; i ++) {
            text.append(textNodes.get(i).text()).append("\n");
        }

        ArrayList<String> photoLinks = new ArrayList<>();
        Elements photoNodes = doc.select("div.PhotoGalery_Block div.PG_ImagePreview");
        for (Element photo : photoNodes) {
            photoLinks.add(BASE_DOMAIN + "/" + photo.children().attr("href"));
        }

        if (photoNodes.size() == 0) {
            photoNodes = doc.select("div.Material_Photo img.Material_PreviewImg");
        }
        if (photoNodes.size() != 0 && !photoNodes.get(0).attr("src").equals("")) {
            photoLinks.add(BASE_DOMAIN + photoNodes.get(0).attr("src").substring(2));
        }

        post.setTitle(news.getTitle());
        post.setText(String.valueOf(text));
        post.setSource(BASE_DOMAIN + news.getLink());
        post.setType(news.getType());
        post.setPhotoLinks(photoLinks);

        return post;
    }

    private Document getDocument(String href){
        Document doc = null;
        try {
            doc = Jsoup.connect(href)
                    .userAgent("Chrome/56.0.2924.87")
                    .timeout(10 * 1000)
                    .get();
        } catch (SocketTimeoutException timeout) {
            System.out.println("Timeout: " + timeout.getMessage());
            timeout.printStackTrace();
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
        return doc;
    }

}
