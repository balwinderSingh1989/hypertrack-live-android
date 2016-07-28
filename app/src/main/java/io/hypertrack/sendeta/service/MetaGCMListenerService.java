package io.hypertrack.sendeta.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.view.BusinessProfile;
import io.hypertrack.sendeta.view.Home;

/**
 * Created by piyush on 27/07/16.
 */
public class MetaGCMListenerService extends GcmListenerService {

    private static final String TAG = MetaGCMListenerService.class.getSimpleName();

    private static final String KEY_NOTIFICATION_TYPE = "type";

    // Notification Types
    public static final String NOTIFICATION_TYPE_DEFAULT = "default";
    public static final String NOTIFICATION_TYPE_TASK_CREATED = "task.created";
    public static final String NOTIFICATION_TYPE_ACCEPT_INVITE = "accept";

    // Notification Ids for NOTIFICATION_TYPE
    public static final int NOTIFICATION_TYPE_DEFAULT_ID = 1;
    public static final int NOTIFICATION_TYPE_TASK_CREATED_ID = 2;
    public static final int NOTIFICATION_TYPE_ACCEPT_INVITE_ID = 3;

    public static final String NOTIFICATION_KEY_MESSAGE = "message";
    public static final String NOTIFICATION_KEY_TITLE = "title";

    // Notification keys for NOTIFICATION_TYPE_TASK_CREATED
    public static final String NOTIFICATION_KEY_ACCOUNT_ID = "account_id";
    public static final String NOTIFICATION_KEY_TASK = "task";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String title = data.getString(NOTIFICATION_KEY_TITLE);
        String message = data.getString(NOTIFICATION_KEY_MESSAGE);
        Log.d(TAG, message);
        Log.d(TAG, data.toString());

        int notificationId = getNotificationType(data);

        // Parse GCM payload
        Intent intent = parseGCMData(notificationId, data);

        // Generate & Send the notification with the given parameters
        sendNotification(notificationId, title, message, intent);
    }

    private int getNotificationType(Bundle data) {
        if (NOTIFICATION_TYPE_TASK_CREATED.equalsIgnoreCase(
                data.getString(KEY_NOTIFICATION_TYPE)))
            return NOTIFICATION_TYPE_TASK_CREATED_ID;

        if(NOTIFICATION_TYPE_ACCEPT_INVITE.equalsIgnoreCase(
                data.getString(KEY_NOTIFICATION_TYPE)))
            return NOTIFICATION_TYPE_ACCEPT_INVITE_ID;

        return NOTIFICATION_TYPE_DEFAULT_ID;
    }

    private Intent parseGCMData(int notificationId, Bundle data) {
        Intent intent = new Intent();

        // Check if the notification is NOTIFICATION_TYPE_TASK_CREATED type
        if (data != null && notificationId == NOTIFICATION_TYPE_TASK_CREATED_ID) {

            // Set Push Destination (Task.Created) Intent For Home Screen
            intent.setClass(getApplicationContext(), Home.class);
            intent.putExtra(Constants.KEY_PUSH_DESTINATION, true);
            intent.putExtra(Constants.KEY_ACCOUNT_ID, data.getString(NOTIFICATION_KEY_ACCOUNT_ID));
            intent.putExtra(Constants.KEY_TASK, data.getString(NOTIFICATION_KEY_TASK));

            return intent;

        }

        if (data != null && notificationId == NOTIFICATION_TYPE_ACCEPT_INVITE_ID) {

            // Set Intent For BusinessProfile Screen to handle Pending Membership Invites
            intent.setClass(getApplicationContext(), BusinessProfile.class);
            intent.putExtra(BusinessProfile.KEY_MEMBERSHIP_INVITE, true);

            return intent;
        }

        // Set Default Intent For Home Screen
        intent.setClass(getApplicationContext(), Home.class);
        return intent;
    }

    private void sendNotification(int notificationId, String title, String message, Intent intent) {

        // Gets an instance of notification builder object
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        // Sets the notification id to menu id
        final int mNotificationId = notificationId;

        // Check if a valid intent was provided
        if (intent == null)
            return;

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // the requestCode has been set to mNotificationId because the pending intent used to be replaced
        // for an old un-clicked notification when a new notification is received
        // (due to use of PendingIntent.FLAG_UPDATE_CURRENT which replaces the existing pending
        // intent with same request code)
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), mNotificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Check if proper message was provided
        if (TextUtils.isEmpty(message))
            message = getApplicationContext().getString(R.string.notification_fallback_message);

        if (TextUtils.isEmpty(title))
            title = getApplicationContext().getString(R.string.app_name);

        // Set the default Notification Sound for the current notification
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(message);

        // Sets the parameters required for standard notification
        builder.setStyle(bigTextStyle)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_logo_notification_small)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_logo_notification_large))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Send the Notification with the specified parameters
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId /* ID of notification */, builder.build());
    }
}
