package info.sopan.fb_noti.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import info.sopan.fb_noti.activity.MainActivity;
import info.sopan.fb_noti.app.Config;
import info.sopan.fb_noti.util.NotificationUtils;

/**
 * Created by Sopan Ahmed on 28/09/16.
 * www.sopan.info
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("FirebaseNoti", "onMessageReceived Called");
        Log.e("FirebaseNoti", "From: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();
        String messageData = data.get("data");
        Log.d("FirebaseNoti", "Whole response : " + messageData);

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("FirebaseNoti", "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.d("FirebaseNoti", "Data Payload: " + remoteMessage.getData().toString());
            //Log.d("FirebaseNoti", "Data Payload: " + remoteMessage.getData().get("message").toString());

            try {
               JSONObject json = new JSONObject(remoteMessage.getData().toString());

               /* JSONObject dataObject = json.getJSONObject("data");

                String title = dataObject.getString("title");
                String message = dataObject.getString("message");
                String timestamp = dataObject.getString("timestamp");
                handleNotification(message);
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", message);

                showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);*/

                handleDataMessage(json);
            } catch (Exception e) {
                Log.e("FirebaseNoti", "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
            Log.d("FirebaseNoti", "Message in App : " + message);
            Log.d("FirebaseNoti", "App Background e nai");
        } else {
            // If the app is in background, firebase itself handles the notification
            //app is in background, broadcast the push message

            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();

            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.putExtra("message", message);

            // check for image attachment
            Log.d("FirebaseNoti", "Message Background : " + message);
            Log.d("FirebaseNoti", "App Background e");
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e("FirebaseNoti", "push json: " + json.toString());

        try {
            JSONObject data = json.getJSONObject("data");

            String title = data.getString("title");
            String message = data.getString("message");
            // boolean isBackground = data.getBoolean("is_background");
            //String imageUrl = data.getString("image");
            String timestamp = data.getString("timestamp");
            // JSONObject payload = data.getJSONObject("payload");

            Log.e("FirebaseNoti", "title: " + title);
            Log.e("FirebaseNoti", "message: " + message);
            Log.e("FirebaseNoti", "timestamp: " + timestamp);
           /* Log.e("FirebaseNoti", "isBackground: " + isBackground);
            Log.e("FirebaseNoti", "payload: " + payload.toString());
            Log.e("FirebaseNoti", "imageUrl: " + imageUrl);
            Log.e("FirebaseNoti", "timestamp: " + timestamp);*/
           /* Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);*/

            // play notification sound
           /* NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();*/


            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            resultIntent.putExtra("message", message);
            showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
            // check for image attachment
               /* if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }*/

        } catch (JSONException e) {
            Log.e("FirebaseNoti", "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e("FirebaseNoti", "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
