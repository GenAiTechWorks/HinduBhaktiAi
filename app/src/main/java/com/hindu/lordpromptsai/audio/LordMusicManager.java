package com.hindu.lordpromptsai.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.hindu.lordpromptsai.R;

public class LordMusicManager {

    private static LordMusicManager instance;

    private MediaPlayer mediaPlayer;
    private boolean isMuted = true;
    private boolean pausedByFocusLoss = false;

    private Context appContext;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;

    private String currentTab;

    private LordMusicManager() {}

    public static synchronized LordMusicManager getInstance() {
        if (instance == null) {
            instance = new LordMusicManager();
        }
        return instance;
    }

    // ✅ CALL ONCE (MainActivity.onCreate)
    public void init(Context context) {
        if (appContext != null) return;
        appContext = context.getApplicationContext();
        audioManager =
                (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
    }

    // ---------------------------------
    // AUDIO FOCUS
    // ---------------------------------
    private final AudioManager.OnAudioFocusChangeListener focusListener =
            focus -> {
                if (mediaPlayer == null) return;

                switch (focus) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        pause();
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        if (mediaPlayer.isPlaying()) {
                            pausedByFocusLoss = true;
                            mediaPlayer.pause();
                        }
                        break;

                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (pausedByFocusLoss && !isMuted) {
                            mediaPlayer.start();
                            pausedByFocusLoss = false;
                        }
                        break;
                }
            };

    private void requestAudioFocus() {
        if (audioManager == null) return;

        focusRequest =
                new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build()
                        )
                        .setOnAudioFocusChangeListener(focusListener)
                        .build();

        audioManager.requestAudioFocus(focusRequest);
    }

    private void abandonAudioFocus() {
        if (audioManager == null) return;

        if (focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest);
            focusRequest = null;
        } else {
            audioManager.abandonAudioFocus(focusListener);
        }
    }

    // ---------------------------------
    // PLAY
    // ---------------------------------
    public void playForTab(Context context, String tabName) {
        if (tabName == null || tabName.equals(currentTab)) return;

        // ✅ Already created and use only for current release next version will remove
        if (mediaPlayer != null) {
            // ✅ If paused → resume
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            return;
        }

        currentTab = tabName;
        stopInternal();

        int resId = getMusicForTab(tabName);
        if (resId == 0) return;

        requestAudioFocus();

        mediaPlayer = MediaPlayer.create(appContext, resId);
        if (mediaPlayer == null) return;

        mediaPlayer.setLooping(true);
        applyVolume();
        mediaPlayer.start();
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeIfNeeded() {
        if (mediaPlayer != null && !isMuted && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        applyVolume();
        if (!isMuted) resumeIfNeeded();
    }

    public boolean isMuted() {
        return isMuted;
    }

    private void applyVolume() {
        if (mediaPlayer != null) {
            float v = isMuted ? 0f : 1f;
            mediaPlayer.setVolume(v, v);
        }
    }

    public void stop() {
        stopInternal();
        abandonAudioFocus();
    }

    private void stopInternal() {
        pausedByFocusLoss = false;

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Commented for next release
    private int getMusicForTab(String tabName) {
        return R.raw.bgmusic;
      /*  switch (tabName) {
            case "Krishna": return R.raw.krishnanew;
            case "Shiva": return R.raw.shivanew;
            case "Vishnu": return R.raw.vishnunew;
            case "Ganesha": return R.raw.ganeshnew;
            case "Ram": return R.raw.ramnew;
            case "Hanuman": return R.raw.hanumannew;
            case "Durga": return R.raw.durganew;
           *//* case "Mahalaxmi": return R.raw.laxmimata;*//*
            case "Favorites": return R.raw.favorites;
            default: return 0;
        }*/
    }
}