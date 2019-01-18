package at.schinivision.sensors.sensor;


import at.schinivision.sensors.common.*;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TemperatureKy013Sensor extends Sensor implements SensorEventListener, Runnable {

    // Singleton Implementation
    private static TemperatureKy013Sensor instance = null;

    private AdcKy053Sensor adc = AdcKy053Sensor.getInstance();

    private double lastVoltageReported = 0.0;

    private double lastTempMeasured = 0.0;

    private int subscriberUpdateRateInMs = 1000;

    // Threadcontrol
    Thread senseThread = null;
    private boolean isShutdown = false;

    private TemperatureKy013Sensor() {
        adc.addListener(this);
        startSensing();
        Logger.getLogger(TemperatureKy013Sensor.class.getName()).log(Level.INFO, TemperatureKy013Sensor.class.getName() + " Starts sensing");
    }

    public static TemperatureKy013Sensor getInstance() {
        if (instance == null) {
            TemperatureKy013Sensor.instance = new TemperatureKy013Sensor();
        }
        return instance;
    }

    private double calcTemp(double voltage) {

        double t = Math.log((10000 / voltage) * (3.3 - voltage));
        t = 1 / (0.001129148 + (0.000234125 + (0.0000000876741 * t * t)) * t);
        t = t - 273.15;

        return (t);
    }

    public void startSensing() {
        if (senseThread == null) {
            senseThread = new Thread(this, this.getClass().getName());
            senseThread.start();
        }
    }


    @Override
    public void update(SensorType sensorType, SensorEvent event, Pin pin) {
        // we know that the temp resistor is measured on adc pin 0 -> therefore ignore all other pins
        if (pin.pinNumber == 0){
            // only report voltage to not waste a lot of time of the adc thread
            lastVoltageReported = event.getValue();
        }
    }

    @Override
    protected void notifyListener(SensorEventListener listener) {
        SensorEvent sensorEvent = new SensorEvent();
        sensorEvent.setUnit(MeasurementUnits.TEMPERATURE_IN_CELSIUS.getUnit());
        sensorEvent.setValue(lastTempMeasured);
        listener.update(SensorType.TEMPERATURE_KY_013, sensorEvent, null);
    }

    @Override
    public void shutdown() {
        adc.shutdown();
        isShutdown = true;
        // interrupt the sense thread during sleep.. so the shutdown thread can exit immediately
        senseThread.interrupt();
    }

    @Override
    public void run() {
        readSensorData(subscriberUpdateRateInMs);
    }

    private void readSensorData(long sleep) {
        while (!isShutdown) {
            try {
                lastTempMeasured = calcTemp(lastVoltageReported);
                // notify listeners
                notifyListeners();
                // Sleep
                Thread.sleep(sleep);
            } catch (Exception ex) {
                // so the thread was interrupted... Might be because shutdown was called... Should not be that problem
//                Logger.getLogger(TempPressureKy052Sensor.class.getName()).log(Level.WARNING, "Sensor", ex);
            }
        }
    }
}