package at.schinivision.sensors.common;

public class SensorEvent {
    private double value = 0.0;
    private String unit = "";

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
