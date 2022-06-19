package kr.co.company.hw3;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicData {
    private static List<Music> musics = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public MusicData(AppCompatActivity app) {
//        Log.i("Music 리스트 생성");
        if(musics!=null || musics.size()==0){
            String[] projection = new String[]{
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DISPLAY_NAME
            };
            String selection = null;
//                MediaStore.Audio.Media.DURATION + " >= ?";
            String[] selectionArgs = null;
//                new String[] {String.valueOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))};
            String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
            try (
                    Cursor cursor = app.getApplicationContext().getContentResolver().query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            sortOrder
                    )) {
                // Cache column indices.
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int durationColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                int typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                while (cursor.moveToNext()) {
                    // Get values of columns for a given Audio.
                    long id = cursor.getLong(idColumn);
                    String type = cursor.getString(typeColumn);
                    String[] types = type.split("[.]");
                    type = types[types.length - 1];
                    String title = cursor.getString(titleColumn) + "." + type;
                    String path = cursor.getString(dataColumn);
                    int duration = cursor.getInt(durationColumn);
                    int size = cursor.getInt(sizeColumn);
                    long albumId = cursor.getLong(albumIdColumn);
                    Bitmap albumArt = null;
                    try {
                        albumArt = getAlbumArtwork(app, albumId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);


                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    musics.add(new Music(id, path, contentUri, title, duration, size, albumArt));
                }
            }
        }
    }
    public MusicData (){};

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Bitmap getAlbumArtwork(AppCompatActivity app, long albumId) throws IOException {
        try{
            Cursor cursor = app.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.ALBUM},
                    MediaStore.Audio.Albums._ID+ "=?",
                    new String[] {String.valueOf(albumId)},
                    null);
            if (cursor.moveToFirst()) {
                Log.d("superdroid", "썸네일 가져오기 ");
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                if(path==null){
                    Uri albumArtUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
                    try {
                        //TODO: 기본 이미지 출력
                        return app.getContentResolver().loadThumbnail(albumArtUri, new Size(500, 500), null);
                    } catch (IOException e) {
//                        e.printStackTrace();
                        return BitmapFactory.decodeResource(app.getApplicationContext().getResources(), R.drawable.music);
                    }
                }

                return BitmapFactory.decodeFile(path);
                // do whatever you need to do
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    public Music getMusic(AppCompatActivity app, long musicId){
//        String[] projection = new String[] {
//                MediaStore.Audio.Media._ID,
//                MediaStore.Audio.Media.TITLE,
//                MediaStore.Audio.Media.DATA,
//                MediaStore.Audio.Media.DURATION,
//                MediaStore.Audio.Media.SIZE,
//                MediaStore.Audio.Media.ALBUM_ID,
//                MediaStore.Audio.Media.DISPLAY_NAME
//        };
//        String selection = null;
////                MediaStore.Audio.Media.DURATION + " >= ?";
////                new String[] {String.valueOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))};
//        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
//        try (
//                Cursor cursor = app.getApplicationContext().getContentResolver().query(
//                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                        projection,MediaStore.Audio.Albums._ID+ "=?",
//                        new String[] {String.valueOf(musicId)},
//                        sortOrder
//                )) {
//            // Cache column indices.
//            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
//            int titleColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
//            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
//            int durationColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
//            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
//            int typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
//            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
//
//            if (cursor.moveToFirst()) {
//                // Get values of columns for a given Audio.
//                long id = cursor.getLong(idColumn);
//                String type = cursor.getString(typeColumn);
//                String[] types =type.split("[.]");
//                type = types[types.length - 1];
//                String title = cursor.getString(titleColumn)+"."+type;
//                String path = cursor.getString(dataColumn);
//                int duration = cursor.getInt(durationColumn);
//                int size = cursor.getInt(sizeColumn);
//                long albumId = cursor.getLong(albumIdColumn);
//                Bitmap albumArt=null;
//                try {
//                    albumArt = getAlbumArtwork(app, albumId);
//                }catch (Exception e){e.printStackTrace();}
//
//                Uri contentUri = ContentUris.withAppendedId(
//                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
//
//               return (new Music(id, path, contentUri, title, duration, size, albumArt));
//            }
//        }
//        return null;
//    }
    public List<Music> getMusics() {
        return musics;
    }
}
