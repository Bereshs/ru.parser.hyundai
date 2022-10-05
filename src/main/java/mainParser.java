
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class mainParser {
    public static String city = "краснодар";
    public static String modelName = "elantra";
    public static String carConfig = "active";
    public static String citiesfilePath = "data/htmlCities";
    public static String outputFile = "data/parsing.json";

    public static void main(String[] args) throws IOException, ParseException {
        long timer=System.currentTimeMillis();
        JSONObject dealerList = new JSONObject();
        CommandLine commandLine = new CommandLine(args);
        ArrayList<Dealer> dealerListForPrint =  new ArrayList<>();

        if(commandLine.get("-city").length()>0) city = commandLine.get("-city");
        if(commandLine.get("-modelname").length()>0) modelName = commandLine.get("-modelname");
        if(commandLine.get("-carconfig").length()>0) carConfig = commandLine.get("-carconfig");
        if(commandLine.get("-filecitiespath").length()>0) citiesfilePath = commandLine.get("-filecitiespath");


        SendSMS sms =  new SendSMS(commandLine.get("-smsKey"), commandLine.get("-smsphone"), "https://sms.ru/sms/send?api_id=");
        JSONObject oldDealerList = new JSONObject();
        DataFile dataFile =  new DataFile(outputFile);
        try {
            oldDealerList = dataFile.readData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CitiesPage citiesPage = new CitiesPage(citiesfilePath);

        citiesPage.getLinks(city).forEach( dealer ->{
            HashMap<String, String> dealerMap;
            dealerMap = (HashMap<String, String>) dealer;
            Dealer dealerData= new Dealer();
            DealerPage dealerPage = null;
            try {
                dealerPage = new DealerPage(dealerMap.get("lego_car_link"));
                ArrayList<String> links = dealerPage.getModelLink();
                dealerData.addCars(links);
                if (dealerData.getCount()>0) {
                    dealerList.put(dealerData.getName(), dealerData.toJson().toString());
                    dealerListForPrint.add(dealerData);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
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

            sms.send(smsMessage.toString());
            dataFile.writeData(dealerList);

        }else {
            System.out.println("Изменения не найдены");
        }

        System.out.println("Script comleted for "+(System.currentTimeMillis()-timer)+" ms");
    }
}
