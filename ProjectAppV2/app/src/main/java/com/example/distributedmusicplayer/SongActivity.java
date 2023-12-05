package com.example.distributedmusicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class SongActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Boolean loaded = false;
    HttpURLConnection conn;

    Boolean playing = false;
    int currentSeconds = 0;
    int totalSeconds = 99;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        // get data
        Intent data = getIntent();
        String name = data.getStringExtra("name");
        String artist = data.getStringExtra("artist");
        byte[] image = data.getByteArrayExtra("image");

        // format time
        totalSeconds = Integer.valueOf(data.getStringExtra("time"));
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String time = String.format("%02d:%02d", minutes, seconds);
        TextView text_endTime = findViewById(R.id.end_time);
        text_endTime.setText(time);

        // initialize views
        TextView text_title = findViewById(R.id.textview_title);
        TextView text_artist = findViewById(R.id.textview_artist);
        text_title.setText(name);
        text_artist.setText(artist);

        // update album cover
        ImageView imageView_image = findViewById(R.id.image);
        if (image != null && imageView_image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            imageView_image.setImageBitmap(bitmap);
            imageView_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        // load song into media player
        if (!loaded) {
            GetSong(name);
            manageTime();
        }

        // Get media buttons
        FloatingActionButton play = findViewById(R.id.button_play);
        FloatingActionButton rewind = findViewById(R.id.button_rewind);
        FloatingActionButton forward = findViewById(R.id.button_forward);
        Button back = findViewById(R.id.button_back);

        // go back to home screen
        back.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            loaded = false;
            finish();
        });

        play.setOnClickListener(v -> {
            // pause song
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playing = false;
                play.setImageDrawable(getDrawable(R.drawable.ic_play_filled));
            }
            // play song
            else if (mediaPlayer != null && loaded) {
                mediaPlayer.start();
                playing = true;
                play.setImageDrawable(getDrawable(R.drawable.ic_pause_filled));
            }
            // wait for song to load
            else {
                Toast.makeText(this, "Loading, please wait", Toast.LENGTH_SHORT).show();
            }
        });

        forward.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int forwardPosition = currentPosition + 10000; // 10000 milliseconds = 10 seconds
                currentSeconds += 10;
                if (forwardPosition <= mediaPlayer.getDuration()) {
                    mediaPlayer.seekTo(forwardPosition);
                } else {
                    currentSeconds = totalSeconds;
                    mediaPlayer.seekTo(mediaPlayer.getDuration()); // If the forward position exceeds the duration, seek to the end
                }
            }
            else {
                Toast.makeText(this, "Loading, please wait", Toast.LENGTH_SHORT).show();
            }
        });

        rewind.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int rewindPosition = currentPosition - 10000; // 10000 milliseconds = 10 seconds
                currentSeconds -= 10;
                if (rewindPosition >= 0) {
                    mediaPlayer.seekTo(rewindPosition);
                } else {
                    currentSeconds = 0;
                    mediaPlayer.seekTo(0); // If the rewind position is negative, seek to the start
                }
            }
            else {
                Toast.makeText(this, "Loading, please wait", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void manageTime() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Slider slider = findViewById(R.id.slider);

                while (true) {
                    if (playing) {
                        currentSeconds++;

                        int minutes = currentSeconds / 60;
                        int seconds = currentSeconds % 60;
                        String time = String.format("%02d:%02d", minutes, seconds);

                        // update view
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                slider.setValueTo(totalSeconds);
                                TextView text_endTime = findViewById(R.id.start_time);
                                text_endTime.setText(time);
                                slider.setValue(currentSeconds);
                            }
                        });

                        // wait 1 second
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
    }

    public void GetSong(String name) {
        Runnable runnable = () -> {
            try {
                // Connect to Server
                URL url = new URL("http://10.0.2.2:8080/request-song" + "?songId=" + name);
                conn = (HttpURLConnection) url.openConnection();

                if (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (conn.getResponseCode() == 200) {
                            // Set up MediaPlayer with the input stream
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build());

                            // local
                            if (Objects.equals(name, "Action Strike")) {
                                AssetFileDescriptor afd = getAssets().openFd("Action_Strike.mp3");
                                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                afd.close();

                                mediaPlayer.setOnPreparedListener(mp -> {
                                    loaded = true;
                                });

                                Log.d("Play", "Local");
                            }
                            // stream
                            else {
                                mediaPlayer.setDataSource(url.toString());
                                mediaPlayer.setOnPreparedListener(mp -> {
                                    loaded = true;
                                });
                                Log.d("Play", "Stream");
                            }

                            mediaPlayer.prepareAsync();
                        }
                    } catch (IOException e) {
                        Log.e("Error playing the streamed song", e.getMessage());
                    }
                } else {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    loaded = false;
                }
            } catch (IOException e) {
                Log.e("Error making HTTP request", e.getMessage());
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        loaded = false;

        if (conn != null) {
            conn.disconnect();
        }
    }
}