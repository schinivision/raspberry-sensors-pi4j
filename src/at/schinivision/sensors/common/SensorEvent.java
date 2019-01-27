package at.schinivision.sensors.common;

/**
 * Base class of a sensor event. Contains the sensor value and the unit
 */
public class SensorEvent {
    private double value = 0.0;
    private String unit = "";

    /**
     * Return sensor value
     * @return Sensor value
     */
    public double getValue() {
        return value;
    }

    /**
     * Set sensor value
     * @param value sensor value
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Return sensor unit @{@link MeasurementUnits}
     * @return sensor unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Set Sensor unit @{@link MeasurementUnits}
     * @param unit sensor unit
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
