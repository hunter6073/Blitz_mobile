package com.example.ted.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Activity_Gps extends AppCompatActivity {
    TextView textView;
    LocationManager locationManager;
    Context c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        c = getApplicationContext();
      //  textView = (TextView) findViewById(R.id.textView2);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        Criteria cri = new Criteria();
        cri.setPowerRequirement(Criteria.POWER_LOW);//设置低耗电
        cri.setAltitudeRequired(true);//设置需要海拔
        cri.setBearingAccuracy(Criteria.ACCURACY_COARSE);//设置COARSE精度标准
        cri.setAccuracy(Criteria.ACCURACY_LOW);//设置低精度

;
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(cri,true));
        updateView(location);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 8, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateView(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

                if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //请求权限
                    ActivityCompat.requestPermissions(Activity_Gps.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                updateView(locationManager.getLastKnownLocation(s));
            }

            @Override
            public void onProviderDisabled(String s) {
                updateView(null);
            }
        });

    }
    public void updateView(Location newLocation)
    {
        if(newLocation!=null)
        {
            String s="";
            s+="longitude:"+newLocation.getLongitude()+"\n";
            s+="latitude:"+newLocation.getLatitude()+"\n";
            s+="altitude:"+newLocation.getAltitude()+"\n";
            s+="speed:"+newLocation.getSpeed()+"\n";
            s+="bearing:"+newLocation.getBearing()+"\n";
            textView.setText(s);
        }


    }
    public void sendMessage(View v)
    {
        Criteria cri = new Criteria();
        cri.setPowerRequirement(Criteria.POWER_LOW);//设置低耗电
        cri.setAltitudeRequired(true);//设置需要海拔
        cri.setBearingAccuracy(Criteria.ACCURACY_COARSE);//设置COARSE精度标准
        cri.setAccuracy(Criteria.ACCURACY_LOW);//设置低精
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(Activity_Gps.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(cri,true));
        updateView(location);
    }
}
