package WebLibrary.Service;

import WebLibrary.Model.Book;
import WebLibrary.Repository.Impl.BookRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepositoryImpl bookRepository;

    public List<Book> getAll(){
        return bookRepository.findAll();
    }


}
