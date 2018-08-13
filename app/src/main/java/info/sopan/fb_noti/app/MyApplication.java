package info.sopan.fb_noti.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

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

        /*startService(new Intent(this, BackgroundService.class));
        Log.d("FirebaseNoti", "service start in BackgroundService");*/

    }


    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

}
