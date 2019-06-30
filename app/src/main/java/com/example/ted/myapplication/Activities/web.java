package com.example.ted.myapplication.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ted.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import utility.map_node;
import utility.test_map;

public class web extends AppCompatActivity {
    TextView tv;
    TextView scantimestv;
    TextView direction;
    LinearLayout llview;
    WifiManager wifi_service;
    String s = "";
    test_map tm= new test_map();
    int UNDER_ROUTER = 25;
    int[] level = {5000,5000,5000,5000,5000};
    int[] previouslevel={5000,5000,5000,5000,5000};
    int[] currentlevel={5000,5000,5000,5000,5000};
    // phone direction
    SensorManager mSensorManager;
    Sensor mDirection;
    float currentDirection;

    Handler h = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x000:
                   //s = getMaC();
                  // s = getclosest();
                    tv.setText(getwifilist()); // show wifi rssi readings
                    scantimestv.setText(getPositionBeta()); // show current position
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        tv = (TextView) findViewById(R.id.test_wifi_view);
        llview = (LinearLayout) findViewById(R.id.web_view);
        direction = (TextView) findViewById(R.id.direction_view);
        scantimestv = (TextView) findViewById(R.id.textView_scantimes);
        wifi_service = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        currentDirection = 0;
        // wifi test use
        wifi_service.startScan();
        IntentFilter irf = new IntentFilter();
        irf.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context c, Intent irf) {
                h.sendEmptyMessage(0x000);
                wifi_service.startScan();
            }
        }, irf);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mDirection = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                    currentDirection =  sensorEvent.values[0];// rotation on z
                direction.setText(currentDirection+" ");

            } // when phone's direction changes

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, mDirection, SensorManager.SENSOR_DELAY_FASTEST);



    }


    String getwifilist() {
        String list = "";
        String[] tlist = new String[5];
        List<android.net.wifi.ScanResult> scanResults = null;
        scanResults = wifi_service.getScanResults();//搜索到的设备列表
        for (android.net.wifi.ScanResult scanResult : scanResults) {
            String s1 = scanResult.SSID;
            if (s1.contains("test_wifi")) {
                String[] temp = s1.split("_");
                int n = Integer.parseInt(temp[2]);
                switch (n) {
                    case 1:
                        list = "";
                        list += "router1 (A):";
                        list += (-1) * scanResult.level;
                        level[0] = (-1) * scanResult.level;
                        tlist[0] = list;

                        break;
                    case 2:
                        list = "";
                        list += "router2 (C):";
                        list += (-1) * scanResult.level;
                        level[1] = (-1) * scanResult.level;
                        tlist[1] = list;
                        break;
                    case 3:
                        list = "";
                        list += "router3 (I):";
                        list += (-1) * scanResult.level;
                        level[2] = (-1) * scanResult.level;
                        tlist[2] = list;
                        break;
                    case 4:
                        list = "";
                        list += "router4 (G):";
                        list += (-1) * scanResult.level;
                        level[3] = (-1) * scanResult.level;
                        tlist[3] = list;
                        break;
                }

            }
             if(!getRouterName(scanResult.BSSID).equals("no router found"))
            {
                list = "";
                if(getRouterName(scanResult.BSSID).equals("E"))
                {
                    list += "router ("+getRouterName(scanResult.BSSID)+"):";
                    list += (-1) * scanResult.level;
                    level[4] = (-1) * scanResult.level;
                }
                tlist[4] = list;
            }
        }
        list = "";
        for (int i = 0; i < 5; i++) {
            list += tlist[i] + "\n";
            currentlevel[i] = level[i];
        }

        return list;
    }


    String getPositionBeta()
    {

        String[] router = {"A","C","I","G","E"};

        // sort list by rssi
        ArrayList<String[]> hm = new ArrayList<String[]>();
        for(int i=0;i<4;i++) // sort level array, smallest first
        {
            int smallest = level[i];
            int sn = i;

            for(int j=i+1;j<4;j++)
            {
                if(level[j]<smallest)
                {
                    smallest = level[j];
                    sn = j;
                }
            }
            hm.add(new String[]{router[sn],level[sn]+""});

            if(sn!=i)
            {
                int tmp = level[i];
                level[i] = level[sn];
                level[sn] = tmp;

                String stmp = router[i];
                router[i] = router[sn];
                router[sn] = stmp;
            }
        }

         //2. process the signal and cut down extra distance due to phone facing

        String closest1 = hm.get(0)[0];
        String closest2 = hm.get(1)[0];

        if(is_cut(closest1,closest2))//if the phone's direction is in cut with routers,go to normal procedure below
        {

          // do nothing;
        }
        else // if the phone is facing towards a certain router
        {
            for(int i=0;i<4;i++) // shave 10 down the rssi value
            {
                if(router[i].equals(getopposed(closest1)))
                {
                    level[i]-=10;
                }
            }
            hm = new ArrayList<>();
            for(int i=0;i<4;i++) // sort level array again, smallest first
            {
                int smallest = level[i];
                int sn = i;

                for(int j=i+1;j<4;j++)
                {
                    if(level[j]<smallest)
                    {
                        smallest = level[j];
                        sn = j;
                    }
                }
                hm.add(new String[]{router[sn],level[sn]+""});

                if(sn!=i)
                {
                    int tmp = level[i];
                    level[i] = level[sn];
                    level[sn] = tmp;

                    String stmp = router[i];
                    router[i] = router[sn];
                    router[sn] = stmp;
                }
            }
            closest1 = hm.get(0)[0];
            closest2 = hm.get(1)[0];

        }

        //3. if the user gets a very strong and intense signal after processing
        for(int i=0;i<level.length;i++)
        {
            if(level[i]<=UNDER_ROUTER)
            {
                tm.current = tm.searchnode(router[i]);
                return "under "+tm.current.getNodename()+" ,coordinates: ("+tm.current.cor.getX()+", "+tm.current.cor.getY()+")";
            }
        }

        if(process_pc()==true) // if readings has stablized (previous readings and current readings has stablized)
        {
            String position="";
            //6. get block inbetween the two routers
            map_node tempnode = tm.getinbetween(closest1,closest2);
            if(tempnode!=null)
            {
                tm.current = tempnode;

                for(int i=0;i<currentlevel.length;i++)
                {
                    previouslevel[i] = currentlevel[i];
                }

                return "under"+tm.current.getNodename()+" ,coordinates: ("+tm.current.cor.getX()+", "+tm.current.cor.getY()+")";

            }


        }

        else // readings hasn't stablized(previous and current is still too much apart)
        {
            for(int i=0;i<currentlevel.length;i++)
            {
                previouslevel[i] = currentlevel[i];
            }
            return "not stablized, still under last"+tm.current.getNodename()+" ,coordinates: ("+tm.current.cor.getX()+", "+tm.current.cor.getY()+")";

        }

        return "not under any routers";


    }


    boolean process_pc() // process the signal and cut down extra distance due to phone facing
    {
       boolean stable = true;
       for(int i=0;i<currentlevel.length;i++)
       {
           if(Math.abs(currentlevel[i]-previouslevel[i])>4)
           {
               stable = false;
           }
       }
       return stable;
    }



    String getRouterName(String Mac)
    {
        String name = "no router found";
        if(Mac.equals("b0:f9:63:19:9e:10"))
        {
            name = "A";
        }
        else if(Mac.equals("70:ba:ef:d3:9c:d0"))
        {
            name = "B";
        }
        else if(Mac.equals("74:1f:4a:cb:21:70"))
        {
            name = "C";
        }
        else if(Mac.equals("88:25:93:3a:c2:08"))
        {
            name = "D";
        }
        else if(Mac.equals("b0:f9:63:19:21:30"))
        {
            name = "E";
        }
        return name;

    }
    String getopposed(String r1)
    {
        if(convertdirection()==1)
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
        if(convertdirection()==2)
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
        if(convertdirection()==0)
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
        if(convertdirection()==3)
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
    public boolean is_cut(String r1,String r2) {
        if (((r1.equals("G")&&r2.equals("I"))||(r1.equals("I")&&r2.equals("G")))&&(convertdirection()==0||convertdirection()==1))
        {
            return true;
        }
        if (((r1.equals("A")&&r2.equals("C"))||(r1.equals("C")&&r2.equals("A")))&&(convertdirection()==0||convertdirection()==1))
        {
            return true;
        }
        if (((r1.equals("A")&&r2.equals("G"))||(r1.equals("G")&&r2.equals("A")))&&(convertdirection()==2||convertdirection()==3))
        {
            return true;
        }
        if (((r1.equals("I")&&r2.equals("C"))||(r1.equals("C")&&r2.equals("I")))&&(convertdirection()==2||convertdirection()==3))
        {
            return true;
        }
        return false;
    }
    public int convertdirection()
    {
        if((currentDirection>=0&&currentDirection<=45)||(currentDirection>315&&currentDirection<=360))
        {
          return 0;
        }
        if((currentDirection>45&&currentDirection<=135))
        {
          return 3;
        }
        if((currentDirection>135&&currentDirection<=225))
        {
           return 1;
        }
        if((currentDirection>225&&currentDirection<=315))
        {
          return 2;
        }
        return -1;
    }

  /*
    String getclosest()
    {
        String result = "";
        List<android.net.wifi.ScanResult> scanResults = null;
        scanResults = wifi_service.getScanResults();//搜索到的设备列表
        for (ScanResult scanResult : scanResults) {
            String s = scanResult.SSID;
            int l = (-1) * scanResult.level;
            if(l<80)
            {
                result+=scanResult.SSID+" "+l+" "+scanResult.BSSID+"\n";
            }
        }
        return result;
    }

    int obtainWifiInfo() {

        int strength = 0;
        WifiInfo info = wifi_service.getConnectionInfo();
        if (info.getBSSID() != null) {
            // 链接信号强度，5为获取的信号强度值在5以内
            //strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            // 链接速度
            int speed = info.getLinkSpeed();
            strength = info.getRssi() * (-1);
            // 链接速度单位
            String units = WifiInfo.LINK_SPEED_UNITS;
            // Wifi源名称
            String ssid = info.getSSID();
        }
//        return info.toString();
        return strength;
    } // get wifi speed and connection
    */



}

