package kr.co.company.hw3;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class SingleMediaPlayer extends MediaPlayer {
    private final List<Music> musics;
    private Music music = null;
    private int position = 0;

    private SingleMediaPlayer(int position){
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mediaPlayer.setOnCompletionListener(mOnComplete);
        mediaPlayer.setOnSeekCompleteListener(mOnSeekComplete);
        MusicData musicData = new MusicData();
        musics = musicData.getMusics();
        loadMedia(position);
    }
    private static volatile SingleMediaPlayer singleMediaPlayer=null;
    private MediaPlayer mediaPlayer;

    public static synchronized MediaPlayer getSingleMediaPlayer(int position){
        if (singleMediaPlayer == null){
            synchronized (SingleMediaPlayer.class){
                Log.i("SingleMediPlayer", position + "");
                if (singleMediaPlayer == null){
                    singleMediaPlayer = new SingleMediaPlayer(position);
                }
                else{
                    singleMediaPlayer.loadMedia(position);
                }
            }
        }
        return singleMediaPlayer;
    }
     MediaPlayer.OnPreparedListener mOnPreparedListener = mp -> {
     };
    MediaPlayer.OnCompletionListener mOnComplete = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            position = (position==musics.size()-1?0:position+1);
            mediaPlayer.reset();
            loadMedia(position);
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    };

     MediaPlayer.OnSeekCompleteListener mOnSeekComplete = mp -> {
     };
    public void loadMedia(int position){
        position = position % musics.size();
        if(position <0)
            position=musics.size()-1;
        this.music = musics.get(position);
        Log.d("loadMedia", music.getTitle()+" "+music.getPath());
        try{
            mediaPlayer.reset();
            mediaPlayer.setDataSource(music.getPath());
            this.position=position;
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void pause(){
        try {
            if (mediaPlayer.isPlaying())
                this.mediaPlayer.pause();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("tag", "mOnClick error:" + e.getMessage());
        }
    }
    @Override
    public void start(){
        try {
            if (!mediaPlayer.isPlaying()) {
                this.mediaPlayer.start();
            }
            Log.i("MediaPlayer", "playing "+mediaPlayer.isPlaying());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("tag", "mOnClick error:" + e.getMessage());
        }
    }
    @Override
    public boolean isPlaying(){
        if (this.mediaPlayer != null)
            return this.mediaPlayer.isPlaying();
        else return false;
    }

    @Override
    public int getCurrentPosition() {
        if (this.mediaPlayer!=null)
            return this.mediaPlayer.getCurrentPosition();
        else return 0;
    }

    @Override
    public int getDuration() {
        if (this.mediaPlayer!=null)
            return this.mediaPlayer.getDuration();
        else return 0;
    }

    @Override
    public void seekTo(int position){
        if(this.mediaPlayer!=null)
            this.mediaPlayer.seekTo(position);
    }
    @Override
    public void release(){
        if(this.mediaPlayer!=null){
            this.mediaPlayer.release();
            mediaPlayer=null;
            singleMediaPlayer=null;
        }
    }

    public Music getMusic() {
        return music;
    }

    public int getPosition() {
        return position;
    }
    public void next(){
        loadMedia(++position);
    }
    public void pre(){
        loadMedia(--position);
    }
}
