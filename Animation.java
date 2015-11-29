package com.moogedii.helicoptergame;

import android.graphics.Bitmap;

public class Animation {

    private Bitmap[] frames;
    private int currentFrame;

    private long startTime;
    private long delay;
    private boolean playedOnce;

    public void setFrames(Bitmap[] frames)
    {
        if(frames==null)
        {   if(this.frames!=null) {
            for (Bitmap b : this.frames) {
                b.recycle();
                b = null;
            }
        }
        }
        else{
        this.frames = frames;
        currentFrame = 0;
        startTime = System.nanoTime();
        }


    }
    public void setDelay(long d){delay = d;}
    public void setFrame(int i){currentFrame = i;}

    public void update()
    {
        long elapsed = (System.nanoTime() - startTime)/1000000;

        if (elapsed>delay)
        {
            currentFrame++;
            startTime = System.nanoTime();
        }
        if(currentFrame == frames.length)
        {
            currentFrame = 0;
            playedOnce=true;
        }
    }
    public int getFrame(){return currentFrame;}
    public Bitmap getImage(){return frames[currentFrame];}
    public boolean getPlayedOnce(){return playedOnce;}

}