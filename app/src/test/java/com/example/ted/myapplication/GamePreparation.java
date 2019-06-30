package com.example.ted.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ted.myapplication.Activities.Entrance;
import com.example.ted.myapplication.Activities.MainActivity;
import com.example.ted.myapplication.Services.BackgroundService;

public class GamePreparation extends AppCompatActivity //1.0 code standard reached. last edited date:2018/3/29
{
    /******************************************************
     this activity is used to prepare the user for the game.
     the activity mainly gets the user's phone direction and calibrates it.
     this activity does not need to connect to the server hence no handler and connection is used
     *****************************************************/

    TextView Rtv;
    EditText currentrotation;
    Button btn;
    SensorManager mSensorManager;
    Sensor mDirection;
    BackgroundService.MyBinder binder;
    ServiceConnection conn;
    Context ct;
    float currentDirection;
    Handler  GamePrepareHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg){ // this handler get its messages from background service
            switch(msg.what)
            {
                case 0x999: // if lost connection from server, output message and set buttons to disabled
                    disconnected();
                    break;
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_preparation);
        initiate();
        //must have code for binding service
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (BackgroundService.MyBinder) service;
                binder.sethandler(GamePrepareHandler);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {binder.sethandler(null);}
        };
        bind_backgroundservice();
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (Math.abs(currentDirection - sensorEvent.values[0]) > 1) // change display if angle movement is larger than 1
                {
                    currentDirection = sensorEvent.values[0];// rotation on z
                    Rtv.setText("当前角度:" + currentDirection);
                }
            } // when phone's direction changes

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, mDirection, SensorManager.SENSOR_DELAY_GAME);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // when user clicks on start game, send the rotation offset to the main activity
                int d = Integer.parseInt(currentrotation.getText().toString());
                float originD = currentDirection;
                originD -= d;
                Intent i = new Intent(GamePreparation.this, MainActivity.class);
                i.putExtra("originDirection", originD);
                i.putExtra("username", getIntent().getStringExtra("username"));
                i.putExtra("team",getIntent().getStringExtra("team"));
                i.putExtra("roomnumber",getIntent().getIntExtra("roomnumber",0));
                startActivityForResult(i, 0);
            }
        });
    }
    void initiate() {
        currentDirection = 0;
        Rtv = (TextView) findViewById(R.id.angle);
        btn = (Button) findViewById(R.id.start_game);
        currentrotation = (EditText) findViewById(R.id.adjust);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mDirection = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        ct = this;
    } //initiate views and parameters

    void bind_backgroundservice() {

        Intent intent1 = new Intent(this, BackgroundService.class);
        bindService(intent1,conn,BIND_AUTO_CREATE);
    }// bind background service
    void disconnected() {
        Toast.makeText(ct,"lost connection",Toast.LENGTH_SHORT).show();
        Intent temp = new Intent(GamePreparation.this,Entrance.class);
        startActivity(temp);
    }//when disconnected from server

}
