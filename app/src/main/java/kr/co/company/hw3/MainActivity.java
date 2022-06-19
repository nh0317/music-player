package kr.co.company.hw3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSION_READ_EXTERNAL_STORAGE =0;
    ListView mListView =null;
    BaseAdapter adapter = null;
    List<Music> mData = null;
    Thread mthread =null;


    String ext= Environment.getExternalStorageState();
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_READ_EXTERNAL_STORAGE);

        if(!ext.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this,"SD 카드가 반드시 필요합니다.",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mListView = (ListView) findViewById(R.id.list_view);
        loadData(this);
    }

    public void loadData(AppCompatActivity app){
        mthread = new Thread("Get Music List") {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            public void run() {
                MusicData musicData = new MusicData(app);
                mData = musicData.getMusics();
                adapter = new BaseAdpaterMusic(app, mData);
                runOnUiThread(() -> {
                    mListView.setAdapter(adapter);
                    mListView.setOnItemClickListener((parent, view, position, id) -> {
                        Intent intent = new Intent(app, PlayMusicActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    });
                });
            }
        };
        mthread.start();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadData(this);
            } else
                Toast.makeText(getApplicationContext(), "앱을 사용하려면 권한을 허용하세요.", Toast.LENGTH_LONG).show();
        }
    }

}