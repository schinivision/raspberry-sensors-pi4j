package at.schinivision.sensors.actors;

import at.schinivision.sensors.common.Actor;
import com.pi4j.io.gpio.*;

/**
 * <p> This class implements a RGB LED actor suitable to use with the KY009 or KY016 module.</p>
 * @see <a href="http://sensorkit.joy-it.net/index.php?title=KY-009_RGB_LED_SMD_Modul">KY-009 RGB SMD Module</a>
 * @see <a href="http://sensorkit.joy-it.net/index.php?title=KY-016_RGB_5mm_LED_Modul">KY-016 RGB 5mm LED Module</a>
 */
public class RgbLed_KY009_KY016 extends Actor {

    Pin PinNumberLedRed = null;
    Pin PinNumberLedGreen = null;
    Pin PinNumberLedBlue = null;

    GpioPinPwmOutput pwmRed = null;
    GpioPinPwmOutput pwmGreen = null;
    GpioPinPwmOutput pwmBlue = null;

    GpioController gpio = GpioFactory.getInstance();

    // pwm max / max color value;
    float pwmFactor = 100.0f / 255.0f;

    /**
     * <p>Instantiate RGB module</p>
     * <p> ATTENTION: KY-009 module red and green channel switched!</p>
     * <p> ATTENTION: GPIO 1, 23, 24 and 26 do not work with this implementation (softpwm)!</p>
     * @param redLedPin Red Led Pin
     * @param greenLedPin Green Led Pin
     * @param blueLedPin Blue Led Pin
     */
    public RgbLed_KY009_KY016(Pin redLedPin, Pin greenLedPin, Pin blueLedPin){

        this.PinNumberLedRed = redLedPin;
        this.PinNumberLedGreen = greenLedPin;
        this.PinNumberLedBlue = blueLedPin;
        init();
    }

    @Override
    protected void init() {
        // Set outputs to pwm
        pwmRed = gpio.provisionSoftPwmOutputPin(PinNumberLedRed);
        pwmGreen = gpio.provisionSoftPwmOutputPin(PinNumberLedGreen);
        pwmBlue = gpio.provisionSoftPwmOutputPin(PinNumberLedBlue);

        // set Pins to input again to avoid pin damage or short circuits
        // set shutdown option if application terminates
        pwmRed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        pwmGreen.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        pwmBlue.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);

        pwmRed.setPwmRange(100);
        pwmGreen.setPwmRange(100);
        pwmBlue.setPwmRange(100);

    }

    /**
     * Set the colour of the led using 16bit color value
     * @param red 8bit red color value 0x00 - 0xFF
     * @param green 8bit green color value 0x00 - 0xFF
     * @param blue 8bit blue color value 0x00 - 0xFF
     */
    public void setColor(byte red, byte green, byte blue){

        int redRaw = red & 0xFF;
        int greenRaw = green & 0xFF;
        int blueRaw = blue & 0xFF;

        int redValue = Math.abs(Math.round(((float)redRaw * pwmFactor)));
        int greenValue = Math.abs(Math.round(((long)greenRaw * pwmFactor)));
        int blueValue = Math.abs(Math.round(((long)blueRaw * pwmFactor)));

        pwmRed.setPwm(redValue);
        pwmGreen.setPwm(greenValue);
        pwmBlue.setPwm(blueValue);

    }

    @Override
    public void shutdown() {
        // Seems there is nothing to do! Shutdown options are set on init;
    }
}
