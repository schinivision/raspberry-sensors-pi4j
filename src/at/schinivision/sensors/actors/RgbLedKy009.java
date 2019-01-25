package at.schinivision.sensors.actors;

import at.schinivision.sensors.common.Actor;
import com.pi4j.io.gpio.*;
import com.pi4j.util.CommandArgumentParser;

public class RgbLedKy009 extends Actor {
    // ATTENTION GPIO 1, 23, 24 and 26 do not work with softpwm!


    Pin PinNumberLedRed = RaspiPin.GPIO_04;
    Pin PinNumberLedGreen = RaspiPin.GPIO_02;
    Pin PinNumberLedBlue = RaspiPin.GPIO_03;

    GpioPinPwmOutput pwmRed = null;
    GpioPinPwmOutput pwmGreen = null;
    GpioPinPwmOutput pwmBlue = null;

    GpioController gpio = GpioFactory.getInstance();

    // pwm max / max color value;
    float pwmFactor = 100.0f / 255.0f;

    public RgbLedKy009(){
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
