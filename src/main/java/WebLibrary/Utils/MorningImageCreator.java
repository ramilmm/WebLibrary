package WebLibrary.Utils;

import WebLibrary.Model.JsonModel.ListItems;
import WebLibrary.Model.WeatherData;
import WebLibrary.Model.JsonModel.WeatherResponseJson;
import WebLibrary.Service.PostService;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

@Component
public class MorningImageCreator {

    private Properties prop = new Properties();
    private InputStream input = null;

    private String API_HOST;
    private String CITY_ID;
    private String UNITS;
    private String LANG;
    private String APP_ID;
    private WeatherData wd = new WeatherData();
    private int yForTopText = 100,
            yForCenterTopText = 360,
            yForCenterBottomText = 440,
            xForWeatherTitle = 163, yForWeatherTitle = 620,
            xForWeatherIcon = 170, yForWeatherIcon = 640,
            xForWeatherTemp = 177, yForWeatherTemp = 715,
            xForWeatherDescr = 165, yForWeatherDescr = 750;
    private int topFontSize = 35, centerTopTextSize = 50, centerBottomTextSize = 70, bottomFontSize = 30;
    private Font ClearSansLight, ClearSansRegular, ClearSansThin, ClearSansBold, ClearSansMedium;
    private String topText = "Доброе утро, Канаш!";
    private String DAY_OF_WEEK, DAY_OF_MONTH;
    private static final Logger log = LogManager.getLogger(PostService.class);

    @PostConstruct
    public void init() {
        this.importFonts();
        try {
            input = new FileInputStream("conf/config.properties");

            prop.load(input);

            API_HOST = prop.getProperty("API_HOST");
            CITY_ID = prop.getProperty("CITY_ID");
            UNITS = prop.getProperty("UNITS");
            LANG = prop.getProperty("LANG");
            APP_ID = prop.getProperty("APP_ID");

            log.info("MorningImageCreator properties set up");
        } catch (IOException e) {
            log.warn("ERROR in MorningImageCreator properties setting up!!!");
            log.warn(e.getMessage());
        }
    }

    public void create() {
        this.formatDate();

        try {
            this.getWeatherData();
            this.buildImage();
            log.info("Morning image GENERATED SUCCESSFULLY");
        } catch (IOException e) {
            log.warn("ERROR in building morning image!!!");
            e.printStackTrace();
        }
    }

