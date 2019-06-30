package utility;

import java.util.LinkedList;

public class test_map {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This class is a map used to create the game board. it is consisted of several map nodes.
     *****************************************************/

    public map_node current = null; // current node,represents the user's current position
    public map_node last = null; // previous node, represents the user's last position
    public LinkedList<map_node> m_array= new LinkedList<map_node>();
    public test_map()
    {
       createAreaMap(0);
    } // constructor , create a map
    public map_node searchnode(String nodename) // search for a node using its name
    {
        for(int i=0;i<m_array.size();i++)
        {
            if(m_array.get(i).nodename.equals(nodename))
            {
                return m_array.get(i);
            }
        }
        return null;
    }
    void addnode(String nodename,map_node newm,int direction) // add a neighbour node to an existing node
    {
        map_node m = searchnode(nodename);
        m.addnode(newm,direction);
    }

    public map_node getinbetween(String nodename1, String nodename2) // get the node between two nodes
    {
        map_node n1 = searchnode(nodename1);
        map_node n2 = searchnode(nodename2);
        for(int i=0;i<n1.neighbournodes.size();i++)
        {
            for(int j=0;j<n2.neighbournodes.size();j++)
            {
                if(!(n1.neighbournodes.get(i)==null)&&!(n2.neighbournodes.get(j)==null)&&n1.neighbournodes.get(i).getNodename().equals(n2.neighbournodes.get(j).getNodename()))
                {
                    if(!n1.neighbournodes.get(i).iscenternode)
                    {
                        return n1.neighbournodes.get(i);
                    }

                }
            }
        }
        return null;
    }

    public void createAreaMap(int type) // create a map
    {
        String [] name = {"A","B","C","D","E","F","G","H","I","J","J-k","K","K-M","M","N-K","N"};
        float [] x = {1,3,5,1,3,5,1,3,5,0,0,0,0,0,3,4};
        float [] y = {1,1,1,3,3,3,5,5,5,0,3,4,7,8,4,4};
       for(int i=0;i<name.length;i++)
       {
           m_array.add(new map_node(name[i],x[i],y[i]));
       }
       searchnode("E").setcenternode();
       if(type==0)
       {
           current = m_array.get(0);
       }
       if(type==1)
       {
           current = searchnode("J");
       }
        // area map
        addnode("A",searchnode("D"),0);
        addnode("A",searchnode("B"),3);
        addnode("A",searchnode("E"),5);
        addnode("B",searchnode("E"),0);
        addnode("B",searchnode("A"),2);
        addnode("B",searchnode("C"),3);
        addnode("B",searchnode("D"),4);
        addnode("B",searchnode("F"),5);
        addnode("C",searchnode("F"),0);
        addnode("C",searchnode("B"),2);
        addnode("C",searchnode("E"),4);
        addnode("D",searchnode("G"),0);
        addnode("D",searchnode("A"),1);
        addnode("D",searchnode("E"),3);
        addnode("D",searchnode("H"),5);
        addnode("D",searchnode("B"),7);
        addnode("E",searchnode("H"),0);
        addnode("E",searchnode("B"),1);
        addnode("E",searchnode("D"),2);
        addnode("E",searchnode("F"),3);
        addnode("E",searchnode("G"),4);
        addnode("E",searchnode("I"),5);
        addnode("E",searchnode("A"),6);
        addnode("E",searchnode("C"),7);
        addnode("F",searchnode("I"),0);
        addnode("F",searchnode("C"),1);
        addnode("F",searchnode("E"),2);
        addnode("F",searchnode("H"),4);
        addnode("F",searchnode("B"),6);
        addnode("G",searchnode("D"),1);
        addnode("G",searchnode("H"),3);
        addnode("G",searchnode("E"),7);
        addnode("H",searchnode("E"),1);
        addnode("H",searchnode("G"),2);
        addnode("H",searchnode("I"),3);
        addnode("H",searchnode("D"),6);
        addnode("H",searchnode("F"),7);
        addnode("I",searchnode("F"),1);
        addnode("I",searchnode("H"),2);
        addnode("I",searchnode("E"),6);

        //tube map
        addnode("J",searchnode("J-K"),0);
        addnode("J-k",searchnode("k"),0);
        addnode("J-k",searchnode("J"),1);
        addnode("K",searchnode("K-M"),0);
        addnode("K",searchnode("J-K"),1);
        addnode("K",searchnode("N-k"),3);
        addnode("K-M",searchnode("M"),0);
        addnode("K-M",searchnode("K"),1);
        addnode("M",searchnode("K-M"),1);
        addnode("N-K",searchnode("K"),2);
        addnode("N-K",searchnode("N"),3);
        addnode("N",searchnode("N-K"),2);

    }


}
