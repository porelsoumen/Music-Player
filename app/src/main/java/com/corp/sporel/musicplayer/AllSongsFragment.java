package com.corp.sporel.musicplayer;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by sporel on 12/12/15.
 */
public class AllSongsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.all_songs_fragment, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.list);

        com.corp.sporel.musicplayer.ListAdapter adapter = new com.corp.sporel.musicplayer.ListAdapter(getActivity(), new GetSongs(getActivity().getBaseContext()).getAllSongs());
        listView.setAdapter(adapter);

        return rootView;
    }
}