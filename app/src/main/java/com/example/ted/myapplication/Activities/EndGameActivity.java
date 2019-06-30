package com.example.ted.myapplication.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.ted.myapplication.R;

public class EndGameActivity extends AppCompatActivity {
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);
        tv = (TextView) findViewById(R.id.Winners);
        if(!getIntent().getStringExtra("lost").equals("null"))
        {
            tv.setText("you have been killed by "+getIntent().getStringExtra("lost"));
        }
        else
        {
            tv.setText("winning team: "+getIntent().getStringExtra("winner"));
        }

    }
}
