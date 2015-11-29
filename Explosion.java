package com.moogedii.helicoptergame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Paymon on 2/22/2015.
 */
public class Explosion {
    private int x;
    private int y;
    private int width;
    private int height;
    private int row;

    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public Explosion(Bitmap res, int x, int y, int w, int h, int numFrames)
    {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;

        Bitmap[] image = new Bitmap[numFrames];

        try {
            spritesheet = res;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        for(int i = 0; i<image.length;i++)
        {
            if(i%5==0&&i>0)row++;
            image[i] = Bitmap.createBitmap(spritesheet,(i-(5*row))*width, row*height, width, height);


        }
        animation.setFrames(image);
        animation.setDelay(10);

    }
    public void update()
    {
        if(!animation.getPlayedOnce())
            animation.update();

    }
    public void draw(Canvas canvas)
    {

        if(!animation.getPlayedOnce())
            canvas.drawBitmap(animation.getImage(), x, y, null);
    }
    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
    public int getHeight()
    {
        return height;
    }
    public int getWidth()
    {return width;}
    public void close()
    {
        animation.setFrames(null);
        spritesheet.recycle();
        spritesheet=null;
    }
}
