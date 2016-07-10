package Adapters;

import DTO.Song;
import DTO.User;
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
    private ArrayList<User> users = new ArrayList<User>();
    private int count;

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        public CardView cv;
        public TextView username;
        public TextView name;
        public TextView artist;
        public TextView postDateTime;
        public SeekBar seekbar;

        public SongViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.song_item_name);
            artist = (TextView) itemView.findViewById(R.id.song_item_artist);
            postDateTime = (TextView) itemView.findViewById(R.id.song_item_post_date_time);
            seekbar = (SeekBar) itemView.findViewById(R.id.song_item_seekbar);
            username = (TextView) itemView.findViewById(R.id.song_item_username);
        }

        public SongViewHolder getSongViewHolder(){
            return this;
        }
    }

    public void add(User user) {
        if (users.add(user)){;
            notifyDataSetChanged();
        }
    }

    public void addAll(ArrayList<User> list) {
        if (list != null) {
            users = list;
            notifyDataSetChanged();
        }
    }

    public void clear(){
        users.clear();
    }

    public void remove(User user) {
        users.remove(user);
        notifyDataSetChanged();
        //Call in activity as such; adapter.remove(adapter.getItem(position));
    }

    public User getItem(int position) {
        return users.get(position);
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
        return users.size();
    }

    public boolean isEmpty(){
        return users.size() < 1 || users == null;
    }

}