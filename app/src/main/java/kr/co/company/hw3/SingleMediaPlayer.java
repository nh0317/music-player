package kr.co.company.hw3;

import android.media.MediaPlayer;

public class SingleMediaPlayer {
    private SingleMediaPlayer(){}
    private static MediaPlayer singleMediaPlayer;
    public static synchronized MediaPlayer getMediaPlayer(){
        if (singleMediaPlayer == null)
            return new MediaPlayer();
        else return singleMediaPlayer;
    }
}
