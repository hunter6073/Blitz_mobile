package com.example.ted.myapplication.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ted.myapplication.R;
import com.example.ted.myapplication.Services.BackgroundService;
import utility.GameUtil;

import com.example.ted.myapplication.view_new.test_fragment;
import server_connections.Room;

public class CreateRoom extends AppCompatActivity {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: this activity is used to create a room for player team distribution.
     * the activity consists of several fragments and sends messages to and from them for information update
     *****************************************************/

    Button btn_start;
    SeekBar sb; // change the number of displayed fragments
    ScrollView sv;
    TextView room_id;
    TextView teamnum;
    test_fragment[] flist = new test_fragment[4];
    Intent i;
    Context c;
    FragmentManager manager;
    BackgroundService.MyBinder binder;
    ServiceConnection conn;
    Room r;
    Handler h = null; // handler for sending messages to background service

    Handler   crhandler  = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        { // this handler get its messages from background service,test fragment and self
            switch (msg.what)
            {
                case 0x001: //get room id from server and showing it in the activity
                    int roomnum = (int)msg.obj;
                    room_id.setText("room number:"+roomnum);
                    r.setRoomnumber(roomnum);
                    RoomUpdate();
                    break;
                case 0x002:// change team number display according to the scrollbar /// got from self
                    int t = (int)msg.obj;
                    teamnum.setText(t+2+"");
                    break;
                case 0x003: // update the current room(local and to server) and the fragments within the room /// got from test fragments
                    r = (Room) msg.obj;
                    RoomUpdate();
                    sendtoserver();
                    break;
                case  0x005: // update the current room with a room instance passed from the server /// got from background service
                    r = (Room) msg.obj;
                    RoomUpdate();
                    break;
                case 0x999: // if lost connection with the server /// got from background service
                    disconnected();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // bind backgroundservice -> get room number from server -> edit teams -> send room information to server -> get refreshed information
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        initialize(); // initialize views and parameters (including fragments)
    }

    void RoomUpdate() {

        for(int i=0;i<r.teamnumber();i++)
        {
            flist[i].fragmenthandler.sendMessage(GameUtil.sendMessage(0x000,this.r)); // update room in team fragments
            flist[i].fragmenthandler.sendMessage(GameUtil.sendMessage(0x002,this.r.getTeam(i))); // update team in team fragments
        }
    }// send to fragments for updating team info
    void sendtoserver() {
        h.sendMessage(GameUtil.sendMessage(0x004,r)); // send to background service
    }// send room to server
    void initialize() {

        room_id = (TextView) findViewById(R.id.room_id);
        teamnum = (TextView) findViewById(R.id.team_num);
        sv = (ScrollView) findViewById(R.id.sv_team);
        btn_start = (Button) findViewById(R.id.prepare_game);
        sb = (SeekBar) findViewById(R.id.sb_teamnum);
        c = this;
        i = getIntent();
        r = new Room();
        //create team fragments and add the first two to the interface
        for (int i = 0; i < 4; i++) {
            flist[i] = new test_fragment();
            flist[i].setcontext(c, this.i.getStringExtra("username"),r);
            flist[i].setHandler(crhandler);
        }
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.ll_team, flist[0]);
        transaction.add(R.id.ll_team, flist[1]);
        transaction.commit();
        flist[0].exists = true;
        flist[1].exists = true;

        //must have code for binding service
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (BackgroundService.MyBinder) service;
                binder.sethandler(crhandler);
                h = binder.getServiceHandler();
                h.sendMessage(GameUtil.sendMessage(0x002,i.getStringExtra("username")));//create room
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder.sethandler(null);
            }
        };
        bind_backgroundservice();  // binding background service

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // change team fragments' display through seekbar
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                crhandler.sendMessage(GameUtil.sendMessage(0x002,progress));
                FragmentTransaction transaction = manager.beginTransaction();
                if (progress == 0) {
                    if (flist[3].exists == true) {
                        transaction.remove(flist[3]);
                        flist[3].exists = false;
                    }
                    if (flist[2].exists == true) {
                        transaction.remove(flist[2]);
                        flist[2].exists = false;
                    }
                } else if (progress == 1) {
                    if (flist[3].exists == true) {
                        transaction.remove(flist[3]);
                        flist[3].exists = false;
                    } else {
                        transaction.add(R.id.ll_team, flist[2]);
                        flist[2].exists = true;
                    }
                } else if (progress == 2) {
                    if (flist[2].exists == false) {
                        transaction.add(R.id.ll_team, flist[2]);
                        flist[2].exists = true;
                    }
                    if (flist[3].exists == false) {
                        transaction.add(R.id.ll_team, flist[3]);
                        flist[3].exists = true;
                    }
                }
                transaction.commit();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });//when user slides the seek bar

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// go into prepraration activity
                enterPrepare();
            }
        });

    }//initialize paramaters and views
    void bind_backgroundservice() {

        Intent intent1 = new Intent(this, BackgroundService.class);
        bindService(intent1,conn,BIND_AUTO_CREATE);
    }// bind background service
    void disconnected() {
        Toast.makeText(c,"lost connection",Toast.LENGTH_SHORT).show();
        Intent temp = new Intent(CreateRoom.this,Entrance.class);
        startActivity(temp);
    }//when disconnected from server
    void enterPrepare() {
        boolean joined = false;
        if(r.teamnumber()>=2)
        {

            for(int l=0;l<r.teamnumber();l++)
            {
                for(int k=0;k<r.getTeam(l).getPlayers().size();k++)
                {
                    if(r.getTeam(l).getPlayers().get(k).equals( i.getStringExtra("username")))
                    {
                        Intent j = new Intent(CreateRoom.this, MainActivity.class);
                        j.putExtra("username", i.getStringExtra("username"));
                        j.putExtra("team",r.getTeam(l).getName());
                        j.putExtra("roomnumber",r.getRoomnumber());
                        startActivity(j);
                        joined = true;
                    }
                }
            }
            if(!joined)
            {
                Toast.makeText(c,"Not joined any teams",Toast.LENGTH_SHORT).show();
            }


        }
        else
        {
            Toast.makeText(c,"Not enough teams!",Toast.LENGTH_SHORT).show();
        }
    }//when clicked start button, enter game preparation activity
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
    } // unbind background service here
    @Override
    protected void onResume() {
        super.onResume();
        bind_backgroundservice();
    } // bind background service here

}
