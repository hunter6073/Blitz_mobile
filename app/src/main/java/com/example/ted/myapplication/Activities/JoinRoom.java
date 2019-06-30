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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ted.myapplication.R;
import com.example.ted.myapplication.Services.BackgroundService;
import utility.GameUtil;

import java.util.LinkedList;

import server_connections.Player_Info_active;
import com.example.ted.myapplication.view_new.test_fragment;
import server_connections.Room;
import server_connections.ClientThread;

public class JoinRoom extends AppCompatActivity { //2.0 code standard reached. last edited date:2018/5/7

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This activity is used to search for a created room and allow the player to join to one team.
     * This activity consists of several fragments and sends messages to and from them for information update
     *****************************************************/

    TextView username;
    EditText roomnumber;
    LinearLayout ll;
    Button btn;
    Button btn_start;
    Context ct;
    Room r;
    Intent i;
    ClientThread clientThread;
    Handler handler;
    BackgroundService.MyBinder binder;
    ServiceConnection conn;
    LinkedList<test_fragment> fragmentlist = new LinkedList<test_fragment>();


    public void sethandler(Handler h) {
        handler = h;
    }

    Handler jrhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) { // this handler get its messages from background service,test fragment and self
            switch (msg.what) {
                case 0x003: // get updated team information from test fragments and sends to server // given by test fragments
                    r = (Room) msg.obj;
                    RoomUpdate();
                    sendtoserver();
                    break;
                case 0x005://got room information from server // given by Background Service
                    r = (Room) msg.obj;
                    clearFragments();
                    setRoom((Room) msg.obj);
                    RoomUpdate();
                    if(r.teamnumber()>0)
                    {
                        btn_start.setEnabled(true);
                    }
                    break;
                case 0x999: // if lost connection from server, output message and set buttons to disabled
                    disconnectedUI();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);
        initialize();
    }

    public void setRoom(Room r) {
        final FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for (int i = 0; i < r.teamnumber(); i++) {
            test_fragment ti = new test_fragment();
            ti.setHandler(jrhandler);
            ti.setcontext(ct, this.i.getStringExtra("username"), r);
            ti.join_use = true;
            ti.Teamname = r.getTeam(i).getName();
            transaction.add(R.id.ll_view, ti);
            ti.fragmenthandler.sendMessage(GameUtil.sendMessage(0x001, r)); // set team name
            fragmentlist.add(ti);
        }
        transaction.commit();
    }// actively set teams

    public void clearFragments() {
        final FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for (int i = 0; i < fragmentlist.size(); i++) {
            transaction.remove(fragmentlist.get(i));
        }
        transaction.commit();

    }// remove all fragments from linear layout

    public void SearchRoom(int roomnumber) {
        Player_Info_active p = new Player_Info_active(i.getStringExtra("username"), roomnumber, 0, 0, 0);
        handler.sendMessage(GameUtil.sendMessage(0x005, p)); // send to Background Service
    } // search for a room on the server

    public void sendtoserver() {
        handler.sendMessage(GameUtil.sendMessage(0x004, r));
    }// send join team info to server

    void RoomUpdate() {
        for(int i=0;i<r.teamnumber();i++)
        {
            fragmentlist.get(i).fragmenthandler.sendMessage(GameUtil.sendMessage(0x000,this.r)); // update room in team fragments
            fragmentlist.get(i).fragmenthandler.sendMessage(GameUtil.sendMessage(0x002,this.r.getTeam(i))); // update team in team fragments
        }
    } // update fragments
    void initialize() {
        ll = findViewById(R.id.ll_view);
        btn_start = findViewById(R.id.bjs);
        btn_start.setEnabled(false);
        username = (TextView) findViewById(R.id.u_name1);
        roomnumber = (EditText) findViewById(R.id.roomnumber);
        btn = (Button) findViewById(R.id.search_room);
        ct = this;
        i = getIntent();
        username.setText("user:" + i.getStringExtra("username"));

        //must have code for binding service
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (BackgroundService.MyBinder) service;
                binder.sethandler(jrhandler);
                handler = binder.getServiceHandler();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder.sethandler(null);
            }
        };
        bind_backgroundservice();  // binding background service

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // when clicked on search room button, search for said room on the server
                int roomnum = 0;
                if(!(roomnumber.getText().toString().equals(null)||roomnumber.getText().toString().equals("")))
                {
                    roomnum = Integer.parseInt(roomnumber.getText().toString());
                }
                clearFragments(); // remove all fragments from linear layout
                SearchRoom(roomnum); // search for room with said room number on the server
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // go to game preparation
                if(r==null||r.teamnumber()==0)
                {
                    Toast.makeText(ct,"player hasn't joined any team",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    boolean joined = false;
                    for(int l=0;l<r.teamnumber();l++)
                    {
                        for(int k=0;k<r.getTeam(l).getPlayers().size();k++)
                        {
                            if(r.getTeam(l).getPlayers().get(k).equals( i.getStringExtra("username")))
                            {
                                joined = true;
                                Intent intent1 = new Intent(JoinRoom.this, MainActivity.class);
                                intent1.putExtra("username", i.getStringExtra("username"));
                                intent1.putExtra("team",r.getTeam(l).getName());
                                intent1.putExtra("roomnumber",r.getRoomnumber());
                                startActivity(intent1);
                            }

                        }
                    }
                    if(!joined)
                    {
                        Toast.makeText(ct,"Not joined any teams",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    } // initialize views and paramenters
    void bind_backgroundservice() {
        Intent intent1 = new Intent(this, BackgroundService.class);
        bindService(intent1, conn, 0);
    }// bind background service
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
    } // unbind service on pause
    void disconnectedUI() {
        Toast.makeText(ct,"lost connection",Toast.LENGTH_SHORT).show();
        Intent temp = new Intent(JoinRoom.this,Entrance.class);
        startActivity(temp);
    }// set UI when disconnected to server
}
