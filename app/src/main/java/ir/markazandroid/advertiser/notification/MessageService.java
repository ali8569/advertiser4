package ir.markazandroid.advertiser.notification;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.signal.Signal;


/**
 * Coded by Ali on 24/12/2017.
 */

public class MessageService extends FirebaseMessagingService {

    private static final String TAG = "message";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        }

        ((AdvertiserApplication)getApplicationContext()).getSignalManager()
                .sendMainSignal(new Signal("refresh",Signal.SIGNAL_REFRESH_RECORDS));

        // Check if message contains a notification payload.
        /*if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotif(this, remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());

        }*/

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /*public static void sendNotif(Context context, String title, String text) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setTicker(title);
        try {
            //      mBuilder.setLargeIcon(MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri));
        } catch (Exception e) {
            e.printStackTrace();
        }
// Creates an explicit intent for an Activity in your app
        Intent vaccine = new Intent(context, AuthenticationActivity.class);


// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        //  stackBuilder.addParentStack(ScheduleActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(vaccine);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(Math.random() + "", Double.valueOf(Math.random()).intValue(), mBuilder.build());
    }*/
}
