package fr.elevator.ws;

import fr.elevator.common.CabinCommand;
import fr.elevator.common.Direction;
import fr.elevator.model.ElevatorModel;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * User: zizou
 */
@Path("/elevator")
public class ElevatorRestService {

    private static Logger logger = Logger.getLogger(ElevatorRestService.class);

    @GET
    @Path("/call")
    public Response userCall(@QueryParam("atFloor") int floor,
                             @QueryParam("to") String direction) {
        logger.info("userCall : floor=" + floor + ",direction= " + direction);
        ElevatorModel.getInstance().callAtFloor(floor, Direction.valueOf(direction));
        return Response.ok().build();
    }

    @GET
    @Path("/go")
    public Response userGo(@QueryParam("floorToGo") int floor,
                           @QueryParam("cabin")     int cabin)
    {
        logger.info("userGo floor=" + floor+ " on cabin "+cabin);
        ElevatorModel.getInstance().floorToGo(floor,cabin);
        return Response.ok().build();
    }

    @GET
    @Path("/userHasEntered")
    public Response userHasEntered(@QueryParam("cabin")int cabin) {
        logger.info("userHasEntered on cabin " + cabin);
        return Response.ok().build();
    }

    @GET
    @Path("/userHasExited")
    public Response userHasExited(@QueryParam("cabin") int cabin) {
        logger.info("userHasExited on cabin "+cabin);
        ElevatorModel.getInstance().userHasExited(cabin);
        return Response.ok().build();
    }

    @GET
    @Path("/reset")
    public Response reset(@QueryParam("cause") String informationMessage,
                          @QueryParam("lowerFloor") Integer lowerFloor,
                          @QueryParam("higherFloor") Integer higherFloor,
                          @QueryParam("cabinSize") Integer cabinSize,
                          @QueryParam("cabinCount") Integer cabinCount) {


        logger.info(String.format("reset cause = %s, lower =%s, higher=%s, cabinSize=%s, cabinCount=%s",
                informationMessage,lowerFloor,higherFloor,cabinSize, cabinCount));

        if ( lowerFloor != null )
            ElevatorModel.getInstance().setLowerFloor(lowerFloor);
        if ( higherFloor != null)
            ElevatorModel.getInstance().setHigherFloor(higherFloor);
        if ( cabinSize != null)
            ElevatorModel.getInstance().setCabinSize(cabinSize);
        if ( cabinCount != null)
            ElevatorModel.getInstance().updateCabinCount(cabinCount);

        ElevatorModel.getInstance().reset();

        return Response.ok().build();
    }

    @GET
    @Path("/nextCommands")
    public String nextCommand() {
        List<CabinCommand> nextCommand1 = ElevatorModel.getInstance().findNextCommand();
        StringBuilder result = new StringBuilder();
        for (CabinCommand CabinCommand : nextCommand1) {
           result = result.append(CabinCommand.toString()+ "\n");
        }
        logger.info("nextCommand : " + result);
        return result.toString();
    }

}