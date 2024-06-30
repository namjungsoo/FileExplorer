package com.duongame.activity.viewer

import android.net.Uri
import android.os.Bundle
import com.duongame.MainApplication
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import java.io.File


class VideoActivity : BaseViewerActivity() {
    private lateinit var player: SimpleExoPlayer
    private var positionMs: Long = 0L // pause/resume을 위해서 설정함
    private lateinit var exoPlayerView: PlayerView

    private fun buildMediaSourceMulti(): ConcatenatingMediaSource {
        val ret = ConcatenatingMediaSource()
        for (video in MainApplication.getInstance(this).videoList) {
            ret.addMediaSource(buildMediaSource(video.path))
        }
        return ret
    }

    private fun buildMediaSource(path: String): MediaSource {
        val uri = Uri.fromFile(File(path))
        val dataSpec = DataSpec(uri)
        val fileDataSource = FileDataSource()
        try {
            fileDataSource.open(dataSpec)
        } catch (e: FileDataSource.FileDataSourceException) {
            e.printStackTrace()
        }
        val factory: DataSource.Factory = DataSource.Factory { fileDataSource }
        return ProgressiveMediaSource.Factory(factory).createMediaSource(uri)
    }

    private fun buildPlayer(): SimpleExoPlayer {
        // simple player 만들기
        val factory = DefaultRenderersFactory(this)
        factory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)// for ffmpeg extension
        val builder = SimpleExoPlayer.Builder(this, factory)
        return builder.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        contentViewResId = R.layout.activity_video
        //ConcatenatingMediaSource
        super.onCreate(savedInstanceState)

        // intent로부터 item받아서 source 처리하기
        val item: ExplorerItem = intent.extras?.get("item") as ExplorerItem
        player = buildPlayer()
        exoPlayerView = findViewById<PlayerView>(R.id.exoPlayerView)
        exoPlayerView.player = player

        val mediaSource = buildMediaSourceMulti()
        var windowIndex = -1
        for (i in MainApplication.getInstance(this).videoList.indices) {
            if (MainApplication.getInstance(this).videoList[i].path == item.path) {
                windowIndex = i
                break
            }
        }

        player.prepare(mediaSource)
        player.seekTo(windowIndex, 0)
        player.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        exoPlayerView.onPause()

        player.playWhenReady = false
        positionMs = player.currentPosition
    }

    override fun onResume() {
        super.onResume()
        exoPlayerView.onResume()

        player.seekTo(positionMs)
        player.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()

        player.stop()
        player.release()
    }
}