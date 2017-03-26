package com.roran.dangerous.makemyschedule;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by dangerous on 06/03/17.
 */

public class FirebaseID extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("tokens");
        myRef.child(refreshedToken).setValue("TRUE");

        //Displaying token on logcat
        System.out.println("test Refr "+refreshedToken);
        Log.d(TAG, "Refreshed token: " + refreshedToken);

    }
}
