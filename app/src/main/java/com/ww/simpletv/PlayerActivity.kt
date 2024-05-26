package com.ww.simpletv

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Pair
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.media3.common.C
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource.HttpDataSourceException
import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer
import androidx.media3.decoder.vp9.LibvpxVideoRenderer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.exoplayer.video.VideoRendererEventListener
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import com.ww.simpletv.databinding.ActivityPlayerBinding
import com.ww.simpletv.dialog.ChannelListDialog
import com.ww.simpletv.dialog.SettingDialog
import java.util.ArrayList

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/27
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
class PlayerActivity : BaseActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var tvList = mutableListOf<TV>()
    private var exoPlayer: ExoPlayer? = null
    private var dialog: ChannelListDialog? = null
    private var exitFlag = false
    private var lastTV: TV? = null
    private var curTVIndex = 0
    private var retryCount = 0

    companion object {
        const val MAX_RETRY_COUNT = 3
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        hideSystemUi()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player)
        tvList.addAll(ChannelUtils.channelSet.toMutableList())
        Log.e("ww", "onCreate: tvList size ${tvList.size}")
        val mmkv = MMKV.defaultMMKV()
        val default = Gson().toJson(ChannelUtils.channelSet.first())
        val tvJson = mmkv.decodeString(Constant.KEY_LAST_CHANNEL, default)
        tvJson?.let { json ->
            val tv = Gson().fromJson(json, TV::class.java)
            tv?.let {
                lastTV = tv
                curTVIndex = tvList.indexOf(tv)
                val renderersFactory = object : DefaultRenderersFactory(this@PlayerActivity) {
                    override fun buildAudioRenderers(
                        context: Context,
                        extensionRendererMode: Int,
                        mediaCodecSelector: MediaCodecSelector,
                        enableDecoderFallback: Boolean,
                        audioSink: AudioSink,
                        eventHandler: Handler,
                        eventListener: AudioRendererEventListener,
                        out: ArrayList<Renderer>
                    ) {
                        out.add(FfmpegAudioRenderer())
                        super.buildAudioRenderers(
                            context,
                            extensionRendererMode,
                            mediaCodecSelector,
                            enableDecoderFallback,
                            audioSink,
                            eventHandler,
                            eventListener,
                            out
                        )
                    }

                    override fun buildVideoRenderers(
                        context: Context,
                        extensionRendererMode: Int,
                        mediaCodecSelector: MediaCodecSelector,
                        enableDecoderFallback: Boolean,
                        eventHandler: Handler,
                        eventListener: VideoRendererEventListener,
                        allowedVideoJoiningTimeMs: Long,
                        out: ArrayList<Renderer>
                    ) {
                        out.add(LibvpxVideoRenderer(1000))
                        super.buildVideoRenderers(
                            context,
                            extensionRendererMode,
                            mediaCodecSelector,
                            enableDecoderFallback,
                            eventHandler,
                            eventListener,
                            allowedVideoJoiningTimeMs,
                            out
                        )
                    }
                }.apply {
                    setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                }
                val loadErrorHandlingPolicy: LoadErrorHandlingPolicy =
                    object : DefaultLoadErrorHandlingPolicy() {
                        override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
                            val errorCount = loadErrorInfo.errorCount
                            val exception = loadErrorInfo.exception
                            Log.e("ww", "getRetryDelayMsFor:errorCount:$errorCount  exception:${exception::class.java.simpleName}")
                            return if (exception is HttpDataSourceException) {
                                5000
                            } else {
                                C.TIME_UNSET
                            }
                        }
                    }
                exoPlayer = ExoPlayer.Builder(this@PlayerActivity, renderersFactory)
                    .setMediaSourceFactory(
                        DefaultMediaSourceFactory(this)
                            .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
                    )
                    .build()
                exoPlayer?.run {
                    playWhenReady = true
                    setMediaItem(MediaItem.fromUri(tv.url))
                    prepare()
                    binding.exoPlay.player = this
                }
//                binding.exoPlay.setShutterBackgroundColor(Color.TRANSPARENT)
            }
        }
        binding.exoPlay.setOnClickListener {
            showChannelList()
        }
        binding.exoPlay.setOnLongClickListener {
            SettingDialog().show(supportFragmentManager, Constant.DIALOG_TAG_SETTING)
            true
        }
        binding.exoPlay.setErrorMessageProvider(object : ErrorMessageProvider<PlaybackException> {
            override fun getErrorMessage(throwable: PlaybackException): Pair<Int, String> {
                Log.e("ww", "getErrorMessage: code:${throwable.errorCode}  name:${throwable.errorCodeName}  retryCount:$retryCount")
                when (throwable.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                        return Pair.create(0, getString(R.string.play_network_error_hint))
                    }

                    PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
                    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
                    PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES -> {
                        return Pair.create(0, getString(R.string.play_decode_error_hint))
                    }

                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                    PlaybackException.ERROR_CODE_DECODING_FAILED -> {
                        return if (retryCount < MAX_RETRY_COUNT) {
                            retryCount++
                            exoPlayer?.prepare()
                            Pair.create(0, "")
                        } else {
                            Pair.create(0, getString(R.string.play_decode_error_hint))
                        }
                    }

                    PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                        exoPlayer?.prepare()
                        return Pair.create(0, "")
                    }

                    else -> return if (retryCount < MAX_RETRY_COUNT) {
                        retryCount++
                        exoPlayer?.prepare()
                        Pair.create(0, "")
                    } else {
                        Pair.create(0, getString(R.string.play_other_error_hint))
                    }
                }
            }
        })
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).run {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.e("ww", "onKeyDown: $keyCode")
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                showChannelList()
                return true
            }

            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BUTTON_B -> {
                if (dialog?.isVisible == true) {
                    dialog?.dismiss()
                } else if (exitFlag) {
                    finish()
                } else {
                    exitFlag = true
                    Toast.makeText(this, R.string.exit_hint, Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        exitFlag = false
                    }, 1500)
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_UP -> {
                if (curTVIndex == 0) {
                    curTVIndex = tvList.size - 1
                } else {
                    curTVIndex--
                }
                lastTV = tvList[curTVIndex]
                exoPlayer?.run {
                    setMediaItem(MediaItem.fromUri(tvList[curTVIndex].url))
                    prepare()
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (curTVIndex == tvList.size - 1) {
                    curTVIndex = 0
                } else {
                    curTVIndex++
                }
                lastTV = tvList[curTVIndex]
                exoPlayer?.run {
                    setMediaItem(MediaItem.fromUri(tvList[curTVIndex].url))
                    prepare()
                }
                return true
            }

            KeyEvent.KEYCODE_MENU -> {
                SettingDialog().show(supportFragmentManager, Constant.DIALOG_TAG_SETTING)
                return true
            }

        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showChannelList() {
        dialog = ChannelListDialog(ChannelUtils.channelSet, lastTV)
        dialog?.onChoose = { tv ->
            lastTV = tv
            curTVIndex = tvList.indexOf(tv)
            retryCount = 0
            exoPlayer?.run {
                setMediaItem(MediaItem.fromUri(tv.url))
                prepare()
            }
        }
        dialog?.show(supportFragmentManager, Constant.DIALOG_TAG_CHANNEL)
    }

    override fun onStart() {
        super.onStart()
        exoPlayer?.run {
            playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        lastTV?.let {
            MMKV.defaultMMKV().encode(Constant.KEY_LAST_CHANNEL, Gson().toJson(lastTV))
        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.run {
            playWhenReady = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.run {
            release()
        }
    }
}