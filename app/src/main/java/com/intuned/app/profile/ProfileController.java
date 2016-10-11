package com.intuned.app.profile;

import Configuration.AppConfig;
import Enums.Font;
import Models.DomainModels.User;
import Navigation.AppNavigator;
import Services.ImageService;
import Services.ModalService;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ProfileController extends AppCompatActivity {
    private ActionBarDrawerToggle drawerToggle;
    public Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView username;
    private TextView followers;
    private TextView followings;
    private TextView bio;
    private FloatingActionButton followBtn;
    private ImageView editProfileIcon;
    private ImageView profileImage;
    private SessionManager sessionManager;
    private Firebase firebase;
    private ModalService modalService = ModalService.getInstance();
    private ImageService imageService = ImageService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
        initUI();
        initObjects();
        setValues();
    }

    private void initFirebase() {
        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase(AppConfig.FIREBASE_URL);
    }

    private void initUI() {
        setContentView(R.layout.activity_profile_controller);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nvView);
        username = (TextView) findViewById(R.id.user_profile_username);
        followers = (TextView) findViewById(R.id.user_profile_followers);
        followings = (TextView) findViewById(R.id.user_profile_followings);
        editProfileIcon = (ImageView) findViewById(R.id.profile_edit_profile);
        followBtn = (FloatingActionButton) findViewById(R.id.user_profile_follow_btn);
        bio = (TextView) findViewById(R.id.user_profile_bio);
        profileImage = (ImageView) findViewById(R.id.user_profile_photo);
    }

    private void initObjects() {
        sessionManager = new SessionManager(this, getApplicationContext());
        toolbar.setBackgroundColor(getResources().getColor(R.color.LightBlue900));
        setSupportActionBar(toolbar);
        setTitle("Vibes");
        // Set the menu icon instead of the launcher icon.
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        drawerToggle = setupDrawerToggle();
        // Tie DrawerLayout events to the ActionBarToggle
        drawerLayout.setDrawerListener(drawerToggle);
        // Setup drawer view
        setupDrawerContent(navigationView);
    }

    private void setValues() {
        modalService.toggleProgressDialogOn(this, "Loading Profile");

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modalService.displayToast("Follow user with ID " + sessionManager.getUserInstance().id, ProfileController.this);
            }
        });

        editProfileIcon.setClickable(true);
        editProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modalService.displayToast("Edit Profile", ProfileController.this);
            }
        });

        User currentUser = sessionManager.getUserInstance();
        //Set Followers Count
        followers.setText(currentUser.followerCount + " Followers");
        followers.setTypeface(Font.Coolvetica.getFont(getAssets()));
        //Set Followings Count
        followings.setText(currentUser.followingCount + " Following");
        followings.setTypeface(Font.Coolvetica.getFont(getAssets()));
        //Set username.
        username.setText(currentUser.username);
        username.setTypeface(Font.Coolvetica.getFont(getAssets()));
        //Set bio.
        bio.setText("It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout.");
        bio.setTypeface(Font.Coolvetica.getFont(getAssets()));
        //Set user's profile image.
        if(currentUser.imageDownloadUrl != null){
            Picasso.with(getApplicationContext()).load(currentUser.imageDownloadUrl).transform(new ImageService.CircleTransform()).into(profileImage);
        }else{
            imageService.roundImage(getResources(), R.drawable.bg_blank_profile, profileImage);
        }

        modalService.toggleProgressDialogOff();
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
