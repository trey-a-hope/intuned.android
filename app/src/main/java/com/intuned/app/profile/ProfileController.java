package com.intuned.app.profile;

import Configuration.AppConfig;
import DTO.User;
import Navigation.AppNavigator;
import Services.ModalService;
import android.app.ProgressDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.intuned.app.R;
import com.intuned.app.authentication.SessionManager;

public class ProfileController extends AppCompatActivity {
    private ActionBarDrawerToggle drawerToggle;
    public Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView username;
    private TextView followers;
    private TextView followings;
    private CardView profileCard;
    private ImageView editProfileIcon;
    private User user;
    private SessionManager sessionManager;
    private Firebase firebase;

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
    }

    private void initUI(){
        setContentView(R.layout.activity_profile_controller);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nvView);
        username = (TextView) findViewById(R.id.profile_username);
        followers = (TextView) findViewById(R.id.profile_followers);
        followings = (TextView) findViewById(R.id.profile_followings);
        profileCard = (CardView) findViewById(R.id.profile_card);
        editProfileIcon = (ImageView) findViewById(R.id.profile_edit_profile);
    }

    private void initObjects(){
        sessionManager = new SessionManager(this, getApplicationContext());
        toolbar.setBackgroundColor(getResources().getColor(R.color.AppToolbarColor));
        setSupportActionBar(toolbar);
        setTitle("Profile");
        // Set the menu icon instead of the launcher icon.
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        drawerToggle = setupDrawerToggle();
        // Tie DrawerLayout events to the ActionBarToggle
        drawerLayout.setDrawerListener(drawerToggle);
        // Setup drawer view
        setupDrawerContent(navigationView);
        user = sessionManager.getUserInstance();
    }

    private void setValues(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading Profile");
        progressDialog.setMessage(AppConfig.WAIT_MESSAGE);
        progressDialog.show();

        editProfileIcon.setClickable(true);
        editProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ModalService.displayToast("Edit Profile", ProfileController.this);
            }
        });

        firebase.child(AppConfig.TABLE_USERS).child(sessionManager.getUserInstance().id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                //Get Followers Count
                int followersCount = (int) dataSnapshot.child("followers").getChildrenCount();
                followers.setText("Followers " + String.valueOf(followersCount));
                //Get Followings Count
                int followingsCount = (int) dataSnapshot.child("followings").getChildrenCount() - 1;
                followings.setText("Following " + String.valueOf(followingsCount));
                //Set username.
                username.setText(user.username);
            }

            @Override
            public void onCancelled(FirebaseError error) {
                ModalService.displayNotification("Lost connection to database, please try again.", "Sorry", ProfileController.this);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_controller, menu);
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

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        AppNavigator.navigate(menuItem.getItemId(), this);

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

}
