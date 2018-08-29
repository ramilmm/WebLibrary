package WebLibrary.Model.JsonModel;

import java.util.ArrayList;

public class WeatherResponseJson {

    private String cod;
    private String message;
    private int cnt;
    private ArrayList<ListItems> list;
    private WeatherCity city;

    public String getCod() {
        return cod;
    }

    public String getMessage() {
        return message;
    }

    public int getCnt() {
        return cnt;
    }

    public ArrayList<ListItems> getList() {
        return list;
    }

    public WeatherCity getCity() {
        return city;
    }
}

class Clouds {
    public int all;
}

class Rain {

}

class Sys {
    public String pod;
}

class WeatherCity {

    public long id;
    public String name;
    public Coord coord;
    public String country;
}

class Coord {
    public float lat;
    public float lon;
}
