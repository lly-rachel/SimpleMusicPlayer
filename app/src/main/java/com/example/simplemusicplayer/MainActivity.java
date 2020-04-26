package com.example.simplemusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView playIv,lastIv,nextIv;
    TextView singerTv,songTv;
    RecyclerView musicRv;

    //音乐进度条
    SeekBar seekBar;
    TextView nowTime,totalTime;
    boolean isStop;

    //将long数据转为'mm:ss'格式展示
    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

    //记录当前播放音乐的位置
    int currentPlayPosition = -1;

    //记录暂停播放时的进度条
    int currentPauseInSongPosition = 0;

    MediaPlayer mediaPlayer;

    /*数据源*/
    List<musicbean> musicData;

    private MusicAdapter adapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            seekBar.setProgress(msg.what);
            String formatTime=sdf.format(new Date(msg.what));
            nowTime.setText(formatTime);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        currentPlayPosition = intent.getIntExtra("i", 0);

        iniView();

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextMusic();
            }
        });


        musicData = new ArrayList<musicbean>();

        //创建适配器对象
        adapter = new MusicAdapter(this,musicData);
        musicRv.setAdapter(adapter);

        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        musicRv.setLayoutManager(layoutManager);

        //设置音乐进度条
        seekBar=findViewById(R.id.seekbar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        //加载本地数据源
        loadMusicData();

        //设置每一项的点击事件
        setEventListener();
    }

    private void setEventListener() {
        /*设置每一项的点击事件*/
        adapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                currentPlayPosition = position;
                musicbean musicbean =  musicData.get(position);

                PlayMusicInMusicBean(musicbean);

            }
        });
    }


    class SeekBarThread implements Runnable {

        @Override
        public void run() {
            while (mediaPlayer != null && isStop == false&& mediaPlayer.isPlaying()) {
                // 将SeekBar位置设置到当前播放位置
                handler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
                try {
                    // 每100毫秒更新一次位置
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    public void PlayMusicInMusicBean(musicbean musicbean) {
        /*按照传入对象，播放音乐*/

        //底部显示的歌曲信息（歌手+歌曲）
        songTv.setText(musicbean.getSong());
        singerTv.setText(musicbean.getSinger());
        totalTime.setText(musicbean.getTime());



        stopMusic();

        //重置mediaplayer
        mediaPlayer.reset();
        //设置新的播放器
        try {
            mediaPlayer.setDataSource(musicbean.getPath());
            playMusic();

            new Thread(new SeekBarThread()).start();//线程开启

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void playMusic() {
        /*播放音乐*/
        if(mediaPlayer!=null&&!mediaPlayer.isPlaying()){

            if(currentPauseInSongPosition==0){
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                //从暂停开始播放
                mediaPlayer.seekTo(currentPauseInSongPosition);
                mediaPlayer.start();
            }


            seekBar.setMax(mediaPlayer.getDuration());
            playIv.setImageResource(R.mipmap.pause);

        }
    }

    private void playNextMusic() {
        if (currentPlayPosition == musicData.size() - 1) {
            //若是最后一首，则下一首为第一首
            currentPlayPosition = 0;
        } else {
            //不是最后一首就++
            currentPlayPosition++;
        }
        musicbean nextbean = musicData.get(currentPlayPosition);
        PlayMusicInMusicBean(nextbean);
    }

    private void pauseMusic(){
        /*暂停音乐*/
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            currentPauseInSongPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            playIv.setImageResource(R.mipmap.play);
        }

    }

    private void stopMusic() {
        /*停止音乐*/

        if(mediaPlayer!=null){
            currentPauseInSongPosition=0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.mipmap.play);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
        isStop=true;
        mediaPlayer.reset();

    }

    private void loadMusicData() {
        /*加载本地存储的音乐mp3文件到集合中*/

        //1.获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
        //2.获取本地音乐存储的URI地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //3.开始查询地址
        Cursor cursor = resolver.query(uri,null,null,null,null,null);
        //4.遍历Cursor
        int id = 0;
        while(cursor.moveToNext()){
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid = String.valueOf(id);
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

            String time = sdf.format(new Date(duration));
            musicData.add(new musicbean(sid,song,singer,album,time,path));
        }

        adapter.notifyDataSetChanged();
    }

    private void iniView() {
        /*初始化控件*/
        playIv=findViewById(R.id.iv_play);
        lastIv=findViewById(R.id.iv_last);
        nextIv=findViewById(R.id.iv_next);
        singerTv=findViewById(R.id.bottom_singer_name);
        songTv=findViewById(R.id.bottom_song_name);
        musicRv=findViewById(R.id.music_rv);

        nowTime=findViewById(R.id.seekbar_now_time);
        totalTime=findViewById(R.id.seekbar_total_time);

        nextIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.iv_last:

                if(currentPlayPosition==0){
                    //若是第一首，则上一首为最后一首
                    currentPlayPosition=musicData.size()-1;
                }else{
                    //不是第一首就--
                    currentPlayPosition--;
                }
                musicbean lastbean = musicData.get(currentPlayPosition);
                PlayMusicInMusicBean(lastbean);
                break;

            case R.id.iv_next:
                playNextMusic();

                break;

            case R.id.iv_play:

                if(mediaPlayer.isPlaying()){
                    //正在播放音乐，点击暂停音乐
                    pauseMusic();
                }else{
                    //没有播放音乐，点击播放音乐
                    playMusic();
                }
                break;

        }
    }
}
