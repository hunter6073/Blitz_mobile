package com.example.ted.myapplication.view_new;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import server_connections.ClientThread;
import server_connections.Player_Info_active;
import utility.GameUtil;

import static android.content.ContentValues.TAG;

/**
 * Created by TED on 2018/4/1.
 */

public class MyRenderer implements GLSurfaceView.Renderer { //2.0 code standard reached. last edited date:2018/5/14

    /******************************************************
     this is the main renderer for graphic output, tells the user if they have hit a target.
     if so, then upload to the server the shooting information
     *****************************************************/
    public LinkedList<Player_Info_active> enemylist = new LinkedList<Player_Info_active>(); // enemy list
    public Player_Info_active self = new Player_Info_active("user", 0, 0f, 0f, 0);
    int mOutputWidth = 0;
    int mOutputHeight = 0;
    boolean shootable = true;
    float i = 0;
    Handler activityhandler = null;

    float[] cubeVertices = new float[]{ // cube vertices
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
    };
    byte[] cubeFacets = new byte[]{ // cube facets
            0, 1, 2,
            0, 2, 3,
            2, 3, 7,
            2, 6, 7,
            0, 3, 7,
            0, 4, 7,
            4, 5, 6,
            4, 6, 7,
            0, 1, 4,
            1, 4, 5,
            1, 2, 6,
            1, 5, 6,
    };
    FloatBuffer cubeVerticesBuffer;
    ByteBuffer cubeFacetsBuffer;

    public Handler h = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x000: // get self direction and position
                    self = (Player_Info_active) msg.obj;
                    break;
                case 0x001: // get enemy list
                       enemylist = (LinkedList<Player_Info_active>) msg.obj;
                    break;
                case 0x002: // send hit message to user
                    activityhandler.sendMessage(GameUtil.sendMessage(0x001, msg.obj));
                    break;


            }

        }

    }; // own handler for receiving messages from background service

    public MyRenderer() {
        cubeVerticesBuffer = floatBufferUtil(cubeVertices);
        cubeFacetsBuffer = ByteBuffer.wrap(cubeFacets);

    }

    public void setActivityhandler(Handler h) {
        activityhandler = h;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        gl.glClearColor(0, 0, 0, 0);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
    } // do not touch or change

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mOutputHeight = height;
        mOutputWidth = width;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float ratio = (float) width / height;
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    } // do not touch or change

    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glMatrixMode(GL10.GL_MODELVIEW);


        for (Player_Info_active enemy : enemylist) {

            if (is_in_sight(enemy)) {
                float n = 0;
                n = (float) Math.toDegrees(Math.atan((enemy.getLocationx()-self.getLocationx()) / (enemy.getLocationy()-self.getLocationy()))); // first quadrant
                if(n>0)
                {
                    if(enemy.getLocationx()<self.getLocationx()&& enemy.getLocationy()<self.getLocationx())
                    {
                        n+=180; // third quadrant
                    }
                }
                else if(n<0)
                {
                    if(enemy.getLocationx()>self.getLocationx()&&enemy.getLocationy()<self.getLocationy())
                    {
                        n+=180; // second quadrant
                    }
                    if(enemy.getLocationy()>self.getLocationy()&&enemy.getLocationx()<self.getLocationx())
                    {
                        n+=360; // fourth quadrant
                    }
                }
                else if(n==0)
                {
                    if(enemy.getLocationy()>=self.getLocationy())
                    {
                        n = 0;
                    }
                    if(enemy.getLocationy()<self.getLocationy())
                    {
                        n = 180;
                    }
                }
                else if (n==-90)
                {
                    n = 270;
                }

                float d = (float) Math.pow(Math.pow(enemy.getLocationx()-self.getLocationx(),2)+Math.pow(enemy.getLocationy()-self.getLocationy(),2),0.5);
                  d+=5;
                  float x = (self.getDirection()-n);
                  if(x>300)
                  {
                      x= x-360;
                  }
                x =  (x/60)*(d/2)*0.8f; // 0.8 is to smooth the shooting process
                  x*=-1;
                drawsquare(gl, x, d); // x being the horizontal offset, d being the distance, normally, the boundary of x is 0.5*d;
                  if(x <= 0.5&&shootable)
                  {
                    h.sendMessage(GameUtil.sendMessage(0x002,enemy.getUsername()));
                    shootable = false;
                    cooldown();
                  }
            }

        }

        gl.glFinish();
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

    }

    private FloatBuffer floatBufferUtil(float[] arr) {
        FloatBuffer mBuffer;
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        qbb.order(ByteOrder.nativeOrder());
        mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    } // do not touch or change

    private IntBuffer IntBufferUtil(int[] arr) {
        IntBuffer mBuffer;
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        qbb.order(ByteOrder.nativeOrder());
        mBuffer = qbb.asIntBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    } // do not touch or change


    private void drawsquare(GL10 gl, float x, float d) {
        gl.glLoadIdentity();
        gl.glTranslatef(x, 0, -1 * d); // z controls the range of visibility. x and y decides the position on the screen
       // gl.glRotatef(0, 0, 0f, 0);
        gl.glColor4f(1.0f, 1f, 1f, 0f);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cubeVerticesBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, cubeFacetsBuffer.remaining(), GL10.GL_UNSIGNED_BYTE, cubeFacetsBuffer);
    }

    private boolean is_in_sight(Player_Info_active enemy) {
        float n = 0;
        n = (float) Math.toDegrees(Math.atan((enemy.getLocationx()-self.getLocationx()) / (enemy.getLocationy()-self.getLocationy()))); // first quadrant
        if(n>0)
        {
            if(enemy.getLocationx()<self.getLocationx()&& enemy.getLocationy()<self.getLocationx())
            {
                n+=180; // third quadrant
            }
        }
        else if(n<0)
        {
             if(enemy.getLocationx()>self.getLocationx()&&enemy.getLocationy()<self.getLocationy())
             {
                 n+=180; // second quadrant
             }
            if(enemy.getLocationy()>self.getLocationy()&&enemy.getLocationx()<self.getLocationx())
            {
               n+=360; // fourth quadrant
            }
        }
       else if(n==0)
        {
            if(enemy.getLocationy()>=self.getLocationy())
            {
                n = 0;
            }
            if(enemy.getLocationy()<self.getLocationy())
            {
                n = 180;
            }
        }
        else if (n==-90)
        {
            n = 270;
        }


        if (Math.abs(self.getDirection()-n)<60 || Math.abs(self.getDirection()-n)>300) {
            return true;
        }
        else {
            return false;
        }
    }

    private void cooldown() //send message to server
    {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Thread.sleep(2000);
                    shootable = true;
                    activityhandler.sendMessage(GameUtil.sendMessage(0x005,null));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();
    }


}
