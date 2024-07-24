package org.unipi.mpsp2343.smartalert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.unipi.mpsp2343.smartalert.Authentication;
import org.unipi.mpsp2343.smartalert.DbProvider;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;
import org.unipi.mpsp2343.smartalert.dto.GetEventResponseDto;
import org.unipi.mpsp2343.smartalert.enums.EventStatus;
import org.unipi.mpsp2343.smartalert.recyclers.eventListRecycler.EventListRecyclerViewAdapter;
import org.unipi.mpsp2343.smartalert.recyclers.reportRecycler.ReportRecyclerViewAdapter;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

//Activity to display the details of an event
public class EventDetailsActivity extends AppCompatActivity {
    Authentication auth;
    DbProvider db;
    String eventId;
    GetEventResponseDto event;
    RecyclerView reportRecyclerView;
    Button confirmBtn, rejectBtn, backBtn;
    TextView eventTypeTextView, eventSeverityTextView, eventLocationTextView,eventTimestampTextView, reportsLabelTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewEventListBtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        auth = ((SmartAlertApp) getApplication()).appContainer.authentication;
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_ADMIN")){
            goToMainActivity();
        }
        confirmBtn = findViewById(R.id.edConfirmBtn); //Button to confirm the reported event
        rejectBtn = findViewById(R.id.edRejectBtn);// Button to reject the reported event
        backBtn = findViewById(R.id.edBackBtn); //Button to go back to the event list
        eventTypeTextView = findViewById(R.id.eventTypeTextView); //Shows the type of the event
        eventSeverityTextView = findViewById(R.id.eventSeverityTextView); //Severity of the event
        eventLocationTextView = findViewById(R.id.eventLocationTextView); //Location of the event(lat, lon)
        eventTimestampTextView = findViewById(R.id.eventTimestampTextView); //Date and time of an event
        reportsLabelTextView = findViewById(R.id.reportsLabelTextView); //Label/header for the reports recycler
        reportRecyclerView = findViewById(R.id.reportRecyclerView); //Recycler to show the reports of the event
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = ((SmartAlertApp) getApplication()).appContainer.dbProvider;
        eventId = getIntent().getStringExtra("EVENT_ID"); //Get the event id from the previous view
        if(eventId != null) {
            getEvent();
        }
        else {
            Toast.makeText(this, getResources().getString(R.string.error_unexpected_error), Toast.LENGTH_SHORT).show();
            finish();
        }
        //</editor-fold>
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_ADMIN")){
            goToMainActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_ADMIN")){
            goToMainActivity();
        }
    }

    public void back(View view) {
        finish();
    }

    //Reject an event
    public void reject(View view) {
        //Block UI
        confirmBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        backBtn.setEnabled(false);
        //Send request
        db.rejectEvent(eventId).addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(this, getResources().getString(R.string.event_reject_succcess), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this, getResources().getString(R.string.error_unexpected_error), Toast.LENGTH_SHORT).show();
                        confirmBtn.setEnabled(true);
                        rejectBtn.setEnabled(true);
                        finish();
                    }
                    backBtn.setEnabled(true);
                }
        );
    }

    //Confirm event
    public void confirm(View view) {
        //block UI
        confirmBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        backBtn.setEnabled(false);
        db.confirmEvent(eventId).addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(this, getResources().getString(R.string.event_confirm_success), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this, getResources().getString(R.string.error_unexpected_error), Toast.LENGTH_SHORT).show();
                        confirmBtn.setEnabled(true);
                        rejectBtn.setEnabled(true);
                        finish();
                    }
                    backBtn.setEnabled(true);
                }
        );
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Get event details
    private void getEvent() {
        //Send request to get event details
        db.getEvent(eventId).addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()) {
                        //Retrieve request result
                        event = task.getResult();

                        //Display event type text based on retrieved event type numeric value
                        String eventTypeString = Arrays.asList(getResources().getStringArray(R.array.event_types)).get(event.getEventType());
                        eventTypeTextView.setText(getResources().getString(R.string.ed_event_type, eventTypeString));

                        //Set severity text based on number of reports
                        String eventSeverityString;
                        if(event.getNumberOfReports() > 20) {
                            eventSeverityString = getResources().getString(R.string.severity_high);
                        } else if (event.getNumberOfReports() > 10) {
                            eventSeverityString = getResources().getString(R.string.severity_medium);
                        } else {
                            eventSeverityString = getResources().getString(R.string.severity_low);
                        }
                        eventSeverityTextView.setText(getResources().getString(R.string.ed_event_severity, eventSeverityString));

                        //Display event location
                        String eventLocationString = "Lat: " + event.getLocation().getLat() + ", Lon: " + event.getLocation().getLon();
                        eventLocationTextView.setText(getResources().getString(R.string.ed_event_location, eventLocationString));

                        //Display formatted timestamp
                        Date date = new Date(event.getTimestamp());
                        android.text.format.DateFormat df = new android.text.format.DateFormat();
                        eventTimestampTextView.setText(getResources().getString(R.string.first_reported, df.format("hh:mm:ss - dd/MM/yyyy", date)));

                        //Set reports view header with number of reports
                        reportsLabelTextView.setText(getResources().getString(R.string.ed_reports_label, String.valueOf(event.getNumberOfReports())));

                        reportRecyclerView.setScrollY(0);
                        //Show reports in recycler view
                        reportRecyclerView.setAdapter(new ReportRecyclerViewAdapter(event.getReports(), this));

                        if(event.getEventStatus() == EventStatus.OPEN) {
                            confirmBtn.setEnabled(true);
                            rejectBtn.setEnabled(true);
                        }
                    }
                    else {
                        Toast.makeText(this, getResources().getString(R.string.error_fetch), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
    }
}