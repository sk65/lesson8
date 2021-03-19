package com.example.lesson8;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.lesson8.App.CHANNEL_ID;
import static com.example.lesson8.MainActivity.LOCATION_MODEL_LIST_SIZE;

public class LocationService extends Service {
    private static final String PACKAGE_NAME = "com.example.lesson8";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    private static final int NOTIFICATION_ID = 12345678;

    private static final int UPDATE_SMALLEST_DISPLACEMENT = 5;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    private final IBinder locationServiceBinder = new LocationServiceBinder();
    private List<LocationModel> locationModels;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location location;
    private NotificationManager notificationManager;
    private LocationModel locationModel;

    @Override
    public void onCreate() {
        super.onCreate();
        locationModels = SharedPreferencesManager.getInstance().getLocationModels();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createLocationCallback();
        createLocationRequest();
        createFusedLocationClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("dev", "Location service onStartCommand() ");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("dev", "Location service onBind()");
        stopForeground(true);
        return locationServiceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i("dev", "Location service onRebind()");
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("dev", "Location service onUnbind()");
        startForeground(NOTIFICATION_ID, getNotification());
        return true;
    }

    @Override
    public void onDestroy() {
        Log.i("dev", "Location service onRebind()");
        super.onDestroy();
    }

    public void requestLocationUpdates() {
        Log.i("dev", "Location service onDestroy()");
        startService(new Intent(getApplicationContext(), LocationService.class));
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.myLooper());
    }

    public void sendData() {
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, locationModel);
        sendBroadcast(intent);
    }

    private void addLocationModel(LocationModel locationModel) {
        if (locationModels.size() >= LOCATION_MODEL_LIST_SIZE) {
            locationModels.remove(0);
        }
        locationModels.add(locationModel);
        SharedPreferencesManager.getInstance().putLocationModels(locationModels);
    }

    public void removeLocationUpdates() {
        Log.i("dev", "Location service removeLocationUpdates()");
        stopForeground(true);
        stopSelf();
    }

    private Notification getNotification() {
        Intent intent = new Intent(this, LocationService.class);

        CharSequence contextText = location.getLatitude() + " " + location.getLongitude();

        String locationTitle = getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));

        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(contextText)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(locationTitle)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(contextText)
                .setWhen(System.currentTimeMillis());
        return builder.build();
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    public class LocationServiceBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    private void createLocationRequest() {
        Log.i("test", "createLocationRequest()");
        locationRequest = LocationRequest.create();
        locationRequest.setSmallestDisplacement(UPDATE_SMALLEST_DISPLACEMENT);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        Log.i("dev", "createLocationCallback()");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
    }

    private void createFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void onNewLocation(Location location) {
        Log.i("dev", "onNewLocation() location " + location.getLongitude() + " " + location.getLatitude());
        locationModel = new LocationModel(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        Log.i("dev", "onNewLocation() location model " + locationModel.getLongitude() + " " + locationModel.getLatitude());
        addLocationModel(locationModel);
        this.location = location;
        sendData();

        if (serviceIsRunningInForeground(this)) {
            notificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }
}