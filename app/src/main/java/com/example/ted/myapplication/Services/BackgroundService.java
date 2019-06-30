package com.example.ted.myapplication.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import utility.GameUtil;

import server_connections.Player_Info_active;
import server_connections.ClientThread;

public class BackgroundService extends Service {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This service starts client thread and handles the connection between activities and client thread.
     This service is created in the Entrance activity and is binded in all activities.(binded when onresume and unbinded when said activity pauses)
     All activities sends their network request through this service and this service forwards them to the client thread, and vice versa.
     *****************************************************/

    ClientThread ct; // client thread

    // service required components
    private MyBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        Handler thandler = null; // handler for the activity using Background Service

        public void sethandler(Handler h) {
            thandler = h;
        }

        public Handler getServiceHandler() {
            return ownHandler;
        }
    }

    Handler ownHandler = new Handler() // handler to deal with different requirements
    {
        @Override
        public void handleMessage(Message msg) { // own handler for reciving messages from other activities
            switch (msg.what) {
                case 0x001: // for testing connections  /// given by client thread to Entrance Activity
                    sendActivityMessage(0x000);
                    break;
                case 0x002: // for creating a new room  /// given by CreateRoom Activity
                    Player_Info_active p = new Player_Info_active((String) msg.obj, 0, 0, 0, 0);
                    sendClientMessage(0x458, p);//send to clientthread for further processing
                    break;
                case 0x003: // for showing current room number /// given by client thread to create room activity
                    sendActivityMessage(0x001, msg.obj);
                    break;
                case 0x004: // send updated room message to server /// given by joinroom to client thread
                    sendClientMessage(0x459, msg.obj);
                    break;
                case 0x005: // send room number to server for room search /// given by joinroom to client thread
                    sendClientMessage(0x460, msg.obj);
                    break;
                case 0x006: // get updated room information from Server ///given by client thread to createroom and joinroom
                    sendActivityMessage(0x005, msg.obj);
                    break;
                case 0x007: // send user position and direction info to client thread /// given by main activity to client thread
                    sendClientMessage(0x456, msg.obj);
                    break;
                case 0x008: // send shoot success response to main activity /// given by client thread to main activity
                    sendActivityMessage(0x001, msg.obj);
                case 0x009: // send got hit response to main activity /// given by client thread to main activity
                    sendActivityMessage(0x002, msg.obj);
                    break;
                case 0x010: // send game over to main activity /// given by client thread to main activity
                    sendActivityMessage(0x004, msg.obj);
                    break;
                case 0x011: // send user killed response to client thread/// given by main activity to client thread
                    sendClientMessage(0x462, msg.obj);
                    break;
                case 0x012: // send user shooting info to client thread /// given by main activity to client thread
                    sendClientMessage(0x463, msg.obj);
                    break;
                case 0x013: // send enemy list to client /// given by client thread to main activity
                    sendActivityMessage(0x003, msg.obj);
                    break;
                case 0x999:// if connection crashes
                    sendActivityMessage(0x999);
                    break;


            }


        }
    };

    public BackgroundService() {
        ct = new ClientThread(ownHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //constructor -> oncreate ->on bind
        return binder;
    }

    @Override
    public void onCreate() //start client thread
    {
        super.onCreate();
        new Thread(ct).start();
    }

    private void sendActivityMessage(int what, Object obj)// send to whichever activity is using this service
    {
        binder.thandler.sendMessage(GameUtil.sendMessage(what, obj));
    }

    private void sendActivityMessage(int what)// send to whichever activity is using this service
    {
        binder.thandler.sendEmptyMessage(what);
    }

    private void sendClientMessage(int what, Object obj) // send to client thread
    {
        ct.revHandler.sendMessage(GameUtil.sendMessage(what, obj));
    }


}
