package server_connections;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by TED on 2018/3/14.
 */

public class Room implements Serializable {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This class is used to store basic room information,
     * including room number and a list of teams in play. This class is
     * mainly used for creating rooms and joining rooms
     *****************************************************/

    private static final long serialVersionUID = 3L;
    List<Team> Teams;
    int roomnumber;

    // different contructors
    public Room() {
        super();
        Teams = new LinkedList<Team>();
    }

    public Room(int Roomnumber) {
        super();
        Teams = new LinkedList<Team>();
        roomnumber = Roomnumber;
    }

    public Room(Room r) {
        super();
        Teams = new LinkedList<Team>();
        roomnumber = r.roomnumber;
        for (int i = 0; i < r.Teams.size(); i++) {
            Teams.add(new Team(r.getTeam(i).getName()));
            for (int j = 0; j < r.getTeam(i).players.size(); j++) {
                Teams.get(i).players.add(r.getTeam(i).players.get(j));
            }
        }
    }

    public void setRoomnumber(int roomnumber1) {
        roomnumber = roomnumber1;
    } // set room number

    public int getRoomnumber() {
        return roomnumber;
    } // get room number

    public void addTeams(Team t) {
        if (searchTeams(t.TeamName) == -1) {
            Teams.add(t);
        }

    } // add a new team to teamlist

    public int searchTeams(String teamname) {
        if (Teams == null) {
            return -1;
        }
        for (int i = 0; i < Teams.size(); i++) {
            if (Teams.get(i).TeamName.equals(teamname)) {
                return i;
            }
        }
        return -1;
    } // search for the team's index position in teamlist using the team's name

    public Team getTeam(int i) {
        if (i == -1) {
            return null;
        }
        Team t = null;
        t = Teams.get(i);
        return t;
    } // get a team from teamlist using the index number

    public void removeplayerfromroom(String playername) {
        for (int i = 0; i < Teams.size(); i++) {
            for (int j = 0; j < Teams.get(i).players.size(); j++) {
                if (Teams.get(i).players.get(j).equals(playername)) {
                    Teams.get(i).players.remove(j);
                }
            }
        }
    } // remove a player from the current room

    public int teamnumber() {
        if (Teams != null) {
            return Teams.size();
        } else {
            return 0;
        }
    } // get current size of teamlist


}
