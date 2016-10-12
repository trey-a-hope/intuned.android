package com.intuned.app.profile;

import Configuration.AppConfig;
import Enums.Font;
import Models.DomainModels.User;
import Navigation.AppNavigator;
import Services.ImageService;
import Services.ModalService;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.intuned.app.R;
import com.intuned.app.authentication.SessionManager;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;

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
    private ImageButton profileImageEditBtn;
    private SessionManager sessionManager;
    private Firebase firebase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ModalService modalService = ModalService.getInstance();
    private ImageService imageService = ImageService.getInstance();
    private static int RESULT_LOAD_IMG = 1;
    private String imgDecodableString;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseStorageSetup();
        initFirebase();
        initUI();
        initObjects();
        setValues();
    }

    private void firebaseStorageSetup() {
        firebaseStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app-camp-central.appspot.com
        storageReference = firebaseStorage.getReferenceFromUrl(AppConfig.FIREBASE_STORAGE_URL).child("Images/Users");
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
        profileImageEditBtn = (ImageButton) findViewById(R.id.user_profile_photo_edit_btn);
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

        profileImageEditBtn.setClickable(true);
        profileImageEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
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

    private void saveImage() {
        modalService.toggleProgressDialogOn(this, "Saving Image");

        // Get the data from an ImageView as bytes.
        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();
        Bitmap bitmap = profileImage.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //Upload image with user's ID as file name.
        UploadTask uploadTask = storageReference.child(sessionManager.getUserInstance().id).putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                modalService.toggleProgressDialogOff();
                modalService.displayNotification("Error", exception.getMessage(), ProfileController.this);
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                modalService.toggleProgressDialogOff();
                final String imageDownloadUrl = taskSnapshot.getDownloadUrl().toString();
                //Update session's information.
                User user = sessionManager.getUserInstance();
                user.imageDownloadUrl = imageDownloadUrl;
                sessionManager.setUserInstance(user);
                //Update data in firebase.
                firebase.child(AppConfig.TABLE_USERS).child(sessionManager.getUserInstance().id).child("imageDownloadUrl").setValue(imageDownloadUrl);
                modalService.displayNotification("Success", "Profile image saved.", ProfileController.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                imagePath = imgDecodableString;
                // Set the Image in ImageView after decoding the String
                imageService.roundImage(getResources(), BitmapFactory.decodeFile(imgDecodableString), profileImage);
                // Proceed to save image to firebase.
                if(imagePath != null){
                    saveImage();
                }else{
                    modalService.displayNotification("Error", "You must select an image first.", this);
                }
            }
            //No image was selected.
            else {
            }
        } catch (Exception e) {
            modalService.displayNotification("Error", e.getMessage(), this);
        }
    }
}
