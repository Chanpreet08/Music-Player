package musicplayer.cplayer.com.cplayer;


import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {
    ArrayList<Song> songs;
    LayoutInflater songinf;

    public SongAdapter(Context c, ArrayList<Song> songs) {
        songinf = LayoutInflater.from(c);
        this.songs = songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override

        public View getView (  int position , View convertView, ViewGroup parent){
            LinearLayout layout = (LinearLayout) songinf.inflate(R.layout.song, parent, false);
            TextView song_title = (TextView) layout.findViewById(R.id.song_title);
            TextView song_artist = (TextView) layout.findViewById(R.id.song_artist);
            Song cuursong = songs.get(position);
            song_title.setText(cuursong.getTrack());
            song_artist.setText(cuursong.getArtist());
            layout.setTag(position);
            return layout;
        }

}
