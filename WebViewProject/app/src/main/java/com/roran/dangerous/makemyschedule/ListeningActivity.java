package com.roran.dangerous.makemyschedule;

/**
 * Created by dangerous on 12/03/17.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public abstract class ListeningActivity extends Activity implements IVoiceControl {

    protected SpeechRecognizer sr;
    protected Context context;
    public boolean isActive;

    @Override
    protected void onResume() {
        super.onResume();
        restartListeningService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    // starts the service

    protected void startListening() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            Intent inten = new Intent(this,NoInternetConnection.class);
            startActivity(inten);
            finish();
        }
        else {
            if (SemesterProgramActivity.voicecontrol) {
                isActive = false;
                try {
                    initSpeech();
                    Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "org.twodee.andytest");
                    recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                    sr.startListening(recognizerIntent);
                } catch (Exception ex) {
                    System.out.println("something is wrong " + ex);
                }
                System.out.println("ssssstartListenint");
            }
        }
    }

    // stops the service
    @Override
    public void stopListening() {
        if (sr != null) {
            sr.stopListening();
            sr.cancel();
            sr.destroy();
        }
        sr = null;
        System.out.println("sssssstopListening");
    }

    protected void initSpeech() {
        //if (sr == null) {
            sr = SpeechRecognizer.createSpeechRecognizer(this);
            /*if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Toast.makeText(context, "Speech Recognition is not available",
                        Toast.LENGTH_LONG).show();
                finish();
            }*/
            sr.setRecognitionListener(VoiceRecognitionListener.getInstance());
        //}
    }

    @Override
    public void finish() {
        System.out.println("onFinish");
        stopListening();
        super.finish();
    }

    @Override
    protected void onStop() {
        System.out.println("onStop");
        //stopListening();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        System.out.println("onDestroy");
        if (sr != null) {
            sr.stopListening();
            sr.cancel();
            sr.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        System.out.println("onPause");
        /*if(sr!=null){
            sr.stopListening();
            sr.cancel();
            sr.destroy();

        }
        sr = null;*/

        super.onPause();

        //restartListeningService();
    }

    //is abstract so the inheriting classes need to implement it. Here you put your code which should be executed once a command was found
    @Override
    public abstract void processVoiceCommands(ArrayList<String> voiceCommands);

    @Override
    public void setActive(boolean t){
        isActive=t;
    }

    @Override
    public void restartListeningService() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            Intent inten = new Intent(this, NoInternetConnection.class);
            startActivity(inten);
            finish();
        }
        else {
            stopListening();
            startListening();
        }
    }
}
