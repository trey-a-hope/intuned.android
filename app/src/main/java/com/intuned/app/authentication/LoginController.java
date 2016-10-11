package com.intuned.app.authentication;

import Configuration.AppConfig;
import Models.DomainModels.User;
import Services.NetworkService;
import Services.ModalService;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.firebase.client.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.intuned.app.home.HomeController;
import com.intuned.app.R;
import java.util.HashMap;

public class LoginController extends AppCompatActivity {
    private TextView forgotPassword;
    private TextView registerNewAccount;
    private EditText loginEmail;
    private EditText loginPassword;
    private Button loginButton;
    private Button loginFacebookButton;
    private SessionManager sessionManager;
    private Firebase firebase;
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    private ModalService modalService = ModalService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
        initUI();
        initObjects();
        setValues();

        if(sessionManager.isLoggedIn()){
            goToMainScreen();
        }
    }

    private void initFirebase() {
        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase(AppConfig.FIREBASE_URL);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth _firebaseAuth) {

            }
        });
    }

    private void initUI(){
        setContentView(R.layout.activity_login_controller);
        forgotPassword = (TextView) findViewById(R.id.forgot_password);
        registerNewAccount = (TextView) findViewById(R.id.register_new_account);
        loginEmail = (EditText) findViewById(R.id.login_email);
        loginPassword = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_button);
        loginFacebookButton = (Button) findViewById(R.id.login_facebook_button);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void initObjects(){
        sessionManager = new SessionManager(this, getApplicationContext());
        toolbar.setBackgroundColor(getResources().getColor(R.color.LightBlue900));
        setSupportActionBar(toolbar);
        setTitle("Vibes");
    }

    private void setValues(){
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
        registerNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginController.this, RegisterController.class);
                startActivity(intent);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modalService.displayToast("TODO: Forgot Password", LoginController.this);
            }
        });
        loginFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modalService.displayToast("TODO: Facebook Login", LoginController.this);
            }
        });
    }

    private boolean isValid() {
        final String _loginEmail = loginEmail.getText().toString();
        final String _loginPassword = loginPassword.getText().toString();

        boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(_loginEmail).matches();
        boolean passwordValid = _loginPassword.length() > 0;

        return emailValid && passwordValid;
    }

    private void loginUser() {
        if (!isValid()) {
            modalService.displayNotification("Error", "Looks like the E-Mail/Password was not valid; try again.", this);
        } else if (!NetworkService.getInstance().isConnected(getApplicationContext())) {
            modalService.displayNotification("Error", "Cannot connect to internet; please check your network settings.", this);
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Authenticating");
            progressDialog.setMessage(AppConfig.WAIT_MESSAGE);
            progressDialog.show();

            final String _loginEmail = loginEmail.getText().toString();
            final String _loginPassword = loginPassword.getText().toString();

            firebase.authWithPassword(_loginEmail, _loginPassword, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(final AuthData authData) {
                    firebaseAuth.signInWithEmailAndPassword(_loginEmail, _loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            //sessionManager.login();
                            //Get unique id of user on successful authentication.
                            if (task.isSuccessful()) {
                                final String uid = authData.getUid();
                                //Fine the user object from the TABLE_USERS object.
                                firebase.child(AppConfig.TABLE_USERS).orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //Convert user object to HashMap.
                                        HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                                        //Get id (not unique id), created on user insert.
                                        if(map != null) {
                                            for (String id : map.keySet()) {
                                                User user = new User();
                                                user.id = (String) dataSnapshot.child(id).child("id").getValue();
                                                user.uid = (String) dataSnapshot.child(id).child("uid").getValue();
                                                user.email = (String) dataSnapshot.child(id).child("email").getValue();
                                                user.username = (String) dataSnapshot.child(id).child("username").getValue();
                                                user.followerCount = 23;//TODO: Calculate properly.
                                                user.followingCount = 19;//TODO: Calculate properly.
                                                sessionManager.setUserInstance(user);
                                            }
                                        }else{
                                            return;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                        modalService.displayNotification("Error", firebaseError.getMessage(), LoginController.this);
                                    }
                                });
                                //Proceed to Main Activity
                                goToMainScreen();
                            } else {
                                modalService.displayNotification("Error", "Could not log in with credentials.", LoginController.this);
                            }
                        }
                    });


                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    progressDialog.dismiss();
                    modalService.displayNotification("Error", firebaseError.getMessage(), LoginController.this);
                }
            });
        }
    }

    private void goToMainScreen(){
        Intent intent = new Intent(LoginController.this, HomeController.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_controller, menu);
        return true;
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
