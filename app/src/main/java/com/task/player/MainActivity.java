package com.task.player;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {
    @BindView(R.id.rv_channel_list) RecyclerView channelRecyclerView;
    @BindView(R.id.player_view) SurfaceView playerView;
    @BindView(R.id.player_buffering) ProgressBar playerBuffering;
    @BindView(R.id.player_area) RelativeLayout playerArea;
    public MediaPlayer surfaceMediaPlayer;
    private SurfaceHolder playerSurfaceHolder;
    LinearLayoutManager linearLayoutManager;
    private Uri videoUri;
    private int selectedItem = 0;
    private int channelPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        String jsonFromAsset = loadJSONFromAsset();

        if(jsonFromAsset != null) {
            populateChannelList(jsonFromAsset);
        } else {
            showErrorLayout();
        }

        playerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                        selectedItem = 0;
                        playerArea.setBackgroundColor(getResources().getColor(R.color.white));
                        channelRecyclerView.requestFocus();
                        channelRecyclerView.getLayoutManager().findViewByPosition(channelPosition).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                        TextView chName = channelRecyclerView.getLayoutManager().findViewByPosition(channelPosition).findViewById(R.id.tv_channel_name);
                        TextView chNumber = channelRecyclerView.getLayoutManager().findViewByPosition(channelPosition).findViewById(R.id.tv_channel_number);
                        chName.setTextColor(getResources().getColor(R.color.white));
                        chNumber.setTextColor(getResources().getColor(R.color.white));
                        channelRecyclerView.getLayoutManager().getChildAt(channelPosition).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_channel));
                    }
                }
                return false;
            }
        });
    }

    private void showErrorLayout() {
    }

    private void populateChannelList(String jsonFromAsset) {
        try {
            JSONObject channelsObject = new JSONObject(jsonFromAsset);
            if(channelsObject.has("results")) {
                JSONObject resultsObj = channelsObject.getJSONObject("results");
                if(resultsObj.has("channels")) {
                    JSONArray channelsArray = resultsObj.getJSONArray("channels");
                    setupChannelList(channelsArray);
                } else {
                    showErrorLayout();
                }
            } else {
                showErrorLayout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupChannelList(JSONArray channelsArray) {
        try {
            channelRecyclerView.setHasFixedSize(true);
            ChannelAdapter adapter = new ChannelAdapter(this, channelsArray, getApplicationContext());
            linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
            channelRecyclerView.setLayoutManager(linearLayoutManager);
            channelRecyclerView.setAdapter(adapter);
            channelRecyclerView.requestFocus();

            startPlayback(channelsArray.getJSONObject(0).getString("streamingURL_HEVC"));

            playerView.getHolder().addCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadJSONFromAsset() {
        String channelJson = null;
        try {
            InputStream is = getAssets().open("getchannels.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            channelJson = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return channelJson;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("onError", "onError");
        Log.e("MEDIAPLAYER ERRORS", "what: " + what + "  extra: "   + extra);
        playerView.setVisibility(View.GONE);
        playerBuffering.setVisibility(View.GONE);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.e("Buffering", "############what " + what);
        if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
            playerBuffering.setVisibility(View.VISIBLE);
        } else if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what || MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
            playerBuffering.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        try {
            Log.e("onPrepared", "prepared");
            playerBuffering.setVisibility(View.GONE);
            surfaceMediaPlayer.start();
        } catch (Exception e) {
            Log.e("onPrepared", "Player failed to prepare");
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("Player", "Surface Created");

        playerBuffering.setVisibility(View.VISIBLE);

        playerSurfaceHolder = holder;

        if(videoUri != null) {
            try {
                surfaceMediaPlayer = new MediaPlayer();
                surfaceMediaPlayer.setDisplay(playerSurfaceHolder);
                Map<String, String> headers = new HashMap<>();
                surfaceMediaPlayer.setDataSource(this, videoUri, headers);
                surfaceMediaPlayer.setOnCompletionListener(this);
                surfaceMediaPlayer.setOnInfoListener(this);
                surfaceMediaPlayer.setLooping(true);
                surfaceMediaPlayer.setOnErrorListener(this);
                surfaceMediaPlayer.setOnPreparedListener(this);
                surfaceMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
                playerView.setVisibility(View.GONE);
                playerBuffering.setVisibility(View.GONE);
            }
        } else {
            playerView.setVisibility(View.GONE);
            playerBuffering.setVisibility(View.GONE);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(surfaceMediaPlayer != null) {
            if(surfaceMediaPlayer.isPlaying()) {
                surfaceMediaPlayer.stop();
            }
            surfaceMediaPlayer.release();
        }
    }

    public void startPlayback(String playbackUrl) {
        try {
            videoUri = Uri.parse(playbackUrl);
            if(surfaceMediaPlayer != null) {
                if(surfaceMediaPlayer.isPlaying()) {
                    surfaceMediaPlayer.stop();
                }
                surfaceMediaPlayer.release();
            }
            playerView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            playerView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
                    ||event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                    || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                if(selectedItem == 0) {
                    channelRecyclerView.dispatchKeyEvent(event);
                } else {
                    playerView.dispatchKeyEvent(event);
                }
                return true;
            } else if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                return false;
            } else if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return false;
            } else {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setSelectedItem(int i) {
        selectedItem = i;
        playerArea.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    public void setChannelPosition(int position) {
        channelPosition = position;
    }
}