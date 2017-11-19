package WebLibrary.Controller;

import WebLibrary.Service.PostService;
import WebLibrary.Utils.HTMLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



@Controller
public class MainController {

    @Autowired
    private PostService postService;


    @GetMapping("/")
    public String render(){
        postService.run();
        return "Hello World!";
    }

    @GetMapping("/check")
    public String checkApp() {
        HTMLParser html = new HTMLParser();
        html.parse("site_1");
        return "Working!";
    }



}
