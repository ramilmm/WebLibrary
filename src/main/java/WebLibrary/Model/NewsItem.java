package WebLibrary.Model;

public class NewsItem {

    private String title;
    private String link;
    private String type;
    private String news_id;
    private String publish_date;

    public NewsItem(){}

    public NewsItem(String title, String link, String type, String news_id, String publish_date) {
        this.title = title;
        this.link = link;
        this.type = type;
        this.news_id = news_id;
        this.publish_date = publish_date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNews_id() {
        return news_id;
    }

    public void setNews_id(String news_id) {
        this.news_id = news_id;
    }

    public String getPublish_date() {
        return publish_date;
    }

    public void setPublish_date(String publish_date) {
        this.publish_date = publish_date;
    }
}

