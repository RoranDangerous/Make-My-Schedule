package com.roran.dangerous.makemyschedule;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.roran.dangerous.makemyschedule.SemesterProgramActivity.selectedCourses;

public class MainActivity extends AppCompatActivity {

    private WebView w;
    public static boolean hasLabs = false;
    public static JSONObject[] courses;
    private int firstPage = 0;
    private static ArrayList<String> lvcourses;
    public ListView lv;
    //private static ArrayList<JSONObject> selectedCourses;
    public static boolean[] lvListener = null;
    private ArrayList<String> schedules;
    private int numCourses =1;
    private Button lvbtn;
    public Context context;
    public boolean auto = false;
    public InterstitialAd mInterstitialAd;
    public static MainActivity ma;
    static LoadingActivity la;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            Intent inten = new Intent(this,NoInternetConnection.class);
            startActivity(inten);
            finish();
            finish();
        }

        Intent in = new Intent(this,LoadingActivity.class);
        startActivity(in);

        lv = (ListView)findViewById(R.id.courselv);

        ma = this;
        /*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_ad_unit_id));

        AdRequest adRequest = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

                if(mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
            }
        });*/
        AdView mAdView2 = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mAdView2.loadAd(adRequest2);
        context = this;
        courses = new JSONObject[0];
        lvcourses = new ArrayList<String>();
        final String key = getIntent().getExtras().getString("term");
        final String subj = getIntent().getExtras().getString("subject");
        int tempNum = getIntent().getExtras().getInt("numC");
        if(tempNum==0)
            auto = true;
        else
            numCourses = tempNum;
        w = new WebView(this);
        WebSettings webSettings = w.getSettings();
        webSettings.setJavaScriptEnabled(true);
        w.addJavascriptInterface(new MyJavaScriptInterface(this), "HTMLOUT");
        //w.addJavascriptInterface(this, "HtmlViewer");
        w.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageFinished(WebView view, String url)
            {
                if(firstPage == 0) {
                    w.evaluateJavascript("javascript:document.getElementById('term_input_id').value = "+key+";document.getElementsByTagName('FORM')[0].submit()", null);
                }
                else if(firstPage == 1) {
                    w.evaluateJavascript("javascript:document.getElementById('subj_id').value = '"+subj+"';document.getElementsByTagName('FORM')[0].submit()", null);
                }
                else if(firstPage == 2){
                    w.evaluateJavascript("javascript:" +
                            //"window.HTMLOUT.processHTML('processHTML');" +
                            "var table  = document.getElementsByClassName(\"datadisplaytable\")[0];" +
                            "var titles = table.getElementsByClassName(\"ddtitle\");" +
                            "var defaults = table.getElementsByClassName(\"datadisplaytable\");" +
                            "window.HTMLOUT.processHTML(titles.length);" +
                            "for(var i=0;i<titles.length;i++){" +
                            "window.HTMLOUT.processHTML(titles[i].outerHTML+defaults[i].outerHTML);" +
                            "}" +
                            "window.HTMLOUT.processHTML('finished');",null);

                }
                firstPage++;
            }
        });
        w.loadUrl("https://banssbprod.tru.ca/banprod/bwckschd.p_disp_dyn_sched");
        final Button test= (Button)findViewById(R.id.testbtn);
        test.setOnTouchListener(new View.OnTouchListener() {
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
                        try {
                            schedules = new ArrayList<String>();
                            for(int i = 0; i < selectedCourses.size(); i++) {
                                String array = "";

                                int[][][] ai = new int[5][selectedCourses.size()][2];
                                generateSchedule(selectedCourses, i, array, ai);
                                if(schedules.size()>1000)
                                    break;
                            }
                            System.out.println("Schedule size before: "+schedules.size());
                            for( String s : schedules)
                                System.out.println(s);
                            removeDuplicates();
                            System.out.println("Schedule size after: "+schedules.size());
                            if(schedules.size()>100){
                                (Toast.makeText(context,"There are more than 100 results. Are you sure you need help with generating a schedule?",Toast.LENGTH_LONG)).show();
                            }
                            else {
                                Intent in = new Intent(MainActivity.this, ScheduleActivity.class);
                                in.putStringArrayListExtra("schedules", schedules);
                                startActivity(in);
                            }
                        }catch(Exception e){}
                        return true;
                }
                return false;
            }
        });

        //updateLV l = new updateLV(this);
        //l.execute();
    }

    public void select(int pos){
        lv.setItemChecked(pos,true);
    }

    public static void addRemoveCourse(int pos){
        lvListener[pos]=!lvListener[pos];
    }
    public static boolean isSelected(int pos){
        return lvListener[pos];
    }

    public void removeDuplicates(){
        Set<String> st = new HashSet<String>();
        st.addAll(schedules);
        schedules.clear();
        for(String s : st)
            if(s.split("@").length==numCourses)
                schedules.add(s);
        //schedules.addAll(st);
    }

    public String generateSchedule(ArrayList<JSONObject> a,int index, String s, int[][][] arr) throws JSONException{
        if(schedules.size()<=100) {
            JSONObject j = a.get(index);
            String[] tempTimeArray = j.get("time").toString().split("@");
            String[] tempDayArray = j.get("day").toString().split("@");
            boolean conflict = false;
            int[][][] tempArr = new int[arr.length][arr[0].length][2];
            for (int b = 0; b < arr.length; b++)
                for (int c = 0; c < arr[b].length; c++)
                    System.arraycopy(arr[b][c], 0, tempArr[b][c], 0, arr[b][c].length);
            outerloop:
            for (int i = 0; i < tempTimeArray.length; i++) {
                int[] tempTime = convertTime(tempTimeArray[i]);
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
                    for (int[] t : tempArr[ind]) {
                        if (((t[0] + t[1] < tempTime[0] + tempTime[1]) && (t[0] + t[1] > tempTime[0])) ||
                                (t[0] > tempTime[0]) && (t[0] < tempTime[0] + tempTime[1]) ||
                                t[0] == tempTime[0] || t[1] + t[0] == tempTime[0] + tempTime[1]) {
                            conflict = true;
                            break outerloop;
                        }
                    }
                    for (int p = 0; p < tempArr[ind].length; p++)
                        if (tempArr[ind][p][0] == 0 && tempArr[ind][p][1] == 0) {
                            tempArr[ind][p] = tempTime;
                            break;
                        }
                }
            }
            if (!conflict && j.has("labs"))
                for (JSONObject jLab : (ArrayList<JSONObject>) j.get("labs")) {
                    tempTimeArray = jLab.get("time").toString().split("@");
                    tempDayArray = jLab.get("day").toString().split("@");
                    outerloop:
                    for (int i = 0; i < tempTimeArray.length; i++) {
                        int[] tempTime = convertTime(tempTimeArray[i]);
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
                            for (int[] t : tempArr[ind]) {
                                if (((t[0] + t[1] < tempTime[0] + tempTime[1]) && (t[0] + t[1] > tempTime[0])) ||
                                        (t[0] > tempTime[0]) && (t[0] < tempTime[0] + tempTime[1]) ||
                                        t[0] == tempTime[0] || t[1] + t[0] == tempTime[0] + tempTime[1]) {
                                    conflict = true;
                                    break outerloop;
                                }
                            }
                            for (int p = 0; p < tempArr[ind].length; p++)
                                if (tempArr[ind][p][0] == 0 && tempArr[ind][p][1] == 0) {
                                    tempArr[ind][p] = tempTime;
                                    break;
                                }
                        }
                    }
                }

            if (!conflict) {
                s += j.get("name") + "@";
                arr = tempArr;
            }

            if (index < selectedCourses.size() - 1) {

                for (int i = index + 1; i < selectedCourses.size(); i++) {
                    generateSchedule(selectedCourses, i, s, arr);
                }
            }
            if (auto)
                if (s.split("@").length > numCourses)
                    numCourses = s.split("@").length;
            if (s.split("@").length == numCourses)
                schedules.add(s);
            return s;
        }
        else return "null";
    }

    public static JSONObject getCourseByName(String s) throws JSONException{
        for(JSONObject j : selectedCourses)
            if(j.get("name").toString().equals(s))
                return j;
        return null;
    }

    public int[] convertTime(String s){
        int result[] = new int[2];
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

    public JSONObject writeJSON(String name, String days, String time, String room, String prof,String shortN) {
        JSONObject course = new JSONObject();
        try {
            course.put("name", name);
            course.put("day", days);
            course.put("time", time);
            course.put("room",room);
            course.put("prof",prof);
            course.put("fullname",shortN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return course;
    }
    public void addCourse(String name, String days, String time, String room, String prof, String shortName) throws JSONException{
        if(courses == null)
            courses = new JSONObject[0];
        courses = Arrays.copyOf(courses,courses.length+1);
        courses[courses.length-1] = writeJSON(name,days,time,room,prof,shortName);
    }
    public void addLabToCourse(String labName, String days, String time, String room, String prof, String courseName,String full,String courseNum) throws JSONException{
        if(courses != null){
            for(JSONObject j : courses) {
                String courseTemp = j.get("name").toString();
                Pattern p = Pattern.compile("("+courseNum+")");
                Matcher m = p.matcher(courseTemp);
                if (m.find()) {
                    ArrayList<JSONObject> aj;
                    if(!j.has("labs"))
                        aj = new ArrayList<JSONObject>();
                    else
                        aj = (ArrayList<JSONObject>)j.get("labs");
                    JSONObject tj = new JSONObject();
                    tj.put("name", labName);
                    tj.put("day", days);
                    tj.put("time", time);
                    tj.put("room",room);
                    tj.put("prof",prof);
                    tj.put("fullname", full);
                    aj.add(tj);
                    j.put("labs",aj);
                }
                /*else{
                    System.out.println("test3");
                    p = Pattern.compile("("+courseName+")");
                    m = p.matcher(courseTemp);
                    if (m.find()) {
                        System.out.println("test4");
                        ArrayList<JSONObject> aj;
                        if(!j.has("labs"))
                            aj = new ArrayList<JSONObject>();
                        else
                            aj = (ArrayList<JSONObject>)j.get("labs");
                        JSONObject tj = new JSONObject();
                        tj.put("name", labName);
                        tj.put("day", days);
                        tj.put("time", time);
                        tj.put("room",room);
                        tj.put("prof",prof);
                        tj.put("fullname", full);
                        aj.add(tj);
                        j.put("labs",aj);
                    }
                }*/
            }
        }
    }


    public static String setLabsArrayList(int position) throws JSONException{
        ArrayList<JSONObject> aj = (ArrayList<JSONObject>)courses[position].get("labs");
        String temp = "";
        for(JSONObject j : aj) {
            temp += j.get("name").toString() + "@";
        }
        return temp;
    }

    public static void addToCourses(String s) throws JSONException{
        if(selectedCourses == null)
            selectedCourses = new ArrayList<JSONObject>();


        /*Pattern pLab = Pattern.compile("([A-Z]{3,4} [0-9]{3,4} - [0-9]{1,2})");
        Matcher m = pLab.matcher(s);
        String courseN = "";
        if(m.find())
            courseN = m.group(1);
        pLab = Pattern.compile("("+courseN+")");*/
        hasLabs = false;
        JSONObject newJ = new JSONObject();
        for(JSONObject j : courses) {
            //m = pLab.matcher(j.get("name").toString());
            if (j.get("name").toString().equals(s)) {
                newJ.put("name", j.get("name").toString());
                newJ.put("day", j.get("day").toString());
                newJ.put("time", j.get("time").toString());
                newJ.put("prof", j.get("prof").toString());
                newJ.put("room", j.get("room").toString());
                newJ.put("fullname", j.get("fullname").toString());
                if(j.has("labs"))
                    hasLabs = true;
            }
        }
        selectedCourses.add(newJ);
    }

    public static String getFullLabName(String s) throws JSONException{
        for(JSONObject j : courses)
            if(j.has("labs")){
                for(JSONObject g : (ArrayList<JSONObject>)j.get("labs"))
                    if(g.get("name").equals(s))
                        return g.get("fullname").toString();
            }
        return null;
    }

    public static void addLabToCourses(String s, String cName) throws  JSONException{
        Pattern pLab = Pattern.compile("([A-Z]{3,4} [0-9]{3,4} - [A-Z][0-9]{1,2})");
        Matcher m = pLab.matcher(s);
        String courseN = "";
        JSONObject labToAdd = new JSONObject();
        if(m.find())
            courseN = m.group(1);
        courseN = courseN.substring(0,courseN.length()-3)+courseN.substring(courseN.length()-2);
        pLab = Pattern.compile("("+courseN+")");
        for(JSONObject jc : courses) {
            courseN = jc.get("name").toString();
            //m = pLab.matcher(courseN);
            if (cName.equals(courseN)) {
                ArrayList<JSONObject> T2 = (ArrayList<JSONObject>) jc.get("labs");
                if (T2 != null) {
                    for(JSONObject j2 : T2){
                        if(j2.get("fullname").toString().equals(s)) {
                            labToAdd = j2;
                        }
                    }
                }
                break;
            }
        }
        for(JSONObject j : selectedCourses) {
            if (j.get("name").toString().equals(cName)) {
                ArrayList<JSONObject> T;
                if(j.has("labs")) {
                    T = (ArrayList<JSONObject>) j.get("labs");
                    if (T == null) {
                        T = new ArrayList<JSONObject>();
                    }
                    T.add(labToAdd);
                }
                else{
                    T = new ArrayList<JSONObject>();
                    T.add(labToAdd);
                }
                j.put("labs",T);
            }
        }
    }

    public static void removeFromCourses(String s) throws JSONException{
        ArrayList<JSONObject> temp = new ArrayList<JSONObject>();
        if(selectedCourses != null) {
            for (JSONObject j : selectedCourses)
                if (!j.get("name").toString().equals(s))
                    temp.add(j);
            selectedCourses = temp;
        }
    }

    public static void removeLabFromCourses(String s, String courseN) throws JSONException{
        if(selectedCourses != null)
            for(JSONObject j : selectedCourses) {
                if(j.get("name").toString().equals(courseN)) {
                    ArrayList<JSONObject> aj = (ArrayList<JSONObject>) j.get("labs");
                    int tempSize = aj.size();
                    for (int i = 0; i < tempSize; i++) {
                        JSONObject jt = aj.get(i);
                        if (jt.get("name").toString().equals(s)) {
                            aj.remove(i);
                            tempSize--;
                            i--;
                        }
                    }
                }
            }
    }

    public static void removeLabsFromCourse(String s){
        if(selectedCourses!=null)
            for(JSONObject j : selectedCourses){
                if(j.has("labs"))
                    j.remove("labs");
            }
    }

    public static void addCourseToArray(String s){ lvcourses.add(s); }

    public void generateList(Context c){
        TextView tv = (TextView)findViewById(R.id.mainnocoursestv);
        LoadingActivity.la.finish();
        if(true){//if(lvcourses.size()>0) {
            tv.setVisibility(View.GONE);
            MyListViewAdapter adapter = new MyListViewAdapter(lvcourses, c);
            lv.setAdapter(adapter);
            //if (lvListener == null)
                lvListener = new boolean[lvcourses.size()];
        }
        else{
            tv.setText("Sorry, there are no courses available.");
            TableRow.LayoutParams parDesc = new TableRow.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT,ViewPager.LayoutParams.MATCH_PARENT,1f);
            tv.setLayoutParams(parDesc);
            tv.setTextSize(30);
            tv.setTextColor(Color.CYAN);
            tv.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
            tv.setVisibility(View.VISIBLE);
        }
    }

    public static ArrayList<String> getArrayListOfCourses(){
        return lvcourses;
    }

    class MyJavaScriptInterface
    {
        Context context;
        public MyJavaScriptInterface(Context c){
            context=c;
        }
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            String courseName="";
            String courseTime="";
            String courseDay="";
            String courseProf="";
            String coursePlace="";
            Pattern pName = Pattern.compile("crn_in=[0-9]{5}\">(.*?)</a>");
            Matcher m = pName.matcher(html);
            if(m.find()) {
                courseName = m.group(1);
            }
            Pattern pTime = Pattern.compile("([0-9]{1,2}:[0-9]{1,2} [a-z]{2} - [0-9]{1,2}:[0-9]{1,2} [a-z]{2})");
            m = pTime.matcher(html);
            while(m.find()){
                courseTime+=m.group(1)+"@";
            }
            Pattern pDay = Pattern.compile("class=\"dddefault\">([A-Z]{1,5})</td>");
            m = pDay.matcher(html);
            while(m.find()){
                courseDay+=m.group(1)+"@";
            }
            Pattern pPlace = Pattern.compile("class=\"dddefault\">([A-Za-z ]* [0-9]*)</td>");
            m = pPlace.matcher(html);
            while(m.find()){
                coursePlace+=m.group(1)+"@";
            }
            Pattern pProf = Pattern.compile("class=\"dddefault\">([\"A-Za-z'. ]*) [(]<abbr");
            m = pProf.matcher(html);
            while(m.find()){
                courseProf+=m.group(1)+"@";
            }

            Pattern pLab = Pattern.compile("([A-Z]{3,4} [0-9]{3,4} - [A-Z][0-9]{1,2})");
            m = pLab.matcher(courseName);
            try {
                if(!courseDay.isEmpty() && !courseTime.isEmpty()) {
                    String tempCName = "";
                    pName = Pattern.compile("(.*?) [(][0-9],[0-9]");
                    Matcher m2 = pName.matcher(courseName);
                    if(m2.find()){
                        tempCName = m2.group(1);
                    }
                    else{
                        pName = Pattern.compile("(.*?) - [0-9]{3,5}");
                        m2 = pName.matcher(courseName);
                        if(m2.find())
                            tempCName = m2.group(1);
                    }
                    pName = Pattern.compile("([A-Z]{3,4} [A-Z]{0,1}[0-9]{3,4})");
                    m2 = pName.matcher(courseName);
                    if(m2.find()){
                        tempCName+=" - "+m2.group(1);
                    }
                    pName = Pattern.compile("[0-9]{2,4}( - [A-Z]{0,1}[0-9]{1,2}[A-Z]{0,1})");
                    m2 = pName.matcher(courseName);
                    if(m2.find()){
                        tempCName+=m2.group(1);
                    }
                    if(!m.find()) {
                        addCourse(tempCName, courseDay, courseTime, coursePlace, courseProf,courseName);
                        MainActivity.addCourseToArray(tempCName);
                    }
                    else{
                        addLabToCourse(tempCName, courseDay, courseTime, coursePlace, courseProf, tempCName.split(" - ")[0],courseName,tempCName.split(" - ")[1]);//m.group(1).split((" - "))[0]);
                    }
                }

            }catch(Exception e){}

            if(html.equals("finished")) {
                class OneShotTask implements Runnable {
                    public void run() {
                        generateList(context);
                    }
                }
                runOnUiThread(new OneShotTask());
            }
        }
    }
}

