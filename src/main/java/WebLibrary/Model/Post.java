package WebLibrary.Model;

import javax.persistence.*;
import java.util.ArrayList;

@Entity
@Table(name = "Posts")
public class Post {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    @Column(length = 512)
    private String title;
    @Transient
    private String newsId;
    @Column
    private String publish_date;
    @Transient
    private String text;
    @Transient
    private String source;
    @Transient
    private ArrayList<String> photoLinks;
    @Column
    private String type;

    public Post(){}

    public Post(String title, String newsId, String publish_date, String text, String source, ArrayList<String> photoLinks, String type) {
        this.title = title;
        this.newsId = newsId;
        this.publish_date = publish_date;
        this.text = text;
        this.source = source;
        this.photoLinks = photoLinks;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public String getPublish_date() {
        return publish_date;
    }

    public void setPublish_date(String publish_date) {
        this.publish_date = publish_date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ArrayList<String> getPhotoLinks() {
        return photoLinks;
    }

    public void setPhotoLinks(ArrayList<String> photoLinks) {
        this.photoLinks = photoLinks;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", newsId='" + newsId + '\'' +
                ", publish_date='" + publish_date + '\'' +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", photoLinks=" + photoLinks +
                ", type='" + type + '\'' +
                '}';
    }
}
