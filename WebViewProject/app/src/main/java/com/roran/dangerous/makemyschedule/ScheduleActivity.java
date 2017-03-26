package com.roran.dangerous.makemyschedule;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dangerous on 23/02/17.
 */

public class ScheduleActivity extends Activity {

    public LinearLayout ll;
    public int[][][] timeSchedule;
    public int maxTime;
    public int minTime;
    public JSONObject courseColors;
    public ArrayList<JSONObject> schedule;
    public ArrayList<String> schedules;
    public Context c;

    public InterstitialAd mInterstitialAd;
    public Random rand;
    public boolean repeat = false;
    public static ScheduleActivity sa;
    public int count = 0;
    public int countHeights = 0;
    public String[] colors = {"#FF0000","#FF7F50","#FFA500","#F08080",
            "#FFD700","#DAA520","#EEE8AA","#9ACD32","#9ACD32",
            "#00FA9A","#66CDAA","#00FFFF","#7FFFD4","#00BFFF",
            "#DDA0DD","#FF00FF","#FAEBD7","#F5DEB3","#D2691E",
    "#FFE4B5","#C0C0C0"};
    public String[][] desccolors = {{"#24248f","#2e2eb8"},{"#004d00","#008000"},{"#806000","#b38600"},{"#660000","#990000"}};
    public boolean[] colorsPick;
    public int[] heights;
    public TextView topHeader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduleactivity);

        topHeader = (TextView)findViewById(R.id.topheadertv);
        topHeader.setVisibility(View.INVISIBLE);

        final ScrollView sv = (ScrollView)findViewById(R.id.mysv);
        sv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                for(int i=0; i<heights.length;i++){
                    if(sv.getScrollY()<(heights[i]-100)) {
                        topHeader.setText("Schedule " + (i + 1));
                        break;
                    }
                }
            }
        });

        sa = this;
        rand = new Random();
        colorsPick = new boolean[21];
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_ad_unit_id));

        AdRequest adRequest = new AdRequest.Builder().build();
        c = this;

        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

                if(mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                MainT t = new MainT(c);
                //t.execute();
                t.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        ll = (LinearLayout)findViewById(R.id.schedulelayout);
        ll.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int chnum = ll.getChildCount();
                for(int i=0;i<chnum;i++)
                    heights[i]=(i==0?0:heights[i-1])+ll.getChildAt(i).getHeight()+100;
            }
        });


    }

    public class MainT extends AsyncTask<String,Integer,String>{

        Context context;
        MainT(Context ct){
            this.context = ct;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                schedules = getIntent().getStringArrayListExtra("schedules");
                System.out.println("schedules "+schedules);
                if(schedules.size()<=0 || schedules.isEmpty()){
                    class OneShotTask implements Runnable {
                        public void run()
                        {
                            ScrollView sv = (ScrollView)findViewById(R.id.mysv);
                            sv.setVisibility(View.GONE);
                            System.out.println("FUCKING TRUE");
                            LinearLayout lmain = (LinearLayout)findViewById(R.id.maiinschedulel);
                            TextView tv2 = new TextView(context);
                            tv2.setText("Sorry, there are no schedules that match your parameters.");
                            TableRow.LayoutParams parDesc = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f);
                            tv2.setLayoutParams(parDesc);
                            tv2.setTextSize(30);
                            tv2.setTextColor(Color.CYAN);
                            tv2.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
                            lmain.addView(tv2);
                            System.out.println("SO??????");
                        }
                    }
                    runOnUiThread(new OneShotTask());
                }
                else {
                    class OneShot implements Runnable{
                        @Override
                        public void run() {
                            topHeader.setVisibility(View.VISIBLE);
                        }
                    }
                    runOnUiThread(new OneShot());

                    heights = new int[schedules.size()];
                    for (String k : schedules) {
                        schedule = new ArrayList<JSONObject>();
                        courseColors = new JSONObject();
                        repeat = k.split("@").length > 21;
                        for (String kk : k.split("@"))
                            schedule.add(MainActivity.getCourseByName(kk));
                        for (int i = 0; i < schedule.size(); i++) {
                            int r = 0;
                            do {
                                r = rand.nextInt(21);
                            } while (colorsPick[r]);
                            courseColors.put(schedule.get(i).get("name").toString(), Color.parseColor(colors[r]));
                            if (!repeat)
                                colorsPick[r] = true;
                        }
                        timeSchedule = generateTimeArray(schedule);
                        //for(int i = 0; i<schedule.size();i++)
                        getMaxMin();
                        generateSchedule(schedule);
                        count++;
                    }
                }
            }
            catch (Exception ignored){}
            return null;
        }

        int[] convertTime(String s, int i){
            int result[] = new int[3];
            result[2] = i;
            String [] temp = s.split(" - ");
            int startHour = Integer.parseInt(temp[0].split(":")[0]);
            int startMin = Integer.parseInt(temp[0].split(":")[1].split(" ")[0]);
            int endHour = Integer.parseInt(temp[1].split(":")[0]);
            int endMin = Integer.parseInt(temp[1].split(":")[1].split(" ")[0]);
            if(temp[1].substring(temp[1].length()-2).equals("am")){
                result[0] = startHour*60+startMin;
                result[1] = endHour*60+endMin-startHour*60-startMin;
            }else if (temp[0].substring(temp[0].length()-2).equals("am")){
                result[0] = startHour * 60 + startMin;
                if(endHour != 12) {
                    result[1] = (endHour + 12) * 60 + endMin - startHour * 60 - startMin;
                }
                else{
                    result[1] = endHour*60 + endMin - startHour*60 - startMin;
                }
            }else{
                if(startHour != 12) {
                    result[0] = (startHour + 12) * 60 + startMin;
                    result[1] = (endHour + 12) * 60 + endMin - (startHour + 12) * 60 - startMin;
                }
                else{
                    result[0] = startHour*60 + startMin;
                    if(endHour == 12)
                        result[1] = endHour*60 + endMin - startHour*60 - startMin;
                    else
                        result[1] = (endHour + 12) * 60 + endMin - startHour * 60 - startMin;
                }
            }
            return result;
        }

        int[][][] generateTimeArray(ArrayList<JSONObject> arr) throws JSONException{
            int[][][] tempArr =  new int[5][getArraySize(arr)][3];
            for(int o =0;o<arr.size();o++) {
                JSONObject j = arr.get(o);
                String[] tempTimeArray = j.get("time").toString().split("@");
                String[] tempDayArray = j.get("day").toString().split("@");
                for (int i = 0; i < tempTimeArray.length; i++) {
                    int[] tempTime = convertTime(tempTimeArray[i],o);
                    for (int c = 0; c < tempDayArray[i].length(); c++) {
                        int ind = 0;
                        switch (tempDayArray[i].charAt(c)) {
                            case 'T':
                                ind = 1;
                                break;
                            case 'W':
                                ind = 2;
                                break;
                            case 'R':
                                ind = 3;
                                break;
                            case 'F':
                                ind = 4;
                                break;
                            default:
                                break;
                        }
                        for (int p = 0; p < tempArr[ind].length; p++)
                            if (tempArr[ind][p][0] == 0 && tempArr[ind][p][1] == 0) {
                                tempArr[ind][p] = tempTime;
                                break;
                            }
                    }
                }
                if (j.has("labs")) {
                    int countLab = 100;
                    for (JSONObject jLab : (ArrayList<JSONObject>) j.get("labs")) {
                        tempTimeArray = jLab.get("time").toString().split("@");
                        tempDayArray = jLab.get("day").toString().split("@");
                        for (int i = 0; i < tempTimeArray.length; i++) {
                            int[] tempTime = convertTime(tempTimeArray[i], o + countLab);
                            for (int c = 0; c < tempDayArray[i].length(); c++) {
                                int ind = 0;
                                switch (tempDayArray[i].charAt(c)) {
                                    case 'T':
                                        ind = 1;
                                        break;
                                    case 'W':
                                        ind = 2;
                                        break;
                                    case 'R':
                                        ind = 3;
                                        break;
                                    case 'F':
                                        ind = 4;
                                        break;
                                    default:
                                        break;
                                }
                                for (int p = 0; p < tempArr[ind].length; p++)
                                    if (tempArr[ind][p][0] == 0 && tempArr[ind][p][1] == 0) {
                                        tempArr[ind][p] = tempTime;
                                        break;
                                    }
                            }
                        }
                        countLab += 100;
                    }
                }
            }
            return tempArr;
        }

        void generateSchedule(ArrayList<JSONObject> jarr) throws JSONException{
            final TableLayout tl = new TableLayout(context);
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT
            );
            //tl.setBackgroundColor(0x761818);
            //if(count!=0)
            //tl.setBackground(getResources().getDrawable(R.drawable.tableborder));
            params.setMargins(0, 0, 0, 100);
            tl.setLayoutParams(params);

            TableRow desc = new TableRow(context);
            TableRow.LayoutParams parDesc = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f);
            desc.setLayoutParams(parDesc);
            LinearLayout llt = new LinearLayout(context);
            parDesc = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f);
            //desc.setBackgroundColor(Color.CYAN);
            llt.setLayoutParams(parDesc);
            llt.setOrientation(LinearLayout.VERTICAL);
            TextView tvs = new TextView(context);
            tvs.setText("Schedule "+(count+1));
            tvs.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
            tvs.setTextSize(30f);
            tvs.setPadding(20,20,20,20);
            parDesc = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.WRAP_CONTENT);
            tvs.setLayoutParams(parDesc);
            TableLayout coursestl = new TableLayout(context);
            Random rand = new Random();
            //TextView coursestv = new TextView(context);
            String listC = "";
            int tempCount = 1;
            int col = rand.nextInt(4);
            for(JSONObject j : jarr){
                TableRow temptr = new TableRow(context);
                parDesc = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f);
                temptr.setLayoutParams(parDesc);
                TextView coursestv = new TextView(context);
                coursestv.setLayoutParams(parDesc);
                coursestv.setPadding(20,20,20,20);
                coursestv.setTextSize(15f);
                coursestv.setTextColor(Color.WHITE);
                coursestv.setText(tempCount+". "+j.get("fullname"));
                temptr.setBackgroundColor(Color.parseColor(desccolors[col][tempCount%2]));
                temptr.addView(coursestv);
                coursestl.addView(temptr);
                listC+="\n"+tempCount+". "+j.get("fullname")+"\n";
                tempCount++;
                if(j.has("labs"))
                    for(JSONObject jl : (ArrayList<JSONObject>)j.get("labs")){
                        temptr = new TableRow(context);
                        parDesc = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f);
                        temptr.setLayoutParams(parDesc);
                        temptr.setBackgroundColor(Color.parseColor(desccolors[col][tempCount%2]));
                        coursestv = new TextView(context);
                        coursestv.setPadding(20,20,20,20);
                        coursestv.setTextSize(15f);
                        coursestv.setLayoutParams(parDesc);
                        coursestv.setTextColor(Color.WHITE);
                        coursestv.setText(tempCount+". "+jl.get("fullname"));
                        temptr.addView(coursestv);
                        coursestl.addView(temptr);
                        tempCount++;
                    }
            }
            //coursestv.setText(listC);
            //parDesc = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.WRAP_CONTENT);
            //coursestv.setLayoutParams(parDesc);
            //coursestv.setTextColor(Color.CYAN);
            //coursestv.setTextSize(15f);
            //llt.addView(tvs);
            llt.addView(coursestl);
            //llt.addView(coursestv);
            desc.addView(llt);
            tl.addView(desc);

            //Schedule design starts
            TableRow rowHeader= new TableRow(context);
            TableRow.LayoutParams par = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f);
            rowHeader.setLayoutParams(par);
            LinearLayout lr = new LinearLayout(context);
            lr.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,100,1f));
            lr.setBackground(getResources().getDrawable(R.drawable.borders));
            String[] months = {"  ","Mon", "Tue","Wed","Thu","Fri"};
            for(String m : months) {
                TextView tvHead = new TextView(context);
                //tvHead.setBackgroundColor(0xFFF99F9F);
                tvHead.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 1f));
                tvHead.setText(m);
                tvHead.setTextSize(12);
                tvHead.setGravity(Gravity.CENTER);
                lr.addView(tvHead);
            }
            rowHeader.addView(lr);
            tl.addView(rowHeader);
            TableLayout tTemp = new TableLayout(context);
            tTemp.setLayoutParams(par);
            if(minTime%60 == 0)
                minTime-=30;
            int tempMax;
            if(maxTime%60>30)
                tempMax = maxTime/60+1;
            else
                tempMax = maxTime/60;
            for(int i = minTime/60;i<tempMax;i++) {
                TableRow rowBody = new TableRow(context);
                lr = new LinearLayout(context);
                lr.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,385,1f));
                TextView tvTime = new TextView(context);
                //tvTime.setBackgroundColor(0xFFF99F9F);
                tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 1f));
                if(i!=tempMax-1)
                    tvTime.setBackground(getResources().getDrawable(R.drawable.borders));
                tvTime.setText((i>12 ? i-12 : i)+" : 30 "+(i<12 ? "am" : "pm"));
                tvTime.setTextSize(10);
                tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
                lr.addView(tvTime);
                int tSize = 9;
                for(int j = 0;j<5;j++){
                    boolean found = false;
                    LinearLayout lv = new LinearLayout(context);
                    //lv.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f));
                    TableRow.LayoutParams parCell = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f);
                    if(j!=4)
                    parCell.setMargins(0,0,10,0);
                    lv.setLayoutParams(parCell);
                    for(int[] tempT : timeSchedule[j]){
                        if(tempT[0] !=0)
                            //start time
                            if(tempT[0] >= i*60+30 && tempT[0] < (i+1)*60 + 30){
                                if(tempT[0] == i*60 + 30) {
                                    if(!(tempT[0]+tempT[1] <= (i+1)*60 + 30)){
                                        tvTime = new TextView(context);
                                        tvTime.setTextSize(tSize);
                                        tvTime.setTypeface(null, Typeface.BOLD);
                                        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
                                        tvTime.setTextColor(Color.BLACK);
                                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 1f));
                                        if(tempT[2]<100)
                                            tvTime.setText(getCourseDesc(schedule.get(tempT[2]%100).get("fullname").toString()));
                                        else
                                            tvTime.setText(getCourseDesc(((ArrayList<JSONObject>)schedule.get(tempT[2]%100).get("labs")).get((tempT[2]/100)-1).get("fullname").toString()));
                                        tvTime.setBackgroundColor(courseColors.getInt(schedule.get(tempT[2]%100).get("name").toString()));
                                        lv.addView(tvTime);
                                    }
                                    else {
                                        float f = (tempT[0] + tempT[1] - (i * 60 + 30)) / 5;
                                        tvTime = new TextView(context);
                                        tvTime.setTextSize(tSize);
                                        tvTime.setTypeface(null, Typeface.BOLD);
                                        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
                                        tvTime.setTextColor(Color.BLACK);
                                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));
                                        JSONObject courseTemp = schedule.get(tempT[2]%100);
                                        if(tempT[2]<100)
                                            tvTime.setText(getCourseDesc(schedule.get(tempT[2]%100).get("fullname").toString()));
                                        else
                                            tvTime.setText(getCourseDesc(((ArrayList<JSONObject>)schedule.get(tempT[2]%100).get("labs")).get((tempT[2]/100)-1).get("fullname").toString()));
                                        tvTime.setBackgroundColor(courseColors.getInt(schedule.get(tempT[2]%100).get("name").toString()));
                                        lv.addView(tvTime);
                                        f = 12 - f;
                                        tvTime = new TextView(context);
                                        tvTime.setTextSize(tSize);
                                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));
                                        tvTime.setBackgroundColor(0xFFFFFFFF);
                                        lv.addView(tvTime);

                                    }
                                }
                                else{
                                    if(tempT[0]+tempT[1] <= (i+1)*60 + 30){
                                        float f = tempT[0] - (i * 60 + 30) / 5;
                                        tvTime = new TextView(context);
                                        tvTime.setTextSize(tSize);
                                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));
                                        tvTime.setBackgroundColor(0xFFFFFFFF);
                                        lv.addView(tvTime);
                                        f = (12 - f) - (tempT[0]+tempT[1] - (i * 60 + 30))/5;
                                        tvTime = new TextView(context);
                                        tvTime.setTextSize(tSize);
                                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));

                                        tvTime.setTypeface(null, Typeface.BOLD);
                                        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
                                        tvTime.setTextColor(Color.BLACK);
                                        if(tempT[2]<100)
                                            tvTime.setText(getCourseDesc(schedule.get(tempT[2]%100).get("fullname").toString()));
                                        else
                                            tvTime.setText(getCourseDesc(((ArrayList<JSONObject>)schedule.get(tempT[2]%100).get("labs")).get((tempT[2]/100)-1).get("fullname").toString()));
                                        tvTime.setBackgroundColor(courseColors.getInt(schedule.get(tempT[2]%100).get("name").toString()));
                                        lv.addView(tvTime);
                                        f= 12-f;
                                        tvTime = new TextView(context);
                                        tvTime.setTextSize(tSize);
                                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));
                                        tvTime.setBackgroundColor(0xFFFFFFFF);
                                        lv.addView(tvTime);
                                    }
                                    else {
                                        float f = (tempT[0] - (i * 60 + 30)) / 5;
                                        tvTime = new TextView(context);
                                        tvTime.setTextSize(tSize);
                                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));
                                        tvTime.setBackgroundColor(0xFFFFFFFF);
                                        lv.addView(tvTime);
                                        f =  12-f;
                                        tvTime = new TextView(context);
                                        tvTime.setTextSize(tSize);
                                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));

                                        tvTime.setTypeface(null, Typeface.BOLD);
                                        tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
                                        tvTime.setTextColor(Color.BLACK);
                                        if(tempT[2]<100)
                                            tvTime.setText(getCourseDesc(schedule.get(tempT[2]%100).get("fullname").toString()));
                                        else
                                            tvTime.setText(getCourseDesc(((ArrayList<JSONObject>)schedule.get(tempT[2]%100).get("labs")).get((tempT[2]/100)-1).get("fullname").toString()));
                                        tvTime.setBackgroundColor(courseColors.getInt(schedule.get(tempT[2]%100).get("name").toString()));
                                        lv.addView(tvTime);

                                    }
                                }
                                found = true;
                            }
                            else if(tempT[0]+tempT[1] >= i*60+30 && tempT[0]+tempT[1] <= (i+1)*60 + 30){
                                if(!(tempT[0]+tempT[1] <= (i+1)*60 + 30)) {
                                    tvTime = new TextView(context);
                                    tvTime.setTextSize(tSize);
                                    tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 1f));
                                    tvTime.setBackgroundColor(courseColors.getInt(schedule.get(tempT[2]%100).get("name").toString()));
                                    lv.addView(tvTime);
                                }
                                else{
                                    float f = (tempT[0] + tempT[1] - (i * 60 + 30)) / 5;
                                    tvTime = new TextView(context);
                                    tvTime.setTextSize(tSize);
                                    tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));
                                    tvTime.setBackgroundColor(courseColors.getInt(schedule.get(tempT[2]%100).get("name").toString()));
                                    lv.addView(tvTime);
                                    f = 12 - f;
                                    tvTime = new TextView(context);
                                    tvTime.setTextSize(tSize);
                                    tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 12-f));
                                    tvTime.setBackgroundColor(0xFFFFFFFF);
                                    lv.addView(tvTime);
                                }
                                found = true;
                            }
                        else if((i*60+30)>tempT[0] && ((i+1)*60+30)<tempT[0]+tempT[1]){
                                tvTime = new TextView(context);
                                tvTime.setTextSize(tSize);
                                tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 1f));
                                tvTime.setBackgroundColor(courseColors.getInt(schedule.get(tempT[2]%100).get("name").toString()));
                                lv.addView(tvTime);
                                found = true;
                            }
                    }
                    if(!found) {
                        tvTime = new TextView(context);
                        tvTime.setTextSize(tSize);
                        tvTime.setLayoutParams(new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT, 1f));
                        tvTime.setBackgroundColor(0xFFFFFFFF);
                        lv.addView(tvTime);
                    }

                    lv.setOrientation(LinearLayout.VERTICAL);
                    lr.addView(lv);
                }
                rowBody.addView(lr);
                //lr.getChildAt(0).setBackgroundColor(0xFFF99F9F);
                tTemp.addView(rowBody);
            }
            tl.addView(tTemp);
            System.out.println("from thread "+ll.getHeight()+" "+tTemp.getHeight()+" "+tl.getMeasuredHeight());
            class OneShotTask implements Runnable {
                public void run() {
                    ll.addView(tl);
                    System.out.println("tl.height "+tl.getHeight()+" "+ll.getHeight());
                    heights[countHeights]= ll.getHeight();
                    countHeights++;
                }
            }
            runOnUiThread(new OneShotTask());
            //ll.addView(tl);
        }

        void getMaxMin(){
            maxTime = 0;
            minTime = 100000;
            for(int[][] t: timeSchedule)
                for(int[] t2 : t){
                    if(t2[0]<minTime && t2[0]!=0)
                        minTime = t2[0];
                    if(t2[0]+t2[1]>maxTime)
                        maxTime = t2[0]+t2[1];
                }
        }

        int getArraySize(ArrayList<JSONObject> arr) throws JSONException{
            int size = arr.size();
            for(JSONObject j : arr)
                if(j.has("labs"))
                    size += ((ArrayList<JSONObject>)j.get("labs")).size();
            return size;
        }

        String getCourseDesc(String j) throws JSONException{
            String result = "";
            String temp = "";
            Pattern p = Pattern.compile("([A-Z]{3,4} [0-9]{3,4} - [A-Z]{0,1}[0-9]{1,2})");
            Matcher m = p.matcher(j);
            if(m.find())
                temp=m.group(1);
            for(String k : temp.split(" "))
                if(!k.equals("-"))
                    result+=k+"\n";
            return result;
        }
    }
}
