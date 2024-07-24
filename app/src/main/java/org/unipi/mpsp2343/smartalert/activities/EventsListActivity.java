package org.unipi.mpsp2343.smartalert.activities;

import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import org.unipi.mpsp2343.smartalert.recyclers.eventListRecycler.EventListRecyclerViewAdapter;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;
import org.unipi.mpsp2343.smartalert.dto.EventListItem;
import org.unipi.mpsp2343.smartalert.recyclers.eventListRecycler.EventListRecyclerViewInterface;

import java.util.List;
import java.util.Objects;

public class EventsListActivity extends AppCompatActivity implements EventListRecyclerViewInterface {
    Authentication auth;
    DbProvider db;
    List<EventListItem> events;
    RecyclerView eventListView;
    Button refreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events_list);
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
        db = ((SmartAlertApp) getApplication()).appContainer.dbProvider;
        eventListView = findViewById(R.id.eventListView); //Recycler view for showing the events
        eventListView.setLayoutManager(new LinearLayoutManager(this));
        refreshBtn = findViewById(R.id.refreshBtn);
        //</editor-fold>
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_ADMIN")){
            goToMainActivity();
        }
        getEventList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_ADMIN")){
            goToMainActivity();
        }
        getEventList();
    }

    public void back(View view) {
        finish();
    }

    public void refresh(View view) {
        getEventList();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Retrieves the list of OPEN events
    private void getEventList() {
        eventListView.setAdapter(null);
        eventListView.setEnabled(false);
        refreshBtn.setEnabled(false);
        db.getEventList().addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()) {
                        events = task.getResult();
                        if(events.isEmpty()) {
                            Toast.makeText(this, getResources().getString(R.string.no_events_found), Toast.LENGTH_SHORT).show();
                        }
                        eventListView.setScrollY(0);
                        //Show events in recycler view
                        eventListView.setAdapter(new EventListRecyclerViewAdapter(events, this, this));
                    }
                    else {
                        Toast.makeText(this, getResources().getString(R.string.error_fetch), Toast.LENGTH_SHORT).show();
                    }
                    eventListView.setEnabled(true);
                    refreshBtn.setEnabled(true);
                }
        );
    }

    //Handles clicks on items of the recycler view
    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("EVENT_ID", events.get(position).getId());
        startActivity(intent);
    }
}