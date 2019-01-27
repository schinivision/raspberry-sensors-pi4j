package at.schinivision.sensors.sensor;

import at.schinivision.sensors.common.*;

import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 *  This Class implements the Ky-053 analog digital converter based on the ADS1115 chip.
 *  Subscriber to this sensor will be notified about every voltage change on any of the analogue input pins A0-A3
 *  The event will contain the PIN that has caused this event described by the @{@link Pin} class
 * </p>
 * <p>
 *  ATTENTION: If analog inputs on the sensor are not tied to any potential (V+ or GND) the analog Input will
 *  drift/float and therefore will cause a significant amount of update events.
 * </p>
 * @see <a href="http://sensorkit.joy-it.net/index.php?title=KY-053_Analog_Digital_Converter">KY-053 I2C Analog Digital Converter</a>
 */
public final class AdcKy053Sensor extends Sensor implements SensorSubscribeInterface, GpioPinListenerAnalog {

    // Singleton Implementation
    private static AdcKy053Sensor instance = null;

    // I2C Bus related variables
    private final I2CBus bus;
    private final int I2CBusNumer = I2CBus.BUS_1;

    // ADC Related Variables
    private final GpioController gpio;// create gpio controller
    private final GpioPinAnalogInput inputA0;
    private final GpioPinAnalogInput inputA1;
    private final GpioPinAnalogInput inputA2;
    private final GpioPinAnalogInput inputA3;
    private final ADS1115GpioProvider gpioProvider; // create custom ADS1115 GPIO provider
    private final ADS1x15GpioProvider.ProgrammableGainAmplifierValue pga = ADS1x15GpioProvider.ProgrammableGainAmplifierValue.PGA_6_144V;

    private final double measureValueChangeThreshold = 100;

    private final String AdcPinNameA0 = "A0";
    private final String AdcPinNameA1 = "A1";
    private final String AdcPinNameA2 = "A2";
    private final String AdcPinNameA3 = "A3";

    // Analog Value Change analogEvent
    private GpioPinAnalogValueChangeEvent analogEvent = null;

    // constructor and sensor initialization
    private AdcKy053Sensor() throws IOException, I2CFactory.UnsupportedBusNumberException {

        this.bus = I2CFactory.getInstance(I2CBusNumer);// Create I2C bus

        gpio = GpioFactory.getInstance();

        //TODO unsupported bus number exception
        gpioProvider = new ADS1115GpioProvider(bus, ADS1115GpioProvider.ADS1115_ADDRESS_0x48);

        inputA0 = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A0, AdcPinNameA0);
        inputA1 = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A1, AdcPinNameA1);
        inputA2 = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A2, AdcPinNameA2);
        inputA3 = gpio.provisionAnalogInputPin(gpioProvider, ADS1115Pin.INPUT_A3, AdcPinNameA3);

        gpioProvider.setProgrammableGainAmplifier(pga, ADS1115Pin.INPUT_A0);
        gpioProvider.setProgrammableGainAmplifier(pga, ADS1115Pin.INPUT_A1);
        gpioProvider.setProgrammableGainAmplifier(pga, ADS1115Pin.INPUT_A2);
        gpioProvider.setProgrammableGainAmplifier(pga, ADS1115Pin.INPUT_A3);

        gpioProvider.setEventThreshold(measureValueChangeThreshold, ADS1115Pin.INPUT_A0);
        gpioProvider.setEventThreshold(measureValueChangeThreshold, ADS1115Pin.INPUT_A1);
        gpioProvider.setEventThreshold(measureValueChangeThreshold, ADS1115Pin.INPUT_A2);
        gpioProvider.setEventThreshold(measureValueChangeThreshold, ADS1115Pin.INPUT_A3);

        inputA0.addListener(this);
        inputA1.addListener(this);
        inputA2.addListener(this);
        inputA3.addListener(this);

        Logger.getLogger(AdcKy053Sensor.class.getName()).log(Level.INFO, AdcKy053Sensor.class.getName() + " Starts sensing");
    }

    /**
     * Returns the ADC sensor instance
     * @return AdcKy053Sensor instance
     */
    public static AdcKy053Sensor getInstance() {
        if (instance == null) {
            try {
                AdcKy053Sensor.instance = new AdcKy053Sensor();
            } catch (IOException e) {
                Logger.getLogger(AdcKy053Sensor.class.getName()).log(Level.WARNING, "IO Exception", e);
            } catch (I2CFactory.UnsupportedBusNumberException e) {
                Logger.getLogger(AdcKy053Sensor.class.getName()).log(Level.WARNING, "I2C Exception", e);
            }
        }
        return instance;
    }

    @Override
    protected void notifyListener(SensorEventListener listener) {
        if (analogEvent != null) {
            // calculate values
            double value = analogEvent.getValue();
            double percent = ((value * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);
            double voltage = gpioProvider.getProgrammableGainAmplifier(analogEvent.getPin()).getVoltage() * (percent / 100);
            SensorEvent sensorEvent = new SensorEvent();
            sensorEvent.setValue(voltage);
            sensorEvent.setUnit(MeasurementUnits.VOLTAGE_IN_V.getUnit());
            switch (analogEvent.getPin().getName()) {
                case AdcPinNameA0:
                    listener.update(SensorType.ADC_KY_053,sensorEvent, Pin.PIN_0);
                    break;
                case AdcPinNameA1:
                    listener.update(SensorType.ADC_KY_053,sensorEvent, Pin.PIN_1);
                    break;
                case AdcPinNameA2:
                    listener.update(SensorType.ADC_KY_053,sensorEvent, Pin.PIN_2);
                    break;
                case AdcPinNameA3:
                    listener.update(SensorType.ADC_KY_053,sensorEvent, Pin.PIN_3);
                    break;
            }
            analogEvent = null;
        }
    }

    /**
     * Shuts down this sensor.
     */
    public void shutdown() {
        inputA0.removeAllListeners();
        inputA1.removeAllListeners();
        inputA2.removeAllListeners();
        inputA3.removeAllListeners();
        // TODO This might shut down all other sensor/actor classes depending on gpioProvider
        gpioProvider.shutdown();
    }

    @Override
    public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event) {
        analogEvent = event;
        notifyListeners();
    }
}