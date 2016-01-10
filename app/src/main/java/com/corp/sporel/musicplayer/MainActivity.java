package com.corp.sporel.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_PLAY = "com.sporel.action.play";
    public static final String ACTION_PAUSE = "com.sporel.action.pause";
    public static final String ACTION_NEXT = "com.sporel.action.next";
    public static final String ACTION_PREV = "com.sporel.action.prev";

    ImageView pauseBtn;
    ImageView playBtn;
    int songId = -1;
    RelativeLayout miniPlayer;
    ArrayList<ListItem> songs;
    SharedPreferences prefs;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout;


        final ListItem[] song = new ListItem[1];

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        this.setupViewPager(viewPager);

        final TextView songName = (TextView)findViewById(R.id.currentsong);
        final TextView second = (TextView)findViewById(R.id.currentdesc);
        songs = new GetSongs(this).getAllSongs();
        pauseBtn = (ImageView)findViewById(R.id.icon_pause);
        playBtn = (ImageView)findViewById(R.id.icon_play);
        miniPlayer = (RelativeLayout)findViewById(R.id.bottombar);

        final Intent objIntent = new Intent(getApplicationContext(), PlayService.class);

        // on click listener for play and pause buttons
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseBtn.setVisibility(View.INVISIBLE);
                playBtn.setVisibility(View.VISIBLE);
                objIntent.setAction(MainActivity.ACTION_PAUSE);
                startService(objIntent);
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playBtn.setVisibility(View.INVISIBLE);
                pauseBtn.setVisibility(View.VISIBLE);
                objIntent.putExtra("id", songs.get(songId).getId());
                objIntent.putExtra("duration", songs.get(songId).getDuration());
                objIntent.setAction(MainActivity.ACTION_PLAY);
                startService(objIntent);
            }
        });


        miniPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = -1;
                if (songName.getText().equals("Touch to Shuffle"))
                {
                    Random r = new Random();
                    id = r.nextInt(songs.size() - 1);
                    //i.putExtra("name", songs.get(id).getName());
                    //i.putExtra("duration", songs.get(id).getDuration());
                    songName.setText(songs.get(id).getName());
                    second.setText(songs.get(id).getAlbum() + " | " + songs.get(id).getArtist());
                    playBtn.setVisibility(View.INVISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE);
                    songId = id;

                    objIntent.putExtra("id", songs.get(songId).getId());
                    objIntent.putExtra("duration", songs.get(songId).getDuration());
                    objIntent.setAction(MainActivity.ACTION_PLAY);
                    startService(objIntent);
                }
                else
                {
                    Intent i = new Intent(getApplicationContext(), PlayerActivity.class);
                    if (songId == id)
                    {
                        i.putExtra("pos",id);
                        i.putExtra("id", songs.get(id).getId());
                        i.putExtra("duration", songs.get(id).getDuration());
                        i.putExtra("name", songs.get(id).getName());
                        i.putExtra("state",pauseBtn.getVisibility());
                    }
                    else
                    {
                        i.putExtra("pos", songId);
                        i.putExtra("id", songs.get(songId).getId());
                        i.putExtra("duration", songs.get(songId).getDuration());
                        i.putExtra("name", songs.get(songId).getName());
                        i.putExtra("state",pauseBtn.getVisibility());
                    }
                    startActivityForResult(i, 99);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                }

            }
        });


        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setStatusBarColor();

        prefs = getSharedPreferences("prefs.xml",Context.MODE_PRIVATE);

        if (prefs.contains("name"))
        {
            pauseBtn.setVisibility(prefs.getBoolean("isplaying", false) ? View.VISIBLE : View.INVISIBLE);
            playBtn.setVisibility(prefs.getBoolean("isplaying", false) ? View.INVISIBLE : View.VISIBLE);
        }
    }

    private void setStatusBarColor()
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window w = this.getWindow();
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            w.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new AllSongsFragment(),"All Songs");
        adapter.addFragment(new PlayListFragment(),"Playlists");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager)
        {
            super(manager);
        }

        /**
         * Return the Fragment associated with a specified position.
         *
         * @param position
         */
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        /**
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title)
        {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

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
        switch(id)
        {
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(int position)
    {
        ListItem song = new GetSongs(this).getAllSongs().get(position);
        songId = position;
        Toast.makeText(this, song.getName() + " clicked", Toast.LENGTH_SHORT).show();

        TextView first = (TextView) findViewById(R.id.currentsong);
        TextView second = (TextView) findViewById(R.id.currentdesc);

        first.setText(song.getName());
        second.setText(song.getAlbum() + " | " + song.getArtist());

        playBtn.setVisibility(View.INVISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);

        Intent objIntent = new Intent(getApplicationContext(), PlayService.class);
        objIntent.putExtra("id", songs.get(position).getId());
        objIntent.putExtra("duration", songs.get(position).getDuration());
        objIntent.setAction(MainActivity.ACTION_PLAY);
        startService(objIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (99) : {
                int state = data.getIntExtra("state", -1);

                if (state == View.VISIBLE)
                {
                    playBtn.setVisibility(View.INVISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE);
                }
                else
                {

                    pauseBtn.setVisibility(View.INVISIBLE);
                    playBtn.setVisibility(View.VISIBLE);
                }

                break;
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (songId != -1)
        {
            SharedPreferences.Editor editor = getSharedPreferences("prefs.xml", MODE_PRIVATE).edit();
            editor.putString("name", songs.get(songId).getName());
            editor.putInt("index", songId);
            editor.putLong("duration", songs.get(songId).getDuration());
            editor.putString("album", songs.get(songId).getAlbum());
            editor.putString("artist", songs.get(songId).getArtist());
            editor.putLong("id", songs.get(songId).getId());
            editor.putBoolean("isplaying", pauseBtn.getVisibility() == View.VISIBLE);
            editor.apply();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        prefs = getSharedPreferences("prefs.xml",Context.MODE_PRIVATE);
        TextView songName = (TextView)findViewById(R.id.currentsong);
        TextView second = (TextView)findViewById(R.id.currentdesc);
        if (prefs.contains("name"))
        {
            songName.setText(prefs.getString("name", "name"));
            second.setText(prefs.getString("album", "album") + " | " + prefs.getString("artist", "artist"));
            songId = prefs.getInt("index", -1);
            Long id = prefs.getLong("id",-1l);
        }

        //refresh song list
        ListAdapter adapter = new ListAdapter(this);
        adapter.refreshSongList();
    }
}
