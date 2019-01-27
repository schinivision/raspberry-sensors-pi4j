package at.schinivision.sensors.common;

/**
 * Enumeration containing sensor types to distinguish the source of an sensor event
 */
public enum SensorType {

    ADC_KY_053("KY-053 Analog Digital Converter", 53),
    ULTRASONIC_KY_050("KY-050 Ultraschallabstandssensor", 50),
    TEMPERATURE_KY_013("KY-013 Temperatur-Sensor Modul", 13),
    TEMPERATURE_PRESSURE_KY_052("KY-052 Temperatur-Druck Sensor Modul", 52);


    private String name;
    private int id;

    SensorType(String name, int id) {

        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
