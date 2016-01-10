package com.corp.sporel.musicplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.logging.Handler;

/**
 * Created by sporel on 12/12/15.
 */
public class PlayerActivity extends AppCompatActivity {

    private TextView songName;
    private SeekBar slider;
    private ImageView playBtn;
    private ImageView pauseBtn;
    private ImageView rewindBtn;
    private ImageView forwardBtn;
    private  Long sid;
    private  int duration;
    private PlayService mService;
    private boolean mBound = false;
    private int mInterval = 1000;
    private android.os.Handler mHandler;
    private TextView elapsed;
    private TextView total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        songName = (TextView) findViewById(R.id.name);
        playBtn = (ImageView) findViewById(R.id.play);
        pauseBtn = (ImageView) findViewById(R.id.pause);
        elapsed = (TextView)findViewById(R.id.elapsed);
        total = (TextView)findViewById(R.id.total);
        slider = (SeekBar)findViewById(R.id.slider);
        forwardBtn = (ImageView)findViewById(R.id.forward);
        rewindBtn = (ImageView)findViewById(R.id.rewind);

        final int pos = getIntent().getIntExtra("pos",-1);
        sid = getIntent().getLongExtra("id",0L);
        duration = getIntent().getIntExtra("duration", 0);
        String name = getIntent().getStringExtra("name");
        int state = getIntent().getIntExtra("state", -1);

        songName.setText(name);
        songName.setSelected(true);

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

        //Intent intent = new Intent(this, PlayService.class);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        final Intent objIntent = new Intent(getApplicationContext(), PlayService.class);
        objIntent.putExtra("id", sid);
        objIntent.putExtra("duration", duration);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objIntent.setAction(MainActivity.ACTION_PLAY);
                startService(objIntent);
                playBtn.setVisibility(View.INVISIBLE);
                pauseBtn.setVisibility(View.VISIBLE);
                setResult(RESULT_OK, new Intent().putExtra("state", 0));
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objIntent.setAction(MainActivity.ACTION_PAUSE);

                startService(objIntent);
                pauseBtn.setVisibility(View.INVISIBLE);
                playBtn.setVisibility(View.VISIBLE);
                setResult(RESULT_OK, new Intent().putExtra("state", 1));
            }
        });

        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objIntent.putExtra("pos", pos);
                objIntent.setAction(MainActivity.ACTION_NEXT);

                startService(objIntent);
                //playBtn.setVisibility(View.INVISIBLE);
                //pauseBtn.setVisibility(View.VISIBLE);
                setResult(RESULT_OK, new Intent().putExtra("state", 1));
            }
        });

        rewindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objIntent.putExtra("pos", pos);
                objIntent.setAction(MainActivity.ACTION_PREV);

                startService(objIntent);
                //playBtn.setVisibility(View.INVISIBLE);
                //pauseBtn.setVisibility(View.VISIBLE);
                setResult(RESULT_OK, new Intent().putExtra("state", 1));
            }
        });

        mHandler = new android.os.Handler();
        setSeekbarText();

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b)
                {
                    //mHandler.removeCallbacks(mStatusUpdater);
                    mService.getPlayer().seekTo(progressToTimer(i, duration));
                    //slider.setProgress(i);
                    //mHandler.postDelayed(mStatusUpdater, mInterval);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mStatusUpdater);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //mHandler.removeCallbacks(mStatusUpdater);
            }
        });

        mHandler.postDelayed(mStatusUpdater, mInterval);
        setResult(RESULT_OK, new Intent().putExtra("state",pauseBtn.getVisibility()));
    }

    private Runnable mStatusUpdater = new Runnable() {

        @Override
        public void run() {
            int currentPos = mService.getCurrentPosition();
            elapsed.setText(millisToTime(currentPos));
            //total.setText(millisToTime(duration));

            slider.setProgress(getProgressPercent(currentPos, duration));
            mHandler.postDelayed(mStatusUpdater, mInterval);
        }
    };

    private void setSeekbarText()
    {
        int secs = duration/1000;
        int mins = secs / 60;
        int seconds = secs % 60;

        total.setText(mins + ":" + (seconds < 10 ? "0" + seconds : seconds));

        //elapsed.setText("0:00");
        //slider.setProgress(0);
        slider.setMax(duration);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_in, R.anim.back_out);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this, PlayService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mHandler.removeCallbacks(mStatusUpdater);
        if (mBound)
        {
            unbindService(mConnection);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayService.LocalBinder binder = (PlayService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected  void onPause()
    {
        super.onPause();
        mHandler.removeCallbacks(mStatusUpdater);
    }

    private int progressToTimer(int progress, int duration)
    {
        int currentPos = 0;
        //duration = duration / 1000;
        currentPos = (int)((double)progress / 100) * duration;

        return currentPos;
    }

    private String millisToTime(int msecs)
    {
        StringBuilder finalTime = new StringBuilder();
        int hours = ( msecs / (1000*60*60));
        int mins = (msecs % (1000*60*60)) / (1000*60);
        int secs = ((msecs % (1000*60*60)) % (1000*60) / 1000);

        if (hours > 0)
            finalTime.append(hours + ":");

        finalTime.append(mins + ":");
        finalTime.append(secs < 10 ? "0"+secs : secs);

        return finalTime.toString();
    }

    private  int getProgressPercent(int progress, int duration)
    {
        Double percent = (double)0;

        progress = progress / 1000;
        duration = duration / 1000;

        percent = ((double)progress / duration) * 100;

        return percent.intValue();
    }
}
