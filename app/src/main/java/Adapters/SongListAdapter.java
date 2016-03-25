package Adapters;

import DTO.Song;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.intuned.app.R;

import java.util.ArrayList;

/*
    Each song's info card.
 */

public abstract class SongListAdapter<VH extends SongListAdapter.SongViewHolder> extends RecyclerView.Adapter<VH> {
    private ArrayList<Song> songs;
    private int count;

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        public CardView cv;
        public TextView name;
        public TextView artist;
        public TextView dateModified;
        public SeekBar seekbar;

        public SongViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.song_item_name);
            artist = (TextView) itemView.findViewById(R.id.song_item_artist);
            dateModified = (TextView) itemView.findViewById(R.id.song_item_date_modified);
            seekbar = (SeekBar) itemView.findViewById(R.id.song_item_seekbar);

        }

        public SongViewHolder getSongViewHolder(){
            return this;
        }
    }

    public void add(Song song) {
        if (song != null) {
            songs.add(song);
            notifyDataSetChanged();
        }
    }

    public void addAll(ArrayList<Song> list) {
        Song song1 = new Song();
        song1.album = "Trey's Revolution";
        song1.title= "Oh Got Damn";
        song1.artist = "Travisty";
        Song song2 = new Song();
        song2.album = "Trey's Revolution Part II";
        song2.title= "Safehouse";
        song2.artist = "Travisty";
        list = new ArrayList<Song>();
        list.add(song1);
        list.add(song2);
        if (list != null) {
            songs = list;
            notifyDataSetChanged();
        }
    }

    public void remove(Song song) {
        songs.remove(song);
        notifyDataSetChanged();
        //Call in activity as such; adapter.remove(adapter.getItem(position));
    }

    public Song getItem(int position) {
        return songs.get(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

}