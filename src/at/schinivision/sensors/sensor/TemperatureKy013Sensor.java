//package at.schinivision.sensors.sensor;
//
//import at.schinivision.sensors.common.Sensor;
//import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
//import com.pi4j.gpio.extension.ads.ADS1115Pin;
//import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider;
//import com.pi4j.io.gpio.GpioController;
//import com.pi4j.io.gpio.GpioFactory;
//import com.pi4j.io.gpio.GpioPinAnalogInput;
//import com.pi4j.io.gpio.analogEvent.GpioPinAnalogValueChangeEvent;
//import com.pi4j.io.gpio.analogEvent.GpioPinListenerAnalog;
//import com.pi4j.io.i2c.I2CFactory;
//import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import at.schinivision.sensors.common.*;
//
//
///**
// * *
// *
// * @TemperatureKy013Sensor wird die Temperatur in °C berechnet.
// */
//public class TemperatureKy013Sensor extends Sensor implements SensorSubscribeInterface, Runnable, GpioPinListenerAnalog {
//
//    public final GpioController gpio;// create gpio controller
//    private final ADS1115GpioProvider gpioProvider; // create custom ADS1115 GPIO provider
//    private final GpioPinAnalogInput myInput;
//
//    private final int id = 1;
//    private Thread senseThread = null;
//    private final long TimerSleep = 1000;
//    protected boolean shutdown = false;// Shutdown hook
//    protected double temp;
//    protected String name;
//
//    public TemperatureKy013Sensor(String name, int bus) throws I2CFactory.UnsupportedBusNumberException, IOException, Exception {
//        // initialisierung
//        this.name = name;
//
//        gpio = GpioFactory.getInstance();
//
//        gpioProvider = new ADS1115GpioProvider(bus, ADS1115GpioProvider.ADS1115_ADDRESS_0x48);//TODO unsupported bus number exception
//
//        myInput = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A0, "AnalogInput-A0");
//
//        gpioProvider.setProgrammableGainAmplifier(ADS1x15GpioProvider.ProgrammableGainAmplifierValue.PGA_4_096V, ADS1115Pin.INPUT_A0);
//        gpioProvider.setEventThreshold(100, ADS1115Pin.INPUT_A0);
//
//        myInput.addListener(this);
//    }
//
//    @Override
//    public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent analogEvent) {
//        double value = analogEvent.getValue();
//        double percent = ((value * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);
//        double voltage = gpioProvider.getProgrammableGainAmplifier(analogEvent.getPin()).getVoltage() * (percent / 100);
//
//        temp = getTemperature(voltage);
//    }
//
//    @Override
//    public void run() {
//        readSensorData(TimerSleep);
//    }
//
//    private void readSensorData(long sleep) {
//        // TODO implement
//        while (!shutdown) {
//            try {
//                Thread.sleep(sleep);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(TemperatureKy013Sensor.class.getName()).log(Level.WARNING, "Sleep", ex);
//            }
//            notifyActors();
//        }
//        myInput.removeAllListeners();
//    }
//
//    public double getTemperature(double voltage) {
//        voltage *= 1000;
//
//        double t = Math.log((10000 / voltage) * (3300 - voltage));
//        t = 1 / (0.001129148 + (0.000234125 + (0.0000000876741 * t * t)) * t);
//        t = t - 273.15;
//
//        return (t);
//    }
//
//    @Override
//    protected void notifyActor(Listener listener) throws IOException {
//        listener.update(getName(), temp, celf);
//    }
//
//    public void startSensing() {
//        System.out.println(name + " starts sensing ...");
//        if (senseThread == null) {
//            senseThread = new Thread(this, name);
//            senseThread.start();
//        }
//    }
//
//    public void shutdown() {
//        if (this != null) {
//            shutdown = true;
//        }
//    }
//
//    public double getTempKy013() {
//        return (temp);
//    }
//
//    public String getName() {
//        return (name);
//    }
//
//}
