package org.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.LoggerFactory;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.util.Properties;

public class MusicProducer {
    public static void main(String[] args) {

        // Kafka producer configuration
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "pkc-lzvrd.us-west4.gcp.confluent.cloud:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, MusicSerializer.class.getName());

        // Additional properties for Confluent Cloud
        properties.put("security.protocol", "SASL_SSL");
        properties.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username='7QD5MUTZRN3ALWZY' password='jq/2+gSOj5Y9HyYPnzxWT+WyILV7zb70OOhneiCd3FVzIEToiKhvgWKFwZoE+vJf';");
        properties.put("sasl.mechanism", "PLAIN");

        // Create Kafka producer
        try (Producer<String, MusicData> producer = new KafkaProducer<>(properties)) {
            // Produce music stream to this topic
            String topic = "song-streams";

            // Assuming songs are stored in a folder
            File songsFolder = new File("Songs");
            File[] songFiles = songsFolder.listFiles();

            if (songFiles != null) {
                for (File songFile : songFiles) {
                    // file attributes
                    String time = "99";
                    String artist = "Unknown";
                    byte[] image = null;
                    String songName = songFile.getName().replaceFirst("[.][^.]+$", "");

                    // read tag
                    Mp3File mp3file = new Mp3File(songFile);
                    if (mp3file.hasId3v2Tag()) {
                        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                        artist = id3v2Tag.getArtist();
                        time = Long.toString(mp3file.getLengthInSeconds());
                        image = id3v2Tag.getAlbumImage();
                    }

                    // create music data object
                    MusicData musicData = new MusicData(songName, artist, songFile.getPath(), time, image);

                    // publish record
                    ProducerRecord<String, MusicData> record = new ProducerRecord<>(topic, musicData);
                    producer.send(record);
                    System.out.println(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
