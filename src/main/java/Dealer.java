import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Dealer {
    private ArrayList<Car> carList = new ArrayList<>();

    private String name;
    private int count;
    private long totalPrice;

    private int hashCode;

    public Dealer() {
        this.count = 0;
    }

    public int getCount() {return count;}

    public void setName(String name) {
        this.name = name;
    }
    public String getName () { return  this.name; }

    public void add(Car newCar) {
        this.carList.add(newCar);
        this.count++;
        this.totalPrice = totalPrice + newCar.getPrice();
        this.hashCode = this.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder dealerString = new StringBuilder();
        if (this.count > 0) {
            dealerString.append(name).append("(").append(count).append(")").append(" - ").append(this.totalPrice).append(" ₽").append("\n");
            carList.forEach(car -> {
                dealerString.append(car);
            });
        }
        return dealerString.toString();
    }

    public int hashCode() {
        final int[] code = {Math.toIntExact(name.charAt(1) + this.count + this.totalPrice)};
        this.carList.forEach(carUnit -> {
            code[0] += carUnit.hashCode();
        });
        return code[0];
    }

    public JSONObject toJson() {
        JSONObject dealerJson= new JSONObject();
        ArrayList<Integer> carsCode = new ArrayList<>();
        this.carList.forEach(carUnit ->{
            carsCode.add(carUnit.hashCode());
        });
        dealerJson.put("carsCode", carsCode);
        dealerJson.put("code", this.hashCode());
        dealerJson.put("name", this.name);
        return dealerJson;
    }
}