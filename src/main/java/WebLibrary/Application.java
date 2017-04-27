package WebLibrary;

import WebLibrary.Model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnableAutoConfiguration
@SpringBootApplication
public class Application {

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

}
