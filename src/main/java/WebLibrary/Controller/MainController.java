package WebLibrary.Controller;

import WebLibrary.Service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@Controller
public class MainController {

    @Autowired
    private PostService postService;



    @GetMapping("/")
    @ResponseBody
    public String render(){
        postService.parse("site_1");
        postService.parse("site_2");
        return "Hello World!";
    }

    @GetMapping("/photo")
    public String photo() {
        return "hello world";
    }


}
