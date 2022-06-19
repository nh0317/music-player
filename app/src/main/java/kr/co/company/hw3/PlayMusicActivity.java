package kr.co.company.hw3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import static kr.co.company.hw3.Actions.*;

public class PlayMusicActivity extends AppCompatActivity {
    SingleMediaPlayer mediaPlayer = null;
    SeekBar mProgress;
    TextView titleView;
    TextView duration;
    ImageView albumImg;
    ImageView playOrPause;
    Handler mProgressHandler;
    boolean mIsBound = false;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        Intent intent = this.getIntent();

        int position = intent.getIntExtra("position",-1);
        Log.i("OnCreate", position + "");
        if(position != -1) {
            mediaPlayer = (SingleMediaPlayer) SingleMediaPlayer.getSingleMediaPlayer(position);
            if (position != mediaPlayer.getPosition())
                mediaPlayer.loadMedia(position);
        }
        if(mBinder!=null){
            try {
                position=mediaPlayer.getPosition();
                Log.d("tag2", position + "");
            }catch (Exception e){e.printStackTrace();}
        }
        Log.d("tag", position + "");

        titleView = (TextView) findViewById(R.id.title);
        albumImg = findViewById(R.id.albumCover);
        duration = (TextView) findViewById(R.id.progressText);
        playOrPause = (ImageView) findViewById(R.id.playOrPause);

        mProgress = (SeekBar) findViewById(R.id.progressBar);
        mProgress.setOnSeekBarChangeListener(mOnSeek);
        mProgressHandler=new Handler(Looper.getMainLooper());
        mProgressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress();
                mProgressHandler.postDelayed(this, 800);
            }
        }, 800);

        loadMedia(position);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("music player"));

        doBindService();
        if (mIsBound)
            sendNotification(this, STARTFORGROUND_ACTION.toString(), position);
    }

    private IMusicService mBinder = null;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = IMusicService.Stub.asInterface(service);
            Log.d("tag", "connect service");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("tag", "disconnect service");
        }
    };
    @Override
    public void onResume(){
        super.onResume();
        initMediaPlayerPosition();
    }

    public void initMediaPlayerPosition(){
        if(mediaPlayer.getCurrentPosition()>0){
            mProgress.setProgress(mediaPlayer.getCurrentPosition());
            setDuration(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition());
            if(!mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                sendNotification(this, PAUSE_ACTION.toString(), mediaPlayer.getPosition());
            }
            else {
                mediaPlayer.start();
                sendNotification(this, PLAY_ACTION.toString(),mediaPlayer.getPosition());
            }
        }
    }
    public void onClickStart(View v){
        if (!mediaPlayer.isPlaying()) {
            playOrPause.setImageResource(R.drawable.pause);
            mediaPlayer.start();
            Log.d("onclick start",mediaPlayer.getPosition()+"");
            sendNotification(this,PLAY_ACTION.toString(),mediaPlayer.getPosition());
        }
        else{
            playOrPause.setImageResource(R.drawable.play);
            mediaPlayer.pause();
            Log.d("onclick pause",mediaPlayer.getPosition()+"");
            sendNotification(this,PAUSE_ACTION.toString(),mediaPlayer.getPosition());
        }
    }
    public void onClickNect(View v){
        sendNotification(this, NEXTPLAY_ACTION.toString(), mediaPlayer.getPosition());
    }
    public void onClickPre(View v){
        sendNotification(this, PREPLAY_ACTION.toString(),mediaPlayer.getPosition());
    }

    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer =null;
        }
        mProgressHandler.removeMessages(0);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        if(mIsBound){
            unbindService(mConnection);
        }
    }

    public void doBindService() {
        mIsBound = bindService(new Intent(this, MusicService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    public void sendNotification(AppCompatActivity app,String action,int position) {
        Intent intent = new Intent(app, MusicService.class);
        intent.setAction(action);
        intent.putExtra("position", position);
        startService(intent);
    }

    SeekBar.OnSeekBarChangeListener mOnSeek= new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                setDuration(mediaPlayer.getDuration(), progress);
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
    public String convertDuration(long duration){
        long seconds = duration/1000;
        long minutes = seconds/60;
        seconds = seconds % 60;
        if (seconds<10)
            return minutes + ":0" + seconds;
        return minutes +":"+seconds;
    }
    public void setDuration (long d, long c){
        runOnUiThread(() -> duration.setText(String.format("%s/%s", convertDuration(c), convertDuration(d))));
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    void loadMedia(int idx){
        MusicData musicData = new MusicData();
        Music music = musicData.getMusics().get(idx);
        titleView.setText(music.getTitle());
        albumImg.setImageBitmap(music.getAlbumArt());
        mProgress.setMax(music.getDuration());
        setDuration(music.getDuration(),0);
    }
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("tag",intent.getStringExtra("state")+" "+ intent.getAction());
            String action = intent.getStringExtra("state");
            switch (action){
                case "pause" :
                    playOrPause.setImageResource(R.drawable.play);
                    break;
                case "play" :
                    playOrPause.setImageResource(R.drawable.pause);
                    break;
                case "close" :
                    finish();
                case "next" :
                case "pre":
                    playOrPause.setImageResource(R.drawable.play);
                    albumImg.setImageBitmap(mediaPlayer.getMusic().getAlbumArt());
                    titleView.setText(mediaPlayer.getMusic().getTitle());
                    mProgress.setProgress(mediaPlayer.getCurrentPosition());
                    mProgress.setMax(mediaPlayer.getDuration());
                    setDuration(mediaPlayer.getDuration(),0);
                    break;
            }
        }
    };
    private void updateProgress() {
        try{
            if(mediaPlayer.isPlaying()){
//                Log.i("currentPosition", "current : "+mediaPlayer.getCurrentPosition());
                mProgress.setProgress(mediaPlayer.getCurrentPosition());
                if(mediaPlayer.getCurrentPosition()==0 || mediaPlayer.getCurrentPosition()<800){
                    runOnUiThread(() -> {
                        ImageView albumImage =findViewById(R.id.albumCover);
                        albumImage.setImageBitmap(mediaPlayer.getMusic().getAlbumArt());
                    });
                }
                runOnUiThread(() -> duration.setText(String.format("%s/%s", convertDuration(mediaPlayer.getCurrentPosition()), convertDuration(mediaPlayer.getDuration()))));
            }

        }catch (Exception e){e.printStackTrace();}
    }
}