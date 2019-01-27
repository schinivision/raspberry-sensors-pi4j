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

    /**
     * used to store subscribers to a sensor
     */
    final private List<SensorEventListener> listeners = new ArrayList<>();//listeners

    /**
     * used to update a certain listener
     * @param listener subscriber to a sensor
     */
    abstract protected void notifyListener(SensorEventListener listener);

    /**
     * Used to shutdown a sensor
     */
    abstract protected void shutdown();

    /**
     * Add a subscriber to a sensor
     * @param listener subscriber to be added
     */
    @Override
    public void addListener(SensorEventListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Remove a subsriber to a sensor
     * @param listener subscriber to be removed
     */
    @Override
    public void removeListener(SensorEventListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Listener update routine
     */
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
