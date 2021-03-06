/*
 * Copyright (C) 2013 yixia.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package u.can.i.up.vitamio_iptv_byr;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoViewBuffer extends Activity implements OnInfoListener, OnBufferingUpdateListener {

    /**
     * path.
     */
    //private String path = "http://tv6.byr.cn/hls/cctv5hd.m3u8";
    private String path;
    private Uri uri;
    private VideoView mVideoView;
    private ProgressBar pb;
    private TextView downloadRateView, loadRateView;
    private Display currDisplay;
    Handler handler=new Handler();
    Runnable runnable=new Runnable(){
        @Override
        public void run() {
            //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作
            mVideoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        currDisplay = this.getWindowManager().getDefaultDisplay();
        setContentView(R.layout.videobuffer);
        mVideoView = (VideoView) findViewById(R.id.buffer);
        pb = (ProgressBar) findViewById(R.id.probar);

        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);

        path = this.getIntent().getExtras().getString("url");

        if (path == "") {
            // Tell the user to provide a media file URL/path.
            Toast.makeText(
                    VideoViewBuffer.this,
                    "Please edit VideoBuffer Activity, and set path"
                            + " variable to your media file URL/path", Toast.LENGTH_LONG).show();
            return;
        } else {
      /*
       * Alternatively,for streaming media you can use
       * mVideoView.setVideoURI(Uri.parse(URLstring));
       */
            uri = Uri.parse(path);
            mVideoView.setVideoURI(uri);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
            mVideoView.requestFocus();
            mVideoView.setOnInfoListener(this);
            mVideoView.setBufferSize(5120);
            mVideoView.setOnBufferingUpdateListener(this);

            mVideoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);

            handler.postDelayed(runnable, 2000);

            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // optional need Vitamio 4.0
                    mediaPlayer.setPlaybackSpeed(1.0f);
                    int vWidth = mediaPlayer.getVideoWidth();
                    int vHeight = mediaPlayer.getVideoHeight();
                    if (vWidth > currDisplay.getWidth() || vHeight > currDisplay.getHeight()){
                        //如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
                        float wRatio = (float) vWidth / (float) currDisplay.getWidth();
                        float hRatio = (float) vHeight / (float) currDisplay.getHeight();
                        //选择大的一个进行缩放
                        float ratio = Math.max(wRatio, hRatio);
                        vWidth = (int) Math.ceil((float) vWidth / ratio);
                        vHeight = (int) Math.ceil((float) vHeight / ratio);
                        //设置surfaceView的布局参数
                        mVideoView.setLayoutParams(new LinearLayout.LayoutParams(vWidth, vHeight));
                        //然后开始播放视频
                        mediaPlayer.start();
                    }
                }
            });
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    pb.setVisibility(View.VISIBLE);
                    downloadRateView.setText("");
                    loadRateView.setText("");
                    downloadRateView.setVisibility(View.VISIBLE);
                    loadRateView.setVisibility(View.VISIBLE);

                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                pb.setVisibility(View.GONE);
                downloadRateView.setVisibility(View.GONE);
                loadRateView.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                downloadRateView.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        loadRateView.setText(percent + "%");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
