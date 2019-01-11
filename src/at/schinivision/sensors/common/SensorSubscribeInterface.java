package at.schinivision.sensors.common;

public interface SensorSubscribeInterface {

    public abstract void addListener(SensorEventListener listener);

    public abstract void removeListener(SensorEventListener listener);
}
