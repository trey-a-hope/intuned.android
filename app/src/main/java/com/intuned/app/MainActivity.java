package com.intuned.app;

import Adapters.SongListAdapter;
import DTO.Song;
import DTO.User;
import Listeners.RecyclerItemClickListener;
import Miscellaneous.DividerDecoration;
import Miscellaneous.SoundFile;
import Navigation.AppNavigator;
import Configuration.AppConfig;
import Services.ModalService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView tvHeader;
    private ActionBar actionBar;
    private Button btnNewSong;
    private SoundFile soundFile;
    //Song
    private String artist;
    private String album;
    private String track;

    private RecyclerView postedSongsRecyclerview;
    private SongItemAdapter songItemAdapter;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonS3 s3;
    private TransferUtility transferUtility;
    private TransferObserver observer;
    private ProgressDialog progressDialog;
    private ProgressDialog seekDialog;
    private ArrayList<Song> listOfUserSongs;
    private Firebase firebase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseStorageSetup();
        initUI();
        scanSdCard();
        initObjects();
        initFirebase();
        setValues();
        initRecyclerView();
        registerReceiver();
    }

    private void SoundFileSetUp(String pathToFile) {
        try {
            soundFile = SoundFile.create(pathToFile, new SoundFile.ProgressListener() {
                public boolean reportProgress(double fractionComplete) {
                    return true;
                }
            });
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (SoundFile.InvalidInputException e) {
        }
    }

    private void registerReceiver() {
        IntentFilter iF = new IntentFilter();

        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.htc.music.metachanged");
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");

        /**
         * Returns track info for music currently playing.
         **/
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String cmd = intent.getStringExtra("command");
                Log.v("tag ", action + " / " + cmd);
                artist = intent.getStringExtra("artist");
                album = intent.getStringExtra("album");
                track = intent.getStringExtra("track");
                Log.v("tag", artist + ":" + album + ":" + track);
            }
        };

        registerReceiver(mReceiver, iF);
    }

    private void initUI() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nvView);
        tvHeader = (TextView) findViewById(R.id.headerTV);
        btnNewSong = (Button) findViewById(R.id.home_new_song_button);
        postedSongsRecyclerview = (RecyclerView) findViewById(R.id.rv);
    }

    private void initObjects() {
        toolbar.setBackgroundColor(getResources().getColor(R.color.AppToolbarColor));
        setSupportActionBar(toolbar);
        setTitle("vIbes");
        actionBar = getSupportActionBar();
        drawerToggle = setupDrawerToggle();
        songItemAdapter = new SongItemAdapter();

        // Initialize the Amazon Cognito credentials provider.
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                AppConfig.S3_IDENTITY_POOL_ID,   // Identity Pool ID
                Regions.US_EAST_1                // Region
        );

        // Initialize the TransferUtility provider.
        s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, getApplicationContext());

        seekDialog = new ProgressDialog(MainActivity.this);
        progressDialog = new ProgressDialog(this);
    }

    private void setValues() {
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
        tvHeader.setText("vIbes, what are you listening to?");
        navigationView.setBackgroundColor(getResources().getColor(R.color.Indigo50));
        setupDrawerContent(navigationView);
        btnNewSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager manager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
                //Validate user is playing music.
                if (manager.isMusicActive()) {
                    if (ModalService.displayConfirmation("Post Song?", "This song will be available for others to listen to.", MainActivity.this)) {
                        //Search for song in songs from device.
                        for (Song s : listOfUserSongs) {
                            String songId = s.artist + s.title;
                            String currentSongId = artist + track;
                            if (songId.equals(currentSongId)) {
                                SoundFileSetUp(s.path);
                                File directory = new File("/storage/emulated/0/Music/Vibes");
                                if (!directory.exists()) {
                                    if (directory.mkdir()) {
                                        ModalService.displayTest("Directory created.", MainActivity.this);
                                    } else {
                                        ModalService.displayTest("Could not create directory.", MainActivity.this);
                                    }
                                }
                                try {
                                    File file = new File("/storage/emulated/0/Music/Vibes/" + s.fileName);
                                    file.createNewFile();
                                    soundFile.WriteWAVFile(file, 20f, 30f);
                                    uploadSongToFirebaseStorage(file);
                                } catch (IOException e) {
                                    ModalService.displayTest(e.getMessage(), MainActivity.this);
                                }

                            }
                        }
                    } else {
                    }
                } else {
                    ModalService.displayToast("Turn on your music first...", MainActivity.this);
                }
            }
        });
    }

    private void uploadSongToFirebaseStorage(File file) {
        byte[] data = readContentIntoByteArray(file);
        //Upload image with user's ID as file name.
        UploadTask uploadTask = storageReference.child("SESSION_MANAGER_USER_ID_HERE").putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressDialog.dismiss();
                ModalService.displayNotification("Error", exception.getMessage(), MainActivity.this);
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                final String imageDownloadUrl = taskSnapshot.getDownloadUrl().toString();
                //TODO: Add new song object to user's profile.
                //Update session's information.
                //sessionManager.updateImageDownloadUrl(imageDownloadUrl);
                //Update data in firebase.
                //firebase.child(AppConfig.TABLE_USERS).child(sessionManager.getUserInstance().id).child("imageDownloadUrl").setValue(imageDownloadUrl);
                ModalService.displayNotification("Success", "Your song has been uploaded.", MainActivity.this);
            }
        });
    }

    private byte[] readContentIntoByteArray(File file) {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            for (int i = 0; i < bFile.length; i++) {
                System.out.print((char) bFile[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFile;
    }

    private void initFirebase() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        //Tag used to cancel the request
        String tag_string_req = "intuned_tracks";

        // Progress Dialog
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Getting timeline.");
        progressDialog.setMessage(AppConfig.WAITING_MESSAGE);
        progressDialog.show();

        //Firebase Setup
        Firebase.setAndroidContext(getApplicationContext());
        firebase = new Firebase(AppConfig.FIREBASE_URL);
        firebase.child(AppConfig.TABLE_USERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressDialog.dismiss();

                if (!songItemAdapter.isEmpty()) {
                    songItemAdapter.clear();
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    //For each item (child), assign data to DTO.
                    User user = new User();
                    //Username
                    user.username = (String) child.child("username").getValue();
                    Song intunedTrack = new Song();
                    //InTuned Track - Title
                    intunedTrack.title = (String) child.child("intunedTrack").child("title").getValue();
                    //InTuned Track = ID
                    intunedTrack.id = (String) child.child("intunedTrack").child("id").getValue();
                    //InTuned Track = Artist
                    intunedTrack.artist = (String) child.child("intunedTrack").child("artist").getValue();
                    //InTuned Track = Album
                    intunedTrack.album = (String) child.child("intunedTrack").child("album").getValue();
                    //InTuned Track = Filename
                    intunedTrack.fileName = (String) child.child("intunedTrack").child("fileName").getValue();
                    user.intunedTrack = intunedTrack;
                    //Add camp to service for UI.
                    songItemAdapter.add(intunedTrack);
                }
                //Update the recyclerview to reflect the most recent changes to data.
                postedSongsRecyclerview.setAdapter(songItemAdapter);
                //Update title.
                setTitle("vIbes (" + songItemAdapter.getItemCount() + ")");
            }

            @Override
            public void onCancelled(FirebaseError error) {
                ModalService.displayNotification("Lost connection to database, please try again.", "Sorry", MainActivity.this);
            }
        });
    }

    private void firebaseStorageSetup() {
        firebaseStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app-camp-central.appspot.com
        storageReference = firebaseStorage.getReferenceFromUrl("gs://project-4361900320818092365.appspot.com").child("Music");
    }

    //Play clip of song selected.
    private void playSong(final int position, String fileName) {
        //TODO: User song download url for playing song.
        String url = AppConfig.S3_MUSIC_BUCKET_URL + fileName; // your URL here
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                    mp.start();
                    songItemAdapter.getSongViewHolder(position).seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (mp != null && mp.isPlaying() && fromUser) {
                                mp.seekTo(progress * 1000);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    final Timer myTimer = new Timer();
                    myTimer.schedule(new TimerTask() {
                        int count = 0;

                        @Override
                        public void run() {
                            count = songItemAdapter.getSongViewHolder(position).seekbar.getProgress();
                            //Play song until time limit is reached.
                            if (count >= AppConfig.SONG_DURATION) {
                                myTimer.cancel();
                                mp.stop();
                                mp.reset();
                                songItemAdapter.getSongViewHolder(position).seekbar.setProgress(0);
                                return;
                            }
                            count++;
                            songItemAdapter.getSongViewHolder(position).seekbar.setProgress(count);
                        }
                    }, 0, 1000);
                }
            });
        } catch (IOException e) {
            ModalService.displayNotification("Error", e.toString(), this);
        }
    }

    //Returns all MP3 files currently on the device.
    private void scanSdCard() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        listOfUserSongs = new ArrayList<Song>();
        try {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Song song = new Song();
                    song.title = cursor.getString(0);
                    song.artist = cursor.getString(1);
                    song.path = cursor.getString(2);
                    song.songDuration = cursor.getString(4);
                    listOfUserSongs.add(song);
                    cursor.moveToNext();
                }

            }

        } catch (Exception e) {
            System.out.print("EXCEPTION!: " +
                    e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //Adapter that holds each song item.
    private class SongItemAdapter extends SongListAdapter<SongListAdapter.SongViewHolder> {

        ArrayList<SongViewHolder> songViewHolders = new ArrayList<SongViewHolder>();
        int lastPosition = -1;

        /**
         * Returns song view (UI) at specified index.
         *
         * @param index
         * @return SongViewHolder
         */
        public SongViewHolder getSongViewHolder(int index) {
            return songViewHolders.get(index);
        }

        @Override
        public SongListAdapter.SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item_view, parent, false);
            return new SongViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(SongViewHolder songViewHolder, int position) {
            Song song = songItemAdapter.getItem(position);
            if (songViewHolder instanceof SongViewHolder) {
                songViewHolder.name.setText(song.title);
                songViewHolder.artist.setText(song.artist);
                songViewHolder.postDateTime.setText(song.postDateTime);
                songViewHolder.seekbar.setProgress(0);
                songViewHolder.seekbar.setMax(AppConfig.SONG_DURATION);
                songViewHolders.add(songViewHolder);
            }

            // Here you apply the animation when the view is bound
            setAnimation(songViewHolder.itemView, position);
        }

        /**
         * Animation for each item in recyclerview.
         **/
        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

    //Initializes the recylcerview of songs.
    private void initRecyclerView() {
        postedSongsRecyclerview.setAdapter(songItemAdapter);

        // Set layout manager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(postedSongsRecyclerview.getContext());
        postedSongsRecyclerview.setLayoutManager(layoutManager);

        // Add decoration for dividers between list items
        postedSongsRecyclerview.addItemDecoration(new DividerDecoration(MainActivity.this));

        postedSongsRecyclerview.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Song song = songItemAdapter.getItem(position);
                playSong(position, song.fileName);
            }
        }));

        songItemAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                //headersDecor.invalidateHeaders();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
