package server_connections;


import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import utility.GameUtil;

import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static android.content.ContentValues.TAG;

/**
 * Created by TED on 2017/9/30.
 */

public class ClientThread implements Runnable {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This is the thread that sends and receives messages to and from the server
     * This is the most important class besides activities, so take care when doing code maintenance
     *****************************************************/

    //parameters
    Socket s = null;
    boolean connected = false;
    boolean thread_start = false;
    BufferedReader br = null;
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;

    public enum MessageType {LOGIN, SHOOTING, CREATEROOM, SEARCHROOM, UPDATE_DP, CHECKCONNECTION, UPDATEROOM, DEAD}

    ;
    public Handler handler = null; // send feedbacks to the player

    // all messages are gotten from and being sent to background service
    public Handler revHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) { // gets shoot permission from game and send userinfo to server
            ///send user info
            switch (msg.what) {
                case 0x456: // send shooting message to server
                    send((Player_Info_active) msg.obj, MessageType.UPDATE_DP);
                    break;
                case 0x458: // send create room message to server
                    send((Player_Info_active) msg.obj, MessageType.CREATEROOM);
                    break;
                case 0x459: // send updated room message to server
                    if (msg.obj != null) {
                        send((Room) msg.obj, MessageType.UPDATEROOM);
                    }
                    break;
                case 0x460: // search for created room on server
                    send((Player_Info_active) msg.obj, MessageType.SEARCHROOM);
                    break;
                case 0x462:
                    send((Player_Info_active) msg.obj, MessageType.DEAD);
                    break;
                case 0x463:
                    send((Player_Info_active)msg.obj,MessageType.SHOOTING);
                    break;
                //add in here for sending to server
            }


        }
    };


    public ClientThread(Handler h) {
        super();
        handler = h;
    } //initialize feedback handler

    public void sethandler(Handler h) {
        handler = h;
    }

    private void send(final Object obj, final MessageType mt) //send message to server
    {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... params) {
                write(obj, mt);
                return null;
            }

        }.execute();
    }

    private void write(Object obj, final MessageType mt) // send message to server according to message type
    {
        Object o = obj;
        String ms = "";

        switch (mt) {
            case LOGIN:
                ms = "login";
                break;
            case UPDATE_DP:
                ms = "update_DP";
                break;
            case CREATEROOM:
                ms = "create_room";
                break;
            case SEARCHROOM:
                ms = "search_room";
                break;
            case CHECKCONNECTION:
                ms = "check_connection";
                break;
            case UPDATEROOM:
                ms = "update_room";
                Room r1 = new Room((Room) obj);
                o = r1;
                break;
            case DEAD:
                ms = "user_killed";
                break;
            case SHOOTING:
                ms = "shoot_success";
                break;
                /*
            case 6:
                break;
            case 7:
                break;
                */
        }
        try {
            Transmit t = new Transmit(ms, o);
            if (connected) {
                oos.writeObject(t);
                oos.flush();

            }
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
            thread_start = false;
            handler.sendEmptyMessage(0x999);
        }
    }


    @Override
    public void run() {

        while (true) {
            try {
                if ((connected == false) && (thread_start == false)) {
                    thread_start = true;
                    // get input from server
                    while (true) {
                        try {
                            while (s == null || connected == false) {
                               // s = new Socket("192.168.199.170", 30000);
                                s = new Socket("172.17.66.230",30000);
                                connected = true;
                            }

                            handler.sendEmptyMessage(0x001);
                            if (oos == null) {
                                oos = new ObjectOutputStream(s.getOutputStream());
                            }
                            if (ois == null) {
                                ois = new ObjectInputStream(s.getInputStream());
                            }

                            Object obj = ois.readObject();
                            Transmit tran = (Transmit) obj;
                            if (tran.type.equals("successful_hit")) {
                                handler.sendMessage(GameUtil.sendMessage(0x008, tran.obj)); // send to BackgroundService
                            } else if (tran.type.equals("got_hit")) {
                                handler.sendMessage(GameUtil.sendMessage(0x009, tran.obj)); // send to BackgroundService
                            } else if (tran.type.equals("game_over")) {
                                handler.sendMessage(GameUtil.sendMessage(0x010, tran.obj)); // send to BackgroundService
                            } else if (tran.type.equals("roomnumber")) {
                                handler.sendMessage(GameUtil.sendMessage(0x003, tran.obj));
                            } else if (tran.type.equals("room")) {
                                handler.sendMessage(GameUtil.sendMessage(0x006, tran.obj)); // send to BackgroundService
                            } else if (tran.type.equals("connection_fine")) {
                                handler.sendEmptyMessage(0x001);
                            } else if (tran.type.equals("enemy_list")) {
                                handler.sendMessage(GameUtil.sendMessage(0x013,tran.obj));
                            }
                            // add in here for server input messages

                        } catch (Exception e) {
                            e.printStackTrace();
                            oos = null;
                            ois = null;
                            handler.sendEmptyMessage(0x999);
                            connected = false;
                            thread_start = false;
                        }
                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

}
