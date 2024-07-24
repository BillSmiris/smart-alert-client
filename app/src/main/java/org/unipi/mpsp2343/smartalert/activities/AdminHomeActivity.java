package org.unipi.mpsp2343.smartalert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.unipi.mpsp2343.smartalert.Authentication;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;

import java.util.Objects;

//Main menu for employees
public class AdminHomeActivity extends AppCompatActivity {
    Authentication auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewEventListBtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        auth = ((SmartAlertApp) getApplication()).appContainer.authentication;
        //</editor-fold>
    }

    //Check if user has the permission to access this activity.
    protected void onStart() {
        super.onStart();
        if(auth.getUser() == null || !Objects.equals(auth.getUser().getRole(), "ROLE_ADMIN")){
            goToMainActivity();
        }
    }

    //Sign out user
    public void signOut(View view) {
        auth.signOut();
        Toast.makeText(this, getResources().getString(R.string.signout_ok), Toast.LENGTH_LONG).show();
        goToMainActivity();
    }

    //Got to main (login) activity
    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Event list
    public void viewEvents(View view) {
        Intent intent = new Intent(this, EventsListActivity.class);
        startActivity(intent);
    }
}