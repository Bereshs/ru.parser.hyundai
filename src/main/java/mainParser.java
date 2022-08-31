
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;


public class mainParser {
    public static String city = "краснодар";
    public static String modelName = "elantra";
    public static String carConfig = "prestige";
    public static String citiesfilePath = "data/htmlCities";

    public static String outputFile = "data/parsing.json";

    public static String smsPhone = "";
    public static String smsKey="";
    public static void main(String[] args) throws IOException {
        long timer=System.currentTimeMillis();
        JSONObject dealerList = new JSONObject();
        JSONObject oldDealerList = new JSONObject();
        ArrayList<Dealer> dealerListForPrint =  new ArrayList<>();

        if(args.length>0) {
            for(int i=0;i<args.length;i++) {
                if(i<args.length-1) {
                    if (args[i].toLowerCase().contains("-city")) city = args[i+1].toLowerCase();
                    if (args[i].toLowerCase().contains("-modelname")) modelName = args[i+1].toLowerCase();
                    if (args[i].toLowerCase().contains("-carconfig")) carConfig = args[i+1].toLowerCase();
                    if (args[i].toLowerCase().contains("-filecitiespath")) citiesfilePath = args[i+1].toLowerCase();
                    if (args[i].toLowerCase().contains("-fileoutputpath")) outputFile = args[i+1].toLowerCase();
                    if (args[i].toLowerCase().contains("-smskey")) smsKey = args[i+1].toLowerCase();
                    if (args[i].toLowerCase().contains("-smsphone")) smsPhone = args[i+1].toLowerCase();
                }
            }
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = new JSONObject();

        try (Reader reader = new FileReader(outputFile)) {
            jsonObject = (JSONObject) parser.parse(reader);
        } catch (FileNotFoundException e) {
            System.out.println("Data file not found "+outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        jsonObject.forEach((name, data)-> {
            oldDealerList.put(name, data);
        });

        getURLDealers().forEach( dealer ->{
            HashMap<String, String> dealerMap;
            dealerMap = (HashMap<String, String>) dealer;
            Dealer dealerData= new Dealer();
            dealerData = parsePage(getModelsLinks(dealerMap.get("lego_car_link")));
            if (dealerData.getCount()>0) {
                dealerList.put(dealerData.getName(), dealerData.toJson().toString());
                dealerListForPrint.add(dealerData);
            }
        });

        if(oldDealerList.toString().length()>1 && !dealerList.equals(oldDealerList)) {
            System.out.println("Найдены изменения:");
            StringBuilder smsMessage =  new StringBuilder();
            smsMessage.append("Новые авто: \n");
            dealerListForPrint.forEach(dealer -> {
                smsMessage.append(dealer.getName()).append("\n");
                System.out.println(dealer.toString());
            });
            if(smsKey.length()>=36 && smsPhone.length()>=11) {
                URL url = new URL("https://sms.ru/sms/send?api_id="+smsKey+"&to="+smsPhone+"&msg="+ URLEncoder.encode(smsMessage.toString()));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("accept", "application/json");
                InputStream responseStream = con.getInputStream();
            }

            try (FileWriter file = new FileWriter(outputFile)) {
                file.write(dealerList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("Изменения не найдены");
        }

        System.out.println("Script comleted for "+(System.currentTimeMillis()-timer)+" ms");
    }

// получаем ссылки на страницы с моделями автомобилей
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

    // Парсинг страницы с доступными к попкпке автомобилей
    public static Dealer parsePage(ArrayList links) {
        Dealer newDealer = new Dealer();
        links.forEach(link -> {
            try {
                Document allHTMLData = Jsoup.connect(link.toString()).ignoreContentType(true).get();
                Elements cars = allHTMLData.select("div.cr__i--fl");
                String dealer;
                if (allHTMLData.select("div.h__name").isEmpty() ) {
                    dealer = allHTMLData.select("span.h__btm-lf__bl-link").get(0).text();
                } else {
                    dealer = allHTMLData.select("div.h__name").get(0).text();
                }
                newDealer.setName(dealer);
                cars.forEach(element -> {
                    if (element.select("a.cr__i-cmp").get(0).text().toLowerCase().contains(carConfig.toLowerCase())) {
                        String carPrice;
                        if (!element.select("div.cr__i-sm__oldvalue").isEmpty()) {
                            carPrice = element.select("div.cr__i-sm__oldvalue").get(0).text();
                        } else {
                            carPrice = element.select("div.cr__i-sm__value").get(0).text();
                        }
                        Car newCar = new Car(element.select("a.cr__i-name").get(0).text(),
                                element.select("div.cr__i-ch__bl").get(0).text().replaceAll("\\s*(Цвет кузова:)\\s*", ""),
                                element.select("a.cr__i-cmp").get(0).text(),
                                carPrice);
    //                    System.out.println(newCar);
                        newDealer.add(newCar);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return newDealer;
    }


    // Получает ссылки на шоурумы всех дилеров из файла
    public static ArrayList getURLDealers () {
        File citiesFile = new File(citiesfilePath);
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
