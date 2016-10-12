package com.intuned.app.home;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;

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

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import Adapters.VibeListAdapter;
import Configuration.AppConfig;
import Enums.Emotion;
import Listeners.RecyclerItemClickListener;
import Miscellaneous.SoundFile;
import Models.DomainModels.Song;
import Models.DomainModels.User;
import Models.ViewModels.SongVM;
import Services.DateTimeService;
import Services.FileService;
import Services.ImageService;
import Services.ModalService;

public class TimeLineFragmentController extends Fragment {
    private HomeController activity;
    private View view;
    private SoundFile soundFile;

    //Services
    private ModalService modalService = ModalService.getInstance();
    private DateTimeService dateTimeService = DateTimeService.getInstance();
    private FileService fileService = FileService.getInstance();
    private ImageService imageService = ImageService.getInstance();

    //Song
    private String artist;
    private String album;
    private String track;

    private RecyclerView postedSongsRecyclerview;
    private VibeItemAdapter vibeItemAdapter;
    private ProgressDialog seekDialog;
    private ArrayList<Song> listOfUserSongs;
    private Firebase firebase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private MediaPlayer mediaPlayer;
    private SessionManager sessionManager;
    private FloatingActionButton btnNewSong;
    private FloatingActionButton btnRefresh;

    private final String FILE_CREATION_DIRECTORY = "/storage/emulated/0/Music/Vibes";
    private final String LOGTAG = "TimeLineFragController";
    private boolean success = false;

