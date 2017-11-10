package WebLibrary.Service;

import WebLibrary.Model.Post;
import WebLibrary.Repository.PostRepository;
import WebLibrary.Utils.HTMLParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Configurable
public class PostService {

    private final String ACCESS_TOKEN = "3192b4e8bce78bb9a9622b54941710405f8213557503c62a69110dabaee965eba5905113aa8c6996a62dd";
    private final Integer PUBLIC_ID = -120120712;
    private final String API_VERSION = "5.69";
    private final Integer ALBUM_ID = 249933107;
    private final Integer PROFILE_ID = 451871926;
    private Long photo_id = 0l;

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
                try {
                    sendToSuggestedNews(post);
                    sendNotification(post);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isExist = false;
        }
    }

    public void sendToSuggestedNews(Post post) throws IOException {
        HttpClient client = HttpClients.custom()
                        .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                        .build();
        HttpPost request = new HttpPost("https://api.vk.com/method/wall.post");

        ArrayList<String> photosId = new ArrayList<>();
        for (int i = 0; i < post.getPhotoLinks().size(); i++) {
            if (i <= 10) {
                photosId.add(downloadPhoto(post.getPhotoLinks().get(i)));
            }else break;
        }

        StringBuilder photo_param = new StringBuilder("");
        for (String photo: photosId) {
            photo_param.append("photo" + PROFILE_ID + "_" + photo + ",");
        }

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("owner_id", PUBLIC_ID.toString()));
        postParameters.add(new BasicNameValuePair("message", post.getTitle() + "\n\n" + post.getText() + "\n\n Источник: " + post.getSource()));
        postParameters.add(new BasicNameValuePair("attachments", String.valueOf(photo_param)));
        postParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        postParameters.add(new BasicNameValuePair("v", API_VERSION));

        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(post.getTitle());
        System.out.println(request);
        HttpResponse response = client.execute(request);
    }

    public String uploadPhoto() throws IOException {
        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        HttpPost request = new HttpPost("https://api.vk.com/method/photos.getUploadServer");
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("album_id", ALBUM_ID.toString()));
        postParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        postParameters.add(new BasicNameValuePair("v", API_VERSION));
        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = client.execute(request);

        String result = getJson(response);
        String uploadUrl = result.substring(result.indexOf("upload_url") + 13, result.indexOf("album_id") - 3);

        request = new HttpPost(uploadUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        File f = new File("image" + photo_id + ".jpg");
        builder.addBinaryBody(
                "file",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
        );

        HttpEntity multipart = builder.build();
        request.setEntity(multipart);
        response = client.execute(request);

        String json = getJson(response);

        String server = json.substring(json.indexOf("server") + 8,json.indexOf("photos_list") - 2);
        String photos_list = json.substring(json.indexOf("photos_list") + 14, json.indexOf("aid") - 3);
        String hash = json.substring(json.indexOf("hash") + 7, json.lastIndexOf("}") - 1);

        request = new HttpPost("https://api.vk.com/method/photos.save");
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("album_id", ALBUM_ID.toString()));
        postParameters.add(new BasicNameValuePair("server", server));
        postParameters.add(new BasicNameValuePair("photos_list", photos_list));
        postParameters.add(new BasicNameValuePair("hash", hash));
        postParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        postParameters.add(new BasicNameValuePair("v", API_VERSION));
        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        response = client.execute(request);

        String sb = getJson(response);
        f.delete();

        return sb.substring(sb.indexOf("id") + 4, sb.indexOf("album_id") - 2);
    }

    public String downloadPhoto(String photo) throws IOException {
        System.out.println(photo);
        try {
            BufferedImage image = null;
            URL url = new URL(photo);
            image = ImageIO.read(url);
            if (image != null){
                ImageIO.write(image, "jpg",new File("image" + ++photo_id + ".jpg"));
            }
        }
        catch (FileNotFoundException e) {
            // handle exception
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uploadPhoto();
    }

    public String getJson(HttpResponse response) {
        StringBuilder buf = null;
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            buf = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                buf.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(buf).replaceAll("\\\\", "");
    }

    public void sendNotification(Post post) {
        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        StringBuilder msg = new StringBuilder("");
        msg.append("Новая новость!").append("\n\n")
                .append(post.getTitle()).append("\n\n")
                .append("https://vk.com/kanash_news");
        HttpPost request = new HttpPost("https://api.vk.com/method/messages.send");
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("user_id", "362122119"));
        postParameters.add(new BasicNameValuePair("random_id", String.valueOf(PUBLIC_ID + PROFILE_ID + photo_id)));
        postParameters.add(new BasicNameValuePair("peer_id", "362122119"));
        postParameters.add(new BasicNameValuePair("message", String.valueOf(msg)));
        postParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        postParameters.add(new BasicNameValuePair("v", API_VERSION));
        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(getJson(response));
    }

}
