package com.roran.dangerous.makemyschedule;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by dangerous on 06/03/17.
 */

public class MessageReceiveActivity extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        System.out.println("test youououo");
        System.out.println("test "+remoteMessage.getFrom());
        System.out.println("test "+remoteMessage.getNotification());
        System.out.println("test "+remoteMessage.getData());
        System.out.println("test "+remoteMessage.getData().get("message"));
        if(remoteMessage.getNotification()!=null)
            Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        //Calling method to generate notification

        sendNotification(remoteMessage.getData().get("message"));
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, SemesterProgramActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (android.support.v7.app.NotificationCompat.Builder)new NotificationCompat.Builder(this)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Firebase Push Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());

    }
}