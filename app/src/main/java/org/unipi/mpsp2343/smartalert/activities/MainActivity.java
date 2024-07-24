package org.unipi.mpsp2343.smartalert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.unipi.mpsp2343.smartalert.Authentication;
import org.unipi.mpsp2343.smartalert.Helpers;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;

import java.util.Objects;

//Main activity. Users can log in through this activity.
public class MainActivity extends AppCompatActivity {
    EditText emailText, passwordText;
    Authentication auth;
    Button loginBtn, signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewEventListBtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        emailText = findViewById(R.id.signInEmailInput); //Input for user email
        passwordText = findViewById(R.id.signInPasswordInput); //Input for user password
        loginBtn = findViewById(R.id.loginBtn); //Button for logging in
        signupBtn = findViewById(R.id.signupBtn1); //Button that takes the user to a sign up form
        auth = ((SmartAlertApp) getApplication()).appContainer.authentication;
        //</editor-fold>
    }

    //Takes an already logged in user to the proper menu, according to their role
    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getUser() != null){
            if(Objects.equals(auth.getUser().getRole(), "ROLE_USER")) {
                goToUserHomeActivity();
            }
            else {
                goToAdminHomeActivity();
            }
        }
    }

    //Logs in the user
    public void login(View view){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        //Check email from input
        if(email.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.login_error_empty_email), Toast.LENGTH_SHORT).show();
            return;
        }
        //Check password from input
        if(password.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.login_error_empty_pwd), Toast.LENGTH_SHORT).show();
            return;
        }
        loginBtn.setEnabled(false);
        signupBtn.setEnabled(false);
        emailText.setEnabled(false);
        passwordText.setEnabled(false);
        //Make the login request and take them to the proper menu, according to their role
        auth.login(email, password).addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(this, getResources().getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        if(Objects.equals(auth.getUser().getRole(), "ROLE_USER")) {
                            goToUserHomeActivity();
                        }
                        else {
                            goToAdminHomeActivity();
                        }
                    } else {
                        Toast.makeText(this, Helpers.getString(this, task.getException().getMessage()), Toast.LENGTH_SHORT).show();
                        loginBtn.setEnabled(true);
                        signupBtn.setEnabled(true);
                        emailText.setEnabled(true);
                        passwordText.setEnabled(true);
                    }
                }
        );
    }

    //Take user to the sign up form
    public void signUp(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    //Take USER user to the proper main menu
    private void goToUserHomeActivity(){
        Intent intent = new Intent(this, UserHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Take ADMIN user to the proper main menu
    private void goToAdminHomeActivity(){
        Intent intent = new Intent(this, AdminHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}