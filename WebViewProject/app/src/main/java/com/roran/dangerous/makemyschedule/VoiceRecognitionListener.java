package com.roran.dangerous.makemyschedule;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

import static java.lang.System.out;

/**
 * Created by dangerous on 12/03/17.
 */

public class VoiceRecognitionListener implements RecognitionListener {

    private static VoiceRecognitionListener instance = null;

    IVoiceControl listener; // This is your running activity (we will initialize it later)

    static VoiceRecognitionListener getInstance() {
        if (instance == null) {
            instance = new VoiceRecognitionListener();
        }
        return instance;
    }

    private VoiceRecognitionListener() { }

    void setListener(IVoiceControl listener) {
        out.println("setListener");
        this.listener = listener;
    }

    public void processVoiceCommands(ArrayList<String> voiceCommands) {
        listener.processVoiceCommands(voiceCommands);
    }

    // This method will be executed when voice commands were found
    public void onResults(Bundle data) {
        out.println("onResults");
        ArrayList<String> voiceText = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        /*ArrayList matches = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String[] commands = new String[matches.size()];*/
        //commands = matches.toArray(commands);
        processVoiceCommands(voiceText);
    }

    // User starts speaking
    public void onBeginningOfSpeech() {
        out.println("OnBeginning");
        listener.setActive(true);
    }

    public void onBufferReceived(byte[] buffer) { }

    // User finished speaking
    public void onEndOfSpeech() {
        out.println("Waiting for result...");
    }

    // If the user said nothing the service will be restarted
    public void onError(int error) {
        out.println("error "+error);
        if (listener != null && error!=8) {
            listener.restartListeningService();
        }
        else if(error == 8) listener.stopListening();
    }
    public void onEvent(int eventType, Bundle params) { }

    public void onPartialResults(Bundle partialResults) { }

    public void onReadyForSpeech(Bundle params) {
        out.println("onReadyforSpeech");
    }

    public void onRmsChanged(float rmsdB) {
    }
}