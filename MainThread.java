
package com.moogedii.helicoptergame;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;


/*
 * The Main thread which contains the game loop. The thread must have access to
 * the surface view and holder to trigger events every game tick.
 */
public class MainThread extends Thread{

    private static final String TAG = MainThread.class.getSimpleName();
    private int FPS = 30;
    private double averageFPS;
    // Surface holder that can access the physical surface
    private SurfaceHolder surfaceHolder;
    // The actual view that handles inputs
    // and draws to the surface
    private GamePanel gamePanel;

    // flag to hold game state
    private boolean running;
    public static Canvas canvas;
    public void setRunning(boolean running) {
        this.running = running;
    }

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {

        Log.d(TAG, "Starting game loop");

        long startTime;
        long URDTimeMillis;
        long waitTime;
        long totalTime = 0;

        int frameCount = 0;

        long targetTime = 1000/FPS;
        while (running) {
            startTime = System.nanoTime();
            canvas = null;

            // try locking the canvas for exclusive pixel editing on the surface

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    // update game state
                    this.gamePanel.update();
                    // draws the canvas on the panel
                    this.gamePanel.draw(canvas);
                }

            } catch(Exception e){}finally {
                // in case of an exception the surface is not left in
                // an inconsistent state
                if (canvas != null) {
                    try {

                        surfaceHolder.unlockCanvasAndPost(canvas);

                    }
                    catch(Exception e){e.printStackTrace();}
                }
                URDTimeMillis = (System.nanoTime() - startTime)/1000000;

                waitTime = targetTime - URDTimeMillis;

                try {
                    this.sleep(waitTime);
                } catch (Exception e){}

                totalTime += System.nanoTime() - startTime;
                frameCount++;
                if (frameCount == FPS)
                {
                    averageFPS = 1000.0/((totalTime/frameCount)/1000000);
                    frameCount = 0;
                    totalTime = 0;
                }

            }
        }
    }



}
