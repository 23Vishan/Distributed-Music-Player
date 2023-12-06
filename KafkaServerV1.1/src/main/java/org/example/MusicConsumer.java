package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.LoggerFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.Properties;
import java.util.function.Consumer;

public class MusicConsumer {

    private static final String KAFKA_TOPIC = "song-streams";
    private static final String GROUP_ID = "song-consumers-group";

    public static void main(String[] args) throws IOException {

        // Kafka consumer configuration
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "pkc-lzvrd.us-west4.gcp.confluent.cloud:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MusicDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Additional properties for Confluent Cloud
        properties.put("security.protocol", "SASL_SSL");
        properties.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username='7QD5MUTZRN3ALWZY' password='jq/2+gSOj5Y9HyYPnzxWT+WyILV7zb70OOhneiCd3FVzIEToiKhvgWKFwZoE+vJf';");
        properties.put("sasl.mechanism", "PLAIN");

        // Create list for retrieved songs
        List<MusicData> musicList = Collections.synchronizedList(new ArrayList<>());

        // Start Kafka consumer thread
        new Thread(() -> {
            try (Consumer<String, MusicData> consumer = new KafkaConsumer<>(properties)) {
                // Subscribe to topic
                consumer.subscribe(Collections.singletonList(KAFKA_TOPIC));

                // Listen for new records
                while (true) {
                    ConsumerRecords<String, MusicData> records = consumer.poll(Duration.ofMillis(100));
                    records.forEach(record -> {
                        MusicData musicData = record.value();
                        System.out.println(musicData.getArtist() + " " + musicData.getTime() + " "
                                + musicData.getTitle() + " " + musicData.getMusicPath());
                        musicList.add(musicData);
                    });
                }
            }
        }).start();

        // Set up HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/request-song", new RequestSongHandler(musicList)); // stream songs
        server.createContext("/request-all-songs", new RequestAllSongsHandler(musicList)); // get song names

        server.setExecutor(null);
        server.start();
    }

    private static class RequestSongHandler implements HttpHandler {
        private final List<MusicData> musicList;

        public RequestSongHandler(List<MusicData> musicList) {
            this.musicList = musicList;
        }

        @Override
        // Called when a HTTP request is received
        public void handle(HttpExchange exchange) throws IOException {
            // Handles HTTP GET requests
            if ("GET".equals(exchange.getRequestMethod())) {
                // Extract song ID from query parameter
                String req = exchange.getRequestURI().getQuery();
                String[] parts = req.split("=");
                String songId = parts.length == 2 ? parts[1] : req;

                while (true) {
                    // Iterate through the records to find the matching song
                    for (MusicData music : musicList) {
                        if (music.getTitle().equals(songId)) {

                            // Stream the MP3 file to the client
                            streamMp3File(exchange, music.getMusicPath());
                            return;
                        }
                    }

                    // If the song is not found send a message
                    String notFoundResponse = "Song not found for ID: " + songId;
                    System.out.println(notFoundResponse + songId);

                    // 404 response indicating that the song was not found
                    exchange.sendResponseHeaders(404, notFoundResponse.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(notFoundResponse.getBytes());
                    }
                }
            }
        }

        private void streamMp3File(HttpExchange exchange, String filePath) throws IOException {
            // Set response headers for streaming
            exchange.getResponseHeaders().set("Content-Type", "audio/mp3");
            exchange.sendResponseHeaders(200, 0);

            // Send over data from mp3 file
            try (OutputStream os = exchange.getResponseBody();
                    FileInputStream fis = new FileInputStream(filePath)) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    private static class RequestAllSongsHandler implements HttpHandler {
        private final List<MusicData> musicList;

        public RequestAllSongsHandler(List<MusicData> musicList) {
            this.musicList = musicList;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                JSONArray jsonArray = new JSONArray();
                for (MusicData music : musicList) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("title", music.getTitle());
                    jsonObject.put("artist", music.getArtist());
                    jsonObject.put("time", music.getTime());

                    // convert byte array to Base64 string
                    // String image = Base64.getEncoder().encodeToString(music.getImage());
                    // jsonObject.put("image", image);

                    jsonArray.put(jsonObject);
                }
                String jsonResponse = jsonArray.toString();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            }
        }
    }
}
