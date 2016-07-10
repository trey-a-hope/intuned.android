package com.intuned.app.authentication;

import Configuration.AppConfig;
import DTO.User;
import Network.ConnectionManager;
import Services.ModalService;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.intuned.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class RegisterController extends AppCompatActivity {
    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerUsername;
    private Button registerButton;
    private SessionManager sessionManager;
    private Firebase firebase;
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    private final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,15})";
    /*
    (			            # Start of group
        (?=.*\d)		    #   must contains one digit from 0-9
        (?=.*[a-z])		    #   must contains one lowercase characters
        (?=.*[A-Z])		    #   must contains one uppercase characters
        (?=.*[@#$%])		#   must contains one special symbols in the list "@#$%"
                    .		#     match anything with previous condition checking
                {6,20}	    #        length at least 6 characters and maximum of 20
    )			            # End of group
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
        initUI();
        initObjects();
        setValues();
    }

    private void initFirebase(){
        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase(AppConfig.FIREBASE_URL);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void initUI(){
        setContentView(R.layout.activity_register_controller);
        registerEmail = (EditText) findViewById(R.id.register_email);
        registerPassword = (EditText) findViewById(R.id.register_password);
        registerUsername = (EditText) findViewById(R.id.register_username);
        registerButton = (Button) findViewById(R.id.register_button);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void initObjects(){
        sessionManager = new SessionManager(this, getApplicationContext());
        toolbar.setBackgroundColor(getResources().getColor(R.color.AppToolbarColor));
        setSupportActionBar(toolbar);
        setTitle("Vibes");
        //Images.roundImage(getResources(), R.drawable.background_blank_profile, imageView); TODO: Create Images class.
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void setValues(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register_controller, menu);
        return true;
    }

    private void clearFields() {
        registerEmail.setText("");
        registerPassword.setText("");
    }

    private boolean isValid() {
        final String _registerEmail = registerEmail.getText().toString();
        final String _registerPassword = registerPassword.getText().toString();

        boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(_registerEmail).matches();
        boolean passwordValid = Pattern.compile(PASSWORD_PATTERN).matcher(_registerPassword).matches();
        boolean usernameValid = true;//TODO: Check database to see if username exists already.

        return emailValid && passwordValid;
    }

    private void registerUser() {
        if (!isValid()) {
            ModalService.displayNotification("Error", "Your credentials were not valid; email must be formatted properly and password must contain one of each, (digit, lowercase letter, uppercase letter) and be between 6-15 characters.", this);
        } else if (!ConnectionManager.isConnected(getApplicationContext())) {
            ModalService.displayNotification("Error", "Cannot connect to internet; please check your network settings.", this);
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Registering");
            progressDialog.setMessage(AppConfig.WAIT_MESSAGE);
            progressDialog.show();

            final String _registerEmail = registerEmail.getText().toString();
            final String _registerPassword = registerPassword.getText().toString();
            final String _registerUsername = registerUsername.getText().toString();

            firebaseAuth.createUserWithEmailAndPassword(_registerEmail, _registerPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid().toString();
                        //Create user profile for persisting data.
                        final String key = firebase.child(AppConfig.TABLE_USERS).push().getKey();
                        User user = new User();
                        user.id = key;
                        user.uid = uid;
                        user.email = _registerEmail;
                        user.username = _registerUsername;
                        user.followings = new ArrayList<String>() {{add(key);}};
                        firebase.child(AppConfig.TABLE_USERS).child(key).setValue(user);
                        finish();
                    } else {
                        ModalService.displayNotification("Error", "Could not register account.", RegisterController.this);
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
