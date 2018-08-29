package WebLibrary.Controller;

import WebLibrary.Service.MorningPostService;
import WebLibrary.Service.PostService;
import WebLibrary.Utils.HTMLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



@Controller
public class MainController {

    @Autowired
    private PostService postService;
    @Autowired
    private MorningPostService morningPostService;


    @GetMapping("/")
    public String render(){
        postService.run();
        return "Hello World!";
    }

    @GetMapping("/check")
    public String checkApp() {
        HTMLParser html = new HTMLParser();
        html.parse("kanashen");
        return "Working!";
    }

    @GetMapping("/morning")
    public String checkMorning() {
        morningPostService.morningTask();
        return "200";
    }




}
