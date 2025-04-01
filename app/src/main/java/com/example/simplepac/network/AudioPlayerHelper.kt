package com.example.simplepac.network

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class AudioPlayerHelper(context: Context) {
    private val exoPlayer = ExoPlayer.Builder(context).build()
    private var currentAudioUrl: String? = null

    fun preparePlayer(url: String) {
        if (url != currentAudioUrl) {
            currentAudioUrl = url
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
        }
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stop() {
        exoPlayer.stop()
    }

    fun release() {
        exoPlayer.release()
    }

    fun bindPlayerView(playerView: PlayerView) {
        playerView.player = exoPlayer
    }
}