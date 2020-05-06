package com.duongame.activity.viewer

import android.net.Uri
import android.os.Bundle
import com.duongame.R
import com.duongame.adapter.ExplorerItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import kotlinx.android.synthetic.main.activity_video.*
import java.io.File


class VideoActivity : BaseViewerActivity() {
    private lateinit var player: SimpleExoPlayer
    private var positionMs: Long = 0L

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
//        ProgressiveMediaSource.Factory(factory)
        return ProgressiveMediaSource.Factory(factory).createMediaSource(uri)
    }

    private fun buildPlayer(): SimpleExoPlayer {
        // simple player 만들기
        val player = SimpleExoPlayer.Builder(this).build()
        return player
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        contentViewResId = R.layout.activity_video
        //ConcatenatingMediaSource
        super.onCreate(savedInstanceState)

        // intent로부터 item받아서 source 처리하기
        val item: ExplorerItem = intent.extras?.get("item") as ExplorerItem
        player = buildPlayer()
        exoPlayerView.player = player

        val mediaSource = buildMediaSource(item.path)
        player.prepare(mediaSource)
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