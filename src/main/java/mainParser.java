import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import  org.jsoup.select.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class mainParser {
    public static String city = "краснодар";
    public static String modelName = "elantra";
    public static String carCmp = "";
//    public static String showroomPage = "https://showroom.hyundai.ru/?utm_medium=referral&utm_source=hyundai.ru&utm_campaign=main_menu";

    public static void main(String[] args) {
        long timer=System.currentTimeMillis();

        getURLDealers().forEach( dealer ->{
            HashMap<String, String> dealerMap;
            dealerMap = (HashMap<String, String>) dealer;
            System.out.print(parsePage(getModelsLinks(dealerMap.get("lego_car_link"))));
        });

        System.out.println((System.currentTimeMillis()-timer)+"millis");

    }


    public static ArrayList getModelsLinks(String htmlPath) {
        ArrayList<String> links = new ArrayList<>();
        String fullPath = (htmlPath + "/cars/").replaceAll("(//cars/)", "/cars/");
        try {
            Document allHTMLData = Jsoup.connect(fullPath).ignoreContentType(true).get();
            Elements allModels = allHTMLData.select("div.c__i");
            allModels.forEach(model -> {
                if (model.select("div.c__i-title").get(0).text().toLowerCase().contains(modelName.toLowerCase())) {
                    if (!links.contains(htmlPath + model.select("a.c__i-img").get(0).attr("href"))) {
                        links.add((htmlPath + model.select("a.c__i-img").get(0).attr("href")).replaceAll("(//cars/)", "/cars/"));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return links;
    }


    public static String parsePage(ArrayList links) {
        StringBuilder builder = new StringBuilder();
        links.forEach(link -> {
            try {
   //             System.out.println(link);
                Document allHTMLData = Jsoup.connect(link.toString()).ignoreContentType(true).get();
                Elements cars = allHTMLData.select("div.cr__i--fl");
                String dealer;
                if (allHTMLData.select("div.h__name").isEmpty() ) {
                    dealer = allHTMLData.select("span.h__btm-lf__bl-link").get(0).text();
                } else {
                    dealer = allHTMLData.select("div.h__name").get(0).text();
                }
                cars.forEach(element -> {
                    if (element.select("a.cr__i-cmp").get(0).text().toLowerCase().contains(carCmp.toLowerCase())) {
                        builder.append(dealer + " - " + element.select("a.cr__i-name").get(0).text() + " - ");
                        builder.append(element.select("a.cr__i-cmp").get(0).text() + " - ");
                        if (!element.select("div.cr__i-sm__oldvalue").isEmpty()) {
                            builder.append(element.select("div.cr__i-sm__oldvalue").get(0).text() + "\n");
                        } else {
                            builder.append(element.select("div.cr__i-sm__value").get(0).text() + "\n");
                        }

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return builder.toString();
    }


    // Получает ссылки на шоурумы всех дилеров
    public static ArrayList getURLDealers () {
        File citiesFile = new File("data/htmlCities");
        ArrayList<HashMap> urlList = new ArrayList<>();
        try {
              Document data = Jsoup.parse(citiesFile);
 //           Document data = Jsoup.connect(showroomPage).ignoreContentType(true).get();
 //           System.out.println(data);
                data.getElementsByTag("script").forEach(element -> {
                if(!element.dataNodes().isEmpty() && element.dataNodes().get(0).toString().contains("form:{dealers:")) {
                    String stringAllData = element.dataNodes().get(0).toString().substring(element.dataNodes().get(0).toString().indexOf("form:{dealers:"));
                    String onlyDealers = stringAllData.substring(stringAllData.indexOf("[{"),stringAllData.indexOf("}]")+2);

                    String jsonDealers = onlyDealers.replaceAll("\\$","O").replaceAll("(\\w+)\\s*:", "\"$1\": " ).replaceAll(":\\s*(\\w+)\\s*,", ": \"$1\"," ).replaceAll("(\"\"https\")\\s*:\\s*","\"https:").replaceAll("(\\w+)}","\"$1\"}").replaceAll("(\"\"http\")\\s*:\\s*","\"http:");
                    JSONParser dealersParser = new JSONParser();
                    JSONArray jsonArray;
                    try {
                        jsonArray = (JSONArray) dealersParser.parse(jsonDealers);
                        jsonArray.forEach(unit->{
                                try {
                                    JSONObject obj= (JSONObject) dealersParser.parse(unit.toString());
                                    if(obj.get("address").toString().toLowerCase().contains(city.toLowerCase()) && obj.get("lego_car_link").toString().toLowerCase().contains("http") ) {
                                        HashMap<String, String> carLink = new HashMap<>();
                                        carLink.put("name",obj.get("name").toString());
                                        carLink.put("address", obj.get("address").toString());
                                        carLink.put("lego_car_link", obj.get("lego_car_link").toString());
                                        urlList.add(carLink);
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
               });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    return urlList;
    }
}
