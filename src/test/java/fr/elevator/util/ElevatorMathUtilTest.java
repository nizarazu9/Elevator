package fr.elevator.util;

import fr.elevator.common.Direction;
import fr.elevator.common.User;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: zizou
 */
public class ElevatorMathUtilTest {

    @Test
    public void should_have_zero_score_when_waiting_too_long(){
        User user = new User(0, Direction.UP);
        user.setWaitTime(60);
        user.setTravelTime(10);

        Double aDouble = ElevatorMathUtil.calculateBestScore(user,0);

        assertThat(aDouble).isEqualTo(Double.valueOf(0D));
    }

    @Test
    public void should_calculate_score_when_user_in_elevator(){

        User user = new User(3,Direction.UP);
        user.setFloorToGo(10);
        user.setWaitTime(6);


        Double score = ElevatorMathUtil.calculateBestScore(user,4);

        assertThat(score).isEqualTo(19);

        Double bestScore = ElevatorMathUtil.calculateBestScore(user, 3);
        assertThat(bestScore).isEqualTo(18);

        user.setTravelTime(1);
        bestScore = ElevatorMathUtil.calculateBestScore(user, 4);
        assertThat(bestScore).isEqualTo(18);

        user.setTravelTime(2);
        bestScore = ElevatorMathUtil.calculateBestScore(user, 5);
        assertThat(bestScore).isEqualTo(18);
    }

    @Test
    public void should_calculate_bast_score_when_user_is_still_waiting(){
        User user = new User(3,Direction.UP);
        user.setWaitTime(6);


        Double bestScore = ElevatorMathUtil.calculateBestScore(user, 1);

        // 8 wait (-4)
        assertThat(bestScore).isEqualTo(17);
    }


}
