package com.roran.dangerous.makemyschedule;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NoInternetConnection extends AppCompatActivity {
    public static boolean closed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet_connection);
        closed = false;
        CheckConnection cc = new CheckConnection();
        cc.execute();
    }

    public void checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            closed = true;
            Intent inte = new Intent(this, SemesterProgramActivity.class);
            startActivity(inte);
            finish();
        }
    }

    public class CheckConnection extends AsyncTask<String, Integer,String> {

        boolean timer = true;
        @Override
        protected String doInBackground(String... params) {
            long time = System.currentTimeMillis();
            while(time > 0){
                if(time % 10000 == 0){
                    if(timer) {
                        System.out.println("stille here");
                            class OneShot implements Runnable{
                                @Override
                                public void run() {
                                    checkConnection();
                                }
                            }
                            runOnUiThread(new OneShot());
                    }
                    timer = false;
                } else timer = true;
                time = System.currentTimeMillis();
                if(closed)
                    break;
            }
            return null;
        }
    }
}
