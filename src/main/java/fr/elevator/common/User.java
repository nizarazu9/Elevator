package fr.elevator.common;

/**
 * User: zizou
 */
public class User {

    // For score purpose
    private Integer waitTime;
    private Integer travelTime;

    // When user call elevator
    private Integer comingFrom;
    private Direction direction;

    // When user enter elevator
    private Integer floorToGo;

    // best score that user can still score
    private Double stillCanScore;

    // User request taken into Account
    private boolean hasElevator;

    public User(Integer commingFrom, Direction direction) {
        this.comingFrom = commingFrom;
        this.direction = direction;
        this.waitTime = 0;
        this.travelTime = 0;
        this.stillCanScore = 0D;
    }

    public Integer getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Integer waitTime) {
        this.waitTime = waitTime;
    }

    public Integer getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(Integer travelTime) {
        this.travelTime = travelTime;
    }

    public Integer getComingFrom() {
        return comingFrom;
    }

    public void setComingFrom(Integer comingFrom) {
        this.comingFrom = comingFrom;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Integer getFloorToGo() {
        return floorToGo;
    }

    public void setFloorToGo(Integer floorToGo) {
        this.floorToGo = floorToGo;
    }

    public UserStatus getUserStatus() {
        return (floorToGo == null) ? UserStatus.WAITING : UserStatus.IN_ELEVATOR ;
    }

    public Double getStillCanScore() {
        return stillCanScore;
    }

    public void setStillCanScore(Double stillCanScore) {
        this.stillCanScore = stillCanScore;
    }

    public boolean isHasElevator() {
        return hasElevator;
    }

    public void setHasElevator(boolean hasElevator) {
        this.hasElevator = hasElevator;
    }

    @Override
    public String toString() {
        return "User{" +
                "stillCanScore=" + stillCanScore +
                ", comingFrom="+ comingFrom +
                ", floorToGo=" + floorToGo +
                '}';
    }
}
