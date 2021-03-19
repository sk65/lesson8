package com.example.lesson8;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 34;
    private final String[] locationPermissions = new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};
    private LocationRecyclerViewAdapter viewAdapter;
    private List<LocationModel> locationModels;
    public static final int LOCATION_MODEL_LIST_SIZE = 50;
    private BroadcastReceiver broadcastReceiver;
    private final ServiceConnection locationServiceConnection = initLocalServiceConnection();
    private LocationService locationService;
    private boolean isLocationServiceBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("dev", "MainActivity onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("dev", "MainActivity onCreate()  isLocationServiceBind = " + isLocationServiceBind);
        locationModels = SharedPreferencesManager.getInstance().getLocationModels();
        initView();
        initBroadcastReceiver();
    }

    @Override
    protected void onStart() {
        Log.i("dev", "MainActivity onStart()");
        super.onStart();
        bindService(new Intent(this, LocationService.class), locationServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (isLocationServiceBind) {
            unbindService(locationServiceConnection);
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_item_start:
                Log.i("dev", "Main Activity onOptionsItemSelected() startButton");
                requestLocationUpdates();
                break;
            case R.id.toolbar_item_stop:
                Log.i("dev", "Main Activity onOptionsItemSelected() stopButton");
                locationService.removeLocationUpdates();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                requestLocationUpdates();
            } else {
                // Permission denied.
                Snackbar.make(
                        findViewById(R.id.constraintLayout_mainActivity_mainContainer),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, view -> startAppSettings())
                        .show();
            }
        }
    }

    private void startAppSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Log.i("dev", "Main Activity requestPermissions()");
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldProvideRationale) {
            Log.i("dev", "Main Activity requestPermissions() shouldProvideRationale");
            Snackbar.make(
                    findViewById(R.id.constraintLayout_mainActivity_mainContainer),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                locationPermissions,
                                REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE);
                    })
                    .show();
        } else {
            Log.i("dev", "Main Activity requestPermissions() else");
            ActivityCompat.requestPermissions(MainActivity.this,
                    locationPermissions,
                    REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestLocationUpdates() {
        Log.i("dev", "Main Activity requestLocationUpdates()");
        if (!checkPermissions()) {
            Log.i("dev", "Main Activity requestLocationUpdates() !checkPermissions()");
            requestPermissions();
        } else {
            Log.i("dev", "Main Activity requestLocationUpdates() else");
            locationEnabled();
            locationService.requestLocationUpdates();
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(LocationService.ACTION_BROADCAST);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private ServiceConnection initLocalServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) service;
                locationService = binder.getService();
                isLocationServiceBind = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                locationService = null;
                isLocationServiceBind = false;
            }
        };
    }

    private void initView() {
        initToolBar();
        initRecyclerView();
    }

    private void initRecyclerView() {
        viewAdapter = new LocationRecyclerViewAdapter(this, locationModels);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_activityMain_locationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(viewAdapter);
    }

    private void initToolBar() {
        setSupportActionBar(findViewById(R.id.toolbar_mainActivity));
    }

    void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("dev", "MainActivity initBroadcastReceiver()");
                 LocationModel locationModel = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
                if (locationModel != null) {
                    addLocationModel(locationModel);
                }
            }
        };
    }

    private void addLocationModel(LocationModel locationModel) {
        if (locationModels.size() >= LOCATION_MODEL_LIST_SIZE) {
            locationModels.remove(0);
            viewAdapter.notifyDataSetChanged();
        }
        Log.i("dev", "MainActivity addLocationModel " + locationModel.getLatitude() + " " + locationModel.getLongitude());
        locationModels.add(locationModel);
        viewAdapter.notifyDataSetChanged();
    }

    private void locationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            requestLocationSettings();
        }
    }

    private void requestLocationSettings() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.location_alert_title)
                .setMessage(R.string.locatio_alert_explanations)
                .setCancelable(false)
                .setPositiveButton(R.string.enable, (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}