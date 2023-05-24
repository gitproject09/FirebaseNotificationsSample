package info.sopan.fb_noti.app;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import androidx.multidex.MultiDex;

/**
 * Created by Sopan Ahmed on 28/09/16.
 * www.sopan.info
 */
public class MyApplication extends Application {


    private static MyApplication mInstance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());

        /*startService(new Intent(this, BackgroundService.class));
        Log.d("FirebaseNoti", "service start in BackgroundService");*/

    }


    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

}
