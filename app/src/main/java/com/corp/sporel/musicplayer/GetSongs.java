package com.corp.sporel.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by sporel on 12/20/15.
 */
public class GetSongs {
    private Context context;
    ArrayList<ListItem> songs;

    public GetSongs(Context context)
    {
        this.context = context;
    }

    public ArrayList<ListItem> getAllSongs()
    {
        Cursor mCursor = null;
        songs = new ArrayList<>();
        ListItem item;
        try
        {
            mCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, "_id");

            System.out.println("cursor count is : " + mCursor.getCount());

            while (mCursor.moveToNext())
            {
                item = new ListItem();
                String album = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String name = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                Long data = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String artist = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                int duration = mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                item.setName(name);
                item.setArtist(artist);
                item.setAlbum(album);
                item.setId(data);
                item.setDuration(duration);

                songs.add(item);

                System.out.println("SOUMEN " + data + " ," +  album + " , " + name + " , " + artist + "," + duration);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (mCursor != null)
            {
                mCursor.close();
                mCursor = null;
            }
        }

        return songs;
    }


}
