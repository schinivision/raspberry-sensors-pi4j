package at.schinivision.sensors.common;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * superclass for actors
 */
public abstract class Actor {

    abstract protected void init();

    abstract public void shutdown();

}
