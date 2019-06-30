package com.example.ted.myapplication.view_new;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class Cross_hair_View extends View {

    /******************************************************
     * Creator: Ted Wong
     * StudentID: 2014012687
     * Major: Software Engineering
     * Class: 143
     * Description: This view is a cross hair. it is used to show the player if they have successfully hit another player or not
     *****************************************************/

    Paint p;
    public Cross_hair_View(Context c, AttributeSet as) // constructor
    {
        super(c,as);
        p = new Paint();
        p.setColor(Color.GREEN);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);
    }
    protected void onDraw(Canvas canvas) // draw a circle crosshair in the middle of the screen
    {
        super.onDraw(canvas);
        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();

        canvas.drawCircle(viewWidth/2,viewHeight/2,viewWidth/10,p);

    }
    public void shoot_red() // change the color of the crosshair to red, means the user didn't successfully shoot an enemy
    {
        p.setColor(Color.RED);
    }
    public void shoot_green() // change the color of the crosshair to green, means the user successfully shot an enemy
    {
        p.setColor(Color.GREEN);

    }

}
