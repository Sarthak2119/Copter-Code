package com.moogedii.helicoptergame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Paymon on 2/22/2015.
 */
public class Background {

    private Bitmap image;
    private int x;
    private int dx;
    private int width;


    public Background(Bitmap res) {


            width = GamePanel.WIDTH;

            image = res;
            dx = GamePanel.MOVESPEED;


    }




    public void update() {


            x += dx;


    }

    public void draw(Canvas canvas) {
       try{
        canvas.drawBitmap(image, (int)x, 0, null);}catch(Exception e){}

        if(x < 0) {
            canvas.drawBitmap(
                    image,
                    (int)x + width,
                    0,
                    null
            );
        }

        if(x<-width)
        {
            x=0;
        }

    }



}
