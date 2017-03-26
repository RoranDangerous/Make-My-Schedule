package com.roran.dangerous.makemyschedule;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class VoiceControllInstructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_controll_instructions);

        TextView instr = (TextView)findViewById(R.id.instructionstv);
        String s= "voice control off";
        SpannableString ss1=  new SpannableString(s);
        //ss1.setSpan(new RelativeSizeSpan(1f), 0,5, 0); // set size
        ss1.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss1.length(), 0);
        ss1.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        String s2= "back";
        SpannableString ss2=  new SpannableString(s2);
        //ss1.setSpan(new RelativeSizeSpan(1f), 0,5, 0); // set size
        ss2.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss2.length(), 0);
        ss2.setSpan(new ForegroundColorSpan(Color.RED), 0, s2.length(), 0);
        String s3= "go back";
        SpannableString ss3=  new SpannableString(s3);
        //ss1.setSpan(new RelativeSizeSpan(1f), 0,5, 0); // set size
        ss3.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss3.length(), 0);
        ss3.setSpan(new ForegroundColorSpan(Color.RED), 0, s3.length(), 0);
        String s4= "exit";
        SpannableString ss4=  new SpannableString(s4);
        //ss1.setSpan(new RelativeSizeSpan(1f), 0,5, 0); // set size
        ss4.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss4.length(), 0);
        ss4.setSpan(new ForegroundColorSpan(Color.RED), 0, s4.length(), 0);
        String s5= "close";
        SpannableString ss5=  new SpannableString(s5);
        //ss1.setSpan(new RelativeSizeSpan(1f), 0,5, 0); // set size
        ss5.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss5.length(), 0);
        ss5.setSpan(new ForegroundColorSpan(Color.RED), 0, s5.length(), 0);
        /*String text = "You enabled voice control. You can pick semester, number of courses and program using your voice. Here are some additional commands:\n" +
                "- \""+ss1+"\" to disable voice control,\n" +
                "- \""+ss2+"\" or " +
                "\""+ss3+"\" to simulate clicking on back button,\n" +
                "- \""+ss4+"\" or \""+ss5+"\" to close the app.\n\nTry to reduce" +
                " background noise and say commands clear and loud.";*/
        instr.setText(TextUtils.concat("You enabled voice control. You can pick semester, number of courses and program using your voice. Here are some additional commands:\n" +
                "- \"",ss1,"\" to disable voice control,\n" +
                "- \"",ss2,"\" or " +
                "\"",ss3,"\" to simulate clicking on the back button,\n" +
                "- \"",ss4,"\" or \"",ss5,"\" to close the app.\n\nTry to reduce" +
                " background noise and say commands clear and loud."));
        instr.setTextColor(Color.WHITE);
        CheckBox cb = (CheckBox)findViewById(R.id.checkBox);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getSharedPreferences("myprefs",0);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("onetime",isChecked);
                editor.apply();
            }
        });
        TextView tv = (TextView)findViewById(R.id.oktv);
        tv.setTextColor(Color.WHITE);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getWindow().setLayout(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
    }
}
