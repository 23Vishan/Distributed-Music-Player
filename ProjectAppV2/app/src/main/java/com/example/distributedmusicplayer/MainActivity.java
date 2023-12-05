package com.example.distributedmusicplayer;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // display list of songs
        GetSongs();
    }

    // get list of songs
    public void GetSongs() {
        Runnable runnable = () -> {
            try {
                // Connect to Server
                URL url = new URL("http://10.0.2.2:8080/request-all-songs");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn.getResponseCode() == 200) {
                    // get response as json object
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String response = readStream(in);
                    JSONArray jsonArray = new JSONArray(response);
                    Log.d("RESPONSE", jsonArray.toString());

                    // turn json object to a list item
                    ArrayList<AudioModal> items = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String title = jsonObject.getString("title");
                        String time = jsonObject.getString("time");
                        String artist;
                        byte[] image;

                        // update artist field
                        if (jsonObject.has("artist")) {
                            artist = jsonObject.getString("artist");
                        } else {
                            artist = "Unknown";
                        }

                        // empty artist field
                        if (artist.equals("")) {
                            artist = "Unknown";
                        }

                        // capitalize first letter
                        artist = artist.substring(0, 1).toUpperCase() + artist.substring(1);

                        if (jsonObject.has("image")) {
                            String tmp = jsonObject.getString("image");
                            image = Base64.decode(tmp, Base64.DEFAULT);
                        } else {
                            image = null;
                        }

                        // add to list
                        AudioModal item = new AudioModal(title, artist, time, image);
                        items.add(item);
                    }

                    // display list of music
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // update list view on the UI thread
                            RecyclerView rv = findViewById(R.id.musicRV);

                            // create adapter with onclick listener
                            AudioRVAdapter adapter = new AudioRVAdapter(items, getApplicationContext(), new AudioRVAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(AudioModal modal) {
                                    // Move to next activity
                                    Intent intent = new Intent(MainActivity.this, SongActivity.class);
                                    intent.putExtra("name", modal.getTitle());
                                    intent.putExtra("time", modal.getTime());
                                    intent.putExtra("artist", modal.getArtist());
                                    intent.putExtra("image", modal.getImage());
                                    startActivity(intent);
                                }
                            });

                            // setting layout manager for our recycler view.
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
                            rv.setLayoutManager(linearLayoutManager);
                            rv.setAdapter(adapter);
                        }
                    });
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.d("error", "could not display songs");
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    // Method to convert InputStream to String
    public String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            Log.d("toString", "error turning response to string");
            return "";
        }
    }
}