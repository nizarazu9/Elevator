package fr.elevator.model;

import fr.elevator.common.CabinCommand;
import fr.elevator.common.Direction;
import fr.elevator.common.User;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: zizou
 */
public class CabinModelTest {

    @Test
    public void should_close_door_when_opened() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(1);
        cabinModel.setDoorOpened(true);

        // When
        CabinCommand nextCommand = cabinModel.findNextCommand();

        // Then
        assertThat(nextCommand).isEqualTo(CabinCommand.CLOSE);
        assertThat(cabinModel.isDoorOpened()).isEqualTo(false);
    }

    @Test
    public void should_do_nothing_when_no_user() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(1);
        cabinModel.setDoorOpened(false);

        // When
        CabinCommand nextCommand = cabinModel.findNextCommand();

        // Then
        assertThat(nextCommand).isEqualTo(CabinCommand.NOTHING);
        assertThat(cabinModel.isDoorOpened()).isEqualTo(false);
        assertThat(cabinModel.getCurrentFloor()).isEqualTo(1);
    }

    @Test
    public void should_free_the_cabin_when_full_without_taking_into_account_user_score() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 1);
        cabinModel.setCurrentFloor(1);
        cabinModel.setDoorOpened(false);

        User user = new User(0, Direction.UP);
        user.setFloorToGo(1);
        user.setStillCanScore(0D);

        cabinModel.getUsersInCabin().add(user);

        // When
        CabinCommand nextCommand = cabinModel.findNextCommand();

        // Then
        assertThat(nextCommand).isEqualTo(CabinCommand.OPEN_UP);
        assertThat(cabinModel.isDoorOpened()).isEqualTo(true);
    }

    @Test
    public void should_open_door_for_user_waiting_with_score() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(1);

        User user = new User(1, Direction.UP);
        user.setStillCanScore(10D);

        cabinModel.getUsersWaitingCabin().add(user);

        // When
        CabinCommand nextCommand = cabinModel.findNextCommand();

        // Then
        assertThat(nextCommand).isEqualTo(CabinCommand.OPEN_UP);
    }

    @Test
    public void should_not_open_door_when_user_waiting_without_score() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(1);

        User user = new User(1, Direction.UP);
        user.setWaitTime(50);

        User user2 = new User(0, Direction.UP);
        user2.setWaitTime(0);

        cabinModel.getUsersWaitingCabin().add(user);
        cabinModel.getUsersWaitingCabin().add(user2);

        // When
        CabinCommand nextCommand = cabinModel.findNextCommand();

        // Then
        assertThat(nextCommand).isEqualTo(CabinCommand.DOWN);
    }

    @Test
    public void should_not_open_door_when_user_waiting_but_not_in_same_direction() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(1);
        cabinModel.setElevatorDirection(Direction.UP);

        User user = new User(1, Direction.DOWN);
        user.setWaitTime(50);

        cabinModel.getUsersWaitingCabin().add(user);
        User user2 = new User(2, Direction.UP);
        user2.setWaitTime(0);

        cabinModel.getUsersWaitingCabin().add(user);
        cabinModel.getUsersWaitingCabin().add(user2);


        // When
        CabinCommand nextCommand = cabinModel.findNextCommand();

        // Then
        assertThat(nextCommand).isEqualTo(CabinCommand.UP);
    }

    @Test
    public void should_have_good_direction(){
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(1);
        cabinModel.setElevatorDirection(Direction.UP);

        User user = new User(1, Direction.DOWN);
        user.setWaitTime(2);

        cabinModel.getUsersWaitingCabin().add(user);

        // When
        CabinCommand nextCommand = cabinModel.findNextCommand();

        // Then
        assertThat(nextCommand).isEqualTo(CabinCommand.OPEN_DOWN);
    }

    @Test
    public void should_consider_user_as_eligible_when_cabin_is_up_and_user_going_up() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(1);
        cabinModel.setElevatorDirection(Direction.UP);

        User user = new User(3, Direction.UP);
        user.setWaitTime(2);

        // When
        boolean userEligibleForCabin = cabinModel.isUserEligibleForCabin(user);

        // Then
        assertThat(userEligibleForCabin).isEqualTo(true);
    }

    @Test
    public void should_not_consider_user_as_eligible_when_cabin_is_up_and_user_going_down() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(1);
        cabinModel.setElevatorDirection(Direction.UP);

        User user = new User(3, Direction.DOWN);
        user.setWaitTime(2);

        // When
        boolean userEligibleForCabin = cabinModel.isUserEligibleForCabin(user);

        // Then
        assertThat(userEligibleForCabin).isEqualTo(false);
    }

    @Test
    public void should_not_consider_user_as_eligible_when_cabin_is_down_and_user_going_down_but_higher_than_cabin() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(2);
        cabinModel.setElevatorDirection(Direction.DOWN);

        User user = new User(5, Direction.DOWN);
        user.setWaitTime(2);

        // When
        boolean userEligibleForCabin = cabinModel.isUserEligibleForCabin(user);

        // Then
        assertThat(userEligibleForCabin).isEqualTo(false);
    }

    @Test
    public void should_not_find_user_going_up() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(2);
        cabinModel.setElevatorDirection(Direction.DOWN);

        User user = new User(2, Direction.UP);
        user.setWaitTime(2);

        cabinModel.getUsersWaitingCabin().add(user);

        // When
        User userGoingDown = cabinModel.findUserCallingFromWithDirection(Direction.DOWN);

        // Then
        assertThat(userGoingDown).isNull();
    }

    @Test
    public void should_find_user_going_down() throws Exception {
        // GIVEN
        CabinModel cabinModel = new CabinModel(0, 5, 5);
        cabinModel.setCurrentFloor(2);
        cabinModel.setElevatorDirection(Direction.DOWN);

        User user = new User(2, Direction.DOWN);
        user.setWaitTime(2);

        cabinModel.getUsersWaitingCabin().add(user);

        // When
        User userGoingDown = cabinModel.findUserCallingFromWithDirection(Direction.DOWN);

        // Then
        assertThat(userGoingDown).isNotNull();
    }

}
