package com.example.distributedmusicplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Iterator;

public class AudioRVAdapter extends RecyclerView.Adapter<AudioRVAdapter.ViewHolder> {
    private ArrayList<AudioModal> audioModals;
    private OnItemClickListener listener;
    private Context context;
    private int counter = 0;

    // constructor
    public AudioRVAdapter(ArrayList<AudioModal> audioModals, Context context, OnItemClickListener listener) {
        this.audioModals = audioModals;
        this.context = context;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(AudioModal modal);
    }

    @NonNull
    @Override
    // inflate layout
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_recycleview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // set data to recycler view item
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        AudioModal modal = audioModals.get(index);

        // format time
        int totalSeconds = Integer.valueOf(modal.getTime());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String time = String.format("%02d:%02d", minutes, seconds);

        // set attributes
        holder.title.setText(modal.getTitle());
        holder.artist.setText(modal.getArtist());
        holder.time.setText(time);

        byte[] image = modal.getImage();
        if (image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            holder.image.setImageBitmap(bitmap);
            holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            int[] colors = new int[]{
                    Color.parseColor("#FFD063"),
                    Color.parseColor("#FF9CE1"),
                    Color.parseColor("#9CFAFF"),
                    Color.parseColor("#9BFF80")
            };

            counter = (counter + 1) % colors.length;

            holder.card.setCardBackgroundColor(colors[counter]);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(modal);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioModals.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, artist, time;
        private ImageView image;
        private CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // get views
            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
            time = itemView.findViewById(R.id.time);
            image = itemView.findViewById(R.id.image);
            card = itemView.findViewById(R.id.card);
        }
    }

    // filtering items
    public void filterList(ArrayList<AudioModal> filteredList) {
        audioModals = filteredList;

        // notify adapter of change in recycler view data
        notifyDataSetChanged();
    }
}