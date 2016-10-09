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

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import Services.FileService;
import Services.ModalService;

public class TimeLineFragmentController extends Fragment {
    private HomeController activity;
    private View view;
    private SoundFile soundFile;
    private ModalService modalService = ModalService.getInstance();

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

    private final String FILE_CREATION_DIRECTORY = "/storage/emulated/0/Music/Vibes";
    private final String FIREBASE_FILESTORAGE_PATH = "gs://project-4361900320818092365.appspot.com";
    private final String LOGTAG = "TimeLineFragController";
    private boolean success = false;

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

        activity.registerReceiver(mReceiver, iF);
    }

    private void initFirebase() {
        Firebase.setAndroidContext(getContext());
        firebase = new Firebase(AppConfig.FIREBASE_URL);
    }

    private void initUI(LayoutInflater inflater, ViewGroup container) {
        view = inflater.inflate(R.layout.fragment_time_line_fragment_controller, container, false);
        postedSongsRecyclerview = (RecyclerView) view.findViewById(R.id.rv);
        btnNewSong = (FloatingActionButton) view.findViewById(R.id.timeline_new_song_button);
    }


    private void initObjects() {
        sessionManager = new SessionManager(activity, getContext());
        vibeItemAdapter = new VibeItemAdapter();
    }

    private void setValues() {
        btnNewSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioManager manager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
                //Validate user is playing music.
                if (manager.isMusicActive()) {
                    if (modalService.displayConfirmation("Post Song?", "This song will be available for others to listen to.", activity)) {
                        new AsyncTask<String, Integer, Boolean>(){

                            protected void onPreExecute() {
                                Log.d(LOGTAG,"AsyncTask Started");
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
                                Log.d(LOGTAG,"onProgressUpdate - " + values[0]);
                            }

                            protected void onPostExecute(Boolean _success) {
                                if(_success){
                                    modalService.displayToast("Upload finished.", activity);
                                }else{
                                    modalService.displayToast("Could not complete upload.", activity);
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
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message1) {
                throw new RuntimeException();
            }
        };

        byte[] data = FileService.getInstance().readContentIntoByteArray(file);
        //Upload image with user's ID as file name.
        UploadTask uploadTask = storageReference.child(sessionManager.getUserInstance().id).putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                handler.sendMessage(handler.obtainMessage());
                //modalService.displayNotification("Error", exception.getMessage(), activity);
                // Handle unsuccessful uploads
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String mp3DownloadUrl = taskSnapshot.getDownloadUrl().toString();
                String key = firebase.child(AppConfig.TABLE_USERS).child(sessionManager.getUserInstance().id).push().getKey();
                String postDateTime = new DateTime().toString();
                song.mp3DownloadUrl = mp3DownloadUrl;
                song.id = key;
                song.postDateTime = postDateTime;
                song.emotionId = Emotion.HAPPY.getValue();
                firebase.child(AppConfig.TABLE_USERS).child(sessionManager.getUserInstance().id).child("song").setValue(song);
                //modalService.displayNotification("Success", "Your song has been uploaded.", activity);
                success = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });

        try {Looper.loop();} catch (RuntimeException e2) {}
        return success;
    }

    private void getTimeLine() {
        modalService.toggleProgressDialogOn(activity, "Getting your timeline.");
        Log.v(LOGTAG, "Getting your timeline.");
        //Iterate over users the user is following.
        firebase.child(AppConfig.TABLE_USERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                modalService.toggleProgressDialogOff();

                if (!vibeItemAdapter.isEmpty()) {
                    vibeItemAdapter.clear();
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    //For each item (child), assign data to DTO.
                    User user = new User();
                    Song song = new Song();
                    //Only add user's who have a song uploaded.
                    if (child.child("song").getValue() != null) {
                        Log.v(LOGTAG, String.valueOf(child.child("song").child("title").getValue()));
                        //InTuned Track - Title
                        song.title = (String) child.child("song").child("title").getValue();
                        //InTuned Track = ID
                        song.id = (String) child.child("song").child("id").getValue();
                        //InTuned Track = Artist
                        song.artist = (String) child.child("song").child("artist").getValue();
                        //InTuned Track = Album
                        song.album = (String) child.child("song").child("album").getValue();
                        //InTuned Track = Filename
                        song.fileName = (String) child.child("song").child("fileName").getValue();
                        //Emotion of Vibe
                        song.emotionId = ((Long) child.child("song").child("emotionId").getValue()).intValue();
                        //Username
                        user.username = (String) child.child("username").getValue();
                        user.song = song;
                        //Add 'vibe'.
                        vibeItemAdapter.add(user);
                    }

                }
                //Update the recyclerview to reflect the most recent changes to data.
                postedSongsRecyclerview.setAdapter(vibeItemAdapter);
            }

            @Override
            public void onCancelled(FirebaseError error) {
                modalService.displayNotification("Lost connection to database, please try again.", "Sorry", activity);
            }
        });
    }

    private void firebaseStorageSetup() {
        firebaseStorage = FirebaseStorage.getInstance();
        // Create a storage reference from our app-camp-central.appspot.com
        storageReference = firebaseStorage.getReferenceFromUrl(FIREBASE_FILESTORAGE_PATH).child("Music");
    }

    //Play clip of song selected.
    private void playSong(final int position, String fileName) {
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
                vibeViewHolder.songName.setText(user.song.title);
                vibeViewHolder.artistName.setText(user.song.artist);
                vibeViewHolder.postDateTime.setText("3 minutes ago");
                vibeViewHolder.username.setText(user.username);
                vibeViewHolder.seekbar.setProgress(0);
                vibeViewHolder.seekbar.setMax(AppConfig.SONG_DURATION);
                vibeViewHolder.color.setBackgroundColor(getVibeColor(user.song.emotionId));
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

    private int getVibeColor(int emotionId){
        //Ensure enums match with this method at all times.
        switch (emotionId){
            case 0:
                return Emotion.HAPPY.getColor();
            case 1:
                return Emotion.SAD.getColor();
            case 2:
                return Emotion.ANGRY.getColor();
            case 3:
                return Emotion.FEARFUL.getColor();
            case 4:
                return Emotion.DISGUSTED.getColor();
            default:
                //User is happy be default :)
                return Emotion.HAPPY.getColor();
        }
    }

}