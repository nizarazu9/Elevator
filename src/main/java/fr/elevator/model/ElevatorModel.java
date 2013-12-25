package fr.elevator.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import fr.elevator.common.CabinCommand;
import fr.elevator.common.Direction;
import fr.elevator.common.User;
import org.apache.log4j.Logger;

import java.util.List;

public class ElevatorModel {

    private static Logger logger = Logger.getLogger(ElevatorModel.class);

    private List<User> usersWaitingElevator;

    private List<CabinModel> cabinsList;

    private Double score;

    private int lowerFloor = 0;
    private int higherFloor = 5;
    private int cabinSize = 5;

    private int cabinCount = 2;
    private int resetNumber = 0;

    private static ElevatorModel INSTANCE;

    private List<User> userToRemove = Lists.newArrayList();

    private int abnormalUserEntring;
    private int abnormalUserExiting;

    private int allUsers;
    private int happyUsers;

    private ElevatorModel() {
        reset();
    }

    public static ElevatorModel getInstance(){
        if ( INSTANCE == null ) INSTANCE = new ElevatorModel();
        return INSTANCE;
    }

    public List<User> getUsersWaitingElevator() {
        return usersWaitingElevator;
    }

    public void setUsersWaitingElevator(List<User> usersWaitingElevator) {
        this.usersWaitingElevator = usersWaitingElevator;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public int getLowerFloor() {
        return lowerFloor;
    }

    public void setLowerFloor(int lowerFloor) {
        this.lowerFloor = lowerFloor;
    }

    public int getHigherFloor() {
        return higherFloor;
    }

    public void setHigherFloor(int higherFloor) {
        this.higherFloor = higherFloor;
    }

    public int getCabinSize() {
        return cabinSize;
    }

    public void setCabinSize(int cabinSize) {
        this.cabinSize = cabinSize;
    }

    public List<CabinModel> getCabinsList() {
        return cabinsList;
    }

    public void setCabinsList(List<CabinModel> cabinsList) {
        this.cabinsList = cabinsList;
    }

    public int getCabinCount() {
        return cabinCount;
    }

    public void setCabinCount(int cabinCount) {
        this.cabinCount = cabinCount;
    }

    public void updateCabinCount(int cabinCount){
        setCabinCount(cabinCount);
        cabinsList = Lists.newArrayListWithExpectedSize(cabinCount);
    }

    public synchronized List<CabinCommand> findNextCommand() {
        updateScore();

        for (User user : usersWaitingElevator) {
            assignCabinToUser(user);
        }

        usersWaitingElevator.removeAll(userToRemove);
        userToRemove.clear();

        List<CabinCommand> CabinCommands = Lists.newArrayList();
        for (CabinModel cabinModel : cabinsList) {
            CabinCommand nextCommand = cabinModel.findNextCommand();
            CabinCommands.add(nextCommand);
        }

        return CabinCommands;
    }

    public synchronized void callAtFloor(Integer floor,final Direction direction) {
        updateAllUsersCount();
        assignCabinToUser(new User(floor,direction));
    }

    @VisibleForTesting
    void assignCabinToUser(final User user) {
        // First find eligible cabin
        Iterable<CabinModel> cabinInSameUserDirection = findEligibleCabins(user);

        int cabin = Iterables.size(cabinInSameUserDirection);

        // If no cabin eligible, user should wait next assignement
        if ( cabin == 0 ){
            if (!getUsersWaitingElevator().contains(user)) {
                getUsersWaitingElevator().add(user);
            }
        }

        // Only one cabin, good, will assign this cabin to the user
        else if ( cabin == 1) {
            userToRemove.add(user);
            Iterables.getOnlyElement(cabinInSameUserDirection).getUsersWaitingCabin().add(user);
        }

        // Ok, we have to find the nearest one...
        else {
            CabinModel cabinModel = findNearestCabinForUser(user, cabinInSameUserDirection);
            userToRemove.add(user);
            cabinModel.getUsersWaitingCabin().add(user);
        }
    }

    @VisibleForTesting
    Iterable<CabinModel> findEligibleCabins(final User user) {
        return Iterables.filter(cabinsList, new Predicate<CabinModel>() {

            public boolean apply(CabinModel cabinModel) {
                // For full cabin...
                if (cabinModel.isFull()) {
                    return false;
                }
                // For empty cabin or cabin with only users with null score
                if (cabinModel.isEmpty() || !cabinModel.isThereUsersWithScore()) {
                    return true;
                }
                return cabinModel.isUserEligibleForCabin(user);
            }
        });
    }

    @VisibleForTesting
    CabinModel findNearestCabinForUser(User user, Iterable<CabinModel> cabinInSameUserDirection) {
        CabinModel nearestCabin = null;

        Integer userComingFrom = user.getComingFrom();
        int minDistance =  higherFloor - lowerFloor;

        for (CabinModel cabin : cabinInSameUserDirection) {
            int distance = Math.abs(cabin.getCurrentFloor() - userComingFrom);
            if ( distance < minDistance){
                minDistance = distance;
                nearestCabin = cabin;
            }
        }

        return nearestCabin;
    }


    public synchronized void floorToGo(Integer floor, int cabin) {
        final CabinModel cabinModel = cabinsList.get(cabin);

        logger.info("User going to "+ floor+ " from "+cabinModel.getCurrentFloor()+ " on cabin "+cabin);

        // Try to find the user from cabin
        User user = cabinModel.findUserCallingFromWithDirection(cabinModel.calculateUserDirection(floor));

        if ( user != null ){

            user.setFloorToGo(floor);
            cabinModel.addUserInCabin(user);
            if ( !cabinModel.removeUserFromWaitingList(user)){
                abnormalUserEntring++;
                logger.error("User Removed from user waiting ERROR");
            }
        }

        else {
            Optional<User> userOptional = Iterables.tryFind(getUsersWaitingElevator(), new Predicate<User>() {

                public boolean apply(User user) {
                    return user.getComingFrom().equals(cabinModel.getCurrentFloor());
                }
            });

            if ( userOptional.isPresent()){
                User usr = userOptional.get();
                usr.setFloorToGo(floor);
                cabinModel.addUserInCabin(usr);
                if ( !cabinModel.removeUserFromWaitingList(usr)){
                    abnormalUserEntring++;
                    logger.error("User Removed from user waiting ERROR");
                }
            }
            else
            {
                boolean userFoundWaitingOtherElevator = false;
                for (CabinModel model : cabinsList) {
                    User userToGoFromCabin = model.findUserCallingFromWithDirection(cabinModel.calculateUserDirection(floor));
                    if ( userToGoFromCabin != null ){
                        model.getUsersWaitingCabin().remove(userToGoFromCabin);
                        userToGoFromCabin.setFloorToGo(floor);
                        cabinModel.getUsersInCabin().add(userToGoFromCabin);
                        userFoundWaitingOtherElevator = true;
                        break;
                    }
                }

                if ( !userFoundWaitingOtherElevator){
                    abnormalUserEntring++;
                    logger.error("No User Found going To "+floor+ " from "+ cabinModel.getCurrentFloor()+ " [il est montee cabin "+cabin+" ]");
                }
            }
        }
    }

    public synchronized void userHasExited(int cabin) {
        final CabinModel cabinModel = cabinsList.get(cabin);

        logger.info("User exited cabin "+cabin+ " on floor "+ cabinModel.getCurrentFloor());

        updateHappyUsersCount();

        User user = cabinModel.findUserThatCanLeave();

        if ( user == null ){
            abnormalUserExiting++;
            logger.error("no user can exit from cabin : "+cabin+ " on floor "+cabinModel.getCurrentFloor());
        }else {
            score = score + user.getStillCanScore();
            cabinModel.getUsersInCabin().remove(user);
        }
    }

    public synchronized void reset() {
        score = 0D;
        setUsersWaitingElevator(Lists.<User>newArrayList());
        cabinsList = Lists.newArrayListWithExpectedSize(cabinCount);
        CabinModel.CABIN_ID = 0;
        resetNumber++;
        abnormalUserEntring = 0;
        abnormalUserExiting = 0;
        allUsers = 0;
        happyUsers = 0;
        for ( int i=0 ; i < cabinCount ; i++){
            cabinsList.add(new CabinModel(lowerFloor,higherFloor,cabinSize));
        }
    }

    public synchronized void updateScore(){
        for (User user : getUsersWaitingElevator()) {
            user.setWaitTime(user.getWaitTime() + 1);
        }
    }

    private void updateHappyUsersCount() {
        happyUsers++;
    }

    private void updateAllUsersCount() {
        allUsers++;
    }

    public int getResetNumber() {
        return resetNumber;
    }

    public void setResetNumber(int resetNumber) {
        this.resetNumber = resetNumber;
    }

    public int getAbnormalUserEntring() {
        return abnormalUserEntring;
    }

    public void setAbnormalUserEntring(int abnormalUserEntring) {
        this.abnormalUserEntring = abnormalUserEntring;
    }

    public int getAbnormalUserExiting() {
        return abnormalUserExiting;
    }

    public void setAbnormalUserExiting(int abnormalUserExiting) {
        this.abnormalUserExiting = abnormalUserExiting;
    }

    public int getHappyUsers() {
        return happyUsers;
    }

    public void setHappyUsers(int happyUsers) {
        this.happyUsers = happyUsers;
    }

    public int getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(int allUsers) {
        this.allUsers = allUsers;
    }
}