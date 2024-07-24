package org.unipi.mpsp2343.smartalert.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.unipi.mpsp2343.smartalert.Authentication;
import org.unipi.mpsp2343.smartalert.DbProvider;
import org.unipi.mpsp2343.smartalert.enums.EventType;
import org.unipi.mpsp2343.smartalert.Helpers;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;
import org.unipi.mpsp2343.smartalert.dto.LocationDto;
import org.unipi.mpsp2343.smartalert.dto.PostDisasterEventDto;

import java.util.Objects;

//Activity to report a disaster event
public class ReportDisasterActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int PHOTO_INTENT_CODE = 500;
    private static final long LOCATION_TIMEOUT = 10000;
    private static final @EventType int DEFAULT_EVENT_TYPE = EventType.FLOOD;
    private Handler handler = new Handler();
    Authentication auth;
    DbProvider db;
    Spinner eventTypesSpinner;
    ArrayAdapter<CharSequence> adapter;
    @EventType int selectedType = DEFAULT_EVENT_TYPE;
    EditText commentsText;
    private LocationManager locationManager;
    private LocationListener locationListener;
    Button reportBackBtn, postReportBtn, takePhotoBtn;
    private boolean isLocationReceived = false;
    ImageView imageView;
    Bitmap eventPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_disaster);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewEventListBtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        auth = ((SmartAlertApp) getApplication()).appContainer.authentication;
        db = ((SmartAlertApp) getApplication()).appContainer.dbProvider;
        eventTypesSpinner = findViewById(R.id.eventTypesSpinner); //Spinner for selecting the event type to report
        adapter= ArrayAdapter.createFromResource(this, R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        eventTypesSpinner.setAdapter(adapter);

        eventTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //When an item is selected from the spinner, save its position to the selectedType variable
                selectedType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        commentsText = findViewById(R.id.commentsText); //Input for entering the user's comment on the event
        //Get a reference to the location service
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        reportBackBtn = findViewById(R.id.reportBackBtn); //Button to take user back to the main menu
        postReportBtn = findViewById(R.id.postReportBtn); //Button to post the report
        takePhotoBtn = findViewById(R.id.takePhotoBtn); //Button to enable camera to take a photo of the event
        imageView = findViewById(R.id.imageView); //View to show the photo taken by the user
        //</editor-fold>
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_USER")){
            goToMainActivity();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void back(View view) {
        finish();
    }

    //Posts the report of the disaster event
    private void postReport(double lat, double lon) {
        //Encapsule the user provided info ina dto
        PostDisasterEventDto event = new PostDisasterEventDto(
                commentsText.getText().toString(), //user's comments
                selectedType, //selected event type
                new LocationDto(lat, lon), //user's current location
                System.currentTimeMillis(), //current system time(timestamp of the event)
                Helpers.bitmapToBase64(eventPhoto)//base64 encoded event photo
        );

        //make the request
        db.postEvent(event).addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(this, getResources().getString(R.string.post_event_success), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        Toast.makeText(this, Helpers.getString(this, task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                        eventTypesSpinner.setEnabled(true);
                        commentsText.setEnabled(true);
                        reportBackBtn.setEnabled(true);
                        postReportBtn.setEnabled(true);
                        takePhotoBtn.setEnabled(true);
                    }
                }
        );
    }

    //Triggers when the event report button is clicked
    public void report(View view) {
        //Checks for location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //Get the user's location once
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        isLocationReceived = true;
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        //pass the user's location to the function that makes the post request
                        postReport(lat, lon);
                    } else {
                        //handle the case where no location is retieved
                        handleNoLocation();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };
            eventTypesSpinner.setEnabled(false);
            commentsText.setEnabled(false);
            reportBackBtn.setEnabled(false);
            postReportBtn.setEnabled(false);
            takePhotoBtn.setEnabled(false);
            //location service timeout handler
            //puts a limit on how much the app should wait to get a location object
            //by default hangs until a location is retrieved
            handler.postDelayed(() -> {
                if (!isLocationReceived) {
                    locationManager.removeUpdates(locationListener);
                    handleNoLocation();
                }
                isLocationReceived = false;
            }, LOCATION_TIMEOUT);
            //request the location
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.location_perm_granted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.location_perm_not_granted), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //If not location is retrieved or the location request times out, unblock the ui and display a
    //failure message
    private void handleNoLocation(){
        eventTypesSpinner.setEnabled(true);
        commentsText.setEnabled(true);
        reportBackBtn.setEnabled(true);
        postReportBtn.setEnabled(true);
        takePhotoBtn.setEnabled(true);
        Toast.makeText(this, getString(R.string.report_error_no_location), Toast.LENGTH_SHORT).show();
    }

    //Enable camera to take a photo of the event
    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,PHOTO_INTENT_CODE);
    }

    //When a photo is taken, display it on an image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 500 && resultCode == RESULT_OK){
            Bundle extra = data.getExtras();
            eventPhoto = (Bitmap) extra.get("data");
            imageView.setImageBitmap(eventPhoto);
        }
    }
}