package at.schinivision.sensors.common;

/**
 * Superclass to add and remove sensor event listeners
 */
public interface SensorSubscribeInterface {

    public abstract void addListener(SensorEventListener listener);

    public abstract void removeListener(SensorEventListener listener);
}
