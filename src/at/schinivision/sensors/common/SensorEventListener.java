package at.schinivision.sensors.common;

/**
 * Listener that has to be implemented to receive sensor value changes
 */
public interface SensorEventListener {

    public abstract void update(SensorType sensorType, SensorEvent event, Pin pin);

}
