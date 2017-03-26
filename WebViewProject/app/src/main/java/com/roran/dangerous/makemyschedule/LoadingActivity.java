package com.roran.dangerous.makemyschedule;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class LoadingActivity extends AppCompatActivity {


    public static LoadingActivity la;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        la = this;
        /*WindowManager.LayoutParams wlmp = this.getWindow()
                .getAttributes();
        wlmp.y = 100;
        wlmp.gravity = Gravity.BOTTOM;*/

        getWindow().setLayout(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
    }
}
