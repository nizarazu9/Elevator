package fr.elevator.util;

import fr.elevator.common.User;
import fr.elevator.common.UserStatus;

/**
 * User: zizou
 */
public class ElevatorMathUtil {

    public static final int INITIAL_SCORE = 22;

    public static Double calculateBestScore(User user, int currentFloor){
        int waitTime = (user.getUserStatus() == UserStatus.IN_ELEVATOR) ?
                user.getWaitTime() : user.getWaitTime() + Math.abs(user.getComingFrom() - currentFloor);

        if ( user.getUserStatus().equals(UserStatus.WAITING)){
            return Math.max(0d,INITIAL_SCORE-1-waitTime/2);
        }

        int travelTimeAverage = user.getTravelTime() + Math.abs(currentFloor - user.getFloorToGo()) + 1;

        return Math.max(0d,
                INITIAL_SCORE - waitTime/2 - travelTimeAverage + Math.abs(user.getComingFrom()-user.getFloorToGo()));

    }

}
