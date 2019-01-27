package at.schinivision.sensors.sensor;

import at.schinivision.sensors.common.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>
 * Implementation of the Temperature and Pressure sensor Ky052 from the Joy-It Sensor kit.
 * This implementation applies to all BMP280 Temperature and Pressure Sensors that are controlled by I²C
 * </p>
 * @see <a href="http://sensorkit.joy-it.net/index.php?title=KY-052_Drucksensor_/_Temperatursensor_-_BMP280_-">KY-052 Drucksensor</a>
 */
public final class TempPressureKy052Sensor extends Sensor implements SensorSubscribeInterface, Runnable {

    // I2C Bus related variables
    private final I2CBus bus;
    private final I2CDevice device;
    private final int I2CBusNumer = I2CBus.BUS_1;
    private final int SensorAddressSDO_0 = 0x76;
    private final int SensorAddressSDO_1 = 0x77;

    // factory calibration data
    private int dig_T1, dig_T2, dig_T3, dig_P1, dig_P2, dig_P3,
            dig_P4, dig_P5, dig_P6, dig_P7, dig_P8, dig_P9;


    // temperature and pressure readings
    double temp = 0.0;
    double pressure = 0.0;

    // analogEvent Messages that are propagated
    SensorEvent t_event = null;
    SensorEvent p_event = null;

    // Threadcontrol
    boolean shutdown = false;
    Thread senseThread = null;
    long TimerSleep = 1000; // determines how often the sensor will be read for new values -> check with mode of setup if it makes sense to reduce this value

    // constructor and sensor initialization
    private TempPressureKy052Sensor() throws IOException, I2CFactory.UnsupportedBusNumberException {
        // connect to I2C bus on board ->
        this.bus = I2CFactory.getInstance(I2CBusNumer);// Create I2C bus
        device = this.bus.getDevice(SensorAddressSDO_1);// Get I2C device, BMP280 I2C address is 0x76(108)
        // read calibration data from sensor
        readFactoryCalibrationDataFromSensor();
        // configure Sensor
        configureBMP280();
        // start sensing
        startSensing();
    }

    // Singleton Implementation
    private static TempPressureKy052Sensor instance = null;

    /**
     * Instantiate Temp Pressure sensor
     *
     * @return singleton instance
     * @implNote Must be connected to I2C bus 1 on the Board!
     * @implNote Check if I2C bus is enabled by config -> raspi-config -> enable I2C!
     */
    public static TempPressureKy052Sensor getInstance() {
        if (instance == null) {
            try {
                TempPressureKy052Sensor.instance = new TempPressureKy052Sensor();
            } catch (IOException e) {
                Logger.getLogger(TempPressureKy052Sensor.class.getName()).log(Level.WARNING, "IO Exception", e);
            } catch (I2CFactory.UnsupportedBusNumberException e) {
                Logger.getLogger(TempPressureKy052Sensor.class.getName()).log(Level.WARNING, "I2C Exception", e);
            }
        }
        return instance;
    }

    private void readFactoryCalibrationDataFromSensor() throws IOException {
        // i2c read byte array
        byte[] b1 = new byte[24];
        // read calibration data form address 0x88
        // TODO if during instantiation this read command will throw an io exception caused by i2c bus
        // the instantiation will fail -> maybe retry
        device.read(0x88, b1, 0, 24);
        // Why are all these bytes "masked" -> to get the representation right!
        // See -> https://stackoverflow.com/questions/4266756/can-we-make-unsigned-byte-in-java/4266841
        // Convert the data
        // calculate temperature correction data (reverse lsb / msb register read)
        dig_T1 = (b1[0] & 0xFF) + ((b1[1] & 0xFF) * 256);
        dig_T2 = (b1[2] & 0xFF) + ((b1[3] & 0xFF) * 256);
        if (dig_T2 > 32767) {
            dig_T2 -= 65536;
        }
        dig_T3 = (b1[4] & 0xFF) + ((b1[5] & 0xFF) * 256);
        if (dig_T3 > 32767) {
            dig_T3 -= 65536;
        }
        // pressure trimming values (reverse lsb / msb register read)
        dig_P1 = (b1[6] & 0xFF) + ((b1[7] & 0xFF) * 256);
        dig_P2 = (b1[8] & 0xFF) + ((b1[9] & 0xFF) * 256);
        if (dig_P2 > 32767) {
            dig_P2 -= 65536;
        }
        dig_P3 = (b1[10] & 0xFF) + ((b1[11] & 0xFF) * 256);
        if (dig_P3 > 32767) {
            dig_P3 -= 65536;
        }
        dig_P4 = (b1[12] & 0xFF) + ((b1[13] & 0xFF) * 256);
        if (dig_P4 > 32767) {
            dig_P4 -= 65536;
        }
        dig_P5 = (b1[14] & 0xFF) + ((b1[15] & 0xFF) * 256);
        if (dig_P5 > 32767) {
            dig_P5 -= 65536;
        }
        dig_P6 = (b1[16] & 0xFF) + ((b1[17] & 0xFF) * 256);
        if (dig_P6 > 32767) {
            dig_P6 -= 65536;
        }
        dig_P7 = (b1[18] & 0xFF) + ((b1[19] & 0xFF) * 256);
        if (dig_P7 > 32767) {
            dig_P7 -= 65536;
        }
        dig_P8 = (b1[20] & 0xFF) + ((b1[21] & 0xFF) * 256);
        if (dig_P8 > 32767) {
            dig_P8 -= 65536;
        }
        dig_P9 = (b1[22] & 0xFF) + ((b1[23] & 0xFF) * 256);
        if (dig_P9 > 32767) {
            dig_P9 -= 65536;
        }
    }

