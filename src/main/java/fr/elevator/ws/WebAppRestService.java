package fr.elevator.ws;

import fr.elevator.model.ElevatorModel;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * User: zizou
 */
@Path("/webapp")
public class WebAppRestService {

    @GET
    @Path("/model")
    @Produces(MediaType.APPLICATION_JSON)
    public ElevatorModel model() {
        return ElevatorModel.getInstance();
    }

}
