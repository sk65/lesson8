package com.example.lesson8;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    private static final String PACKAGE_NAME = "com.example.lesson8";
    public static final String CHANNEL_ID = PACKAGE_NAME + ".channel_id";
    public static final String SERVICE_CHANNEL_NAME = PACKAGE_NAME + ".service_channel_name";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesManager.init(this);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    SERVICE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
