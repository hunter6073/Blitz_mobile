package server_connections;

import java.io.Serializable;

public class Transmit implements Serializable {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This class is used to box different objects
     * for transmission between phone and server.
     *****************************************************/

    private static final long serialVersionUID = 2L;
    public String type;
    public Object obj;

    public Transmit(String typ, Object b) {
        type = typ;
        obj = b;
    }

}