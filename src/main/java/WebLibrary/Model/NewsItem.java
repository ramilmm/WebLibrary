package WebLibrary.Model;

public class NewsItem {

    private String title;
    private String link;
    private String type;

    public NewsItem(){}

    public NewsItem(String title, String link, String type) {
        this.title = title;
        this.link = link;
        this.type = type;
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
}
