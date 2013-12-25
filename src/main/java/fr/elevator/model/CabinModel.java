package fr.elevator.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fr.elevator.common.CabinCommand;
import fr.elevator.common.Direction;
import fr.elevator.common.User;
import fr.elevator.util.ElevatorMathUtil;

import java.util.List;

/**
 * User: zizou
 */
public class CabinModel {

    public static final double THREESHOLD = 0.8;

    // Cabin id
    private int id;

    // All users in this cabin
    private List<User> usersInCabin;

    // All users waiting this cabin
    private List<User> usersWaitingCabin;

    private int currentFloor;

    private boolean doorOpened;
    private CabinCommand lastCommand;

    private Direction elevatorDirection;

    private int lowerFloor;

    private int higherFloor;
    private int cabinSize;
    static int CABIN_ID = 0;

    private static final Predicate<User> USER_WITH_SCORE_PREDICATE = new Predicate<User>() {
        @Override
        public boolean apply(User input) {
            return input.getStillCanScore() > 0;
        }
    };

    public CabinModel(int lowerFloor, int higherFloor, int cabinSize) {
        this.lowerFloor = lowerFloor;
        this.higherFloor = higherFloor;
        this.cabinSize = cabinSize;
        elevatorDirection = Direction.UP;
        currentFloor = 0;
        usersInCabin = Lists.newArrayList();
        usersWaitingCabin = Lists.newArrayList();
        setId(CABIN_ID++);
    }

    public List<User> getUsersInCabin() {
        return usersInCabin;
    }

