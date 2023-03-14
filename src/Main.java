import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {

        System.out.println("Välj en stad");
        Scanner input = new Scanner(System.in);
        String city = input.nextLine().toLowerCase();
        input.close();
        findCityKey(city);
    }

    static void findCityKey(String city) throws IOException, ParseException {
        URL url = new URL("https://opendata-download-metobs.smhi.se/api/version/1.0/parameter/1.json");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();

        JSONObject data = (JSONObject) new JSONParser().parse(String.valueOf(stringBuilder));
        JSONArray stationArray = (JSONArray) data.get("station");
        for (int i = 0; i < stationArray.size(); i++) {
            JSONObject objectsInStationArray = (JSONObject) stationArray.get(i);
            String cityName = (String) objectsInStationArray.get("name");
            if (cityName.toLowerCase().equals(city)) {
                double longitude = Double.parseDouble(objectsInStationArray.get("longitude").toString());
                double latitude = Double.parseDouble(objectsInStationArray.get("latitude").toString());
                showTheWeather(longitude, latitude, cityName);
                break;
            }
            if (i == 959 && !cityName.toLowerCase().equals(city)) {
                for (int j = 0; j < stationArray.size(); j++) {
                    objectsInStationArray = (JSONObject) stationArray.get(j);
                    cityName = (String) objectsInStationArray.get("name");
                    if (cityName.toLowerCase().contains(city)) {
                        double longitude = Double.parseDouble(objectsInStationArray.get("longitude").toString());
                        double latitude = Double.parseDouble(objectsInStationArray.get("latitude").toString());
                        showTheWeather(longitude, latitude, cityName);
                        break;
                    }
                    if (j == 959 && !cityName.toLowerCase().contains(city)){
                        System.out.println("Hittade ingen matchande stad");
                    }
                }
            }
        }
    }


    static void showTheWeather(double longitude, double latitude, String cityName) throws
            IOException, ParseException {

        URL url = new URL("https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/" + longitude + "/lat/" + latitude + "/data.json");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        if (conn.getResponseCode() == 200) {
            System.out.println("Anslutning upprättad");
        } else {
            System.out.println("Anslutning misslyckades");
        }

        StringBuilder strBldr = new StringBuilder();
        Scanner scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            strBldr.append(scanner.nextLine());
        }
        scanner.close();

//        JSONParser parser = new JSONParser();
        JSONObject weatherData = (JSONObject) new JSONParser().parse(String.valueOf(strBldr));

        JSONArray timeArray = (JSONArray) weatherData.get("timeSeries");
        for (int i = 0; i < 72; i++) {
            JSONObject propertiesOfTimeArray = (JSONObject) timeArray.get(i);
            String time = propertiesOfTimeArray.get("validTime").toString();
            JSONArray parameters = (JSONArray) propertiesOfTimeArray.get("parameters");
            JSONObject tempObj = (JSONObject) parameters.get(10);
            JSONArray valueArray = (JSONArray) tempObj.get("values");
            double temp = Double.parseDouble(valueArray.get(0).toString());
            System.out.println("Temperaturen i " + cityName + " " + time + " är " + temp);
        }

    }
}
