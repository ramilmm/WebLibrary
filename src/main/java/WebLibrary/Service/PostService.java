package WebLibrary.Service;

import WebLibrary.Model.Post;
import WebLibrary.Repository.PostRepository;
import WebLibrary.Utils.HTMLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Service
@Configurable
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public void savePost(Post post) {
        postRepository.save(post);
    }

    public Post findPostByTitle(String title) {
        return postRepository.findPostByTitle(title);
    }

    public List<Post> getAll(){
        return postRepository.findAll();
    }

    public void parse(String site) {
        HTMLParser html = new HTMLParser();
        html.parse(site);

        ArrayList<Post> allPosts = (ArrayList<Post>) postRepository.findAll();
        ArrayList<Post> mainNews = html.getPosts("main");
        saveList(mainNews,allPosts);

        allPosts = (ArrayList<Post>) postRepository.findAll();
        ArrayList<Post> news = html.getPosts("news");
        saveList(news,allPosts);

        allPosts = (ArrayList<Post>) postRepository.findAll();
        ArrayList<Post> declarations = html.getPosts("declarations");
        saveList(declarations,allPosts);
    }

    public void saveList(ArrayList<Post> list, ArrayList<Post> alreadyExist){
        Boolean isExist = false;
        for (Post post : list) {
            for (Post exist: alreadyExist) {
                if (post.getTitle().equals(exist.getTitle())) {
                    isExist = true;
                }
            }
            if (!isExist) {
                postRepository.save(post);
            }
            isExist = false;
        }
    }

}
