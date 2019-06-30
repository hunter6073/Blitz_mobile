package server_connections;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Created by TED on 2018/3/14.
 */

public class Team implements Serializable {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This class is used to store team information. Each team
     * consists of one teamname and a list of players. This class is mainly
     * used in creating or joining rooms. also used in transmitting information
     *****************************************************/

    private static final long serialVersionUID = 4L;
    String TeamName; // the name of the team
    public ArrayList<String> players; // the list of players(names) inside this team
    public Team(String name)//constructor
    {
        super();
        TeamName = name;
        players = new ArrayList<String>();
    }
    public void addPlayer(String player) // add a new player to this team
    {
        players.add(player);
    }
    public void removePlayer(String player) // remove a player from this team
    {
        for(int i=0;i<players.size();i++)
        {
            if(player.equals(players.get(i)))
            {
                players.remove(i);
            }
        }
    }
    public boolean searchPlayer(String player) // determine if the player exists
    {
        for(String p:players)
        {
            if(p.equals(player))
            {
                return true;
            }
        }
        return false;
    }
    // getters and setters, auto generated
    public void setName(String name)
    {
        TeamName = name;
    }

    public String getName()
    {
        return TeamName;
    }

    public ArrayList getPlayers()
    {
        return players;
    }


}
