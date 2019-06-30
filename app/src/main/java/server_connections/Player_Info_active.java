package server_connections;

import java.io.Serializable;

/**
 * Created by TED on 2017/10/5.
 */

public class Player_Info_active implements Serializable { // 2.0 code standard reached. last edited date:2018/5/6

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This class is used to store basic user information,
     * including username, roomnumber, location and direction. This is mainly
     * used for transmitting user information between phone and server during the shootout
     *****************************************************/

    private static final long serialVersionUID = 1L;
    String username;
    int roomnumber;
    float locationx;
    float locationy;
    float direction;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRoomnumber() {
        return roomnumber;
    }

    public void setRoomnumber(int roomnumber) {
        this.roomnumber = roomnumber;
    }

    public float getLocationx() {
        return locationx;
    }

    public void setLocationx(float locationx) {
        this.locationx = locationx;
    }

    public float getLocationy() {
        return locationy;
    }

    public void setLocationy(float locationy) {
        this.locationy = locationy;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public Player_Info_active(String username, int roomnumber, float locationx, float locationy, float direction) {
        super();
        this.username = username;
        this.roomnumber = roomnumber;
        this.locationx = locationx;
        this.locationy = locationy;
        this.direction = direction;
    }


}