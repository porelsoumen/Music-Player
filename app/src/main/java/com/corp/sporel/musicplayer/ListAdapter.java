package com.corp.sporel.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sporel on 12/19/15.
 */
public class ListAdapter extends BaseAdapter implements View.OnClickListener{

    private ArrayList<ListItem> songs;
    private Activity act;

    public ListAdapter(Activity act)
    {
        this.act = act;
    }
    public ListAdapter(Activity act, ArrayList<ListItem> songs)
    {
        this.act = act;
        this.songs = songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return songs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return songs.indexOf(songs.get(i));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder vh;
        if (view == null)
        {
            LayoutInflater inflater = (LayoutInflater)act.getSystemService(act.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.songs_list_item, viewGroup, false);

            vh = new ViewHolder();
            vh.header = (TextView)view.findViewById(R.id.firstLine);
            vh.footer = (TextView)view.findViewById(R.id.secondLine);
            vh.img = (ImageView)view.findViewById(R.id.albumart);

            view.setTag(vh);
        }
        else
        {
            vh = (ViewHolder)view.getTag();
        }

        if (songs.size() >= 0)
        {
            ListItem item = songs.get(i);
            vh.header.setText(item.getName());
            vh.footer.setText(item.getArtist() + " | " + item.getAlbum());

            view.setOnClickListener(new MyItemClickListener(i));
        }

        return view;
    }

    @Override
    public void onClick(View view) {

    }

    private class ViewHolder
    {
        public TextView header;
        public TextView footer;
        public ImageView img;
    }

    private class MyItemClickListener implements View.OnClickListener
    {
        private int mPos;

        MyItemClickListener(int position)
        {
            mPos = position;
        }
        @Override
        public void onClick(View view) {
            MainActivity activity = (MainActivity)act;
            activity.onItemClick(mPos);
        }
    }

    public void refreshSongList()
    {
        songs = new GetSongs(act).getAllSongs();
        this.notifyDataSetChanged();
    }
}
