package com.roran.dangerous.makemyschedule;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dangerous on 22/02/17.
 */

public class chooseLabActivity extends Activity {
    public static String cN;
    public static boolean[] arrs ;
    public static String[] name;
    public static chooseLabActivity ca;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitychooselab);

        ca = this;
        cN = getIntent().getExtras().getString("courseSelected");
        name = getIntent().getExtras().getString("labs").split("@");
        ListView lv = (ListView)findViewById(R.id.lvlabs);
        ArrayList<String> lvcourses = new ArrayList<String>();
        arrs = new boolean[name.length];
        Collections.addAll(lvcourses, name);
        MyListViewAdapter adapter = new MyListViewAdapter(lvcourses, this);
        lv.setAdapter(adapter);
        final Button back = (Button)findViewById(R.id.cancelbtnlab);
        final Button ad = (Button)findViewById(R.id.donebtnlab);
        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setScaleX(.95f);
                        v.setScaleY(.95f);
                        MainActivity.removeLabsFromCourse(cN);
                        finish();
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.setScaleX(1);
                        v.setScaleY(1);
                        finish();
                        return true;
                }
                return false;
            }
        });
        ad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setScaleX(.95f);
                        v.setScaleY(.95f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.setScaleX(1);
                        v.setScaleY(1);
                        finish();
                        return true;
                }
                return false;
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8), ViewPager.LayoutParams.WRAP_CONTENT);
    }
    public  static String getFullLabName(int pos){
        return name[pos];
    }
}
