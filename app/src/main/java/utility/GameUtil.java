package utility;

import android.content.Context;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;

import java.util.LinkedList;

import server_connections.Player_Info_active;
import utility.coordinates;

import static android.content.ContentValues.TAG;

public class GameUtil {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This class is a utility class containing functions for different purposes.
     *****************************************************/

    public SpannableString setss(Context c, int u, String s, int a, int b){
        ImageSpan imgSpan = new ImageSpan(c, u);
        SpannableString spannableString = new SpannableString(s);
        spannableString.setSpan(imgSpan, a, b, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    } // write a string message with a picture embedded in it

    public static Message sendMessage(int what,Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        return msg;
    } // send a message to a handler

    public static  LinkedList<Player_Info_active> unpacklist(String s,int roomnumber) {
        LinkedList<Player_Info_active> enemylist = new LinkedList<Player_Info_active>();
        String[] firstlevel = s.split(";");
        for(int i=0;i<firstlevel.length;i++)
        {
            String temp = firstlevel[i];

            String[] secondlevel = temp.split(":");

            enemylist.add(new Player_Info_active(secondlevel[0],roomnumber,Float.parseFloat(secondlevel[1]),Float.parseFloat(secondlevel[2]),Float.parseFloat(secondlevel[3])));
        }

        return enemylist;
    }// unpack a list containing players' information

    public static String listtostring(LinkedList<Player_Info_active> l,Player_Info_active self)  {
        String msg = "";

        for(Player_Info_active enemy:l)
        {
            float n;
            n = (float) Math.toDegrees(Math.atan((enemy.getLocationx()-self.getLocationx()) / (enemy.getLocationy()-self.getLocationy()))); // first quadrant
            if(n>0)
            {
                if(enemy.getLocationx()<self.getLocationx()&& enemy.getLocationy()<self.getLocationx())
                {
                    n+=180; // third quadrant
                }
            }
            else if(n<0)
            {
                if(enemy.getLocationx()>self.getLocationx()&&enemy.getLocationy()<self.getLocationy())
                {
                    n+=180; // second quadrant
                }
                if(enemy.getLocationy()>self.getLocationy()&&enemy.getLocationx()<self.getLocationx())
                {
                    n+=360; // fourth quadrant
                }
            }
            else if(n==0)
            {
                if(enemy.getLocationy()>=self.getLocationy())
                {
                    n = 0;
                }
                if(enemy.getLocationy()<self.getLocationy())
                {
                    n = 180;
                }
            }
            else if (n==-90)
            {
                n = 270;
            }

            float p = self.getDirection()-n;
            msg+=enemy.getUsername()+" "+ p+"\n";
        }
        return msg;
    }// convert a list of players' information into a string for transmission

    public int convertdirection(float current_rotation)
    {
        if((current_rotation>=0&&current_rotation<=45)||(current_rotation>315&&current_rotation<=360))
        {
            return 0;
        }
        if((current_rotation>45&&current_rotation<=135))
        {
            return 3;
        }
        if((current_rotation>135&&current_rotation<=225))
        {
            return 1;
        }
        if((current_rotation>225&&current_rotation<=315))
        {
            return 2;
        }
        return -1;
    }
    public String getopposed(String r1,float direction)
    {
        if(convertdirection(direction)==1)
        {
            if(r1.equals("G"))
            {
                return "A";
            }
            if(r1.equals("I"))
            {
                return "C";
            }
        }
        if(convertdirection(direction)==2)
        {
            if(r1.equals("I"))
            {
                return "G";
            }
            if(r1.equals("C"))
            {
                return "A";
            }
        }
        if(convertdirection(direction)==0)
        {
            if(r1.equals("A"))
            {
                return "G";
            }
            if(r1.equals("C"))
            {
                return "I";
            }
        }
        if(convertdirection(direction)==3)
        {
            if(r1.equals("A"))
            {
                return "C";
            }
            if(r1.equals("G"))
            {
                return "I";
            }
        }

        String t="";
        return t;
    }
    public boolean is_cut(String r1,String r2,float direction) {
        if (((r1.equals("G")&&r2.equals("I"))||(r1.equals("I")&&r2.equals("G")))&&(convertdirection(direction)==0||convertdirection(direction)==1))
        {
            return true;
        }
        if (((r1.equals("A")&&r2.equals("C"))||(r1.equals("C")&&r2.equals("A")))&&(convertdirection(direction)==0||convertdirection(direction)==1))
        {
            return true;
        }
        if (((r1.equals("A")&&r2.equals("G"))||(r1.equals("G")&&r2.equals("A")))&&(convertdirection(direction)==2||convertdirection(direction)==3))
        {
            return true;
        }
        if (((r1.equals("I")&&r2.equals("C"))||(r1.equals("C")&&r2.equals("I")))&&(convertdirection(direction)==2||convertdirection(direction)==3))
        {
            return true;
        }
        return false;
    }

   public  String getRouterName(String Mac) // get the name of the router through the mac address
    {
        String name = "no router found";
        if (Mac.equals("b0:f9:63:19:9e:10")) {
            name = "J";
        } else if (Mac.equals("70:ba:ef:d3:9c:d0")) {
            name = "K";
        } else if (Mac.equals("74:1f:4a:cb:21:70")) {
            name = "M";
        } else if (Mac.equals("88:25:93:3a:c2:08")) {
            name = "N";
        } else if (Mac.equals("b0:f9:63:19:21:30")) {
            name = "E";
        }
        return name;

    }







}