    private void buildImage() throws IOException {
        BufferedImage bg = ImageIO.read(new File(this.getRandomBG()));

        BufferedImage result = new BufferedImage(bg.getWidth(), bg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D holst = (Graphics2D) result.getGraphics();

        holst.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        holst.drawImage(bg, 0, 0, null);

        holst.setFont(ClearSansLight);

        printCenteredText(holst, topText, yForTopText);

        //rendering date

        holst.setFont(ClearSansRegular);

        printCenteredText(holst, DAY_OF_WEEK, yForCenterTopText);

        holst.setFont(ClearSansBold);

        printCenteredText(holst, DAY_OF_MONTH, yForCenterBottomText);

        //rendering weather data

        System.out.println(wd.toString());

        holst.setFont(ClearSansThin);

        holst.drawString("Утро", xForWeatherTitle, yForWeatherTitle);
        holst.drawString("День", xForWeatherTitle + 200, yForWeatherTitle);
        holst.drawString("Вечер", xForWeatherTitle + 400, yForWeatherTitle);


        holst.drawImage(ImageIO.read(new File(wd.getMorning_icon())), xForWeatherIcon, yForWeatherIcon, 50, 50, null, null);
        holst.drawImage(ImageIO.read(new File(wd.getDay_icon())), xForWeatherIcon + 200, yForWeatherIcon, 50, 50, null, null);
        holst.drawImage(ImageIO.read(new File(wd.getNight_icon())), xForWeatherIcon + 400, yForWeatherIcon, 50, 50, null, null);

        holst.drawString(String.valueOf(wd.getMorning_temp()), xForWeatherTemp, yForWeatherTemp);
        holst.drawString(String.valueOf(wd.getDay_temp()), xForWeatherTemp + 200, yForWeatherTemp);
        holst.drawString(String.valueOf(wd.getNight_temp()), xForWeatherTemp + 400, yForWeatherTemp);

        holst.drawString(String.valueOf(wd.getMorning_wind()) + "м/с", xForWeatherDescr, yForWeatherDescr);
        holst.drawString(String.valueOf(wd.getDay_wind()) + "м/с", xForWeatherDescr + 200, yForWeatherDescr);
        holst.drawString(String.valueOf(wd.getNight_wind()) + "м/с", xForWeatherDescr + 400, yForWeatherDescr);


        //compile image

        ImageIO.write(result, "PNG", new File("morning.png"));
    }

    private void getWeatherData() throws IOException {

        StringBuilder api = new StringBuilder();
        api.append(API_HOST).append("id=").append(CITY_ID)
                .append("&units=").append(UNITS)
                .append("&lang=").append(LANG)
                .append("&appid=").append(APP_ID);


        HttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        HttpGet request = new HttpGet(String.valueOf(api));

        HttpResponse response = client.execute(request);

        String responseString = new BasicResponseHandler().handleResponse(response);

        System.out.println(responseString);

        Gson gson = new Gson();
        WeatherResponseJson weather = gson.fromJson(responseString, WeatherResponseJson.class);

        setWD(weather);

    }

    private void setWD(WeatherResponseJson weather) {

        if (weather.getCod().equals("200")) {
            ArrayList<ListItems> list = weather.getList();
            for (int i = 2; i < 10; i++) {
                ListItems lt = list.get(i);
                if (lt.getDt_txt().contains("06:00")) {
                    wd.setMorning_temp(Math.round(lt.getMain().temp));
                    wd.setMorning_wind(Math.round(lt.getWind().getSpeed()));
                    wd.setMorning_icon(getIconPath(lt.getWeather().get(0).icon));
                } else if (lt.getDt_txt().contains("12:00")) {
                    wd.setDay_temp(Math.round(lt.getMain().temp));
                    wd.setDay_wind(Math.round(lt.getWind().getSpeed()));
                    wd.setDay_icon(getIconPath(lt.getWeather().get(0).icon));
                } else if (lt.getDt_txt().contains("18:00")) {
                    wd.setNight_temp(Math.round(lt.getMain().temp));
                    wd.setNight_wind(Math.round(lt.getWind().getSpeed()));
                    wd.setNight_icon(getIconPath(lt.getWeather().get(0).icon));
                }
            }
        }

    }

    private String getIconPath(String icon) {
        return "icons/0" + icon + ".png";
    }

    private void importFonts() {
        File new_font = new File("fonts/ClearSans-Light.ttf");
        try {
            ClearSansLight = Font.createFont(Font.TRUETYPE_FONT, new_font);
            ClearSansLight = ClearSansLight.deriveFont(Font.PLAIN, topFontSize);

            new_font = new File("fonts/ClearSans-Regular.ttf");
            ClearSansRegular = Font.createFont(Font.TRUETYPE_FONT, new_font);
            ClearSansRegular = ClearSansRegular.deriveFont(Font.PLAIN, centerTopTextSize);

            new_font = new File("fonts/ClearSans-Thin.ttf");
            ClearSansThin = Font.createFont(Font.TRUETYPE_FONT, new_font);
            ClearSansThin = ClearSansThin.deriveFont(Font.PLAIN, bottomFontSize);

            new_font = new File("fonts/ClearSans-Medium.ttf");
            ClearSansMedium = Font.createFont(Font.PLAIN, new_font);
            ClearSansMedium = ClearSansMedium.deriveFont(Font.PLAIN, topFontSize);

            new_font = new File("fonts/ClearSans-Bold.ttf");
            ClearSansBold = Font.createFont(Font.TRUETYPE_FONT, new_font);
            ClearSansBold = ClearSansBold.deriveFont(Font.PLAIN, centerBottomTextSize);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private void formatDate() {
        ImageGenerator ig = new ImageGenerator();
        StringBuilder DISPLAYED_DATE = ig.getDate(true);
        DAY_OF_WEEK = DISPLAYED_DATE.toString().substring(0, DISPLAYED_DATE.indexOf(",")).trim();
        DAY_OF_MONTH = DISPLAYED_DATE.toString().substring(DISPLAYED_DATE.indexOf(",") + 1).trim();
    }


    private void printCenteredText(Graphics2D holst, String s, int y) {
        FontMetrics fm = holst.getFontMetrics();
        Rectangle2D textsize = fm.getStringBounds(s, holst);
        int xPos = (int) ((800 - textsize.getWidth()) / 2);
        holst.drawString(s, xPos, y);
    }

    private String getRandomBG() {
        int random = 1 + (int) (Math.random() * 19);
        System.out.println(random);
        return "morning/m" + random + ".png";
    }

}
