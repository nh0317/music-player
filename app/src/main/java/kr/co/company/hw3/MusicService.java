package kr.co.company.hw3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static kr.co.company.hw3.Actions.*;

public class MusicService extends Service {

    public static final String Channel_ID = "chanel1";
    public final int NOTIFICATION_ID=1;
    String name = "Title";

    SingleMediaPlayer mediaPlayer;
    RemoteViews notificationLayout;
    Notification noti;
    NotificationManager manager;
    NotificationCompat.Builder builder;

    int position;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(){
        super.onCreate();
        notificationLayout = new RemoteViews(getPackageName(), R.layout.noti_layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null){
            Log.d("tag","intent is null");

        }
        super.onStartCommand(intent, flags, startId);

        PendingIntent playPendingIntent = makePendingIntent(PLAY_ACTION, "play");
        PendingIntent pausePendingIntent = makePendingIntent(PAUSE_ACTION, "pause");
        PendingIntent nextPendingIntent = makePendingIntent(NEXTPLAY_ACTION, "next");
        PendingIntent prePendingIntent = makePendingIntent(PREPLAY_ACTION, "pre");


        Intent closeIntent = new Intent(this, MusicService.class);
        closeIntent.setAction(CLOSE_ACTION.toString());
        PendingIntent closePendingIntent = PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        switch (Actions.valueOf(Objects.requireNonNull(intent).getAction())){
            case STARTFORGROUND_ACTION:
                position = intent.getIntExtra("position",-1);
                mediaPlayer = (SingleMediaPlayer) SingleMediaPlayer.getSingleMediaPlayer(position);
                if (position != mediaPlayer.getPosition())
                    mediaPlayer.loadMedia(position);
                if(position==-1){
                    return START_STICKY;
                }
                updateRemoteView();
                Intent mainIntent = new Intent(this, PlayMusicActivity.class);
                mainIntent.setFlags(FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
                mainIntent.putExtra("position", position);
                mainIntent.setAction(MAINACTIVITY_ACTION.toString());
                PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Log.d("tag", "start foreground "+position);

                notificationLayout.setOnClickPendingIntent(R.id.albumCover,mainPendingIntent);
                notificationLayout.setOnClickPendingIntent(R.id.next,nextPendingIntent);
                notificationLayout.setOnClickPendingIntent(R.id.previous,prePendingIntent);

                if(!mediaPlayer.isPlaying())
                    notificationLayout.setOnClickPendingIntent(R.id.playOrPause,playPendingIntent);
                else
                    notificationLayout.setOnClickPendingIntent(R.id.playOrPause,pausePendingIntent);

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

            case MAINACTIVITY_ACTION:
                Log.i("Main", mediaPlayer.getCurrentPosition() + "");
                break;
            case PLAY_ACTION:
                mediaPlayer.start();
                resendNotification(pausePendingIntent, R.drawable.pause,R.drawable.play);
                sendMusicBraodcast("play");
                break;

            case PAUSE_ACTION:
                mediaPlayer.pause();
                notificationLayout.setImageViewResource(R.id.playOrPause,R.drawable.play);
                resendNotification(playPendingIntent, R.drawable.play,R.drawable.pause);
                sendMusicBraodcast("pause");

                break;
//            case STOPFOREGROUND_ACTION:
//                Log.d("tag","stop foreground Intent");
////                mediaPlayer.release();
//                stopForeground(true);
//                stopSelf();
//                break;
            case CLOSE_ACTION:
                Log.d("tag","stop foreground Intent");
                mediaPlayer.release();
                mediaPlayer=null;
                sendMusicBraodcast("close");
                stopForeground(true);
                stopSelf();
                break;
            case NEXTPLAY_ACTION:
                mediaPlayer.next();
                Log.i("Next",mediaPlayer.getMusic().getTitle());
                updateRemoteView();
                resendNotification(playPendingIntent, R.drawable.play,R.drawable.pause);
                sendMusicBraodcast("next");
                break;
            case PREPLAY_ACTION:
                mediaPlayer.pre();
                updateRemoteView();
                resendNotification(playPendingIntent, R.drawable.play,R.drawable.pause);
                sendMusicBraodcast("pre");
            default:break;
        }
        return START_STICKY;
    }

    private void sendMusicBraodcast(String state) {
        Intent broadcastIntent = new Intent("music player");
        broadcastIntent.putExtra("state",state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void resendNotification(PendingIntent playPendingIntent, int imageId, int iconId) {
        notificationLayout.setOnClickPendingIntent(R.id.playOrPause,playPendingIntent);
        manager.cancel(NOTIFICATION_ID);
        notificationLayout.setImageViewResource(R.id.playOrPause,imageId);
        builder.setSmallIcon(iconId);
        noti = builder.build();
        manager.notify(NOTIFICATION_ID,noti);
    }

    private PendingIntent makePendingIntent(Actions playAction, String play) {
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(playAction.toString());
        playIntent.putExtra("state", play);
        return PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    IMusicService.Stub mBinder = new IMusicService.Stub() {
        @Override
        public int getPosition(){
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

    public void updateRemoteView(){
        Log.i("update view", mediaPlayer.getMusic().getTitle());
        notificationLayout.setTextViewText(R.id.title,mediaPlayer.getMusic().getTitle());
        notificationLayout.setImageViewBitmap(R.id.albumCover, mediaPlayer.getMusic().getAlbumArt());
    }
}
