package Model;

public class Vehicle {
    private int code;
    private int depot;
    private int cap;
    private int speed;
    public Vehicle(int code, int depot, int speed, int cap) {
        this.depot = depot;
        this.cap = cap;
        this.speed = speed;
        this.code = code;
    }
    public int getDepot() {
        return depot;
    }
    public int getCap() {
        return cap;
    }
    public int getSpeed() {return speed;}
    public int getCode() {return code;}
    public int getFixCost() {return 0;}
    public int getLoadFactor() {
        return 1;
    }
    @Override
    public String toString() {
        return "Model.Vehicle{" +
                "depot=" + depot +
                ", cap=" + cap +
                ", speed=" + speed +
                '}';
    }
}

