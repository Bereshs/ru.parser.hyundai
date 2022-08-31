public class Car {
    private final String  model;
    private final String color;
    private final String config;
    private final String price;
    public Car(String model, String color, String config, String price) {
        this.model = model;
        this.color = color;
        this.config = config;
        this.price = price;
    }
    @Override
    public String toString() {
        StringBuilder car = new StringBuilder();
        car.append(this.model).append(" - ").append(this.color).append(" - ").append(this.config).append(" - ").append(price).append("\n");
        return car.toString();
    }

    public long getPrice () {
        return Long.parseLong(this.price.replaceAll("(,)","").replaceAll("\\s*(₽)\\s*","").toLowerCase().replaceAll("по запросу", "0"));
    }

        @Override
    public int hashCode() {
        return Math.toIntExact(model.charAt(1) + color.charAt(1) + config.charAt(1) + this.getPrice());
    }
}
