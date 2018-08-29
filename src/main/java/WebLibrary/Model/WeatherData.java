package WebLibrary.Model;

public class WeatherData {

    private int morning_temp;
    private int day_temp;
    private int night_temp;
    private int morning_wind;
    private int day_wind;
    private int night_wind;
    private String morning_icon;
    private String day_icon;
    private String night_icon;


    public int getMorning_temp() {
        return morning_temp;
    }

    public void setMorning_temp(int morning_temp) {
        this.morning_temp = morning_temp;
    }

    public int getDay_temp() {
        return day_temp;
    }

    public void setDay_temp(int day_temp) {
        this.day_temp = day_temp;
    }

    public int getNight_temp() {
        return night_temp;
    }

    public void setNight_temp(int night_temp) {
        this.night_temp = night_temp;
    }


    public int getMorning_wind() {
        return morning_wind;
    }

    public void setMorning_wind(int morning_wind) {
        this.morning_wind = morning_wind;
    }

    public int getDay_wind() {
        return day_wind;
    }

    public void setDay_wind(int day_wind) {
        this.day_wind = day_wind;
    }

    public int getNight_wind() {
        return night_wind;
    }

    public void setNight_wind(int night_wind) {
        this.night_wind = night_wind;
    }

    public String getMorning_icon() {
        return morning_icon;
    }

    public void setMorning_icon(String morning_icon) {
        this.morning_icon = morning_icon;
    }

    public String getDay_icon() {
        return day_icon;
    }

    public void setDay_icon(String day_icon) {
        this.day_icon = day_icon;
    }

    public String getNight_icon() {
        return night_icon;
    }

    public void setNight_icon(String night_icon) {
        this.night_icon = night_icon;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "morning_temp=" + morning_temp +
                ", day_temp=" + day_temp +
                ", night_temp=" + night_temp +
                ", morning_wind=" + morning_wind +
                ", day_wind=" + day_wind +
                ", night_wind=" + night_wind +
                ", morning_icon='" + morning_icon + '\'' +
                ", day_icon='" + day_icon + '\'' +
                ", night_icon='" + night_icon + '\'' +
                '}';
    }
}
