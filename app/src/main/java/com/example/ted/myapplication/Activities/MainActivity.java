package com.example.ted.myapplication.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ted.myapplication.R;
import com.example.ted.myapplication.Services.BackgroundService;

import utility.GameUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import server_connections.Player_Info_active;
import utility.map_node;
import utility.test_map;

import com.example.ted.myapplication.view_new.Cross_hair_View;
import com.example.ted.myapplication.view_new.MyRenderer;

import static android.content.ContentValues.TAG;


public class MainActivity extends Activity implements SensorEventListener {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This is the main activity that the user uses to play the game
     * This activity mainly uses the camera thread to get the visual stream
     * and uses other sensors to get get the essential information for the game
     *****************************************************/

    //camera
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice.StateCallback mStateCallback;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mTextureListener;
    private String mCameraId;
    private Handler mCameraHandler;
    private Size mPreviewSize;
    private HandlerThread mCameraThread;
    private Cross_hair_View ch;
    BackgroundService.MyBinder binder;
    ServiceConnection conn;
    Context ct;
    // textview UIs
    TextView healthview;
    TextView bulletview;
    TextView teamview;
    TextView enemyview;
    TextView username;
    TextView wifi_level;
    TextView current_rot;
    TextView current_cor;
    // accelerometer
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor Orientation;
    //wifi test;
    Timer timer;
    Button btn_test;
    MyRenderer renderer;
    boolean locked = false;

    Intent intent; // get data from precursor activity
    GameUtil GU;
    // self-created cross hair view
    GLSurfaceView glView;
    boolean calibrated = false;
    //player position and rotation
    float original_rotation;
    float current_rotation;
    test_map tm = new test_map();
    WifiManager wifi_service;
    //game mechanics
    int bullets; // bullets left
    int lives; // lives left
    String team; // team player is on

