package musicplayer.cplayer.com.cplayer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.widget.Toast;
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener{
    //creating mediaplayer
    private MediaPlayer mediaplayer;
    // creating playlist
    private ArrayList<Song>playlist;
    //current position
    int song_positon;

    private final IBinder musicbind = new MusicBinder();

    private String songTitle="";
    private String songArtist="";
    private Bitmap album;
    private static final int NOTIFY_ID= 1;

    private boolean shuffle =false;
    private Random rand;

    @Override
    public void onCompletion(MediaPlayer mp) {
      if(mediaplayer.getCurrentPosition()>0)
      {
          mp.reset();
          playNext();
      }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

       mp.start();
        Toast.makeText(this,songTitle,Toast.LENGTH_SHORT).show();
        Intent notintent = new Intent(this,MainActivity.class);
        notintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pdintent = PendingIntent.getActivity(this,0,notintent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pdintent).setSmallIcon(R.drawable.play).setTicker(songTitle).setOngoing(true).setContentTitle(songTitle).setContentText(songArtist);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

    public MusicService() {
    }
    public void onCreate()
    {
        //creating service
        super.onCreate();
        //initialising song
        song_positon=0;
        //creating player
        mediaplayer = new MediaPlayer();
        initMusicPlayer();
        rand = new Random();

    }
    public boolean shuffle_status(){
        return shuffle;
    }

    public void setShuffle()
    {
        if(shuffle)
            shuffle=false;
        else {
            shuffle = true;
            song_positon=getrandom();
            playsong();
        }

    }


    private int getrandom()
    {
        int newsong= rand.nextInt(playlist.size());
        return newsong;
    }
    public void initMusicPlayer()
    {
        mediaplayer.setWakeMode(getApplicationContext(),PowerManager.PARTIAL_WAKE_LOCK);
        mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaplayer.setOnPreparedListener(this);
        mediaplayer.setOnErrorListener(this);
        mediaplayer.setOnCompletionListener(this);

    }
    public void setList(ArrayList <Song> song)
    {
        this.playlist= song;
    }


    public class MusicBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }
    public void playsong()
    {


        mediaplayer.reset();
        //getting song
        Song playsong = playlist.get(song_positon);
        songTitle = playsong.getTrack();
        songArtist= playsong.getArtist();
        album =playsong.getArbum_art();
        long currsong = playsong.getId();
        Uri trackuri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currsong);
        try{
            mediaplayer.setDataSource(getApplicationContext(),trackuri);
        }
        catch(Exception e )
        {
            Log.e("Music service","error setting data source",e);
        }
       mediaplayer.prepareAsync();

    }


    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void setSong(int song_index)
    {
        song_positon= song_index ;
    }

    public int getPosn(){
        return mediaplayer.getCurrentPosition();
    }

    public int getDur(){
        return mediaplayer.getDuration();
    }

    public boolean isPng(){
        return mediaplayer.isPlaying();
    }

    public void pausePlayer(){
        mediaplayer.pause();
    }

    public void seek(int posn) {
        mediaplayer.seekTo(posn);
    }

    public void go() {
        mediaplayer.start();
    }

    public void playprev()
    { if(shuffle){
          int newsong=getrandom();
        song_positon=newsong;
        playsong();
       }
        else{
        song_positon--;
        if (song_positon <= 0) {
            song_positon = playlist.size() - 1;
        }
    }
        playsong();
    }
    public void playNext(){
        if(shuffle){
            int newSong = song_positon;
            while(newSong==song_positon){
                newSong=rand.nextInt(playlist.size());
            }
            song_positon=newSong;
        }
        else{
            song_positon++;
            if(song_positon>=playlist.size()) song_positon=0;

        }
        playsong();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return musicbind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaplayer.stop();
        mediaplayer.release();
        return false;
    }

}
