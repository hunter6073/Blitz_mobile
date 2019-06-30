package com.example.ted.myapplication.view_new;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ted.myapplication.R;
import utility.GameUtil;

import java.util.Timer;
import java.util.TimerTask;

import server_connections.Room;
import server_connections.Team;

import static com.example.ted.myapplication.R.layout.support_simple_spinner_dropdown_item;

/**
 * Created by TED on 2018/2/20.
 */

public class test_fragment extends Fragment {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description:  This fragment is used to display team information, mainly used in create room activity and join room activity.
     * This fragment sends its messages directly to the main activities holding it
     *****************************************************/

    Handler handler;//handler for sending messages to activity(create room and join room)
    public void setHandler(Handler h) {
        handler = h;
    }

    public boolean exists = false;    // the exist states of the fragment
    public boolean join_use = false; //used to determine if this is used for create room or join room
    public String Teamname = "";
    String playername;
    TextView tv1;
    TextView tv2;
    EditText editText = null;
    Button confirm;
    Button join;
    Spinner s;
    LinearLayout layout;
    Context c;
    Room r = new Room();
    ArrayAdapter<String> arrayAdapter = null;


    public Handler fragmenthandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0x000: // update fragment UI according to Room ///given by create room
                    r = (Room) msg.obj;
                    if (r.searchTeams(Teamname) != -1) {
                        arrayAdapter = new ArrayAdapter<String>(c, support_simple_spinner_dropdown_item, r.getTeam(r.searchTeams(Teamname)).getPlayers());
                        s.setAdapter(arrayAdapter);
                    }
                    break;
                case 0x002: // get already created room information from server /// given by create room
                    Team t = (Team) msg.obj;
                    Teamname = t.getName();
                    editText.setText(Teamname);
                    confirm.setEnabled(false);
                    join.setEnabled(true);
                    arrayAdapter = new ArrayAdapter<String>(c, support_simple_spinner_dropdown_item, t.getPlayers());
                    s.setAdapter(arrayAdapter);
                    break;
                case 0x001: //this is used to intialize the UI when starting activity,used by joinRoom // given by self
                    r = (Room) msg.obj;
                    if (editText != null) {
                        editText.setText(Teamname);
                        editText.setEnabled(false);
                        r = (Room) msg.obj;
                        arrayAdapter = new ArrayAdapter<String>(c, support_simple_spinner_dropdown_item, r.getTeam(r.searchTeams(Teamname)).getPlayers());
                        s.setAdapter(arrayAdapter);
                        exists = true;
                    }
                    break;
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) // intiate view and components, different between join_room use and create_room use
    {
        initialize();
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join();
            }
        });


    }

    void initialize() {
        layout = new LinearLayout(this.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(R.drawable.border);
        tv1 = new TextView(this.getContext());//team name label
        tv1.setText("team name");
        editText = new EditText(this.getContext()); // team name edittext
        tv2 = new TextView(this.getContext());//team member label
        tv2.setText("team members");
        s = new Spinner(this.getContext()); // team members spinner
        confirm = new Button(this.getContext());
        confirm.setText("confirm");
        confirm.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        join = new Button(this.getContext());
        join.setText("join");
        join.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        layout.addView(tv1);
        layout.addView(editText);
        layout.addView(tv2);
        layout.addView(s);
        if (join_use == false) // if used in create room
        {
            layout.addView(confirm);
            join.setEnabled(false);
        }
        layout.addView(join);
    }//initialize paramaters and views
    void confirm() {
        Teamname = editText.getText().toString();
        if (editText.getText().toString().equals("")) {
            Toast.makeText(c, "please input a team name", Toast.LENGTH_SHORT).show();
        } else {
            if (r.searchTeams(Teamname) != -1) {
                Toast.makeText(c, "team already exist!", Toast.LENGTH_SHORT).show();
            } else {

                r.addTeams(new Team(Teamname));
                editText.setEnabled(false);
                confirm.setEnabled(false);
                join.setEnabled(true);
                handler.sendMessage(GameUtil.sendMessage(0x003, r)); // send to create room activity
            }
        }
    } // when user clicks on the confirm button
    void join() {
        if (r.getTeam(r.searchTeams(Teamname)).searchPlayer(playername)) {
            Toast.makeText(c, "already joined this team", Toast.LENGTH_SHORT).show();
        } else {
            r.removeplayerfromroom(playername);
            r.getTeam(r.searchTeams(Teamname)).addPlayer(playername);
            handler.sendMessage(GameUtil.sendMessage(0x003, r)); // send to create room activity or join room activity
        }

    }// join player to team
    public void setcontext(Context c1, String playername1, Room r) {

        c = c1;
        playername = playername1;
        this.r = r;
    }// set main activity's context, username, and room info

}
