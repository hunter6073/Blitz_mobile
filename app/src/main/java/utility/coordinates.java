package utility;

/**
 * Created by TED on 2018/4/9.
 */

public class coordinates {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This class is used to record the user's position,
     * basically only containing the X and Y coordinates
     *****************************************************/

    float x; // Coordinates on the X axis
    float y; // Coordinates on the Y axis

    public coordinates(float x, float y) // constructor
    {
        this.x = x;
        this.y = y;
    }

    // getters and setters, auto generated
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

}
