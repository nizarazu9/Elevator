package fr.elevator.ws;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * User: zizou
 */
@ApplicationPath("/rest")
public class MyApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();

        // register root resource
        classes.add(ElevatorRestService.class);
        classes.add(WebAppRestService.class);

        return classes;
    }
}
