# Distributed Music Player
Distributed Systems - Final Project
## How To Run
Open ProjectAppV2 in Android Studios.
<br>
Open KafkaServerV1.1 and Enter the Following Commands
```
# To Run Kafka Producer
./gradlew runMusicProducer

# To Run Kafka Consumer
./gradlew runMusicConsumer
```
## Expected Output
Main screen will be initally empty. Once data containing album image, artist name, audio length, etc, arrives the screen will update. Approx 5-10 seconds. Kafka consumer must stay continuously running in terminal.
<br>
![](https://github.com/23Vishan/Distributed-Music-Player/blob/main/Screenshots/User_Interface.png)
## Confluent Cloud
Cloud-native service for data streaming using Apache Kafka. The api key and password required are hardcoded in to MusicConsumer.java and MusicProducer.java
![](https://github.com/23Vishan/Distributed-Music-Player/blob/main/Screenshots/Cloud.png)
<br>
## Group 8
- Vishan Patel - 100784201
- Eria Hua - 100777617
- Furqan Mahmood - 100790243
- Dillon Dudley - 100743584
- Sanjith Gnanabaskaran - 100635268
