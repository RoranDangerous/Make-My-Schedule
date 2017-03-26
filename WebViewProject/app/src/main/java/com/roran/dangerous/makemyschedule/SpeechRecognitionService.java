package com.roran.dangerous.makemyschedule;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by dangerous on 11/03/17.
 */

public class SpeechRecognitionService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("created service");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