    GameUtil utility = new GameUtil();
    //data transfer mechanics
    int UNDER_ROUTER = 25;
    int[] level = {5000, 5000, 5000, 5000, 5000};
    int[] previouslevel = {5000, 5000, 5000, 5000, 5000};
    int[] currentlevel = {5000, 5000, 5000, 5000, 5000};
    Handler Bhandler = null; // background service handler
    Handler h = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x000: // get positioning info
                    String s = getwifilist();
                    wifi_level.setText(s);
                    if (!locked) {
                        current_cor.setText(getPositionBeta());
                    }
                    break;
                case 0x001:
                    ch.shoot_red();
                    ch.invalidate();
                    if (bullets > 0) {
                        bullets--;
                        bulletview.setText(GU.setss(MainActivity.this, R.drawable.bullet, "子弹：1X" + bullets, 3, 4));
                    } else // refill bullets
                    {
                        bullets = 10;
                        bulletview.setText(GU.setss(MainActivity.this, R.drawable.bullet, "子弹：1X" + bullets, 3, 4));
                    }
                    Toast.makeText(ct, "successfully hit player " + (String) msg.obj, Toast.LENGTH_SHORT).show();
                    Bhandler.sendMessage(GameUtil.sendMessage(0x012, new Player_Info_active((String) msg.obj, getIntent().getIntExtra("roomnumber", 0), 0, 0, 0)));
                    break;
                case 0x002:
                    // received hit
                    lives--;
                    if (lives > 0) {

                        healthview.setText(GU.setss(MainActivity.this, R.drawable.heart, "生命：1X" + lives, 3, 4));
                        Toast.makeText(ct, "got hit by " + msg.obj, Toast.LENGTH_SHORT).show();
                    } else {
                        username.setText(username.getText() + " dead");
                        Player_Info_active p = new Player_Info_active(intent.getStringExtra("username"), getIntent().getIntExtra("roomnumber", 0), tm.current.cor.getX(), tm.current.cor.getY(), current_rotation);
                        Bhandler.sendMessage(GameUtil.sendMessage(0x011, p));
                        Intent temp = new Intent(MainActivity.this, EndGameActivity.class);
                        temp.putExtra("lost", (String) msg.obj);
                        startActivity(temp);

                    }
                    break;
                case 0x003: // get enemy list
                    LinkedList<Player_Info_active> enemylist = GameUtil.unpacklist((String) msg.obj, getIntent().getIntExtra("roomnumber", 0)); // enemy list
                    renderer.h.sendMessage(GameUtil.sendMessage(0x001, enemylist));
                    enemyview.setText(GameUtil.listtostring(enemylist, new Player_Info_active(getIntent().getStringExtra("username"),getIntent().getIntExtra("roomnumber",0),tm.current.cor.getX(),tm.current.cor.getY(),current_rotation)));
                    break;
                case 0x004:
                    //game over
                    EndGame((String) msg.obj);
                    break;
                case 0x005://reload complete
                    ch.shoot_green();
                    ch.invalidate();
                    break;
                case 0x999:
                    disconnectedUI();
                    break;

            }

        }

    }; // own handler for receiving messages from background service


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_game);
        initialize();

        //camera stuff
        mTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //当SurefaceTexture可用的时候，设置相机参数并打开相机
                setupCamera(width, height);
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                configureTransform(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        }; // open the camera when texture is usable
        mStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice = camera;
                startPreview();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                camera.close();
                mCameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                camera.close();
                mCameraDevice = null;
            }
        }; // when camera is opened, set camera device and start preview function

    }

    @Override
    protected void onResume() {
        super.onResume();
        start_backgroundservice();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST); // register sensor for accelorometer
        mSensorManager.registerListener(this, Orientation, SensorManager.SENSOR_DELAY_FASTEST); // register sensor for accelorometer
        startCameraThread();
        if (!mTextureView.isAvailable()) {
            mTextureView.setSurfaceTextureListener(mTextureListener);
        } else {
            startPreview();
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() { // update current position and direction to the renderer
                while (true) {
                    if (calibrated) {
                        Player_Info_active p = new Player_Info_active(getIntent().getStringExtra("username"), getIntent().getIntExtra("roomnumber", 0), tm.current.cor.getX(), tm.current.cor.getY(), current_rotation);
                        renderer.h.sendMessage(GameUtil.sendMessage(0x000, p));
                        glView.requestRender();// refresh every frame(used to get the ondrawframe function)

                        if (Bhandler != null) {
                            upload_DP();
                        }
                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }

            }

        };
        new Thread(runnable).start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        stopCameraThread();
        unbindService(conn);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    } // useless code, needed for sensors

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensortype = sensorEvent.sensor.getType();
        if (sensortype == Sensor.TYPE_ORIENTATION) {
            if (Math.abs(current_rotation - sensorEvent.values[0]) > 0.5) // change display if angle movement is larger than 0.5
            {

                current_rotation = sensorEvent.values[0];// rotation on z
                if (!calibrated) {
                    current_rot.setText(current_rotation + "");
                } else {
                    current_rotation = (original_rotation - current_rotation);
                    if (current_rotation < 0) {
                        current_rotation *= -1;
                    } else {
                        current_rotation = 360 - current_rotation;
                    }
                    current_rot.setText(current_rotation + " ");

                }


            }
        }
        if (sensortype == Sensor.TYPE_ACCELEROMETER) {

            //  test_updown.setText(sensorEvent.values[1]+"");// up/down rotation, this is reserved for later use

        }


    } // get current phone rotation

    void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

    } // start camera thread

    void stopCameraThread() {
        mCameraThread.quitSafely();
        try {
            mCameraThread.join();
            mCameraThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    } // stop camera thread

    void setupCamera(int width, int height) {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //遍历所有摄像头
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //此处默认打开后置摄像头
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                //根据TextureView的尺寸设置预览尺寸
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                //此ImageReader用于拍照所需。2代表ImageReader中最多可以获取两帧图像流
                mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 2);
                mCameraId = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    void openCamera() {

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    } //ask for system permission to open the camera

    void startPreview() {
        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();
        //give data buffer to the texture view , can be changed for better performances
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(mSurfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            mCaptureRequestBuilder.addTarget(previewSurface);
            //crop out the regions  you want,,very important code;


            //  Rect zoom = new Rect(0,0,t,t*(mPreviewSize.getHeight()/mPreviewSize.getWidth()));
            //   mCaptureRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {

                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    } //set camerapreview onto texture surface

    Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    public void upload_DP() {
        Player_Info_active p = new Player_Info_active(intent.getStringExtra("username"), getIntent().getIntExtra("roomnumber", 0), tm.current.cor.getX(), tm.current.cor.getY(), current_rotation);
        Bhandler.sendMessage(GameUtil.sendMessage(0x007, p));

    } // update user direction and position to server

    void start_backgroundservice() {
        Intent intent1 = new Intent(this, BackgroundService.class);
        bindService(intent1, conn, BIND_AUTO_CREATE);
    } // start background service

    void initialize() {
        GU = new GameUtil();
        healthview = (TextView) findViewById(R.id.healthview);
        bulletview = (TextView) findViewById(R.id.bullet_view);
        teamview = (TextView) findViewById(R.id.team_view);
        enemyview = (TextView) findViewById(R.id.enemy_left);
        username = (TextView) findViewById(R.id.user);
        wifi_level = (TextView) findViewById(R.id.up_down);
        current_cor = findViewById(R.id.current_coordinates);
        current_rot = findViewById(R.id.rotation_view);
        ch = (Cross_hair_View) findViewById(R.id.cross_hair_View);
        mTextureView = (TextureView) findViewById(R.id.camera_view);
        healthview.setText(GU.setss(MainActivity.this, R.drawable.heart, "生命：1X5", 3, 4));
        bulletview.setText(GU.setss(MainActivity.this, R.drawable.bullet, "子弹：1X10", 3, 4));
        enemyview.setText("剩余敌人：");
        btn_test = findViewById(R.id.button_testing);

        //initialize parameters
        ct = this;
        intent = getIntent();
        original_rotation = intent.getFloatExtra("originDirection", 0);
        team = intent.getStringExtra("team");
        teamview.setText("team:" + team);
        username.setText("player:" + intent.getStringExtra("username"));
        bullets = 10;
        lives = 5;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //opengl
        glView = findViewById(R.id.glv_main1);
        renderer = new MyRenderer();
        renderer.setActivityhandler(h);
        glView.setZOrderOnTop(true);
        glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glView.setRenderer(renderer);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        ch = findViewById(R.id.cross_hair_View);
        ch.bringToFront();

        //must have code for binding service
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (BackgroundService.MyBinder) service;
                binder.sethandler(h);
                Bhandler = binder.getServiceHandler();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder.sethandler(null);
            }
        };
        wifi_service = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifi_service.startScan();
        IntentFilter irf = new IntentFilter();
        irf.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context c, Intent irf) {
                h.sendEmptyMessage(0x000);
                wifi_service.startScan();
            }
        }, irf);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!calibrated)
                {
                    original_rotation = current_rotation;
                    calibrated = true;
                    btn_test.setText("unlocked");
                }
                else
                {
                    locked = !locked;
                    String stemp ="";
                    if(locked)
                    {
                     stemp = "locked";
                    }
                    else
                    {
                        stemp = "unlocked";
                    }
                    btn_test.setText(stemp);
                }


            }
        });


    } // initialize views and parameters





