package info.sopan.fb_noti.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import info.sopan.fb_noti.app.Config;
import info.sopan.fb_noti.util.NotificationUtils;

/**
 * Created by Sopan Ahmed on 28/09/16.
 * www.sopan.info
 */
public class BackgroundService extends Service {

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public BackgroundService()  {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {

        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    private MyBinder myBinder = new MyBinder();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);


        if(intent != null){


            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    // checking for type intent filter
                    if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                        // gcm successfully registered
                        // now subscribe to `global` topic to receive app wide notifications
                        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                        displayFirebaseRegId();

                    } /*else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                        // new push notification is received

                        String message = intent.getStringExtra("message");

                        Toast.makeText(getApplicationContext(), "Push notification from Back : " + message, Toast.LENGTH_LONG).show();

                    }*/
                }
            };

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.REGISTRATION_COMPLETE));

            // register new push message receiver
            // by doing this, the activity will be notified each time a new message arrives
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));

            // clear the notification area when the app is opened
            NotificationUtils.clearNotifications(getApplicationContext());


            Log.d("FirebaseNoti", "Firebase noti reg in Background Service");
            return START_STICKY;

        } else {
            return START_NOT_STICKY;

        }

    }

    @Override
    public void onDestroy() {
        Log.d("FirebaseNoti", "Firebase noti destroy in Background Service");

        super.onDestroy();

    }


    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        if (!TextUtils.isEmpty(regId))
            Log.e("FirebaseNoti", "Firebase reg id in Background service Class: " + regId);
        else
            Log.e("FirebaseNoti", "Firebase Reg Id is not received yet!");
    }

}