    public void setUsersInCabin(List<User> usersInCabin) {
        this.usersInCabin = usersInCabin;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public boolean isDoorOpened() {
        return doorOpened;
    }

    public void setDoorOpened(boolean doorOpened) {
        this.doorOpened = doorOpened;
    }

    public CabinCommand getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(CabinCommand lastCommand) {
        this.lastCommand = lastCommand;
    }

    public List<User> getUsersWaitingCabin() {
        return usersWaitingCabin;
    }

    public Direction getElevatorDirection() {
        return elevatorDirection;
    }

    public void setElevatorDirection(Direction elevatorDirection) {
        this.elevatorDirection = elevatorDirection;
    }

    public CabinCommand findNextCommand(){

        updateScore();

        if ( doorOpened ){
            closeElevatorDoor();
            return CabinCommand.CLOSE;
        }

        // If cabin is full or no users with score are in the cabin, so open the door for user that can leave
        if ( (cabinAlmostFull() || !isThereUsersWithScore() )
                && thereIsUserThatCanLeaveTheElevator() && lastCommand != CabinCommand.CLOSE){

            openElevatorDoor();

            if (currentFloor == lowerFloor)
                return CabinCommand.OPEN_UP;

            if ( currentFloor == higherFloor)
                return CabinCommand.OPEN_DOWN;

            return (elevatorDirection == Direction.UP )
                    ? CabinCommand.OPEN_UP : CabinCommand.OPEN_DOWN;
        }

        boolean thereUserInElevatorDirection = isThereUserInElevatorDirection(elevatorDirection);

        if ( lastCommand != CabinCommand.CLOSE &&
                (isThereUserWithScoreThatCanLeaveElevator() || ( isThereUserWithPositiveThatCanTakeTheElevator() && usersInCabin.size() < cabinSize)))
        {
            openElevatorDoor();

            if (currentFloor == lowerFloor)
                return CabinCommand.OPEN_UP;

            if ( currentFloor == higherFloor)
                return CabinCommand.OPEN_DOWN;

            if (elevatorDirection == Direction.UP && thereUserInElevatorDirection || elevatorDirection == Direction.DOWN && !thereUserInElevatorDirection){
                return CabinCommand.OPEN_UP;
            }
            return CabinCommand.OPEN_DOWN;
        }

        if ( isCabinEmpty()){
            lastCommand = CabinCommand.NOTHING;
            return CabinCommand.NOTHING;
        }

        if ( currentFloor == lowerFloor ){
            up();
            return CabinCommand.UP;
        }

        if ( currentFloor == higherFloor ){
            down();
            return CabinCommand.DOWN;
        }

        CabinCommand command;

        if ( thereUserInElevatorDirection){
            command =  (elevatorDirection == Direction.UP) ?  CabinCommand.UP : CabinCommand.DOWN;
        }
        else {
            command =  (elevatorDirection == Direction.UP) ?  CabinCommand.DOWN : CabinCommand.UP;
        }
        if ( command.equals(CabinCommand.UP)){
            up();
        }
        if ( command.equals(CabinCommand.DOWN)){
            down();
        }

        return command;
    }

    private boolean isCabinEmpty() {
        return usersInCabin.size() == 0 && usersWaitingCabin.size() == 0;
    }

    public void openElevatorDoor() {
        lastCommand = CabinCommand.OPEN;
        setDoorOpened(true);
    }

    public synchronized void closeElevatorDoor() {
        lastCommand = CabinCommand.CLOSE;
        setDoorOpened(false);
    }

    public void down() {
        lastCommand = CabinCommand.DOWN;
        setCurrentFloor(getCurrentFloor() - 1);
        elevatorDirection = Direction.DOWN;
    }

    public void up() {
        lastCommand = CabinCommand.UP;
        setCurrentFloor(getCurrentFloor() + 1);
        elevatorDirection = Direction.UP;
    }

    @VisibleForTesting
    boolean isThereUserWithScoreThatCanLeaveElevator() {
        for (User user : getUsersInCabin()) {
            if ( user.getFloorToGo() == getCurrentFloor() && user.getStillCanScore() > 0){
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    boolean isThereUserWithPositiveThatCanTakeTheElevator(){
        for (User userAsk : getUsersWaitingCabin()) {
            if (userWaitingInThisFloor(userAsk) && userAsk.getStillCanScore() > 0){
                return true;
            }
        }

        return false;
    }

    @VisibleForTesting
    boolean userWaitingInThisFloor(User userAsk) {
        return userAsk.getComingFrom() == getCurrentFloor();
    }

    protected boolean thereIsUserThatCanLeaveTheElevator() {
        for (User user : getUsersInCabin()) {
            if ( user.getFloorToGo() == getCurrentFloor()){
                return true;
            }
        }
        return false;
    }

    public void addUserInCabin(User user){
        getUsersInCabin().add(user);
    }

    public boolean removeUserFromWaitingList(User user){
        return getUsersWaitingCabin().remove(user);
    }

    public boolean isThereUserInElevatorDirection(final Direction direction){
        if ( !isThereUsersWithScore() || cabinAlmostFull()){
            Predicate<User> USER_IN_CABIN_DIRECTION_PREDICATE = new Predicate<User>() {
                @Override
                public boolean apply(User user) {
                    if (direction == Direction.UP) {
                        return currentFloor < user.getFloorToGo();
                    } else if (direction == Direction.DOWN) {
                        return currentFloor > user.getFloorToGo();
                    } else return false;
                }
            };
            Iterable<User> elevatorUsers = Iterables.filter(usersInCabin, USER_IN_CABIN_DIRECTION_PREDICATE);
            Iterable<User> elevatorUserWaiting = Iterables.filter(usersWaitingCabin, new Predicate<User>() {
                @Override
                public boolean apply(User user) {
                    if (direction == Direction.UP) {
                        return  (currentFloor < user.getComingFrom()  || currentFloor == user.getComingFrom() && user.getDirection() == Direction.UP);
                    } else if (direction == Direction.DOWN) {
                        return  (currentFloor > user.getComingFrom()  || currentFloor == user.getComingFrom() && user.getDirection() == Direction.DOWN);
                    } else return false;
                }
            });

            return Iterables.size(elevatorUsers) > 0 ||
                    Iterables.size(elevatorUserWaiting) > 0;
        }

        Iterable<User> elevatorUsers = Iterables.filter(usersInCabin, new Predicate<User>() {
            @Override
            public boolean apply(User input) {
                if (direction == Direction.UP) {
                    return currentFloor < input.getFloorToGo() && input.getStillCanScore() > 2;
                } else if (direction == Direction.DOWN) {
                    return currentFloor > input.getFloorToGo() && input.getStillCanScore() > 2;
                } else return false;
            }
        });
        Iterable<User> elevatorUserWaiting = Iterables.filter(usersWaitingCabin, new Predicate<User>() {
            @Override
            public boolean apply(User input) {
                if (direction == Direction.UP) {
                    return input.getStillCanScore() > 4
                            && (currentFloor < input.getComingFrom()  || currentFloor == input.getComingFrom() && input.getDirection() == Direction.UP);
                } else if (direction == Direction.DOWN) {
                    return input.getStillCanScore() > 4
                            && (currentFloor > input.getComingFrom()  || currentFloor == input.getComingFrom() && input.getDirection() == Direction.DOWN);
                } else return false;
            }
        });

        return Iterables.size(elevatorUsers) > 0 ||
                Iterables.size(elevatorUserWaiting) > 0;
    }

    private boolean cabinAlmostFull() {
        return usersInCabin.size() > cabinThreeshold();
    }

    private int cabinThreeshold() {
        return Double.valueOf(cabinSize * THREESHOLD).intValue();
    }

    public boolean isThereUsersWithScore(){
        Iterable<User> elevatorUsers = Iterables.filter(usersInCabin, USER_WITH_SCORE_PREDICATE);
        if ( Iterables.size(elevatorUsers) > 0 )
        {
            return true;
        }
        Iterable<User> elevatorUserWaiting = Iterables.filter(usersWaitingCabin, USER_WITH_SCORE_PREDICATE);
        if ( Iterables.size(elevatorUserWaiting) > 0 )
        {
            return true;
        }
        return false;
    }

    public boolean isUserEligibleForCabin(User user){
        if ( user.getDirection() != elevatorDirection){
            return false;
        }

        if (getElevatorDirection() == Direction.UP) {
            return getCurrentFloor() <= user.getComingFrom();

        }

        if (getElevatorDirection() == Direction.DOWN) {
            return getCurrentFloor() >= user.getComingFrom();
        }

        return false;
    }

    public User findUserCallingFromWithDirection(final Direction direction){
        Iterable<User> userShouldEnter = Iterables.filter(getUsersWaitingCabin(), new Predicate<User>() {

            @Override
            public boolean apply(User user) {
                return user.getComingFrom().equals(getCurrentFloor()) && user.getDirection() == direction;
            }
        });

        return Iterables.getFirst(userShouldEnter, null);
    }

    public synchronized void updateScore(){
        for (User user : getUsersWaitingCabin()) {
            user.setWaitTime(user.getWaitTime() + 1);
            user.setStillCanScore(ElevatorMathUtil.calculateBestScore(user, getCurrentFloor()));
        }
        for (User user : usersInCabin) {
            user.setTravelTime(user.getTravelTime() + 1);
            user.setStillCanScore(ElevatorMathUtil.calculateBestScore(user, getCurrentFloor()));
        }
    }

    @Override
    public String toString() {
        return "CabinModel{" +
                "\nCabinId=" + id +
                "\nusersInElevator=" + usersInCabin +
                ",\n usersWaitingElevator=" + usersWaitingCabin +
                ",\n currentFloor=" + currentFloor +
                ",\n doorOpened=" + doorOpened +
                ",\n elevatorDirection=" + elevatorDirection +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @VisibleForTesting
    Direction calculateUserDirection(Integer floor) {
        Direction userDirection = Direction.DOWN;
        if ( floor > getCurrentFloor()){
            userDirection = Direction.UP;
        }
        return userDirection;
    }

    public User findUserThatCanLeave(){
        Optional<User> user = Iterables.tryFind(getUsersInCabin(), new Predicate<User>() {

            @Override
            public boolean apply(User user) {
                if ( user.getFloorToGo() == null ) return false;
                return user.getFloorToGo().equals(getCurrentFloor());
            }
        });
        return user.orNull();
    }

    public boolean isEmpty(){
        return Iterables.isEmpty(getUsersInCabin()) && Iterables.isEmpty(getUsersWaitingCabin());
    }

    public boolean isFull(){
        return Iterables.size(getUsersInCabin()) == cabinSize;
    }

}
