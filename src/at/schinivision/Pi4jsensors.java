package at.schinivision;

import at.schinivision.sensors.common.Pin;
import at.schinivision.sensors.common.SensorEvent;
import at.schinivision.sensors.common.SensorEventListener;
import at.schinivision.sensors.common.SensorType;
import at.schinivision.sensors.sensor.AdcKy053Sensor;
import at.schinivision.sensors.sensor.TempPressureKy052Sensor;
import at.schinivision.sensors.sensor.TemperatureKy013Sensor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Pi4jsensors {

    static boolean shutdown = false;

    @SuppressWarnings("Duplicates") // -> just so that idea does not complain about code duplication of the sample code
    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting down ...");
                shutdown = true;
            }
        });

        TempPressureKy052Sensor bmp280 = TempPressureKy052Sensor.getInstance();
        bmp280.addListener(new SensorEventListener() {
            @Override
            public void update(SensorType sensorType, SensorEvent event, Pin pin) {
                Logger.getLogger("Main").log(Level.INFO, "SensorType: " + sensorType.getName() + " Event: " + event.getValue() + " " + event.getUnit());
            }
        });

        TemperatureKy013Sensor tempSense = TemperatureKy013Sensor.getInstance();
        tempSense.addListener(new SensorEventListener() {
            @Override
            public void update(SensorType sensorType, SensorEvent event, Pin pin) {
                Logger.getLogger("Main").log(Level.INFO, "SensorType: " + sensorType.getName() + " Event: " + event.getValue() + " " + event.getUnit());
            }
        });

        AdcKy053Sensor adc = AdcKy053Sensor.getInstance();
        adc.addListener(new SensorEventListener() {
            @Override
            public void update(SensorType sensorType, SensorEvent event, Pin pin) {
                Logger.getLogger("Main").log(Level.INFO, "SensorType: " + sensorType.getName() + " " + pin.getName() +" Event: " + event.getValue() + " " + event.getUnit());
            }
        });

        while (shutdown) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Main interrupded");
            }
        }

        if (shutdown) {
            bmp280.shutdown();
            tempSense.shutdown();
//            adc.shutdown();
        }
    }
}
