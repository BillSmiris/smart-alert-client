package org.unipi.mpsp2343.smartalert.services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.unipi.mpsp2343.smartalert.DbProvider;
import org.unipi.mpsp2343.smartalert.LocalStorage;
import org.unipi.mpsp2343.smartalert.NotificationHelper;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;
import org.unipi.mpsp2343.smartalert.dto.SavedAlert;
import org.unipi.mpsp2343.smartalert.dto.SendAlertDto;

import java.util.Date;
import java.util.List;

//Service that checks periodically the server for alerts
public class AlertService extends Service {
    private static final long CHECK_ALERTS_INTERVAL = 10000; //Interval for checking for alerts
    private static final long LOCATION_TIMEOUT = 5000; //Timeout for waiting for a location update
    public String PM_TAG = "PowerManager";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler locationTimeoutHandler;
    private HandlerThread alertHandlerThread;
    private Handler alertHandler;
    private boolean isLocationReceived = false;
    Context context;
    DbProvider db;
    NotificationHelper notificationHelper;
    PowerManager.WakeLock wakeLock;
    PowerManager powerManager;
    LocalStorage localStorage;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationTimeoutHandler = new Handler();
        alertHandlerThread = new HandlerThread("AlertHandlerThread");
        alertHandlerThread.start();
        alertHandler = new Handler(alertHandlerThread.getLooper());
        db = ((SmartAlertApp) getApplication()).appContainer.dbProvider;
        localStorage = ((SmartAlertApp) getApplication()).appContainer.localStorage;
        notificationHelper =  new NotificationHelper(context);
        startForegroundService();

        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PM_TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AlertService", "Service started");
        alertHandler.post(checkAlertsRunnable);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //releases possibly acquired wake lock
        releaseWakeLock();
        //removes callbacks from handlers and terminates the service thread
        locationTimeoutHandler.removeCallbacksAndMessages(null);
        alertHandler.removeCallbacks(checkAlertsRunnable);
        alertHandlerThread.quit();
        Log.d("AlertService", "Service stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //dipslays a notification that the alert service has started running and start the service as a foreground service
    private void startForegroundService() {
        Notification notification = notificationHelper.getInitNotification().build();
        startForeground(1, notification);
    }

    //acquires a wake lock
    private void acquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    //releases the wake lock
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    //Runnable for the thread that checks for alerts
    private final Runnable checkAlertsRunnable = new Runnable() {
        @Override
        public void run() {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                Log.d("AlertService", "Checking for alerts...");
                //wake lock is acquired to keep the service running
                acquireWakeLock();

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            isLocationReceived = true;
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            Log.d("!!", lat + ", " + lon);
                            //Gets the user's position and checks for alerts
                            locationManager.removeUpdates(this);
                            checkForAlerts(lat, lon);
                        }
                        else {
                            locationManager.removeUpdates(this);
                            alertHandler.postDelayed(checkAlertsRunnable, CHECK_ALERTS_INTERVAL);
                            releaseWakeLock();
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };

                //times out waiting for a gps signal
                locationTimeoutHandler.postDelayed(() -> {
                    if (!isLocationReceived) {
                        locationManager.removeUpdates(locationListener);
                        alertHandler.postDelayed(this, CHECK_ALERTS_INTERVAL);
                        releaseWakeLock();
                        Log.d("!!", "gps timeout");
                    }
                    isLocationReceived = false;
                }, LOCATION_TIMEOUT);
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }
        }
    };

    //Performs the request to check for alerts
    private void checkForAlerts(Double lat, Double lon) {
        //Checks for alerts in the db
        db.checkForAlerts(lat, lon).addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()) {
                        List<SendAlertDto> results = task.getResult();
                        for(SendAlertDto alert: results){
                            SavedAlert savedAlert = new SavedAlert(
                                    alert.getEventType(),
                                    alert.getLocation(),
                                    new Date()

                            );
                            //saves each alert to the local db
                            localStorage.saveAlert(savedAlert);
                            //notifies the user of the received alert
                            setNotification(context, savedAlert);
                        }
                    }
                    else {
                        Log.d("!!", "request failed");
                    }
                    alertHandler.postDelayed(checkAlertsRunnable, CHECK_ALERTS_INTERVAL);
                    releaseWakeLock();
                }
        );
    }

    //Creates an alert notification and displays it
    private void setNotification(Context context, SavedAlert alertData) {
        NotificationCompat.Builder builder = notificationHelper.getNotification(alertData);
        notificationHelper.getManager().notify((int) System.currentTimeMillis(), builder.build());
    }
}
