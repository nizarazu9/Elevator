package fr.elevator.model;

import com.google.common.collect.Iterables;
import fr.elevator.common.Direction;
import fr.elevator.common.User;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: zizou
 */
public class ElevatorModelTest {

    @Test
    public void should_not_consider_full_cabin_as_eligible_for_new_users(){
        // Given
        int cabinCount = 1;
        int cabinSize = 2;
        int minFloor = 0;
        int maxFloor = 10;

        ElevatorModel elevatorModel = createElevator(cabinCount, cabinSize, minFloor, maxFloor);

        CabinModel fullCabin = Iterables.get(elevatorModel.getCabinsList(), 0);
        fullCabin.getUsersInCabin().add(createUser(1,Direction.UP));
        fullCabin.getUsersInCabin().add(createUser(1,Direction.UP));

        // When
        Iterable<CabinModel> cabins = elevatorModel.findEligibleCabins(createUser(3, Direction.DOWN));

        // Then
        assertThat(cabins).isEmpty();
    }

    @Test
    public void should_consider_empty_cabin_as_eligible_for_new_users(){
        // Given
        int cabinCount = 1;
        int cabinSize = 2;
        int minFloor = 0;
        int maxFloor = 10;

        ElevatorModel elevatorModel = createElevator(cabinCount, cabinSize, minFloor, maxFloor);

        // When
        Iterable<CabinModel> cabins = elevatorModel.findEligibleCabins(createUser(3, Direction.DOWN));

        // Then
        assertThat(cabins).hasSize(1);
    }

    @Test
    public void should_not_assign_cabin_to_new_user_when_user_direction_is_not_same_as_cabin_direction(){
        // Given
        int cabinCount = 2;
        int cabinSize = 10;
        int minFloor = 0;
        int maxFloor = 10;

        ElevatorModel model = createElevator(cabinCount, cabinSize, minFloor, maxFloor);

        CabinModel firstCabin = Iterables.get(model.getCabinsList(), 0);
        firstCabin.setCurrentFloor(5);
        firstCabin.setElevatorDirection(Direction.UP);
        firstCabin.getUsersInCabin().add(createUserWithFloorToGo(0, 10, Direction.UP));

        CabinModel secondCabin = Iterables.get(model.getCabinsList(), 1);
        secondCabin.setCurrentFloor(3);
        secondCabin.setElevatorDirection(Direction.UP);
        secondCabin.getUsersInCabin().add(createUserWithFloorToGo(0,10,Direction.UP));

        // When
        Iterable<CabinModel> cabins = model.findEligibleCabins(createUser(7,Direction.DOWN));

        // Then
        assertThat(cabins).isEmpty();
    }

    @Test
    public void should_find_nearest_cabin_when_more_than_one_cabin_is_eligible(){
        // Given
        int cabinCount = 2;
        int cabinSize = 10;
        int minFloor = 0;
        int maxFloor = 10;

        ElevatorModel elevatorModel = createElevator(cabinCount, cabinSize, minFloor, maxFloor);

        CabinModel firstCabin = Iterables.get(elevatorModel.getCabinsList(), 0);
        firstCabin.setCurrentFloor(5);
        firstCabin.setElevatorDirection(Direction.UP);

        CabinModel secondCabin = Iterables.get(elevatorModel.getCabinsList(), 1);
        secondCabin.setCurrentFloor(3);
        secondCabin.setElevatorDirection(Direction.UP);

        // When
        CabinModel nearestCabinForUser = elevatorModel.findNearestCabinForUser(createUser(7, Direction.UP), elevatorModel.getCabinsList());

        // Then
        assertThat(nearestCabinForUser).isEqualTo(firstCabin);
    }


    /*
    @Test
    public void testName() throws Exception {
        ElevatorModel elevatorModel = ElevatorModel.getInstance();
        elevatorModel.setCabinCount(2);
        elevatorModel.setHigherFloor(35);
        elevatorModel.setLowerFloor(-5);
        elevatorModel.setCabinSize(30);

        elevatorModel.reset();

        elevatorModel.callAtFloor(10, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(1, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(11, Direction.UP);

        elevatorModel.floorToGo(5,0);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(26, Direction.DOWN);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(25, Direction.DOWN);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(3, Direction.DOWN);
        elevatorModel.callAtFloor(-1, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(17, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(10, Direction.DOWN);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(21, Direction.UP);
        elevatorModel.userHasExited(0);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(19, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(15, Direction.UP);
        elevatorModel.callAtFloor(30, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(2, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(0, Direction.UP);
        elevatorModel.callAtFloor(4, Direction.DOWN);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(-5, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(13, Direction.UP);
        elevatorModel.callAtFloor(22, Direction.DOWN);
        elevatorModel.callAtFloor(25, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(-1, Direction.DOWN);
        elevatorModel.floorToGo(15,0);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(5, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(1, Direction.UP);
        elevatorModel.floorToGo(22,0);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(18, Direction.DOWN);
        elevatorModel.callAtFloor(0, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(1, Direction.UP);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(26, Direction.DOWN);
        elevatorModel.userHasExited(1);
        elevatorModel.floorToGo(17,1);
        assertThat(elevatorModel.getCabinsList().get(1).getCurrentFloor()).isEqualTo(15);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(19, Direction.UP);
        elevatorModel.callAtFloor(-1, Direction.DOWN);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(22, Direction.DOWN);
        elevatorModel.findNextCommand();

        elevatorModel.callAtFloor(-2, Direction.UP);
        elevatorModel.userHasExited(1);
        elevatorModel.floorToGo(34,1);
        assertThat(elevatorModel.getCabinsList().get(1).getCurrentFloor()).isEqualTo(17);
        elevatorModel.findNextCommand();
    }
    */

    private ElevatorModel createElevator(int cabinCount, int cabinSize, int minFloor, int maxFloor){
        ElevatorModel instance = ElevatorModel.getInstance();
        instance.setLowerFloor(minFloor);
        instance.setHigherFloor(maxFloor);
        instance.setCabinSize(cabinSize);
        instance.setCabinCount(cabinCount);

        instance.reset();

        return instance;
    }

    private User createUser(int comingFrom, Direction direction) {
        return new User(comingFrom,direction);
    }

    private User createUserWithFloorToGo(int comingFrom,int floorToGo, Direction direction) {
        User user = new User(comingFrom, direction);
        user.setFloorToGo(floorToGo);
        user.setStillCanScore(10D);
        return user;
    }
}
