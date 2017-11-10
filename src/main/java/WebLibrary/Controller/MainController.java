package WebLibrary.Controller;

import WebLibrary.Service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@Controller
@EnableScheduling
public class MainController {

    @Autowired
    private PostService postService;

    @Scheduled(fixedDelay = 1200000, initialDelay = 1000)
    @GetMapping("/")
    public String render(){
        postService.parse("site_1");
        postService.parse("site_2");
        return "Hello World!";
    }




}
