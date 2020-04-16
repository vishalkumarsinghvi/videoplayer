package com.vishal.youtubeplayer.ui;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vishal.youtubeplayer.R;
import com.vishal.youtubeplayer.adapter.VideoAdapter;
import com.vishal.youtubeplayer.model.VideoModel;
import com.vishal.youtubeplayer.util.VideoControllerView;
import com.vishal.youtubeplayer.util.VideoControllerView.MediaPlayerControl;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
    MediaPlayer.OnPreparedListener, MediaPlayerControl {

  private MediaPlayer player;
  private VideoControllerView controller;
  private ArrayList<VideoModel> videoList = new ArrayList<>();
  private ProgressBar progressBar;
  private RecyclerView rvVideoData;
  private boolean hasActiveHolder;
  private boolean isFullScreen = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getDataList();
    initUI();
  }

  private void getDataList() {
    Gson gson = new Gson();
    Type type = new TypeToken<List<VideoModel>>() {
    }.getType();
    List<VideoModel> yourList = gson.fromJson(VideoModel.jsonMovie, type);
    videoList.addAll(yourList);
  }

  private void initUI() {
    setUpNewsDataRecyclerView();
    SurfaceView videoSurface = findViewById(R.id.videoview);
    progressBar = findViewById(R.id.progressBar_cyclic);
    SurfaceHolder videoHolder = videoSurface.getHolder();
    videoHolder.addCallback(this);
    controller = new VideoControllerView(this);
    getMediaPlayerInstance();
    playVideo(videoList.get(0).getSources());
  }

  private void setUpNewsDataRecyclerView() {
    rvVideoData = findViewById(R.id.rv_video_data);
    VideoAdapter videoAdapter = new VideoAdapter(this, videoList);
    rvVideoData.setLayoutManager(new LinearLayoutManager(this,
        RecyclerView.VERTICAL, false));
    rvVideoData.setHasFixedSize(true);
    rvVideoData.addItemDecoration(
        new DividerItemDecoration(Objects.requireNonNull(this),
            DividerItemDecoration.VERTICAL));
    rvVideoData.setAdapter(videoAdapter);
  }


  public void getMediaPlayerInstance() {
    if (player == null) {
      player = new MediaPlayer();
      player.setAudioAttributes(new AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setLegacyStreamType(AudioManager.STREAM_MUSIC)
          .build());

    }
  }

  public void playVideo(String url) {
    progressBar.setVisibility(View.VISIBLE);
    try {
      player.setDataSource(url);
    } catch (IOException e) {
      e.printStackTrace();
    }
    player.setOnPreparedListener(this);
  }

  public void playNextVideo(String newUri) throws IOException {
    progressBar.setVisibility(View.VISIBLE);
    player.reset();
    player.setOnPreparedListener(this);
    player.setDataSource(newUri);
    player.prepareAsync();
  }


  @Override
  protected void onPause() {
    super.onPause();
    player.pause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    player.pause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    releaseMediaPlayer();
  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    controller.show();
    return false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  }


  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    synchronized (this) {
      hasActiveHolder = true;
      this.notifyAll();
    }
    synchronized (this) {
      while (!hasActiveHolder) {
        try {
          this.wait();
        } catch (InterruptedException ignored) {
        }
      }
      player.setDisplay(holder);
      player.prepareAsync();
    }

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    synchronized (this) {
      hasActiveHolder = false;

      synchronized (this) {
        this.notifyAll();
      }
    }
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    controller.setMediaPlayer(this);
    controller.setAnchorView(findViewById(R.id.videoSurfaceContainer));
    player.start();
    progressBar.setVisibility(View.GONE);
  }

  @Override
  public boolean canPause() {
    return true;
  }

  @Override
  public boolean canSeekBackward() {
    return true;
  }

  @Override
  public boolean canSeekForward() {
    return true;
  }

  @Override
  public int getBufferPercentage() {
    return 0;
  }

  @Override
  public int getCurrentPosition() {
    return player.getCurrentPosition();
  }

  @Override
  public int getDuration() {
    return player.getDuration();
  }

  @Override
  public boolean isPlaying() {
    return player.isPlaying();
  }

  @Override
  public void pause() {
    player.pause();
  }

  @Override
  public void seekTo(int i) {
    player.seekTo(i);
  }

  @Override
  public void start() {
    player.start();
  }

  @Override
  public boolean isFullScreen() {
    return isFullScreen;
  }

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  public void toggleFullScreen() {
    isFullScreen = !isFullScreen;
    Log.d("main", "toggleFullScreen: " + isFullScreen);
    if (isFullScreen) {
      rvVideoData.setVisibility(View.GONE);
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
      rvVideoData.setVisibility(View.VISIBLE);

    }
  }


  private void releaseMediaPlayer() {
    if (player != null) {
      player.setDisplay(null);
      player.release();
      player = null;
    }
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
      rvVideoData.setVisibility(View.GONE);
      controller.updateFullScreen();
      isFullScreen = true;
    } else {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
      rvVideoData.setVisibility(View.VISIBLE);
      isFullScreen = false;
      controller.updateFullScreen();

    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    controller.show();
  }
}
