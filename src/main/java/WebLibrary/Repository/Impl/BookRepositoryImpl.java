package WebLibrary.Repository.Impl;

import WebLibrary.Application;
import WebLibrary.Model.Book;
import WebLibrary.Repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class BookRepositoryImpl implements BookRepository{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<Book> findAll() {
        jdbcTemplate.execute("DROP TABLE Books IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE Books(" +
                "id INTEGER, name VARCHAR(255), author VARCHAR(255))");
        List<Object[]> splitUpNames = Stream.of("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long")
                .map(name -> name.split(" "))
                .collect(Collectors.toList());
        splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));
        jdbcTemplate.batchUpdate("INSERT INTO Books(name, author) VALUES (?,?)", splitUpNames);
        return jdbcTemplate.query(
                "SELECT * FROM Books",
                (rs, rowNum) -> new Book(rs.getLong("id"),rs.getString("name"),rs.getString("author"))
        );
    }

    @Override
    public List<Book> findById() {
        return null;
    }

    @Override
    public List<Book> saveBooks(List<Book> books) {
        return null;
    }
}
