package at.schinivision.sensors.common;

public enum Pin {

    PIN_0("PIN 0", 0),
    PIN_1("PIN 1", 1),
    PIN_2("PIN 2", 2),
    PIN_3("PIN 3", 3),
    PIN_4("PIN 4", 4),
    PIN_5("PIN 5", 5),
    PIN_6("PIN 6", 6),
    PIN_7("PIN 7", 7),
    PIN_8("PIN 8", 8),
    PIN_9("PIN 9", 9),
    PIN_10("PIN 10", 10),
    PIN_11("PIN 11", 11),
    PIN_12("PIN 12", 12),
    PIN_13("PIN 13", 13),
    PIN_14("PIN 14", 14),
    PIN_15("PIN 15", 15);

    public int pinNumber;
    public String name;

    Pin(String name, int id) {

        this.name = name;
        this.pinNumber = id;
    }

    public String getName() {
        return name;
    }

    public int getPinNumber() {
        return pinNumber;
    }
}
