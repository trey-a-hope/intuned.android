package com.intuned.app;

import Adapters.SongListAdapter;
import DTO.Song;
import Listeners.RecyclerItemClickListener;
import Miscellaneous.DividerDecoration;
import Services.ModalService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView tvHeader;
    private ActionBar actionBar;
    private Button btnNewSong;
    private String artist, album, track;
    private RecyclerView postedSongsRecyclerview;
    private SongItemAdapter songItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();
        initObjects();
        setValues();
        initRecyclerView();

        IntentFilter iF = new IntentFilter();
        // Read action when music player changed current song
        // I just try it with stock music player form android

        // stock music player
        iF.addAction("com.android.music.metachanged");

        // MIUI music player
        iF.addAction("com.miui.player.metachanged");

        // HTC music player
        iF.addAction("com.htc.music.metachanged");

        // WinAmp
        iF.addAction("com.nullsoft.winamp.metachanged");

        // MyTouch4G
        iF.addAction("com.real.IMP.metachanged");

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

    private void initObjects(){
        toolbar.setBackgroundColor(getResources().getColor(R.color.Red900));
        setSupportActionBar(toolbar);
        setTitle("inTuned");
        actionBar = getSupportActionBar();
        drawerToggle = setupDrawerToggle();
        songItemAdapter = new SongItemAdapter();
    }

    private void setValues(){
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
        tvHeader.setText("InTuned, what are you listening to?");
        navigationView.setBackgroundColor(getResources().getColor(R.color.Indigo50));
        setupDrawerContent(navigationView);
        btnNewSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager manager = (AudioManager)MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
                if(manager.isMusicActive()){
                    if(ModalService.displayConfirmation("Post Song?", "This song will be played for others.", MainActivity.this)){
                        Song song = new Song();
                        song.artist = artist;
                        song.title = track;
                        song.album = album;
                        songItemAdapter.add(song);
                        //ModalService.displayToast(track, MainActivity.this);
                    }else{
                        //TODO;
                    }
                }else{
                    ModalService.displayToast("Turn on your music first...", MainActivity.this);
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
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

    private class SongItemAdapter extends SongListAdapter<SongListAdapter.SongViewHolder> {

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
                songViewHolder.dateModified.setText(song.dateModified);
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

    private void initRecyclerView() {
        // Set adapter populated with example dummy data

        songItemAdapter.addAll(null);
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
                ModalService.displayNotification(song.title, song.artist, MainActivity.this);
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

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }
}
