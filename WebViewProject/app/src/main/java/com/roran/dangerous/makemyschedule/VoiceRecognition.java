package com.roran.dangerous.makemyschedule;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceRecognition extends Service {

    public boolean closed = false;
    public Runnable rb;
    public Handler mainHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Started onCreate");
        rb = new Runnable() {

            Intent recognizerIntent;
            SpeechRecognizer recognizer;
            RecognitionListener rl;
            void startNewService(){
                System.out.println("Starting new service");
                if(SemesterProgramActivity.voicecontrol) {
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
                                    ActivityManager am = (ActivityManager) SemesterProgramActivity.spa.getSystemService(Context.ACTIVITY_SERVICE);
                                    final ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                                    switch (k) {
                                        case "close":
                                        case "exit":
                                            closed = true;
                                            //closeApp();
                                            break;
                                        case "voice control off":
                                            //sw.setChecked(false);
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
                                            /*if (cn.getShortClassName().equals(".SemesterProgramActivity")) {
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
                                            }*/ /*else if (cn.getShortClassName().equals(".MainActivity")) {
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

                                        }*/
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
                    recognizer = SpeechRecognizer.createSpeechRecognizer(SemesterProgramActivity.spa);
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
        };

        if(mainHandler==null) {
            System.out.println("start");
            mainHandler = new Handler();
            mainHandler.post(rb);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Gets data from the incoming Intent
        String dataString = intent.getDataString();
        System.out.println("STARTED");


        // Do work here, based on the contents of dataString
        return null;
    }
}
