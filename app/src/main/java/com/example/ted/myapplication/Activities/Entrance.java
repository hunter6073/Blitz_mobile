package com.example.ted.myapplication.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ted.myapplication.R;
import com.example.ted.myapplication.Services.BackgroundService;

public class Entrance extends AppCompatActivity{

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This activity is the first activity users will see, it starts
     * Background Service and binds it. This activity tells the user whether they
     * have connected to the server,and gives the user access to further activities
     * such as create room and join room.
     *****************************************************/

    Button btn_createRoom; // click this button to create a room
    Button btn_joinRoom; // click this button to join a room
    EditText username; // plain text for inputing username
    Intent i; // intent for accessing other activities
    Context ct; // current context for using toast
    String user_n = ""; // username
    // sharedrpeferences components for remembering username
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    //service components below
    BackgroundService.MyBinder binder;
    ServiceConnection conn;

    Handler  EntranceHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg){ // this handler get its messages from background service
            switch(msg.what)
            {
                case 0x000: // if get connection from server, output message and set buttons to enabled
                   connectedUI();
                    break;
                case 0x999: // if lost connection from server, output message and set buttons to disabled
                   disconnectedUI();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initiate views -> bind and start background service
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        initialize(); //initiate views
        start_backgroundservice();  // starting background service
    }

    @Override
    protected void onResume()// bind service here
    {
        super.onResume();
        start_backgroundservice();
    }
    void initialize() //initialize paramaters and views, set connection for service and listeners for buttons
    {
        btn_createRoom = (Button) findViewById(R.id.create_room);
        btn_joinRoom = (Button) findViewById(R.id.join_room);
        username = (EditText) findViewById(R.id.username);
        preferences = getSharedPreferences("Blitz",MODE_PRIVATE);
        editor = preferences.edit();
        ct = this;
        if(preferences.contains("username"))
        {
            username.setText(preferences.getString("username","usname"));
        }

        //must have code for binding service
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (BackgroundService.MyBinder) service;
                binder.sethandler(EntranceHandler);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {binder.sethandler(null);}
        };

        //get into createroom activity
        btn_createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterRoom(CreateRoom.class);
            }
        });
        //get into joinroom activity
        btn_joinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterRoom(JoinRoom.class);
            }
        });

    }
    void EnterRoom(Class t) // things to do when clicking on buttons for joining rooms or creating rooms
    {
        user_n = username.getText().toString();
        if(!(user_n.equals("")||user_n.equals(null)))
        {
            editor.putString("username", user_n); // remember the user's username
            editor.commit();
            i = new Intent(Entrance.this,t); // jump to according activities
            i.putExtra("username",user_n);
            startActivity(i);
        }
        else
        {
            Toast.makeText(ct,"please input username",Toast.LENGTH_SHORT).show();
        }

    }
    void start_backgroundservice()// start background service
    {
        Intent intent1 = new Intent(this, BackgroundService.class);
        bindService(intent1,conn,BIND_AUTO_CREATE);
    }
    void connectedUI() // set UI when connected to server
    {
        if(btn_joinRoom.isEnabled()==false || btn_createRoom.isEnabled()==false)
        {
            btn_createRoom.setEnabled(true);
            btn_joinRoom.setEnabled(true);
            Toast.makeText(ct,"connected to server", Toast.LENGTH_SHORT).show();
        }
    }
    void disconnectedUI()// set UI when disconnected to server
    {
        if(btn_joinRoom.isEnabled()==true || btn_createRoom.isEnabled()==true)
        {
            btn_createRoom.setEnabled(false);
            btn_joinRoom.setEnabled(false);
            Toast.makeText(ct,"disconnected", Toast.LENGTH_SHORT).show();
        }
    }

}
