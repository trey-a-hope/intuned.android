package Models.DomainModels;

import org.joda.time.DateTime;

/*
    Song information.
 */
public class Song {
    public String id;
    public String title;
    public String artist;
    public String album;
    public DateTime postDateTime;
    public String fileName;
    public String path;
    public String songDuration;
    public String mp3DownloadUrl;
    public int emotionId;
    public int currentPosition;
}
