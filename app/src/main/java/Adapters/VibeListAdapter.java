package Adapters;

import Models.DomainModels.User;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.intuned.app.R;

import java.util.ArrayList;

/*
    Each song's info card.
 */

public abstract class VibeListAdapter<VH extends VibeListAdapter.VibeViewHolder> extends RecyclerView.Adapter<VH> {
    private ArrayList<User> users = new ArrayList<User>();
    private int count;

    public static class VibeViewHolder extends RecyclerView.ViewHolder {
        public CardView cv;
        public TextView username;
        public TextView songName;
        public TextView artistName;
        public TextView postDateTime;
        public RelativeLayout color;
        public SeekBar seekbar;

        public VibeViewHolder(View itemView) {
            super(itemView);
            songName = (TextView) itemView.findViewById(R.id.vibe_song_name);
            artistName = (TextView) itemView.findViewById(R.id.vibe_song_artist);
            postDateTime = (TextView) itemView.findViewById(R.id.vibe_post_date_time);
            seekbar = (SeekBar) itemView.findViewById(R.id.vibe_seekbar);
            username = (TextView) itemView.findViewById(R.id.vibe_username);
            color = (RelativeLayout) itemView.findViewById(R.id.vibe_color);
        }

        public VibeViewHolder getSongViewHolder(){
            return this;
        }
    }

    public void add(User user) {
        if (users.add(user)){;
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