    private BroadcastReceiver broadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (HomeController) getActivity();
        firebaseStorageSetup();
        initUI(inflater, container);
        scanSdCard();
        initObjects();
        initFirebase();
        getTimeLine();
        setValues();
        initRecyclerView();
        registerReceiver();
        return view;
    }

    @Override
    public void onStop() {
        Log.v(LOGTAG, "onStop()");
        //activity.unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private void SoundFileSetUp(String pathToFile) {
        try {
            soundFile = SoundFile.create(pathToFile, new SoundFile.ProgressListener() {
                public boolean reportProgress(double fractionComplete) {
                    return true;
                }
            });
        } catch (FileNotFoundException fileNotFoundException) {
            modalService.displayNotification("Error", fileNotFoundException.getMessage(), activity);
        } catch (IOException ioException) {
            modalService.displayNotification("Error", ioException.getMessage(), activity);
        } catch (SoundFile.InvalidInputException invalidInputException) {
            modalService.displayNotification("Error", invalidInputException.getMessage(), activity);
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
        broadcastReceiver = new BroadcastReceiver() {
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

        activity.registerReceiver(broadcastReceiver, iF);
    }

    private void initFirebase() {
        Firebase.setAndroidContext(getContext());
        firebase = new Firebase(AppConfig.FIREBASE_URL);
    }

    private void initUI(LayoutInflater inflater, ViewGroup container) {
        view = inflater.inflate(R.layout.fragment_time_line_fragment_controller, container, false);
        postedSongsRecyclerview = (RecyclerView) view.findViewById(R.id.rv);
        btnNewSong = (FloatingActionButton) view.findViewById(R.id.timeline_new_song_button);
        btnRefresh = (FloatingActionButton) view.findViewById(R.id.timeline_refresh_button);
    }


    private void initObjects() {
        sessionManager = new SessionManager(activity, getContext());
        vibeItemAdapter = new VibeItemAdapter();
    }

    private void setValues() {
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOGTAG, "Refreshing timeline...");
                //Clear adapter before retrieving items.
                if (!vibeItemAdapter.isEmpty()) {
                    vibeItemAdapter.clear();
                }
                getTimeLine();
            }
        });
        btnNewSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioManager manager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                //Validate user is playing music.
                if (manager.isMusicActive()) {
                    if (modalService.displayConfirmation("Post Song?", "This song will be available for others to listen to.", activity)) {
                        new AsyncTask<String, Integer, Boolean>() {
                            protected void onPreExecute() {
                                Log.d(LOGTAG, "AsyncTask Started");
                                modalService.displayToast("Starting upload...", activity);
                            }

                            protected Boolean doInBackground(String... params) {
                                Looper.prepare();
                                //Search for song in songs from device.
                                for (Song song : listOfUserSongs) {
                                    String songId = song.artist + song.title;
                                    String currentSongId = artist + track;
                                    if (songId.equals(currentSongId)) {
                                        SoundFileSetUp(song.path);
                                        File directory = new File(FILE_CREATION_DIRECTORY);
                                        if (!directory.exists()) {
                                            if (directory.mkdir()) {
                                                Log.d("Success", "Directory created.");
                                            } else {
                                                Log.d("Failure", "Could not create directory");
                                            }
                                        }
                                        try {
                                            File file = new File(FILE_CREATION_DIRECTORY + "/" + song.fileName);
                                            //Create a new file.
                                            file.createNewFile();
                                            //Split song into segment based off start & end times in seconds.
                                            soundFile.WriteWAVFile(file, 20f, 30f);
                                            //Upload clipped song to firebase storage.
                                            return uploadSongToFirebaseStorage(file, song);
                                        } catch (IOException e) {
                                            modalService.displayTest(e.getMessage(), activity);
                                        }

                                    }
                                }
                                Looper.myLooper().quit();
                                return false;
                            }

                            protected void onProgressUpdate(Integer... values) {
                                Log.d(LOGTAG, "onProgressUpdate - " + values[0]);
                            }

                            protected void onPostExecute(Boolean _success) {
                                if (_success) {
                                    Log.d(LOGTAG, "Successful upload.");
                                } else {
                                    Log.d(LOGTAG, "Failed upload.");
                                }
                            }
                        }.execute();
                    } else {
                    }
                } else {
                    modalService.displayToast("Turn on your music first...", activity);
                }
            }
        });
    }

    private boolean uploadSongToFirebaseStorage(File file, final Song song) {
        Log.v(LOGTAG, "uploadSongToFirebaseStorage()");
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message1) {
                throw new RuntimeException();
            }
        };

        byte[] data = fileService.readContentIntoByteArray(file);
        //Upload image with user's ID as file name.
        UploadTask uploadTask = storageReference.child(sessionManager.getUserInstance().id).putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.v(LOGTAG, exception.getMessage());
                handler.sendMessage(handler.obtainMessage());
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String mp3DownloadUrl = taskSnapshot.getDownloadUrl().toString();
                String key = firebase.child(AppConfig.TABLE_USERS).child(sessionManager.getUserInstance().id).push().getKey();
                SongVM songVM = new SongVM();
                songVM.id = key;
                songVM.album = song.album;
                songVM.artist = song.artist;
                songVM.title = song.title;
                songVM.fileName = song.fileName;
                songVM.songDuration = song.songDuration;
                songVM.path = song.path;
                songVM.mp3DownloadUrl = mp3DownloadUrl;
                songVM.postDateTime = new DateTime().toString(AppConfig.DATE_TIME_FORMAT);
                songVM.emotionId = Emotion.HAPPY.getValue();
                firebase.child(AppConfig.TABLE_USERS).child(sessionManager.getUserInstance().id).child("song").setValue(songVM);
                //modalService.displayNotification("Success", "Your song has been uploaded.", activity);
                success = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });

        try {
            Looper.loop();
        } catch (RuntimeException e2) {
        }
        return success;
    }

    private void getTimeLine() {
        //TODO: May not need modal if loading all users over a period of time.
        modalService.toggleProgressDialogOn(activity, "Getting your timeline.");

        //TODO: ADD TO LIST OF FOLLOWERS
        final ArrayList<String> followedUserIdS = new ArrayList<String>();
        followedUserIdS.add("-KO8GEGTVW0crE4gNabD");
        followedUserIdS.add("-KTrTQYJdQi54blBchd9");

        //Iterate over users the user is following.
        for (final String followedUserId : followedUserIdS) {
            Log.v(LOGTAG, "Followed User ID " + followedUserId);
            firebase.child(AppConfig.TABLE_USERS).child(followedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.v(LOGTAG, dataSnapshot.toString());
                    modalService.toggleProgressDialogOff();

                    User user = new User();
                    Song song = new Song();
                    //Only add user's who have a song uploaded.
                    if (dataSnapshot.child("song").getValue() != null) {
                        Log.v(LOGTAG, String.valueOf(dataSnapshot.child("song").child("title").getValue()));
                        //InTuned Track - Title
                        song.title = (String) dataSnapshot.child("song").child("title").getValue();
                        //InTuned Track = ID
                        song.id = (String) dataSnapshot.child("song").child("id").getValue();
                        //InTuned Track = Artist
                        song.artist = (String) dataSnapshot.child("song").child("artist").getValue();
                        //InTuned Track = Album
                        song.album = (String) dataSnapshot.child("song").child("album").getValue();
                        //InTuned Track = Filename
                        song.fileName = (String) dataSnapshot.child("song").child("fileName").getValue();
                        //Emotion of Vibe
                        song.emotionId = ((Long) dataSnapshot.child("song").child("emotionId").getValue()).intValue();
                        //Post Date Time.
                        song.postDateTime = dateTimeService.stringToDateTime((String) dataSnapshot.child("song").child("postDateTime").getValue());
                        //Username
                        user.username = (String) dataSnapshot.child("username").getValue();
                        //Image Url
                        user.imageDownloadUrl = (String) dataSnapshot.child("imageDownloadUrl").getValue();
                        //Add song to user.
                        user.song = song;
                        //Add 'vibe'.
                        vibeItemAdapter.add(user);
                    }
                    //Update the recyclerview to reflect the most recent changes to data.
                    postedSongsRecyclerview.setAdapter(vibeItemAdapter);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    modalService.displayNotification(firebaseError.getMessage(), "Error", activity);
                }
            });
        }
    }

    private void firebaseStorageSetup() {
        firebaseStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app-camp-central.appspot.com
        storageReference = firebaseStorage.getReferenceFromUrl(AppConfig.FIREBASE_STORAGE_URL).child("Music");
    }

    //Play clip of song selected.
    private void playSong(final int position, String fileName) {
        Log.v(LOGTAG, "Play song button hit.");
        //TODO: User song download url for playing song.
        String url = "MP3 DOWNLOAD URL GOES HERE"; // your URL here
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                    mp.start();
                    vibeItemAdapter.getSongViewHolder(position).seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                            count = vibeItemAdapter.getSongViewHolder(position).seekbar.getProgress();
                            //Play song until time limit is reached.
                            if (count >= AppConfig.SONG_DURATION) {
                                myTimer.cancel();
                                mp.stop();
                                mp.reset();
                                vibeItemAdapter.getSongViewHolder(position).seekbar.setProgress(0);
                                return;
                            }
                            count++;
                            vibeItemAdapter.getSongViewHolder(position).seekbar.setProgress(count);
                        }
                    }, 0, 1000);
                }
            });
        } catch (IOException e) {
            modalService.displayNotification("Error", e.toString(), activity);
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
            cursor = activity.getContentResolver().query(uri, projection, selection, null, sortOrder);
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
    private class VibeItemAdapter extends VibeListAdapter<VibeListAdapter.VibeViewHolder> {

        ArrayList<VibeViewHolder> vibeViewHolders = new ArrayList<VibeViewHolder>();
        int lastPosition = -1;

        /**
         * Returns song view (UI) at specified index.
         *
         * @param index
         * @return SongViewHolder
         */
        public VibeViewHolder getSongViewHolder(int index) {
            return vibeViewHolders.get(index);
        }

        @Override
        public VibeListAdapter.VibeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vibe_item_view, parent, false);
            return new VibeViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(VibeViewHolder vibeViewHolder, int position) {
            User user = vibeItemAdapter.getItem(position);
            if (vibeViewHolder instanceof VibeViewHolder) {
                if(user.imageDownloadUrl != null){
                    Picasso.with(getContext()).load(user.imageDownloadUrl).transform(new ImageService.CircleTransform()).into(vibeViewHolder.profileImage);
                }else{
                    imageService.roundImage(getResources(), R.drawable.bg_blank_profile, vibeViewHolder.profileImage);
                }
                vibeViewHolder.songName.setText(user.song.title);
                vibeViewHolder.songName.setTextColor(Emotion.getColor(user.song.emotionId));
                vibeViewHolder.artistName.setText(user.song.artist);
                vibeViewHolder.postDateTime.setText(dateTimeService.timeDifference(user.song.postDateTime, new DateTime()));
                vibeViewHolder.username.setText(user.username);
                vibeViewHolder.seekbar.setProgress(0);
                vibeViewHolder.seekbar.setMax(AppConfig.SONG_DURATION);
                vibeViewHolder.color.setBackgroundColor(Emotion.getColor(user.song.emotionId));
                vibeViewHolders.add(vibeViewHolder);
            }

            // Here you apply the animation when the view is bound
            setAnimation(vibeViewHolder.itemView, position);
        }

        /**
         * Animation for each item in recyclerview.
         */
        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
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
        postedSongsRecyclerview.setAdapter(vibeItemAdapter);

        // Set layout manager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(postedSongsRecyclerview.getContext());
        postedSongsRecyclerview.setLayoutManager(layoutManager);

        postedSongsRecyclerview.addOnItemTouchListener(new RecyclerItemClickListener(activity, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                User user = vibeItemAdapter.getItem(position);
                playSong(position, user.song.fileName);
            }
        }));

        vibeItemAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
            }
        });
    }
}
