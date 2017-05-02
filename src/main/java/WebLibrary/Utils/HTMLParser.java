package ru.veusdas.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.veusdas.Model.Book;
import ru.veusdas.Model.Review;

import javax.print.Doc;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;


public class HTMLParser implements Runnable{

    private static final ExecutorService workers = Executors.newCachedThreadPool();
    private static final String BASE_DOMAIN = "http://www.labirint.ru";
    private static final String REVIEW_DOMAIN = "http://www.labirint.ru/reviews/goods/";
    private static final String CATEGORY_DOMAIN = "http://www.labirint.ru/genres/2308/";

    @Override
    public void run() {
        int maxPage = Integer.valueOf(getLastPage());
        Collection<Callable<ArrayList<Book>>> tasks = new ArrayList<>();
        for (int i = 1; i <= maxPage; i++) {
            String finalI = String.valueOf(i);
            tasks.add(() -> {
                ArrayList<Book> result = new ArrayList<>();
                ArrayList<String> books = getPageLinks(finalI);
                for (String book_link : books) {
                    result.add(getBook(book_link));
                }
                return result;
            });
        }

        List<Future<ArrayList<Book>>> results = new ArrayList<>();
        try {
            results = workers.invokeAll(tasks, (long) 200, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Error getting results: " + e.getMessage());
        }
        ArrayList<Book> parsedBooks = new ArrayList<>();
        for (Future<ArrayList<Book>> book : results) {
            try {
                parsedBooks.addAll(book.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
//        for (Book book : parsedBooks) {
            System.out.println(parsedBooks.size());
//        }
    }

    private static String getLastPage() {
        Document doc = getDocument(CATEGORY_DOMAIN + "?onpage=100");
        assert doc != null;
        Element lastPageNumber = doc.select("div.pagination-numbers :nth-child(1) li:last-child a").get(0);
        String href = lastPageNumber.attr("href");
        return href.substring(href.length() - 1);
    }

    public static ArrayList<String> getPageLinks(String page_num) {
        Document doc = getDocument(CATEGORY_DOMAIN + "?onpage=100&page=" + page_num);
        assert doc != null;
        Elements links = doc.select("a.cover");
        ArrayList<String> bookLinks = new ArrayList<>();
        for (Element link: links) {
            bookLinks.add(link.attr("href"));
        }
        return bookLinks;
    }

    public static Book getBook(String href){
        Document doc = getDocument(BASE_DOMAIN + href);
        assert doc != null;
        Book book = new Book();

        String name = doc.select("div#product-title :nth-child(1)").get(0).text();

        String author = "", editor = "";
        Elements authors = doc.select("div.authors");
        for (Element el: authors){
            if (el.text().toLowerCase().contains("автор")) {
                author = el.children().text();
            }else if (el.text().toLowerCase().contains("редактор")) {
                editor = el.children().text();
            }
        }

        Element pub = doc.select("div.publisher").get(0);
        String publisher = pub.children().text();
        String year = pub.text().substring(pub.text().indexOf(',') + 1);

        Elements cost_element = doc.select("span.buying-price-val-number");
        Float cost = 1f;
        if (cost_element.size() != 0) {
            cost = Float.valueOf(cost_element.get(0).text());
        }

        String id = doc.select("div.articul").get(0).text();
        Long item_id = Long.valueOf(id.substring(id.indexOf(":") + 2));

        String isbn_str = doc.select("div.isbn").get(0).text();
        String ISBN = isbn_str.substring(isbn_str.indexOf(":") + 2);

        Elements page_element = doc.select("div.pages2");
        Integer page_count = 0;
        String pages;
        if (page_element.size() != 0) {
            pages = page_element.get(0).text();
            page_count = Integer.valueOf(pages.substring(pages.indexOf(":") + 2, pages.lastIndexOf(" ")));
        }

//        String typography = doc.select("div.popup-middle").get(0).text();

        Elements weigth_element = doc.select("div.weight");
        String weight_str, weight = "";
        if (weigth_element.size() != 0) {
            weight_str = weigth_element.get(0).text();
            weight = weight_str.substring(weight_str.indexOf(":") + 2);
        }

        Elements dimensions_element = doc.select("div.dimensions");
        String dimensions_str,dimensions = "";
        if (dimensions_element.size() != 0) {
            dimensions_str = dimensions_element.get(0).text();
            dimensions = dimensions_str.substring(dimensions_str.indexOf(":") + 2);
        }

        String annotation = doc.select("div#product-about").get(0).child(1).text();

        String rate = doc.select("div#rate").get(0).text();

        book.setName(name);
        book.setAuthor(author);
        book.setEditor(editor);
        book.setPublisher(publisher);
        book.setYear(year);
        book.setCost(cost);
        book.setItem_id(item_id);
        book.setISBN(ISBN);
        book.setPage_count(page_count);
        book.setWeight(weight);
        book.setDimensions(dimensions);
        book.setAnnotation(annotation);
        book.setRate(rate);
        book.setReviews(getReviews(item_id));

        return book;
    }

    public static ArrayList<Review> getReviews(Long book_id) {
        ArrayList<Review> reviews = new ArrayList<>();
        Document doc = getDocument(REVIEW_DOMAIN + book_id);
        assert doc != null;
        Elements comments = doc.select("div.comment-user-info");
        if (comments == null)
            return null;
        for (int i = 0; i < comments.size(); i++) {
            Review review = new Review();
            review.setAvatar(comments.get(i).getElementsByClass("comment-user-avatar").get(0).child(0).children().attr("src"));
            review.setUsername(comments.get(i).getElementsByClass("user-name").get(0).children().text());
            review.setReview_text(comments.get(i).parent().getElementsByClass("comment-text").get(i).text());
            Elements pic = comments.get(i).parent().getElementsByClass("comment-text").get(i).getElementsByClass("comment-user-pic");
            ArrayList<String> images = new ArrayList<>();
            if (pic.size() != 0) {
                Elements pictures = pic.get(0).getElementsByClass("comment-pic-container");
                for (int j = 0; j < pictures.size(); j++) {
                    images.add(pictures.get(j).children().attr("data-src"));
                }
            }
            String date = comments.get(i).parent().getElementsByClass("date").get(i).text();
            DateFormat format = new SimpleDateFormat("d.MM.YYYY H:m:s", Locale.ENGLISH);
            Date pub_date = null;
            try {
                pub_date = format.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            review.setDate(pub_date);
            review.setReview_picture(images);
            reviews.add(review);
        }

        return reviews;
    }

    private static Document getDocument(String href){
        Document doc = null;
        try {
            doc = Jsoup.connect(href)
                    .userAgent("Chrome/56.0.2924.87")
                    .get();
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return doc;
    }

}
