package at.schinivision.sensors.common;

public enum MeasurementUnits {

    TEMPERATURE_IN_CELSIUS("°C"),
    TEMPERATURE_IN_FARENHEIT("°F"),
    AIR_PRESSURE_IN_HECTO_PASCAL("hPa"),
    DISTANCE_IN_MM("mm"),
    DISTANCE_IN_CM("cm"),
    DISTANCE_IN_M("m"),
    VOLTAGE_IN_V("V");

    private String unit;

    MeasurementUnits(String measurementUnit) {
        this.unit = measurementUnit;
    }

    public String getUnit() {
        return unit;
    }
}
