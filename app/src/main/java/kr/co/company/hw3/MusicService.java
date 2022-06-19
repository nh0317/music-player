package kr.co.company.hw3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

public class MusicService extends Service {
    public static final String PREFIX = "kr.co.company.hw3.foregroundservice.action.";
    public static final String MAINACTIVITY_ACTION = PREFIX+"mainActivity";
    public static final String PLAY_ACTION = PREFIX + "play";
    public static final String PAUSE_ACTION = PREFIX + "pause";
    public static final String PROGRESS_ACTION = PREFIX + "progress";
    public static final String NEXTPLAY_ACTION = PREFIX + "next";
    public static final String STARTFORGROUND_ACTION = PREFIX + "startforeground";
    public static final String STOPFOREGROUND_ACTION = PREFIX + "stopforeground";
    public static final String SEEKTO_ACTION = PREFIX + "seekTo";
    public static final String CLOSE_ACTION= PREFIX + "close";


    public static final String Channel_ID = "chanel1";
    public final int NOTIFICATION_ID=1;
    String name = "Title";

    private int currentPosition;
    private List<Music> mData;
    int position;
    boolean flagPlay = false;
    Music music;

    MediaPlayer mediaPlayer;
    RemoteViews notificationLayout;
    Notification noti;
    NotificationManager manager;
    NotificationCompat.Builder builder;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(){
        super.onCreate();
        notificationLayout = new RemoteViews(getPackageName(), R.layout.noti_layout);

        mData = new MusicData().getMusics();
        mediaPlayer = SingleMediaPlayer.getMediaPlayer();
        mediaPlayer.setOnCompletionListener(mOnComplete);
        mediaPlayer.setOnSeekCompleteListener(mOnSeekComplete);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null){
            Log.d("tag","intent is null");

        }
        super.onStartCommand(intent, flags, startId);
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(PLAY_ACTION);
        playIntent.putExtra("state","play");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(PAUSE_ACTION);
        pauseIntent.putExtra("state","pause");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent mainIntent = new Intent(this, PlayMusicActivity.class);
        mainIntent.setFlags(FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.putExtra("position",position);
        mainIntent.putExtra("current_position", mediaPlayer.getCurrentPosition());
        mainIntent.putExtra("is_playing", mediaPlayer.isPlaying());
        mainIntent.setAction(MAINACTIVITY_ACTION);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(this, MusicService.class);
        closeIntent.setAction(CLOSE_ACTION);
        PendingIntent closePendingIntent = PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        switch (intent.getAction()){
            case STARTFORGROUND_ACTION:
                position = intent.getIntExtra("position",-1);
                if(position==-1){
                    return START_STICKY;
                }
                LoadMedia(position);
                updateRemoteView();

                Log.d("tag", "start foreground "+position);
                notificationLayout.setOnClickPendingIntent(R.id.albumCover,mainPendingIntent);

                if(!flagPlay)
                    notificationLayout.setOnClickPendingIntent(R.id.playOrPause,playPendingIntent);
                else
                    notificationLayout.setOnClickPendingIntent(R.id.playOrPause,pausePendingIntent);

                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.play);

                NotificationChannel channel = new NotificationChannel(Channel_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
                manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
                builder = new NotificationCompat.Builder(this, Channel_ID)
                        .setSmallIcon(R.drawable.pause)
                        .setCustomContentView(notificationLayout)
                        .setContentIntent(closePendingIntent)
                        .setOngoing(true)
                        .setAutoCancel(true);
                noti = builder.build();

                startForeground(NOTIFICATION_ID,noti);
                break;

            case PLAY_ACTION:
                Log.d("tag", position +" "+ intent.getIntExtra("position", -1));

                int currentPausePosition = intent.getIntExtra("current_position",-1);
                Log.d("tag","clicked play "+currentPausePosition+" "+mediaPlayer.getCurrentPosition());
                if(currentPausePosition!=-1 ) {
                    mediaPlayer.seekTo(currentPausePosition);
                }
                flagPlay = true;

                try {
                    mediaPlayer.start();
                } catch (Exception e) {
                    Log.d("tag", "mOnClick error:" + e.getMessage());
                }

                notificationLayout.setOnClickPendingIntent(R.id.playOrPause,pausePendingIntent);

                manager.cancel(NOTIFICATION_ID);
                notificationLayout.setImageViewResource(R.id.playOrPause,R.drawable.pause);
                builder.setSmallIcon(R.drawable.play);
                noti = builder.build();
                manager.notify(NOTIFICATION_ID,noti);
                Intent broadcastIntent = new Intent("music player");
                broadcastIntent.putExtra("state","play");
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                break;

            case PAUSE_ACTION:
                currentPausePosition = intent.getIntExtra("current_position",-1);
                Log.d("tag","clicked pause "+currentPausePosition+" "+mediaPlayer.getCurrentPosition());
                if(currentPausePosition!=-1 ) {
                    mediaPlayer.setVolume(0,0);
                    mediaPlayer.start();
                    mediaPlayer.seekTo(currentPausePosition);
                }

                notificationLayout.setImageViewResource(R.id.playOrPause,R.drawable.play);
                flagPlay=false;
                try {
                    mediaPlayer.pause();
                } catch (Exception e) {
                    Log.d("tag", "mOnClick error:" + e.getMessage());
                }
                if(currentPausePosition!=-1 || currentPausePosition!=0)
                    mediaPlayer.setVolume(1,1);
                Log.d("tag","clicked pause "+mediaPlayer.getCurrentPosition());

                notificationLayout.setOnClickPendingIntent(R.id.playOrPause,playPendingIntent);

                manager.cancel(NOTIFICATION_ID);
                notificationLayout.setImageViewResource(R.id.playOrPause,R.drawable.play);
                builder.setSmallIcon(R.drawable.pause);
                noti = builder.build();
                manager.notify(NOTIFICATION_ID,noti);


                broadcastIntent = new Intent("music player");
                broadcastIntent.putExtra("state","pause");
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                break;
            case STOPFOREGROUND_ACTION:
                Log.d("tag","stop foreground Intent");
                stopForeground(true);
                stopSelf();
                break;
            case PROGRESS_ACTION:
                if(mediaPlayer!=null && mediaPlayer.isPlaying()) {
                    broadcastIntent = new Intent("music player");
                    broadcastIntent.putExtra("state", "progress");
                    broadcastIntent.putExtra("current_position", mediaPlayer.getCurrentPosition());
                    broadcastIntent.putExtra("is_playing", mediaPlayer.isPlaying());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                }
                break;
            case SEEKTO_ACTION:
                if(mediaPlayer!=null) {
                    int progress = intent.getIntExtra("position", -1);
                    if (progress == -1) {
                        Log.d("tag", "seek to error");
                        break;
                    }
                    Log.d("tag",""+progress);
                    mediaPlayer.seekTo(progress);
                }
                break;
//            case CLOSE_ACTION:
//                manager.
//                break;
                default:break;
        }
        return START_STICKY;
    }
    IMusicService.Stub mBinder = new IMusicService.Stub() {
        @Override
        public int getCurrentPosition() throws RemoteException {
            return currentPosition;
        }
        @Override
        public int getPosition() throws RemoteException{
            return position;
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer =null;
        }
    }


    boolean LoadMedia(int idx){
        music = mData.get(idx);
        Log.d("tag", music.getTitle());
        try{
            mediaPlayer.reset();
            mediaPlayer.setDataSource(music.getPath());
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        if(!PrePare())
            return false;
        return true;
    }
    boolean PrePare(){
        try{
            mediaPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    MediaPlayer.OnCompletionListener mOnComplete = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
//            position = (position==mData.size()-1?0:position+1);
//            mediaPlayer.reset();
//            LoadMedia(position);
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    };
    MediaPlayer.OnSeekCompleteListener mOnSeekComplete = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.d("tag", "seek complete " + flagPlay);
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    };
    SeekBar.OnSeekBarChangeListener mOnSeek= new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.start();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    public void updateRemoteView(){
        Music music = mData.get(position);
        notificationLayout.setTextViewText(R.id.title,music.getTitle());
        notificationLayout.setImageViewBitmap(R.id.albumCover, music.getAlbumArt());
    }
}
