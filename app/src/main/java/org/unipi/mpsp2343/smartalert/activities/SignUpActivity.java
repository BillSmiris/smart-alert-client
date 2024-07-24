package org.unipi.mpsp2343.smartalert.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.unipi.mpsp2343.smartalert.Authentication;
import org.unipi.mpsp2343.smartalert.Helpers;
import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.app.SmartAlertApp;

//Activity for new users to sign up
public class SignUpActivity extends AppCompatActivity {
    EditText emailText, passwordText, confirmPasswordText;
    Authentication auth;
    Button signupBtn, backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewEventListBtn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        emailText = findViewById(R.id.signUpEmailInput); //Input for user's email
        passwordText = findViewById(R.id.signUpPasswordInput); //Input for user's password
        confirmPasswordText = findViewById(R.id.signUpPasswordConfirmationInput); //Input for password confirmation
        signupBtn = findViewById(R.id.signupBtn); //Button to perform the sign up
        backBtn = findViewById(R.id.suBackBtn); //Button that takes the user back to the log in activity
        auth = ((SmartAlertApp) getApplication()).appContainer.authentication;
        //</editor-fold>
    }

    //Sign ups the user. Performs some local checks on the provided data
    public void signUp(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String confirmPassword = confirmPasswordText.getText().toString();
        //check if provided email is empty
        if(email.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.signup_error_email_empty), Toast.LENGTH_LONG).show();
            return;
        }
        //check if password is empty
        if(password.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.signup_error_password_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        //check if password confirmation field is empty
        if(confirmPassword.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.signup_error_password_confirmation_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        //compare passwords
        if(!password.equals(confirmPassword)){
            Toast.makeText(this, getResources().getString(R.string.signup_error_password_no_match), Toast.LENGTH_SHORT).show();
            return;
        }
        signupBtn.setEnabled(false);
        backBtn.setEnabled(false);
        emailText.setEnabled(false);
        passwordText.setEnabled(false);
        confirmPasswordText.setEnabled(false);

        //Send sign up request
        auth.signup(email, password).addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(this, getResources().getString(R.string.signup_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Helpers.showMessage(this, getResources().getString(R.string.error), Helpers.getString(this, task.getException().getMessage()));
                        signupBtn.setEnabled(true);
                        backBtn.setEnabled(true);
                        emailText.setEnabled(true);
                        passwordText.setEnabled(true);
                        confirmPasswordText.setEnabled(true);
                    }
                }
        );
    }

    public void back(View view) {
        finish();
    }
}