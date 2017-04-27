package WebLibrary.Controller;

import WebLibrary.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @Autowired
    private BookService bookService;

    @GetMapping("/")
    @ResponseBody
    public String render(){
        System.out.println(bookService.getAll());
        return "Hello World!";
    }

}
