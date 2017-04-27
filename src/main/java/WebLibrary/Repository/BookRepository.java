package WebLibrary.Repository;


import WebLibrary.Model.Book;

import java.util.List;

public interface BookRepository {

    List<Book> findAll();
    List<Book> findById();
    List<Book> saveBooks(List<Book> books);

}
