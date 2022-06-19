package kr.co.company.hw3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PlayMusicActivity extends AppCompatActivity {
//    public static String MainActivity_ACTION = "kr.co.company.hw3.foreground.action.main";
    public static final String PREFIX = "kr.co.company.hw3.foregroundservice.action.";
    public static final String MIAN_ACTION = PREFIX+"main";
    public static final String PLAY_ACTION = PREFIX + "play";
    public static final String PAUSE_ACTION = PREFIX + "pause";
    public static final String NEXTPLAY_ACTION = PREFIX + "next";
    public static final String STARTFORGROUND_ACTION = PREFIX + "startforeground";
    public static final String STOPFOREGROUND_ACTION = PREFIX + "stopforeground";
    public static final String SEEKTO_ACTION = PREFIX + "seekTo";

    List<Music> mData = null;
//    MediaPlayer mediaPlayer = null;
    SeekBar mProgress;
    TextView titleView;
    TextView duration;
    ImageView albumImg;
    ImageView playOrPause;
    ProgressHandler mProgressHandler;
    boolean mIsBound = false;
    boolean flagPlay = false;
    int mIdx;
    int totalDuration;

    int NOTIFICATION_ID=1;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        Intent intent = this.getIntent();

        int position = intent.getIntExtra("position",-1);
        if(mBinder!=null){
            try {
                position = mBinder.getPosition();
                Log.d("tag2", position + "");
            }catch (Exception e){e.printStackTrace();}
        }
        Log.d("tag", position + "");
        mIdx = position;

        titleView = (TextView) findViewById(R.id.title);
        albumImg = findViewById(R.id.albumCover);
        duration = (TextView) findViewById(R.id.progressText);
        playOrPause = (ImageView) findViewById(R.id.playOrPause);
//        mediaPlayer= SingleMediaPlayer.getMediaPlayer();
//        mediaPlayer.setOnCompletionListener(mOnComplete);
//        mediaPlayer.setOnSeekCompleteListener(mOnSeekComplete);

        mProgress = (SeekBar) findViewById(R.id.progressBar);
        mProgress.setOnSeekBarChangeListener(mOnSeek);
        mProgressHandler = new ProgressHandler(this,position);
        mProgressHandler.sendEmptyMessageDelayed(0,800);

//        if(!LoadMedia(position)){
//            finish();
//        }
//        else {
        LoadMedia(position);
            LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("music player"));

            doBindService();
            if (mIsBound)
                sendNotification(this, STARTFORGROUND_ACTION, position);
//        }
    }
    private IMusicService mBinder = null;
    private ServiceConnection mConnection = new ServiceConnection() {
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
        Intent intent = getIntent();
        int currentPosition = intent.getIntExtra("current_position",-1);
        Log.d("tag", "onResume currentPosition "+ currentPosition);

        if(currentPosition!=-1){
            flagPlay = intent.getBooleanExtra("is_playing",false);
            Log.d("tag", "onResume "+ currentPosition+" "+flagPlay);
            mProgress.setProgress(currentPosition);
            setDuration(totalDuration, currentPosition);
            int position = intent.getIntExtra("position",-1);
            if(position==-1){
                Log.d("tag", "onResume error playing music");
                return;
            }
            Log.d("tag", "onResume paused music");
            if(!flagPlay) {
                sendNotification(this, PAUSE_ACTION, position,currentPosition);
            }
            else {
                sendNotification(this, PLAY_ACTION,position,currentPosition);
            }
        }
    }
    public void onClickStart(View v){
        if (!flagPlay) {
            flagPlay=true;
            playOrPause.setImageResource(R.drawable.pause);

            Log.d("onclick",mIdx+"");
            sendNotification(this,PLAY_ACTION,mIdx);
        }
        else{
            playOrPause.setImageResource(R.drawable.play);
            flagPlay=false;
            sendNotification(this,PAUSE_ACTION,mIdx);
        }
    }

    public void onDestroy() {
        super.onDestroy();
//        if (mediaPlayer!=null){
//            mediaPlayer.release();
//            mediaPlayer =null;
//        }
        mProgressHandler.removeMessages(0);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        if(mIsBound){
            unbindService(mConnection);
        }
    }

    public void doBindService() {
        mIsBound =bindService(new Intent(this, MusicService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    public void sendNotification(AppCompatActivity app,String action,int position) {
        Intent intent = new Intent(app, MusicService.class);
        intent.setAction(action);
        intent.putExtra("position", position);
        startService(intent);
    }
    public void sendNotification(AppCompatActivity app,String action,int position,int currentPositon) {
        Intent intent = new Intent(app, MusicService.class);
        intent.setAction(action);
        intent.putExtra("position", position);
        intent.putExtra("current_position", currentPositon);
        startService(intent);
    }
    boolean LoadMedia(int idx){
        MusicData musicData = new MusicData();
        mData = musicData.getMusics();
        Music music = mData.get(idx);
//        try{
//            mediaPlayer.setDataSource(music.getPath());
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//        if(!PrePare())
//            return false;
        titleView.setText(music.getTitle());
        albumImg.setImageBitmap(music.getAlbumArt());
        totalDuration = music.getDuration();
        mProgress.setMax(totalDuration);
        setDuration(music.getDuration(),0);
        return true;
    }
//    boolean PrePare(){
//        try{
//            mediaPlayer.prepare();
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//    MediaPlayer.OnCompletionListener mOnComplete = new MediaPlayer.OnCompletionListener() {
//        @Override
//        public void onCompletion(MediaPlayer mp) {
//            mIdx = (mIdx==mData.size()-1?0:mIdx+1);
//            mediaPlayer.reset();
//            LoadMedia(mIdx);
//            mediaPlayer.start();
//        }
//    };

    SeekBar.OnSeekBarChangeListener mOnSeek= new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                setDuration(totalDuration, progress);
                sendNotification(PlayMusicActivity.this,SEEKTO_ACTION,progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            flagPlay = mediaPlayer.isPlaying();
//            if(flagPlay)
//                mediaPlayer.pause();
//                sendNotification(this,PAUSE_ACTION,mIdx,currentPosition);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                duration.setText(convertDuration(c)+"/"+convertDuration(d));
            }
        });
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
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
                case "progress":
                    int currentPosition = intent.getIntExtra("current_position",-1);
                    if(currentPosition==-1){
                        Log.d("tag", "broadcast receiver에서 오류 발생");
                        return;
                    }
                    Log.d("tag", convertDuration(currentPosition)+" "+flagPlay);

                    flagPlay = intent.getBooleanExtra("is_playing",false);
                    if(flagPlay){
                        mProgress.setProgress(currentPosition);
                        setDuration(totalDuration, currentPosition);
                    }
                    break;
            }
        }
    };
}

class ProgressHandler extends Handler{
    public static final String PREFIX = "kr.co.company.hw3.foregroundservice.action.";
    public static final String PROGRESS_ACTION = PREFIX + "progress";

    AppCompatActivity app;
    int position;

    public ProgressHandler(AppCompatActivity app, int position){
        this.app = app;
        this.position =position;
    }

    @Override
    public void handleMessage(@NonNull Message msg){
        super.handleMessage(msg);
        try{
            sendNotification(app, PROGRESS_ACTION, position);
        }catch (Exception e){e.printStackTrace();}
        this.sendEmptyMessageDelayed(0,200);
    }

    public void sendNotification(AppCompatActivity app,String action,int position) {
        Intent intent = new Intent(app, MusicService.class);
        intent.setAction(action);
        intent.putExtra("position", position);
        app.startService(intent);
    }
}