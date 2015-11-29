package com.moogedii.helicoptergame;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;



public class MainActivity extends Activity {


    public static SharedPreferences prefs;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        prefs = this.getSharedPreferences( "com.moogedii.helicoptergame.HIGHSCORE", Context.MODE_PRIVATE);




        // set our GamePanel as the View
        setContentView(new GamePanel(this));














    }
    @Override
    protected void onResume()
    {
        super.onResume();

    }

    @Override
    protected void onDestroy() {



        super.onDestroy();


    }

    @Override
    protected void onStop() {



        super.onStop();
    }




}