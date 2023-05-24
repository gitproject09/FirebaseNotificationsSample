package info.sopan.fb_noti.service;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import info.sopan.fb_noti.R;
import info.sopan.fb_noti.activity.MainActivity;
import info.sopan.fb_noti.app.Config;
import info.sopan.fb_noti.util.MyWorker;
import info.sopan.fb_noti.util.NotificationUtils;

import static info.sopan.fb_noti.app.Config.SHARED_PREF;

/**
 * Created by Sopan Ahmed on 28/09/16.
 * www.sopan.info
 */
public class MyFirebaseTestMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseTestMessagingService.class.getSimpleName();
    private NotificationUtils notificationUtils;

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        storeRegIdInPref(token);
        // sending reg id to your server
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void storeRegIdInPref(String token) {
       // SharePrefUtil.setSharePrefData(CommonConstant.SHARE_PREF_KEY_FIREBASE_REG_ID, token);
        getSharedPreferences(SHARED_PREF, Activity.MODE_PRIVATE).edit().putString("regId", token).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("FirebaseNoti", "onMessageReceived Called");
        Log.e("FirebaseNoti", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        /*if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (*//* Check if data needs to be processed by long running job *//* true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }*/

        handleDataResponse(remoteMessage);


        /*Map<String, String> data = remoteMessage.getData();
        String messageData = data.get("data");
        Log.d("FirebaseNoti", "Whole response : " + messageData);

        if (remoteMessage == null) return;

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

               *//* JSONObject dataObject = json.getJSONObject("data");

                String title = dataObject.getString("title");
                String message = dataObject.getString("message");
                String timestamp = dataObject.getString("timestamp");
                handleNotification(message);
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", message);

                showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);*//*

                handleDataMessage(json);
            } catch (Exception e) {
                Log.e("FirebaseNoti", "Exception: " + e.getMessage());
            }
        }*/
    }

    private void handleDataResponse(RemoteMessage remoteMessage) {
        Map<String, String> params = remoteMessage.getData();
        String jsonStr = params.get("data");
        Log.i(TAG, "FireBase -> JSON: " + jsonStr);

        try {

            JSONObject jsonObject = new JSONObject(jsonStr);
            final int id = jsonObject.getInt("id");
            final String notificationType = jsonObject.getString("notificationType");

            final String title = jsonObject.getString("title");
            final String message = jsonObject.getString("message");
            final String description = jsonObject.getString("description");
            final String image = jsonObject.getString("imageUrl");
            final String timestamp = jsonObject.getString("timestamp");

            Glide.with(this).asBitmap().load(image).listener(new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    showNotification(title, description, null, id);
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    showNotification(title, description, resource, id);
                    return false;
                }
            }).submit();
        } catch (JSONException e) {
            e.printStackTrace();
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

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class).build();
        WorkManager.getInstance(this).beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }
    private void showNotification(String title, String description, Bitmap bitmap, int id) {
        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("id", id);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setTicker(getString(R.string.app_name))
                        .setSound(defaultSoundUri)
                        .setContentIntent(resultPendingIntent);

        if (bitmap != null) {
            NotificationCompat.BigPictureStyle bpStyle = new NotificationCompat.BigPictureStyle();
            bpStyle.bigPicture(bitmap);
            bpStyle.build();
            builder.setStyle(bpStyle);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence _name = getString(R.string.notification_channel_name);
            String _description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id), _name, importance);
            channel.setDescription(_description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(id, builder.build());
        } else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(id, builder.build());
        }
    }
}
