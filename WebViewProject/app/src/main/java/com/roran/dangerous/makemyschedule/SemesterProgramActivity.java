package com.roran.dangerous.makemyschedule;

import android.*;
import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.jar.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemesterProgramActivity extends ListeningActivity {

    public WebView wTerm;
    public ArrayList<String> terms = new ArrayList<String>();
    public ArrayList<String> termsID = new ArrayList<String>();
    public ArrayList<String> subjects = new ArrayList<String>();
    public  ArrayList<JSONObject> termSubject = new ArrayList<JSONObject>();
    public WebView wSubject;
    public String currentTerm = "";
    public Spinner sp1, sp2;
    public NumberPicker np;
    public boolean page = true;
    public ArrayList<String> numCourses;
    public static ArrayList<JSONObject> selectedCourses;
    public Context context;
    public Handler mainHandler;
    public boolean closed = false;
    public String[] valuesI;
    public Switch sw;
    public static boolean voicecontrol;
    public TextView cnumtv;
    public Runnable rb;
    public boolean oneTimeMessage;
    public static SemesterProgramActivity spa;
    public AdView mAdView;

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            Intent inten = new Intent(SemesterProgramActivity.this,NoInternetConnection.class);
            startActivity(inten);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester_program);

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            Intent inten = new Intent(this,NoInternetConnection.class);
            startActivity(inten);
            finish();
        }
        else {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            //database.setPersistenceEnabled(true);
            DatabaseReference myRef = database.getReference("tokens");
            if (FirebaseInstanceId.getInstance().getToken() != null)
                myRef.child(FirebaseInstanceId.getInstance().getToken()).setValue("TRUE");

            spa = this;


            AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            amanager.setStreamMute(AudioManager.STREAM_RING, true);
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

            //startService(new Intent(this,VoiceRecognition.class));

            cnumtv = (TextView) findViewById(R.id.numcoursestv);
            cnumtv.setVisibility(View.INVISIBLE);
            sw = (Switch) findViewById(R.id.switch1);
            voicecontrol = sw.isChecked();
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences sp = getSharedPreferences("myprefs", 0);
                    oneTimeMessage = sp.getBoolean("onetime", false);
                    System.out.println("oneTimeMessage " + isChecked);
                    voicecontrol = isChecked;
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("enabled", isChecked);
                    editor.apply();
                    if (isChecked) {
                        int permissionCheck = ContextCompat.checkSelfPermission(SemesterProgramActivity.this,
                                Manifest.permission.RECORD_AUDIO);
                        if(permissionCheck!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(SemesterProgramActivity.this,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    1);
                        }
                        if(ContextCompat.checkSelfPermission(SemesterProgramActivity.this,
                                Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                            sw.setChecked(false);
                        }
                        else {
                            if (!oneTimeMessage) {
                                Intent load = new Intent(SemesterProgramActivity.this, VoiceControllInstructions.class);
                                startActivity(load);
                            }
                            //open instructions window

                            context = getApplicationContext();
                            VoiceRecognitionListener.getInstance().setListener(SemesterProgramActivity.this); // Here we set the current listener
                            startListening();

                            CheckVR cvr = new CheckVR();
                            cvr.execute();
                        }
                    } else {
                        stopListening();
                    }
                }
            });

            SharedPreferences sp = getSharedPreferences("myprefs", 0);
            sw.setChecked(sp.getBoolean("enabled", false));
            //startListening();
            context = this;
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            AdView mAdView2 = (AdView) findViewById(R.id.adView2);
            AdRequest adRequest2 = new AdRequest.Builder().build();
            mAdView2.loadAd(adRequest2);

            //MobileAds.initialize(getApplicationContext(), "ca-app-pub-7537205919736031~3576909907");

        /*rb = new Runnable() {

            Intent recognizerIntent;
            SpeechRecognizer recognizer;
            RecognitionListener rl;
            void startNewService(){
                System.out.println("Starting new service");
                if(voicecontrol) {
                    if (recognizer != null)
                        recognizer.destroy();
                    rl = new RecognitionListener() {
                        @Override
                        public void onReadyForSpeech(Bundle params) {
                        }

                        @Override
                        public void onBeginningOfSpeech() {

                        }

                        @Override
                        public void onRmsChanged(float rmsdB) {
                        }

                        @Override
                        public void onBufferReceived(byte[] buffer) {
                        }

                        @Override
                        public void onEndOfSpeech() {
                        }

                        @Override
                        public synchronized void onError(int error) {
                            System.out.println("error " + error);
                            if (error != 8)
                                startNewService();
                        }

                        @Override
                        public void onResults(Bundle results) {
                            checkForCommands("Results: ", results);
                        }

                        @Override
                        public void onPartialResults(Bundle partialResults) {
                        }

                        private void checkForCommands(String s, Bundle bundle) {

                            ArrayList<String> voiceText = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                            if (voiceText != null)
                                for (String k : voiceText) {
                                    System.out.println(s + k);
                                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                                    final ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                                    switch (k) {
                                        case "close":
                                        case "exit":
                                            closed = true;
                                            closeApp();
                                            break;
                                        case "voice control off":
                                            sw.setChecked(false);
                                            break;
                                        case "back":
                                        case "go back":
                                            if (cn.getShortClassName().equals(".SemesterProgramActivity")) {
                                                closed = true;
                                                onBackPressed();
                                            } else if (cn.getShortClassName().equals(".MainActivity"))
                                                MainActivity.ma.finish();
                                            else if (cn.getShortClassName().equals(".ScheduleActivity"))
                                                ScheduleActivity.sa.finish();
                                            else if (cn.getShortClassName().equals(".chooseLabActivity"))
                                                chooseLabActivity.ca.finish();
                                            break;
                                        default:
                                            Pattern p;
                                            Matcher m;
                                            if (cn.getShortClassName().equals(".SemesterProgramActivity")) {
                                                if (k.split(" ").length == 2) {
                                                    p = Pattern.compile("([0-9]{4})");
                                                    m = p.matcher(k.split(" ")[1]);
                                                    if (m.find()) {
                                                        String year = m.group(1);
                                                        for (int i = 0; i < terms.size(); i++) {
                                                            if (((terms.get(i).charAt(0) + "").toLowerCase().concat(terms.get(i).substring(1))).equals(k)) {
                                                                sp1.setSelection(i);
                                                            }
                                                        }
                                                    }
                                                } else if (!(isInteger(k) || k.toLowerCase().equals("auto"))) {
                                                    if (k.length() == 4) {
                                                        for (int i = 1; i < subjects.size(); i++) {
                                                            if (subjects.get(i).split("-")[0].toLowerCase().equals(k)) {
                                                                sp2.setSelection(i);
                                                            }
                                                        }
                                                    } else {
                                                        for (int i = 1; i < subjects.size(); i++) {
                                                            if (subjects.get(i).split("-").length == 2)
                                                                if (subjects.get(i).split("-")[1].toLowerCase().equals(k)) {
                                                                    sp2.setSelection(i);
                                                                }
                                                        }
                                                    }
                                                } else {
                                                    if (k.toLowerCase().equals("auto"))
                                                        np.setValue(0);
                                                    else if (isInteger(k))
                                                        if (Integer.parseInt(k) >= 0 && Integer.parseInt(k) < 10)
                                                            np.setValue(Integer.parseInt(k));
                                                }
                                            } /*else if (cn.getShortClassName().equals(".MainActivity")) {
                                                ListView lv = new ListView(MainActivity.ma);
                                                lv.setSelectidItem(0,true);
                                                p = Pattern.compile("([0-9]{4} section [0-9]{1,2})");
                                                m = p.matcher(k.toLowerCase());
                                                if (m.find()) {
                                                    String[] tempArr = m.group(1).split(" section ");
                                                    for (int i = 0; i < MainActivity.courses.length; i++) {
                                                        try {
                                                            Pattern pNum = Pattern.compile("(" + tempArr[0] + " - [0]{0,1}" + tempArr[1] + ")");
                                                            Matcher mNum = pNum.matcher(MainActivity.courses[i].get("name").toString());
                                                            if (mNum.find()) {
                                                                //MainActivity.lv.setSelection(i);
                                                                //MainActivity.lvListener[i] = !MainActivity.lvListener[i];
                                                            }
                                                        } catch (JSONException e) {
                                                        }
                                                    }
                                                }
                                            } /*else if (cn.getShortClassName().equals(".chooseLabActivity")) {

                                        }
                                            break;
                                    }
                                }
                            if (!closed)
                                startNewService();
                        }

                        @Override
                        public void onEvent(int eventType, Bundle params) {
                            System.out.println("eventype "+eventType);
                        }
                    };
                    recognizer = SpeechRecognizer.createSpeechRecognizer(SemesterProgramActivity.this);
                    recognizer.setRecognitionListener(rl);
                    recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "org.twodee.andytest");
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                    recognizer.startListening(recognizerIntent);
                }
                else if(recognizer != null) recognizer.destroy();
            }

            @Override
            public void run() {
                startNewService();
            }
        };*/
        /*Intent myInt = new Intent(this,SpeechRecognitionService.class);
        //startActivity(myInt);
        SpeechRecognitionService sp = new SpeechRecognitionService();

        sp.startService(myInt);*/

            numCourses = new ArrayList<String>();
            numCourses.add("Auto");
            for (int i = 1; i < 11; i++)
                numCourses.add(i + "");
            terms.add("Choose semester");
            termsID.add("0");
            subjects.add("Choose program");

            wTerm = new WebView(this);
            wSubject = new WebView(this);
            sp1 = (Spinner) findViewById(R.id.spinner);
            sp2 = (Spinner) findViewById(R.id.spinner2);
            np = (NumberPicker) findViewById(R.id.numpicker);

            sp2.setVisibility(View.INVISIBLE);
            sp2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0) {
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
                            Intent inten = new Intent(SemesterProgramActivity.this, NoInternetConnection.class);
                            startActivity(inten);
                            finish();
                        } else {
                            String key = "";
                            Pattern p = Pattern.compile("([A-Z]{4})");
                            Matcher m = p.matcher(subjects.get(position));
                            if (m.find()) {
                                Intent in = new Intent(SemesterProgramActivity.this, MainActivity.class);
                                in.putExtra("term", termsID.get((int) sp1.getSelectedItemId()));
                                in.putExtra("subject", m.group());
                                in.putExtra("numC", np.getValue());
                                startActivity(in);
                                wSubject.destroy();
                                sp2.setSelection(0);
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        /*sp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sw.setChecked(false);
            }
        });
        np.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sw.setChecked(false);
            }
        });*/
            sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!termsID.get(position).equals(currentTerm)) {
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
                            Intent inten = new Intent(SemesterProgramActivity.this, NoInternetConnection.class);
                            startActivity(inten);
                            finish();
                        }
                        currentTerm = termsID.get(position);
                        selectedCourses = new ArrayList<JSONObject>();
                        if (position != 0) {
                            subjects = new ArrayList<String>();
                            subjects.add("Choose program");
                            final int pos = position;
                            wSubject = new WebView(context);
                            WebSettings webSettings2 = wSubject.getSettings();
                            webSettings2.setJavaScriptEnabled(true);
                            wSubject.addJavascriptInterface(new SemesterProgramActivity.MyJavaScriptInterface(), "HTMLOUT");
                            wSubject.setWebViewClient(new WebViewClient() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    if (page) {
                                        wSubject.evaluateJavascript("javascript:document.getElementById('term_input_id').value = " + termsID.get(pos) + ";document.getElementsByTagName('FORM')[0].submit();", null);
                                    } else {
                                        wSubject.evaluateJavascript("javascript:" +
                                                "var select = document.getElementById(\"subj_id\");" +
                                                "for(var i=0;i<select.options.length;i++){" +
                                                "window.HTMLOUT.processHTML2(select.options[i].outerHTML);}" +
                                                "window.HTMLOUT.processHTML2('finish')", null);
                                    }
                                    page = !page;
                                }
                            });
                            wSubject.loadUrl("https://banssbprod.tru.ca/banprod/bwckschd.p_disp_dyn_sched");
                        } else {
                            sp2.setVisibility(View.INVISIBLE);
                            np.setVisibility(View.INVISIBLE);
                            cnumtv.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            WebSettings webSettings = wTerm.getSettings();
            webSettings.setJavaScriptEnabled(true);
            wTerm.addJavascriptInterface(new SemesterProgramActivity.MyJavaScriptInterface(), "HTMLOUT");
            //w.addJavascriptInterface(this, "HtmlViewer");
            wTerm.setWebViewClient(new WebViewClient() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onPageFinished(WebView view, String url) {
                    wTerm.evaluateJavascript("javascript:" +
                            "var select = document.getElementById(\"term_input_id\");" +
                            "for(var i=0;i<select.options.length;i++){" +
                            "window.HTMLOUT.processHTML(select.options[i].outerHTML);}" +
                            "window.HTMLOUT.processHTML('finished');", null);
                }
            });
            wTerm.loadUrl("https://banssbprod.tru.ca/banprod/bwckschd.p_disp_dyn_sched");


            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    System.out.println("Ads updated");
                    //restartListeningService();
                /*if(mainHandler!=null && rb!=null)
                mainHandler.post(rb);*/
                }
            });
            mAdView2.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    System.out.println("Ads2 updated");
                    //restartListeningService();
                /*if(mainHandler!=null && rb!=null)
                mainHandler.post(rb);*/
                }
            });
        /*if(mainHandler==null && voicecontrol) {
            mainHandler = new Handler();
            mainHandler.post(rb);
        }*/
            // starts listening
        }
    }

    // Here is where the magic happens
    @Override
    public void processVoiceCommands(ArrayList<String> voiceText) {
        if (voiceText != null)
            for (String k : voiceText) {
                System.out.println("command " + k);
                ActivityManager am = (ActivityManager) SemesterProgramActivity.spa.getSystemService(Context.ACTIVITY_SERVICE);
                final ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                switch (k) {
                    case "close":
                    case "exit":
                        closed = true;
                        closeApp();
                        break;
                    case "voice control off":
                        sw.setChecked(false);
                        break;
                    case "ad":
                        mAdView.performClick();
                        break;
                    case "back":
                    case "go back":
                        if (cn.getShortClassName().equals(".SemesterProgramActivity")) {
                            closed = true;
                            SemesterProgramActivity.spa.onBackPressed();
                        } else if (cn.getShortClassName().equals(".MainActivity"))
                            MainActivity.ma.finish();
                        else if (cn.getShortClassName().equals(".ScheduleActivity"))
                            ScheduleActivity.sa.finish();
                        else if (cn.getShortClassName().equals(".chooseLabActivity"))
                            chooseLabActivity.ca.finish();
                        break;
                    default:
                        Pattern p;
                        Matcher m;
                        if (cn.getShortClassName().equals(".SemesterProgramActivity")) {
                            if (k.split(" ").length == 2) {
                                p = Pattern.compile("([0-9]{4})");
                                m = p.matcher(k.split(" ")[1]);
                                if (m.find()) {
                                    String year = m.group(1);
                                    for (int i = 0; i < terms.size(); i++) {
                                        if (((terms.get(i).charAt(0) + "").toLowerCase().concat(terms.get(i).substring(1))).equals(k)) {
                                            sp1.setSelection(i);
                                        }
                                    }
                                }
                            } else if (!(isInteger(k) || k.toLowerCase().equals("auto"))) {
                                if (k.length() == 4) {
                                    for (int i = 1; i < subjects.size(); i++) {
                                        if (subjects.get(i).split("-")[0].toLowerCase().equals(k.toLowerCase())) {
                                            sp2.setSelection(i);
                                        }
                                    }
                                } else {
                                    for (int i = 1; i < subjects.size(); i++) {
                                        if (subjects.get(i).split("-").length == 2)
                                            if (subjects.get(i).split("-")[1].toLowerCase().equals(k.toLowerCase())) {
                                                sp2.setSelection(i);
                                            }
                                    }
                                }
                            } else {
                                if (k.toLowerCase().equals("auto"))
                                    np.setValue(0);
                                else if (isInteger(k))
                                    if (Integer.parseInt(k) >= 0 && Integer.parseInt(k) < 10)
                                        np.setValue(Integer.parseInt(k));
                            }
                        } /*else if (cn.getShortClassName().equals(".MainActivity")) {
                                                ListView lv = new ListView(MainActivity.ma);
                                                lv.performClick();
                                                p = Pattern.compile("([0-9]{4} section [0-9]{1,2})");
                                                m = p.matcher(k.toLowerCase());
                                                if (m.find()) {
                                                    String[] tempArr = m.group(1).split(" section ");
                                                    for (int i = 0; i < MainActivity.courses.length; i++) {
                                                        try {
                                                            Pattern pNum = Pattern.compile("(" + tempArr[0] + " - [0]{0,1}" + tempArr[1] + ")");
                                                            Matcher mNum = pNum.matcher(MainActivity.courses[i].get("name").toString());
                                                            if (mNum.find()) {
                                                                //MainActivity.lv.setSelection(i);
                                                                //MainActivity.lvListener[i] = !MainActivity.lvListener[i];
                                                            }
                                                        } catch (JSONException e) {
                                                        }
                                                    }
                                                }
                                            } else if (cn.getShortClassName().equals(".chooseLabActivity")) {

                                        }*/
                        break;
                }
            }
        if (!closed) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
                Intent inten = new Intent(this, NoInternetConnection.class);
                startActivity(inten);
                finish();
            }
            else
            restartListeningService();
        }
    }


    public void createTermSpinner(){
        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, terms);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        terms);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_checked);
        sp1.setAdapter(adapter);
        wTerm.destroy();
    }

    public void createSubjectSpinner(){
        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,subjects);
        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,R.layout.spinnerlayout,R.id.tvspinner,subjects);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        MySpinnerAdapter dataAdapter = new MySpinnerAdapter(subjects,this);
        sp2.setAdapter(adapter);
        sp2.setVisibility(View.VISIBLE);
        np.setVisibility(View.VISIBLE);
        cnumtv.setVisibility(View.VISIBLE);
        np.setMinValue(0);
        np.setMaxValue(9);
        valuesI = new String[]{"Auto","1","2","3","4","5","6","7","8","9"};
        np.setDisplayedValues(valuesI);
    }

    class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
                Pattern p = Pattern.compile("value=\"([0-9]*)\">");
                Matcher m = p.matcher(html);
                String termID = "";
                if (m.find())
                    termID = m.group(1);
                p = Pattern.compile(">([A-Za-z]{2,9} [0-9]{4}) \\(");
                m = p.matcher(html);
                String termName = null;
                if (m.find())
                    termName = m.group(1);
                if (termName != null) {
                    terms.add(termName);
                    termsID.add(termID);
                }
                if (html.equals("finished")) {
                    class OneShotTask implements Runnable {
                        public void run() {

                            createTermSpinner();
                        }
                    }
                    runOnUiThread(new OneShotTask());
                }
        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML2(String html)
        {
            Pattern p = Pattern.compile("\">(.*?)</");
            Matcher m = p.matcher(html);
            String subjectFull = "";
            if (m.find())
                subjectFull = m.group(1);
            subjects.add(subjectFull);

            if (html.equals("finish")) {
                class OneShotTask implements Runnable {
                    public void run() {
                        createSubjectSpinner();
                    }
                }
                runOnUiThread(new OneShotTask());
            }
        }
    }

    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    /*public void startListening(){
        mainHandler = new Handler();
        closed = false;


        mainHandler.post(rb);
    }*/

    public boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {}
        return false;
    }

    public void closeApp(){
        if(Build.VERSION.SDK_INT>=21)
            finishAndRemoveTask();
        else
            this.finishAffinity();
        System.exit(0);
    }

    public class CheckVR extends AsyncTask<String, Integer,String>{

        boolean timer = true;
        @Override
        protected String doInBackground(String... params) {
            long time = System.currentTimeMillis();
            while(time > 0){
                if(time % 10000 == 0){
                    if(timer) {
                        if(voicecontrol) {
                            System.out.println("still working");
                            class OneShot implements Runnable{
                                @Override
                                public void run() {
                                    restartListeningService();
                                }
                            }
                            runOnUiThread(new OneShot());
                        }
                    }
                    timer = false;
                } else timer = true;
                time = System.currentTimeMillis();
                if(!checkConnection()) {
                    class OneShot implements Runnable{
                        @Override
                        public void run() {
                            restartListeningService();
                        }
                    }
                    runOnUiThread(new OneShot());
                    break;
                }
            }
            return null;
        }
    }
}
