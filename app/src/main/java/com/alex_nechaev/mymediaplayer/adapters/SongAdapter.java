package com.alex_nechaev.mymediaplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alex_nechaev.mymediaplayer.FileManager;
import com.alex_nechaev.mymediaplayer.R;
import com.alex_nechaev.mymediaplayer.Song;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private OnSongListener callBack;
    public Context context;

    public interface OnSongListener {
        void onSongClicked(int position, View view);

        void onSongLongClicked(int position, View view);
    }

    public void setCallBack(OnSongListener callBack) {
        this.callBack = callBack;
    }

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {

        ImageView albumImg;
        TextView artistName;
        TextView songName;
        CheckBox liked;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            albumImg = itemView.findViewById(R.id.cell_artist_album_img);
            artistName = itemView.findViewById(R.id.cell_artist_name_text_view);
            songName = itemView.findViewById(R.id.cell_artist_song_text_view);
            liked = itemView.findViewById(R.id.cell_like_checkbox);

            liked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    songList.get(getAdapterPosition()).setLiked(isChecked);
                    FileManager.writeToFile(buttonView.getContext(), songList, "song_list");
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callBack != null) {
                        callBack.onSongClicked(getAdapterPosition(), view);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (callBack != null) {
                        callBack.onSongLongClicked(getAdapterPosition(), view);
                    }
                    return false;
                }
            });
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_song, parent, false);
        SongViewHolder songViewHolder = new SongViewHolder(view);
        return songViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);

        Glide
                .with(this.context)
                .load(song.getImgUri())
                .centerCrop()
                .placeholder(R.drawable.music_album_icon)
                .transform(new MultiTransformation<>(new CenterCrop(),new RoundedCornersTransformation(30, 5)))
                .into(holder.albumImg);

        holder.artistName.setText(song.getArtistName());
        holder.songName.setText(song.getSongName());
        holder.liked.setChecked(song.isLiked());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}