/*
    void write(String content) {
        String filename = this.getFilesDir().getPath().toString() + "/Blitz_wifilog.txt";
        try {
            File f = new File(filename);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(f, true);
            PrintStream ps = new PrintStream(fos);
            ps.println(content);
            ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String read() {
        try {

            String filename = this.getFilesDir().getPath().toString() + "/Blitz_wifilog.txt";
            File f = new File(filename);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileInputStream fis = new FileInputStream(f);
            byte[] buff = new byte[1024];
            int hasRead = 0;
            StringBuilder sb = new StringBuilder("");
            while ((hasRead = fis.read(buff)) > 0) {
                sb.append(new String(buff, 0, hasRead));
            }
            fis.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
*/

    void disconnectedUI() {
        Toast.makeText(ct, "lost connection", Toast.LENGTH_SHORT).show();
        Intent temp = new Intent(MainActivity.this, Entrance.class);
        startActivity(temp);
    }// set UI when disconnected to server

    void EndGame(String winTeam) {
        Intent temp = new Intent(MainActivity.this, EndGameActivity.class);
        temp.putExtra("winner", winTeam);
        temp.putExtra("lost","null");
        startActivity(temp);
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
            if(!utility.getRouterName(scanResult.BSSID).equals("no router found"))
            {
                list = "";
                if(utility.getRouterName(scanResult.BSSID).equals("E"))
                {
                    list += "router ("+utility.getRouterName(scanResult.BSSID)+"):";
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


    String getPositionBeta() {

        String[] router = {"A", "C", "I", "G", "E"};

        // sort list by rssi
        ArrayList<String[]> hm = new ArrayList<String[]>();
        for (int i = 0; i < 4; i++) // sort level array, smallest first
        {
            int smallest = level[i];
            int sn = i;

            for (int j = i + 1; j < 4; j++) {
                if (level[j] < smallest) {
                    smallest = level[j];
                    sn = j;
                }
            }
            hm.add(new String[]{router[sn], level[sn] + ""});

            if (sn != i) {
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

        if (utility.is_cut(closest1, closest2, current_rotation))//if the phone's direction is in cut with routers,go to normal procedure below
        {

            // do nothing;
        } else // if the phone is facing towards a certain router
        {
            for (int i = 0; i < 4; i++) // shave 10 down the rssi value
            {
                if (router[i].equals(utility.getopposed(closest1, current_rotation))) {
                    level[i] -= 10;
                }
            }
            hm = new ArrayList<>();
            for (int i = 0; i < 4; i++) // sort level array again, smallest first
            {
                int smallest = level[i];
                int sn = i;

                for (int j = i + 1; j < 4; j++) {
                    if (level[j] < smallest) {
                        smallest = level[j];
                        sn = j;
                    }
                }
                hm.add(new String[]{router[sn], level[sn] + ""});

                if (sn != i) {
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
        for (int i = 0; i < level.length; i++) {
            if (level[i] <= UNDER_ROUTER) {
                tm.current = tm.searchnode(router[i]);
                return "under " + tm.current.getNodename() + " ,coordinates: (" + tm.current.cor.getX() + ", " + tm.current.cor.getY() + ")";
            }
        }

        if (process_pc() == true) // if readings has stablized (previous readings and current readings has stablized)
        {
            String position = "";
            //6. get block inbetween the two routers
            map_node tempnode = tm.getinbetween(closest1, closest2);
            if (tempnode != null) {
                tm.current = tempnode;

                for (int i = 0; i < currentlevel.length; i++) {
                    previouslevel[i] = currentlevel[i];
                }

                return "under" + tm.current.getNodename() + " ,coordinates: (" + tm.current.cor.getX() + ", " + tm.current.cor.getY() + ")";

            }


        } else // readings hasn't stablized(previous and current is still too much apart)
        {
            for (int i = 0; i < currentlevel.length; i++) {
                previouslevel[i] = currentlevel[i];
            }
            return "not stablized, still under last" + tm.current.getNodename() + " ,coordinates: (" + tm.current.cor.getX() + ", " + tm.current.cor.getY() + ")";
        }
        return "not under any routers";
    }

    boolean process_pc() // process the signal and cut down extra distance due to phone facing
    {
        boolean stable = true;
        for (int i = 0; i < currentlevel.length; i++) {
            if (Math.abs(currentlevel[i] - previouslevel[i]) > 4) {
                stable = false;
            }
        }
        return stable;
    }


}