    private void configureBMP280() throws IOException {
        // NOTE WRITE TO CONFIG REGISTER WHILE IN SLEEP MODE -> WRITES TO CONFIG REGISTER IN NORMAL MODE MIGHT BE IGNORED!!!
        // writing to register 0xF5 "config" -> This register sets the rate, filter and interface options of the device
        // See BMP280 DataSheet Chapter 4.3.5
        // 0xA0 -> 101|000|0|0 -> t_standby | filter | not used | spi3w_en -> 1000ms | filter off | x | spi 3 wire off
        device.write(0xF5, (byte) 0xA0);
        // NOTE FIRST SET CONFIG REGISTER BEFORE CHANGING INTO NORMAL MODE!!!
        // writing to register 0xF4 "ctrl_meas" -> This register sets the data acquisition options of the device
        // See BMP280 DataSheet Chapter 4.3.4
        // 0x27 -> 001|001|11 -> osrs_t|osrs_p|mode -> oversampling x1 | oversampling x1 | normal mode
        device.write(0xF4, (byte) 0x27);
    }

    private void readMeasurements() throws IOException {
        // Check Status Register 0xF3 if sensor is currently measuring -> Delay read
        int status = device.read(0xF3);
        // positive value indicates successful read from bus
        if (status >= 0) {
            // check if sensor is currently measuring
            if ((status & 0x08) > 0) {
                // if so wait arbitrary time to let the sensor update the register
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // If sleep would be interrupted, who cares.. so the read temp value might not be as accurate -> next second it will be
                }
            }
        } else {
            Logger.getLogger(TempPressureKy052Sensor.class.getName()).log(Level.WARNING, "Could not read Sensor status flag");
        }
        // Read 6 bytes of data from address 0xF7(247)
        // pressure msb1, pressure msb, pressure lsb, temp msb1, temp msb, temp lsb, humidity lsb, humidity msb
        byte[] data = new byte[6];
        try {
            device.read(0xF7, data, 0, 6);

            // Convert pressure and temperature data to 19-bits
            long adc_p = (((long) (data[0] & 0xFF) * 65536) + ((long) (data[1] & 0xFF) * 256) + (long) (data[2] & 0xF0)) / 16;
            long adc_t = (((long) (data[3] & 0xFF) * 65536) + ((long) (data[4] & 0xFF) * 256) + (long) (data[5] & 0xF0)) / 16;

            // Temperature offset calculations
            double var1_temp = (((double) adc_t) / 16384.0 - ((double) dig_T1) / 1024.0) * ((double) dig_T2);
            double var2_temp = ((((double) adc_t) / 131072.0 - ((double) dig_T1) / 8192.0)
                    * (((double) adc_t) / 131072.0 - ((double) dig_T1) / 8192.0)) * ((double) dig_T3);
            double t_fine = (long) (var1_temp + var2_temp);
            temp = (var1_temp + var2_temp) / 5120.0;

            // Pressure offset calculations
            double var1 = ((double) t_fine / 2.0) - 64000.0;
            double var2 = var1 * var1 * ((double) dig_P6) / 32768.0;
            var2 = var2 + var1 * ((double) dig_P5) * 2.0;
            var2 = (var2 / 4.0) + (((double) dig_P4) * 65536.0);
            var1 = (((double) dig_P3) * var1 * var1 / 524288.0 + ((double) dig_P2) * var1) / 524288.0;
            var1 = (1.0 + var1 / 32768.0) * ((double) dig_P1);
            double p = 1048576.0 - (double) adc_p;
            p = (p - (var2 / 4096.0)) * 6250.0 / var1;
            var1 = ((double) dig_P9) * p * p / 2147483648.0;
            var2 = p * ((double) dig_P8) / 32768.0;
            pressure = (p + (var1 + var2 + ((double) dig_P7)) / 16.0) / 100;
        } catch (IOException ex)
        {
         // Suppress any IO exception errors that can occur during read on i2c bus
        }
    }

    @Override
    protected void notifyListener(SensorEventListener listener) {
        // propagate temperature analogEvent
        if (t_event != null) {
            listener.update(SensorType.TEMPERATURE_PRESSURE_KY_052, t_event, null);
        }
        // propagate pressure analogEvent
        if (p_event != null) {
            listener.update(SensorType.TEMPERATURE_PRESSURE_KY_052, p_event, null);
        }
    }

    @Override
    public void run() {
        readSensorData(TimerSleep);
    }

    private void readSensorData(long sleep) {
        while (!shutdown) {
            try {
                // read sensor
                readMeasurements();
                // generate events
                t_event = new SensorEvent();
                t_event.setUnit(MeasurementUnits.TEMPERATURE_IN_CELSIUS.getUnit());
                t_event.setValue(temp);
                p_event = new SensorEvent();
                p_event.setUnit(MeasurementUnits.AIR_PRESSURE_IN_HECTO_PASCAL.getUnit());
                p_event.setValue(pressure);
                // notify listeners
                notifyListeners();
                // Sleep
                Thread.sleep(sleep);
            } catch (Exception ex) {
                Logger.getLogger(TempPressureKy052Sensor.class.getName()).log(Level.WARNING, "Sensor", ex);
            }
        }
    }

    private void startSensing() {
        Logger.getLogger(TempPressureKy052Sensor.class.getName()).log(Level.INFO, TempPressureKy052Sensor.class.getName() + " Starts sensing");
        if (senseThread == null) {
            senseThread = new Thread(this, this.getClass().getName());
            senseThread.start();
        }
    }

    public void shutdown() {
        // TODO this != null ??
        if (senseThread != null) {
            // set shutdown variable for thread to exit
            shutdown = true;
            // wake it up so it can terminate
            senseThread.interrupt();
        }
    }
}