package WebLibrary.Utils;

import WebLibrary.Model.Post;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

public class ImageGenerator {

    private int xForNewsTitle = 388, yForNewsTitle = 500,
            xForAdvertTitle = 300, yForAdvertTitle = 500,
            xForNotificationTitle = 263, yForNotificationTitle = 450,
            xForNotificationSubtitle = 430, yForNotificationSubtitle = 550,
            xForDate = 50, yForBottomInfo = 910,
            xForCityTitle = 1100;
    private int titleFontSize = 95, dateFontSize = 45, notificationByFontSize = 75;
    private String newsTitle = "# НОВОСТЬ",
            advertTitle = "# ОБЪЯВЛЕНИЕ",
            notificationTitle = "# УВЕДОМЛЕНИЕ",
            notificationSubtitle = "ЭЛЕКТРОЭНЕРГИЯ",
            cityTitle = "Канаш";
    private Font ClearSansLight, ClearSansRegular, ClearSansThin;
    private StringBuilder DISPLAYED_DATE = new StringBuilder();

    public void getPost(Post myPost) {

        DISPLAYED_DATE = getDate(false);
        importFonts();

        try {
            buildImage(myPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DISPLAYED_DATE.setLength(0);

    }

    public void buildImage(Post post) throws IOException {

        BufferedImage bg = ImageIO.read(new File("img1.png"));

        BufferedImage result = new BufferedImage(bg.getWidth(), bg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D holst = (Graphics2D) result.getGraphics();

        holst.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        holst.drawImage(bg, 0, 0, null);

        holst.setFont(ClearSansLight);

        switch (post.getType()) {
            case "news":
                holst.drawString(newsTitle, xForNewsTitle, yForNewsTitle);
                break;
            case "main":
                holst.drawString(newsTitle, xForNewsTitle, yForNewsTitle);
                break;
            case "declarations": {
                String text = post.getTitle() + "\n" + post.getText().toLowerCase();
                if (text.contains("водоканал") || text.contains("газпром") || text.contains("электроэнергии") || text.contains("погодные условия") || text.contains("метеорологических")) {
                    holst.drawString(notificationTitle, xForNotificationTitle, yForNotificationTitle);

                    holst.setFont(ClearSansThin);

                    if (text.contains("водоканал")) {
                        notificationSubtitle = "ВОДОКАНАЛ";
                    } else if (text.contains("газпром")) {
                        notificationSubtitle = "ГАЗПРОМ";
                    } else if (text.contains("электроэнергии")) {
                        notificationSubtitle = "ЭЛЕКТРОЭНЕРГИЯ";
                    } else if (text.contains("погодные условия") || text.contains("метеорологических")) {
                        notificationSubtitle = "ПОГОДА";
                    }
                    center(notificationSubtitle);

                    holst.drawString(notificationSubtitle, xForNotificationSubtitle, yForNotificationSubtitle);
                } else {
                    holst.drawString(advertTitle, xForAdvertTitle, yForAdvertTitle);
                    break;
                }
            }
        }


        holst.setFont(ClearSansRegular);

        holst.drawString(DISPLAYED_DATE.toString(), xForDate, yForBottomInfo);

        holst.drawString(cityTitle, xForCityTitle, yForBottomInfo);

        ImageIO.write(result, "PNG", new File("result.png"));

    }

    public void importFonts() {

        File new_font = new File("fonts/ClearSans-Light.ttf");
        try {
            ClearSansLight = Font.createFont(Font.TRUETYPE_FONT, new_font);
            ClearSansLight = ClearSansLight.deriveFont(Font.PLAIN, titleFontSize);

            new_font = new File("fonts/ClearSans-Regular.ttf");
            ClearSansRegular = Font.createFont(Font.TRUETYPE_FONT, new_font);
            ClearSansRegular = ClearSansRegular.deriveFont(Font.PLAIN, dateFontSize);

            new_font = new File("fonts/ClearSans-Thin.ttf");
            ClearSansThin = Font.createFont(Font.TRUETYPE_FONT, new_font);
            ClearSansThin = ClearSansThin.deriveFont(Font.PLAIN, notificationByFontSize);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

    }

    public StringBuilder getDate(Boolean forMorningPost) {

        Calendar c = Calendar.getInstance();
        if (forMorningPost) {
            c.add(Calendar.DATE, 1);
        }
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int DAY = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);

        dayOfWeek = dayOfWeek == 1 ? dayOfWeek + 7 : dayOfWeek;

        DayOfWeek d = DayOfWeek.of(dayOfWeek - 1);
        Month m = Month.of(month + 1);

        Locale loc = Locale.forLanguageTag("ru");

        String MONTH = m.getDisplayName(TextStyle.FULL, loc);
        String WEEK = d.getDisplayName(TextStyle.FULL, loc);

        DISPLAYED_DATE.append(WEEK).append(", ").append(DAY).append(" ").append(MONTH);

        String buf1 = DISPLAYED_DATE.substring(0, 1).toUpperCase(), buf2 = DISPLAYED_DATE.substring(1);

        DISPLAYED_DATE.setLength(0);

        return DISPLAYED_DATE.append(buf1).append(buf2);

    }

    private void center(String notificationSubtitle) {

        switch (notificationSubtitle) {
            case "ВОДОКАНАЛ":
                xForNotificationSubtitle = 430;
                break;
            case "ГАЗПРОМ":
                xForNotificationSubtitle = 476;
                break;
            case "ПОГОДА":
                xForNotificationSubtitle = 500;
                break;
            case "ЭЛЕКТРОЭНЕРГИЯ":
                xForNotificationSubtitle = 330;
                break;
        }

    }


}
