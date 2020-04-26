package com.example.simplemusicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder>{

    private Context context;
    private List<musicbean> musicData;

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        public void onItemClick(View view ,int position);
    }

    public MusicAdapter(Context context, List<musicbean> musicData) {
        this.context = context;
        this.musicData = musicData;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_music,parent,false);
        MusicViewHolder holder = new MusicViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, final int position) {

        musicbean musicbean = musicData.get(position);
        holder.idTv .setText(musicbean.getId());
        holder.songTv .setText(musicbean.getSong());
        holder.singerTv .setText(musicbean.getSinger());
        holder.albumTv .setText(musicbean.getAlbum());
        holder.timeTv .setText(musicbean.getTime());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onItemClickListener.onItemClick(v,position);//接口回调

            }
        });

    }

    @Override
    public int getItemCount() {
        return musicData.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder{

        TextView idTv,songTv,singerTv,albumTv,timeTv;

        public MusicViewHolder(View itemView) {
            super(itemView);

            idTv = itemView.findViewById(R.id.item_music_number);
            songTv = itemView.findViewById(R.id.item_music_song);
            singerTv = itemView.findViewById(R.id.item_music_singer);
            albumTv = itemView.findViewById(R.id.item_music_album);
            timeTv = itemView.findViewById(R.id.item_music_time);

        }
    }
}
