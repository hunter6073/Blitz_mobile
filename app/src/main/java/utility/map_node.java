package utility;

import java.util.LinkedList;

/**
 * Created by TED on 2018/4/30.
 */

public class map_node {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This class is used to change a map into linking nodes
     *****************************************************/
    String nodename;
    public LinkedList<map_node> neighbournodes = new LinkedList<map_node>(); // a list of neighbournodes
    public coordinates cor = null; // the coordinates of this node
    public boolean iscenternode = false; // if this node is a center node in an area map

    public map_node(String name, float x, float y) // creating a node
    {
        for (int i = 0; i < 8; i++) {
            neighbournodes.add(null);
        }
        this.nodename = name;
        cor = new coordinates(x, y);

    }

    public void addnode(map_node m, int direction) // add new neighbour node
    {
        neighbournodes.remove(direction);
        neighbournodes.add(direction, m);
    }

    public void setcenternode() // set the node to a center node
    {
        iscenternode = true;
    }

    public String getNodename() {
        return nodename;
    }


}
