package WebLibrary.Service;

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
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

@Service
@Configurable
public class MorningPostService {

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
//    private final Integer SEND_TO = 296861219;      //TEST



    private static final Logger log = LogManager.getLogger(PostService.class);

    @Autowired
    PostService postService;
    @Autowired
    MorningImageCreator mic;

    @PostConstruct
    public void init(){
        log.info("START SETTING UP!!!");
        try {
            input = new FileInputStream("conf/config.properties");

            prop.load(input);

            ACCESS_TOKEN = prop.getProperty("VK_TOKEN");
            PUBLIC_ID = Integer.valueOf(prop.getProperty("PUBLIC_ID_2"));
            ALBUM_ID = Integer.valueOf(prop.getProperty("ALBUM_ID"));
            API_VERSION = prop.getProperty("API_VERSION");
            PROFILE_ID = Integer.valueOf(prop.getProperty("PROFILE_ID"));
            SEND_TO = Integer.valueOf(prop.getProperty("SEND_TO"));
            log.info("MorningPostService properties set up");
        } catch (IOException e) {
            log.warn("ERROR in MorningPostService properties setting up!!!");
            log.warn(e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 20 * * *")
    public void morningTask(){
        log.info("Start building morning image");
        mic.create();
        sendToSuggestedNews();
        sendNotification();
    }

    public void sendToSuggestedNews(){

        System.out.println(ACCESS_TOKEN);
        log.info("Sending morning image to suggested news...");
        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        HttpPost request = new HttpPost("https://api.vk.com/method/wall.post");
        ArrayList<String> photosId = new ArrayList<>();
        try {
            photosId.add(uploadPhotoToVk());
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder photo_param = new StringBuilder();
        for (String photo : photosId) {
            photo_param.append("photo").append(PROFILE_ID).append("_").append(photo).append(",");
        }

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("owner_id", PUBLIC_ID.toString()));
        postParameters.add(new BasicNameValuePair("attachments", String.valueOf(photo_param)));
        postParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        postParameters.add(new BasicNameValuePair("v", API_VERSION));

        try {
            request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("#RESPONSE: " + response);
        log.info("MORNING POST SUGGESTING TASK DONE!");
    }

    public String uploadPhotoToVk() throws IOException {
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

        String result = postService.getJson(response);
        String uploadUrl = result.substring(result.indexOf("upload_url") + 13, result.indexOf("album_id") - 3);

        request = new HttpPost(uploadUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        File f = new File("morning.png");
        try {
            builder.addBinaryBody(
                    "file",
                    new FileInputStream(f),
                    ContentType.APPLICATION_OCTET_STREAM,
                    f.getName()
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        HttpEntity multipart = builder.build();
        request.setEntity(multipart);
        response = client.execute(request);

        String json = postService.getJson(response);

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

        String sb = postService.getJson(response);
        f.delete();

        return sb.substring(sb.indexOf("id") + 4, sb.indexOf("album_id") - 2);

    }

    public void sendNotification() {
        log.info("Sending notification...");
        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        StringBuilder msg = new StringBuilder("");
        msg.append("Утренняя картинка готова!").append("\n")
                .append("https://vk.com/podslushano_kanash");
        HttpPost request = new HttpPost("https://api.vk.com/method/messages.send");

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("user_id", String.valueOf(SEND_TO)));
        postParameters.add(new BasicNameValuePair("random_id", String.valueOf(PUBLIC_ID + PROFILE_ID + Math.random())));
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
