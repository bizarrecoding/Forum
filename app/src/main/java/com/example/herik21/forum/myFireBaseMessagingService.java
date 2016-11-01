package com.example.herik21.forum;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class myFireBaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d("MessageService", "From: " + remoteMessage.getFrom());
        Log.d("MessageService", "Notification Message Body: " + remoteMessage.getNotification().getBody());
        FirebaseMessaging.getInstance().subscribeToTopic("movil");
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d("MessageService",s);
    }
}
