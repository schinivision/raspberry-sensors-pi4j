package at.schinivision.sensors.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * superclass for sensors
 */
public abstract class Sensor implements SensorSubscribeInterface {

    final private List<SensorEventListener> listeners = new ArrayList<>();//listeners

    abstract protected void notifyListener(SensorEventListener listener);

    abstract protected void shutdown();

    @Override
    public void addListener(SensorEventListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(SensorEventListener listener) {
        this.listeners.remove(listener);
    }

    protected void notifyListeners() {
        for (SensorEventListener listener : listeners) {
            try {
                notifyListener(listener);
            } catch (Exception ex) {
                Logger.getLogger(Sensor.class.getName()).log(Level.WARNING, "notifyListener", ex);
            }
        }
    }

}
