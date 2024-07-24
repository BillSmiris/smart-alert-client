package org.unipi.mpsp2343.smartalert.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.unipi.mpsp2343.smartalert.Authentication;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;
import org.unipi.mpsp2343.smartalert.services.AlertService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Main menu for normal users
public class UserHomeActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int BACKGROUND_PERMISSION_REQUEST_CODE = 124;

    Authentication auth;
    Intent alertServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewEventListBtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        auth = ((SmartAlertApp) getApplication()).appContainer.authentication;
        //</editor-fold>
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_USER")){
            goToMainActivity();
        }

        checkAndRequestPermissions();
    }

    public void signOut(View view) {
        stopAlertService();
        auth.signOut();
        Toast.makeText(this, getResources().getString(R.string.signout_ok), Toast.LENGTH_LONG).show();
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Take user to the event reporting form
    public void report(View view) {
        Intent intent = new Intent(this, ReportDisasterActivity.class);
        startActivity(intent);
    }

    //Take user to the saved alerts activity
    public void savedAlerts(View view) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }

    //Start the service that periodically checks for new alerts
    private void startAlertService() {
        if(alertServiceIntent == null) {
            alertServiceIntent = new Intent(this, AlertService.class);
            ContextCompat.startForegroundService(this, alertServiceIntent);
        }
    }

    //Stop the service that periodically checks for new alerts
    private void stopAlertService() {
        if(alertServiceIntent != null) {
            this.stopService(alertServiceIntent);
        }
    }

    //The following fold contains the logic required to handle the granting of the required
    //permissions for the alert service to be able to operate. The service requires the
    //user to grant the following permissions:
    //1.Location
    //2.Notifications
    //3.Background location
    //<editor-fold name="Permission handling">
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean locationPermissionGranted = false;
            boolean notificationPermissionGranted = true;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        || permissions[i].equals(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    locationPermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                } else if (permissions[i].equals(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    notificationPermissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }

            if (locationPermissionGranted && notificationPermissionGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_PERMISSION_REQUEST_CODE);
                } else {
                    startAlertService();
                }
            } else {
                Toast.makeText(this, getString(R.string.location_or_notification_perm_not_granted), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == BACKGROUND_PERMISSION_REQUEST_CODE) {
            boolean backgroundLocationPermissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (backgroundLocationPermissionGranted) {
                startAlertService();
            } else {
                Toast.makeText(this, getString(R.string.background_location_perm_not_granted), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAndRequestPermissions() {
        boolean locationPermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        boolean backgroundLocationPermissionGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationPermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }

        boolean notificationPermissionGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }

        if (locationPermissionGranted && backgroundLocationPermissionGranted && notificationPermissionGranted) {
            startAlertService();
        } else {
            requestNecessaryPermissions(locationPermissionGranted, backgroundLocationPermissionGranted, notificationPermissionGranted);
        }
    }

    private void requestNecessaryPermissions(boolean locationPermissionGranted, boolean backgroundLocationPermissionGranted, boolean notificationPermissionGranted) {
        List<String> permissionsNeeded = new ArrayList<>();

        if (!locationPermissionGranted) {
            permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!notificationPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else if (!backgroundLocationPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_PERMISSION_REQUEST_CODE);
            }
        }
    }
    //</editor-fold>
}