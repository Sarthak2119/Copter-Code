
package com.moogedii.helicoptergame;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;
import android.media.SoundPool;


public class GamePanel extends SurfaceView implements
        SurfaceHolder.Callback {

    private SoundPool explodeSound;
    private int explodeID;
    private SoundPool heliSound;
    private int heliID;
    private MainThread thread;
    private Player player;
    private Background bg;
    private ArrayList<BotBorder> botborder;
    private ArrayList<TopBorder> topborder;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;

    private long smokeStartTime, missileStartTime;
    private Random rand = new Random();
    private boolean topDown = true;
    private boolean botDown = true;
    private int maxBorderHeight;
    private int minBorderHeight;
    public static int WIDTH = 856;
    public static int HEIGHT = 480;
    public static int MOVESPEED = -5;
    private boolean newGameCreated;

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;


    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best = MainActivity.prefs.getInt("HighScore", 0);

    public GamePanel(Context context) {

        super(context);


        // adding the callback (this) to the surface holder to intercept events
        getHolder().addCallback(this);

        // make the GamePanel focusable so it can handle events
        setFocusable(true);



        explodeSound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        explodeID = explodeSound.load(context, R.raw.explosion, 1);

        heliSound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        heliID = heliSound.load(context, R.raw.helicopter, 1);





    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {



        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter),65,25,3);


        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));

        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        botborder = new ArrayList<BotBorder>();
        topborder = new ArrayList<TopBorder>();



        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();



        // create the game loop thread
        thread = new MainThread(getHolder(), this);

        thread.setRunning(true);

        thread.start();








    }
    //this method is called initially
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
        {


            // tell the thread to shut down and wait for it to finish
            // this is a clean shutdown
            int counter = 1;
            boolean retry = true;
            while (retry && counter<1000) {
                try {
            counter++;
            thread.setRunning(false);
            thread.join();
            retry = false;
            thread = null;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        }


        }
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {


            if(!player.getPlaying()&&newGameCreated&&reset)
            {
                player.setPlaying(true);
                heliSound.play(heliID, 1, 1, 1, 5, 1);



            }
            if(player.getPlaying())
            {
                if(!started)started = true;
                reset = false;
                player.setUp(true);

            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            player.setUp(false);

            return true;
        }
        return super.onTouchEvent(event);
    }
    public void update()
    {


        if(player.getPlaying()) {

            if(botborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }
            if(topborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }

            //update background
            bg.update();

            //update player
            player.update();

            //add smokepuffs, with timer delay loop
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 100) {
                smoke.add(new Smokepuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            //add missile, with timer delay loop
            long missileElapsed = (System.nanoTime() - missileStartTime)/1000000;

            //change how often the missiles reappear onto screen


            if (missileElapsed>(2000 - player.getScore()/4))
            {


                //first missile always goes down the middle
                if(missiles.size()==0)
                {


                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH+10,
                            HEIGHT/2, 45 ,15,  player.getScore(),13));

                }
                else
                {

                        missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH+10,
                                (int)((rand.nextDouble()*((HEIGHT - maxBorderHeight*2))
                                                +maxBorderHeight+15)), 45 ,15,  player.getScore(),13));
                }
                missileStartTime = System.nanoTime();

            }

            // calculate the threshold of height it can have based on score
            //max and min border height are updated, the border switches direction when either one is met

            maxBorderHeight = 30 + player.getScore()/progressDenom;
            //cap max top height. borders can only take up a total of 1/2 the screen
            if( maxBorderHeight>= HEIGHT/4) maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5 + player.getScore()/progressDenom;








            //update missiles and check for collision
            for(int i = 0; i< missiles.size(); i++)
            {
                missiles.get(i).update();

                if(collision(missiles.get(i),player))
                {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                if(missiles.get(i).getX()<-1000)
                {
                    missiles.remove(i);
                    break;
                }
            }
            //update top border
            this.updateTopBorder();

            //update bot border
            this.updateBottomBorder();

            //check top border collision
            for(int i = 0; i< topborder.size(); i++)
            {
               if(collision(topborder.get(i),player))
                   player.setPlaying(false);
            }
            //check bottom border collision
            for(int i = 0; i< botborder.size(); i++)
            {
                if (collision(botborder.get(i),player))
                {
                    player.setPlaying(false);

                }
            }

            //update smoke puffs
            for(int i = 0; i< smoke.size(); i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }



        }
        else
        {
            player.resetDY();
            if(!reset)
            {
                if(started) {
                    explodeSound.play(explodeID, 1, 1, 1, 0, 1);
                }
                heliSound.autoPause();

                newGameCreated = false;
                startReset = System.nanoTime();
                reset=true;
                    dissapear=true;

                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),
                            R.drawable.explosion),player.getX(),player.getY()-30,100,100,25);


            }

            explosion.update();

            long resetElapsed = (System.nanoTime() - startReset)/1000000;


            if (resetElapsed>(2500)&&!newGameCreated)
            {

                newGame();

            }


        }

    }
    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX =  getWidth() /(WIDTH * 1.f);
        final float scaleFactorY= getHeight()/(HEIGHT * 1.f);

        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);

            //draw smokepuffs
            for(Smokepuff sp: smoke)
            {
                sp.draw(canvas);
            }

            if(!dissapear)
            player.draw(canvas);



            //draw top border
            for(TopBorder tb: topborder)
            {
                tb.draw(canvas);
            }
            //draw bot border
            for(BotBorder bb: botborder)
            {
                bb.draw(canvas);
            }
            //draw missiles
            for(Missile b: missiles)
            {
                b.draw(canvas);
            }
            //draw explosions
            if(started)
            {
                explosion.draw(canvas);
            }

            drawText(canvas);

            canvas.restoreToCount(savedState);


        }


    }
    public void newGame()
    {
        dissapear=false;

        botborder.clear();
        topborder.clear();


        maxBorderHeight = 30;
        minBorderHeight = 5;




        missiles.clear();
        smoke.clear();
        if(player.getScore()>best)
        {
            best = player.getScore();
            MainActivity.prefs.edit().putInt("HighScore", best).commit();

        }
        player.resetScore();
        player.setY(HEIGHT/2);




        for(int i = 0; i*20<WIDTH+40;i++)
        {
            if (i==0)
            {

                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),i*20,0,10));
            }
            else
            {

                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),i*20, 0,
                            topborder.get(i-1).getHeight()+1));
            }

        }

        //create initial bottom border

        for(int i = 0; i*20<WIDTH+40;i++)
        {


            if (i==0)
            {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            i*20,HEIGHT- minBorderHeight));

            }
            else
            {

                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            i*20, botborder.get(i-1).getY()-1));

            }
        }
        newGameCreated = true;

    }
    public int getBest(){
        return best;
    }
    public void setBest(int b)
    {
        best = b;
    }
    public boolean collision(GameObject a, GameObject b)
    {
        if (Rect.intersects(a.getRectangle(),b.getRectangle()))
        {
            return true;
        }
        return false;
    }

    public void updateTopBorder()
    {
        //every 50 points, insert some randomly placed top blocks that break the pattern
        if (player.getScore()%50==0)
        {

            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                    R.drawable.brick),topborder.get(topborder.size()-1).getX()+20, 0,
                    (int)((rand.nextDouble()*(maxBorderHeight))+1)));
        }

        //update top border
        for(int i = 0; i< topborder.size(); i++)
        {

            topborder.get(i).update();
            if(topborder.get(i).getX()<-20)
            {
                topborder.remove(i);
                //remove the top border, then add a new one to the last one in previous

                if(topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight)
                {
                    topDown = false;
                }
                if(topborder.get(topborder.size()-1).getHeight()<=minBorderHeight)
                {
                    topDown = true;
                }

                //new border added will have larger height
                if(topDown) {

                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size() - 1).getX() + 20,
                            0, topborder.get(topborder.size() - 1).getHeight() + 1));

                }
                //new border added will have smaller height
                else {

                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topborder.get(topborder.size() - 1).getX() + 20,
                            0, topborder.get(topborder.size() - 1).getHeight() - 1));
                }

            }
        }
    }

    public void updateBottomBorder()
    {
        //every 40 points, insert randomly placed bottom blocks that break the pattern
        if (player.getScore()%40==0)
        {

            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),
                    R.drawable.brick),botborder.get(botborder.size()-1).getX()+20, (int)
                    ((rand.nextDouble()*maxBorderHeight)+(HEIGHT - maxBorderHeight))));
        }

        //update bot border
        for(int i = 0; i< botborder.size(); i++)
        {
            botborder.get(i).update();

            if(botborder.get(i).getX()<-20)
            {
                botborder.remove(i);
                //remove bot border when off the screen, then add new one




                if(botborder.get(botborder.size()-1).getY()<=HEIGHT-maxBorderHeight)
                {
                    botDown = true;
                }
                if(botborder.get(botborder.size()-1).getY()>=HEIGHT- minBorderHeight)
                {
                    botDown = false;
                }

                //new border added will start lower on screen
                if(botDown) {

                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botborder.get(botborder.size() - 1).getX() + 20,
                            botborder.get(botborder.size() - 1).getY() + 1));
                }
                //new border added will start higher on screen
                else {

                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botborder.get(botborder.size() - 1).getX() + 20,
                            botborder.get(botborder.size() - 1).getY() - 1));
                }
            }
        }

    }
    public void drawText(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore() * 3), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if (!player.getPlaying() && newGameCreated && reset)

        {


            Paint paint1 = new Paint();
            paint1.setColor(Color.BLACK);

            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, (HEIGHT / 2) + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, (HEIGHT / 2) + 40, paint1);


        }
    }




    }
