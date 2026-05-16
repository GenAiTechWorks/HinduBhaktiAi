package com.hindu.lordpromptsai.audio;

import android.content.Context;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

public class BackgroundVideoPlayer {

    private ExoPlayer player;
    private final Context context;
    private final int videoRes;

    public BackgroundVideoPlayer(Context context, int videoRes) {
        this.context = context.getApplicationContext();
        this.videoRes = videoRes;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void attach(PlayerView playerView) {

        player = new ExoPlayer.Builder(context).build();

        playerView.setPlayer(player);
        playerView.setUseController(false);
        playerView.setResizeMode(
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        );

        MediaItem mediaItem =
                MediaItem.fromUri(
                        "android.resource://" +
                                context.getPackageName() +
                                "/" + videoRes
                );

        player.setMediaItem(mediaItem);

        // 🔇 SILENT & NO AUDIO FOCUS
        player.setVolume(0f);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        player.prepare();
        player.play();
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    // ✅ ADD THIS
    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }
    public void play() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }
}