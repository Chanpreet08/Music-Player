package musicplayer.cplayer.com.cplayer;


import android.graphics.Bitmap;

public class Song {


    // Variables
    private long id;
    private String artist ="";
    private String track="";
    private Bitmap arbum_art;

  // Constructor
    public Song(String artist, long id, String track,Bitmap album) {
        this.artist = artist;
        this.id = id;
        this.track = track;
        this.arbum_art=album;
    }

    public String getArtist() {
        return artist;
    }

    public long getId() {
        return id;
    }

    public String getTrack() {
        return track;
    }

    public Bitmap getArbum_art() {
        return arbum_art;
    }
}
