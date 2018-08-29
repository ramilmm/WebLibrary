package WebLibrary.Service;

import WebLibrary.Model.Post;
import WebLibrary.Repository.PostRepository;
import WebLibrary.Utils.HTMLParser;
import WebLibrary.Utils.ImageGenerator;
import WebLibrary.Utils.MorningImageCreator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@Configurable
public class PostService {

    private Properties prop = new Properties();
    private InputStream input = null;

    private String ACCESS_TOKEN;
    private Integer PUBLIC_ID;
//    private Integer PUBLIC_ID = -149091110;   //TEST
    private String API_VERSION;
    private Integer ALBUM_ID;
//    private Integer ALBUM_ID = 249933107;     //TEST
    private Integer PROFILE_ID;
    private Integer SEND_TO;
//    private Integer SEND_TO = 296861219;      //TEST
    private Long photo_id = 0l;
    private Boolean GENERATED_IMG = false;
    private List<Post> notificationCity;
    private HTMLParser html = new HTMLParser();
    private ImageGenerator ig = new ImageGenerator();

    private static final Logger log = LogManager.getLogger(PostService.class);

    @Autowired
    private PostRepository postRepository;

    @PostConstruct
    public void init(){
        log.info("START SETTING UP!!!");
        try {
            input = new FileInputStream("conf/config.properties");

            prop.load(input);

            ACCESS_TOKEN = prop.getProperty("VK_TOKEN");
            PUBLIC_ID = Integer.valueOf(prop.getProperty("PUBLIC_ID"));
            ALBUM_ID = Integer.valueOf(prop.getProperty("ALBUM_ID"));
            API_VERSION = prop.getProperty("API_VERSION");
            PROFILE_ID = Integer.valueOf(prop.getProperty("PROFILE_ID"));
            SEND_TO = Integer.valueOf(prop.getProperty("SEND_TO"));
            log.info("PostService properties set up");
        } catch (IOException e) {
            log.warn("ERROR in PostService properties setting up!!!");
            log.warn(e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 1200000, initialDelay = 1000)
    public void run() {
        log.info("Start parsing gov.cap");
        parseGovCap();

        log.info("Start parsing kanashen.ru");
        parseKanashen();
    }

    public void parseKanashen() {
        html.parse("kanashen");

        ArrayList<Post> allPosts = postRepository.findFirst50ByOrderByIdDesc();
        ArrayList<Post> news = html.getPostsKanashen();
        saveList(news,allPosts);
    }

    public void parseGovCap() {
        html.parse("govcap");


        ArrayList<Post> allPosts = postRepository.findFirst50ByOrderByIdDesc();
        ArrayList<Post> mainNews = html.getPostsGovcap("main");
        saveList(mainNews, allPosts);

        allPosts = postRepository.findFirst50ByOrderByIdDesc();
        ArrayList<Post> news = html.getPostsGovcap("news");
        saveList(news, allPosts);


        allPosts = postRepository.findFirst50ByOrderByIdDesc();

        notificationCity = allPosts.stream()
                .filter(post -> post.getTitle().contains("Водоканал") || post.getTitle().contains("Информационное донесение") || post.getTitle().contains("неблагоприятных метеорологических"))
                .collect(Collectors.toList());

        ArrayList<Post> declarations = html.getPostsGovcap("declarations");
        saveList(declarations, allPosts);

    }

    public void saveList(ArrayList<Post> list, ArrayList<Post> alreadyExist) {
        log.info("Checking post...");
        DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        Date date1, date2;
        Boolean isExist = false;
        for (Post post : list) {
            for (Post exist : alreadyExist) {
                if (post.getTitle().equals(exist.getTitle())) {
                    isExist = true;
                    if (post.getTitle().contains("Водоканал") || post.getTitle().contains("Информационное донесение") || post.getTitle().contains("неблагоприятных метеорологических")) {
                        for (Post p : notificationCity) {
                            if (post.getTitle().equals(p.getTitle()) && post.getPublish().equals(p.getPublish())) {
                                isExist = true;
                                log.info("Post already exist");
                                break;
                            } else {
                                long diffMin = 0;
                                try {
                                    date1 = dateFormat.parse(post.getPublish());
                                    if (p.getPublish() != null) {
                                        date2 = dateFormat.parse(p.getPublish());
                                        long diff = Math.abs(date1.getTime() - date2.getTime());
                                        diffMin = diff / (60 * 1000);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                isExist = diffMin < 3;
                            }
                        }
                    }
                }
            }
            if (!isExist) {
                log.info("Saving new post...");
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
        log.info("Sending to suggested news...");
        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        HttpPost request = new HttpPost("https://api.vk.com/method/wall.post");

        ArrayList<String> photosId = new ArrayList<>();
        if (post.getPhotoLinks().size() > 0) {
            for (int i = 0; i < post.getPhotoLinks().size(); i++) {
                if (i <= 10) {
                    photosId.add(downloadPhoto(post.getPhotoLinks().get(i)));
                } else break;
            }
        }else if (post.getPhotoLinks().size() == 0) {
            GENERATED_IMG = true;
            log.info("USING GENERATED IMAGE");
            ig.getPost(post);
            photosId.add(uploadPhoto());
        }

        StringBuilder photo_param = new StringBuilder();
        for (String photo : photosId) {
            photo_param.append("photo").append(PROFILE_ID).append("_").append(photo).append(",");
        }

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("owner_id", PUBLIC_ID.toString()));
        postParameters.add(new BasicNameValuePair("message", post.getTitle() + "\n\n" + post.getText() + "\n\n Источник: " + post.getSource()));
        postParameters.add(new BasicNameValuePair("attachments", String.valueOf(photo_param)));
        postParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        postParameters.add(new BasicNameValuePair("v", API_VERSION));

        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info(post.getTitle());

        HttpResponse response = client.execute(request);
        GENERATED_IMG = false;

        log.info("#RESPONSE: " + response);
        log.info("POST SUGGESTING TASK DONE!");
    }

    public String uploadPhoto() throws IOException {
        log.info("Uploading photo...");
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
            request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = client.execute(request);

        String result = getJson(response);
        String uploadUrl = result.substring(result.indexOf("upload_url") + 13, result.indexOf("album_id") - 3);

        request = new HttpPost(uploadUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        File f = new File("image" + photo_id + ".jpg");
        if (GENERATED_IMG) {
            f = new File("result.png");
            GENERATED_IMG = false;
        }
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

        String server = json.substring(json.indexOf("server") + 8, json.indexOf("photos_list") - 2);
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
            request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        response = client.execute(request);
        log.info("#RESPONSE: " + response);

        String sb = getJson(response);
        f.delete();

        return sb.substring(sb.indexOf("id") + 4, sb.indexOf("album_id") - 2);
    }

    public String downloadPhoto(String photo) throws IOException {
        try {
            BufferedImage image = null;
            URL url = new URL(photo);
            image = ImageIO.read(url);
            if (image != null) {
                ImageIO.write(image, "jpg", new File("image" + ++photo_id + ".jpg"));
            }
        } catch (FileNotFoundException ignored) {
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
        log.info("Sending notification...");
        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        StringBuilder msg = new StringBuilder("");
        msg.append("Новая новость!").append("\n")
                .append(post.getTitle()).append("\n")
                .append("https://vk.com/kanash_news");
        HttpPost request = new HttpPost("https://api.vk.com/method/messages.send");
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("user_id", String.valueOf(SEND_TO)));
        postParameters.add(new BasicNameValuePair("random_id", String.valueOf(PUBLIC_ID + PROFILE_ID + photo_id)));
        postParameters.add(new BasicNameValuePair("peer_id", String.valueOf(SEND_TO)));
        postParameters.add(new BasicNameValuePair("message", String.valueOf(msg)));
        postParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        postParameters.add(new BasicNameValuePair("v", API_VERSION));
        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HttpResponse response = null;
        try {
            response = client.execute(request);
            log.info("#RESPONSE: " + response);
            log.info("NOTIFICATION SENDING TASK DONE!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
