package musicplayer.cplayer.com.cplayer;
import musicplayer.cplayer.com.cplayer.MusicService.MusicBinder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.View;
import android.widget.ListView;
import android.net.Uri;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.widget.MediaController.MediaPlayerControl;


public class MainActivity extends Activity implements MediaPlayerControl {

    ArrayList<Song> songlist;
    ListView song_view;
    private MusicService musicservice;
    private Intent intent;
    private Boolean musicbound = false;
    private MusicController music_controller;
    private boolean paused = false;
    private boolean playback_paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        song_view = (ListView) findViewById(R.id.song_list);
        songlist = new ArrayList<Song>();
        getsong();
        Collections.sort(songlist, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return (a.getTrack().compareTo(b.getTrack()));
            }
        });
        SongAdapter adpter = new SongAdapter(this, songlist);
        song_view.setAdapter(adpter);
        setController();
    }
    // Method for Retrieving Songs
    public void getsong()
    {
        ContentResolver cr = getContentResolver();
        Uri musicuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = cr.query(musicuri,null,null,null,null);

        //Iterating
        if(cursor!=null && cursor.moveToFirst()) {
            //getcolumn
            int title_column = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int id_column = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artist_column = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int album_art = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            //adding songs to list

            do {
                Long thisid = cursor.getLong(id_column);
                String this_title = cursor.getString(title_column);
                String this_artist = cursor.getString(artist_column);
                String this_album= cursor.getString(album_art);
                Bitmap albumart = BitmapFactory.decodeFile(this_album);
                songlist.add(new Song(this_artist, thisid, this_title,albumart));
            }
            while (cursor.moveToNext());
        }
    }

    private ServiceConnection music_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //getservice
            musicservice = binder.getService();
            musicservice.setList(songlist);
            musicbound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicbound = false;
        }
    };

    public void setController() {
        music_controller = new MusicController(this);
        music_controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            public void onClick(View v) {
                playPrev();
            }
        });

        music_controller.setMediaPlayer(this);
        music_controller.setAnchorView(findViewById(R.id.song_list));
        music_controller.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        music_controller.hide();
    }

    private void playNext() {
        musicservice.playNext();
        if (playback_paused) {
            setController();
            playback_paused = false;
        }
        music_controller.show(0);
    }

    private void playPrev() {
        musicservice.playprev();
        if (playback_paused) {
            setController();
            playback_paused = false;
        }
        music_controller.show(0);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(intent==null)
        {
            intent = new Intent(this,MusicService.class);
            bindService(intent,music_connection,Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(intent);
        musicservice= null;
        super.onDestroy();
    }


    public void songPicked(View view){

        if(musicservice.shuffle_status())
        {
            musicservice.setShuffle();
            musicservice.setSong(Integer.parseInt(view.getTag().toString()));
            musicservice.playsong();
            setController();
            music_controller.show(0);

        }
        else
        {
            musicservice.setSong(Integer.parseInt(view.getTag().toString()));
            musicservice.playsong();
            setController();
            music_controller.show(0);
        }
        if(playback_paused){
            setController();
            playback_paused=false;
        }
        music_controller.show(0);
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public void start() {
      musicservice.go();
    }

    @Override
    public void pause() {
        playback_paused=true;
       musicservice.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicservice!=null && musicservice.isPng()&& musicbound)
        return musicservice.getDur();
        else
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicservice!=null )
            return musicservice.getPosn();
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {
     musicservice.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicservice!=null)
        return musicservice.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
      switch(item.getItemId())
      {
          case R.id.action_shuffle:
              musicservice.setShuffle();
              setController();
              music_controller.show(0);
              break;
          case R.id.action_end:
              stopService(intent);
              musicservice=null;
              System.exit(0);
              break;
      }

        return super.onOptionsItemSelected(item);
    }
}
