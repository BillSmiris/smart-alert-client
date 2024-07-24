package org.unipi.mpsp2343.smartalert.activities;

import android.content.Intent;
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
import org.unipi.mpsp2343.smartalert.LocalStorage;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;
import org.unipi.mpsp2343.smartalert.dto.EventListItem;
import org.unipi.mpsp2343.smartalert.dto.SavedAlert;
import org.unipi.mpsp2343.smartalert.recyclers.eventListRecycler.EventListRecyclerViewAdapter;
import org.unipi.mpsp2343.smartalert.recyclers.savedAlertListRecycler.SavedAlertListRecyclerViewAdapter;

import java.util.List;
import java.util.Objects;

//Activity to display saved alerts
public class StatisticsActivity extends AppCompatActivity {
    Authentication auth;
    LocalStorage localStorage;
    List<SavedAlert> alerts;
    RecyclerView savedAlertsRecyclerView;
    Button refreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewEventListBtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        auth = ((SmartAlertApp) getApplication()).appContainer.authentication;
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_USER")){
            goToMainActivity();
        }
        localStorage = ((SmartAlertApp) getApplication()).appContainer.localStorage;
        savedAlertsRecyclerView = findViewById(R.id.savedAlertsRecyclerView); //recycler view for displaying saved alerts
        savedAlertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        refreshBtn = findViewById(R.id.refreshBtn);
        //</editor-fold>
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_USER")){
            goToMainActivity();
        }
        getSavedAlerts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_USER")){
            goToMainActivity();
        }
        getSavedAlerts();
    }

    public void back(View view) {
        finish();
    }

    //refreshes the saved alert list
    public void refresh(View view) {
        getSavedAlerts();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //retrieved saved alerts from local storage
    private void getSavedAlerts() {
        savedAlertsRecyclerView.setAdapter(null);
        alerts = localStorage.getAllAlerts();
        savedAlertsRecyclerView.setScrollY(0);
        savedAlertsRecyclerView.setAdapter(new SavedAlertListRecyclerViewAdapter(alerts, this));
    }
}