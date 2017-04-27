package WebLibrary.Model;

public class Book {

    private long id;
    private String Name;
    private String Author;

    public Book(){}

    public Book(long id, String name, String author) {
        this.id = id;
        Name = name;
        Author = author;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }
}
