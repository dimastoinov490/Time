package com.neo.clock.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import kotlin.concurrent.thread

object SoundEngine {
    private const val TAG = "SoundEngine"
    private const val SAMPLE_RATE = ProceduralTimbres.SAMPLE_RATE

    val ALL_SOUNDS: List<SoundItem>
        get() = BuiltInMelodies.LIST

    @Volatile
    private var isPlaying = false
    private var activeTrack: AudioTrack? = null
    private var activePlayer: MediaPlayer? = null
    private var playThread: Thread? = null

    /**
     * Resolves display title for any sound ID (builtin, device, or custom URI).
     */
    fun resolveTitle(context: Context, soundId: String): String {
        return when {
            soundId.startsWith("builtin:") -> {
                BuiltInMelodies.getTitle(soundId)
            }
            soundId.startsWith("device:") -> {
                val uriStr = soundId.removePrefix("device:")
                try {
                    val ringtone = android.media.RingtoneManager.getRingtone(context, Uri.parse(uriStr))
                    ringtone?.getTitle(context) ?: "Звук устройства"
                } catch (_: Exception) {
                    "Звук устройства"
                }
            }
            soundId.startsWith("custom:") -> {
                val uriStr = soundId.removePrefix("custom:")
                DeviceSoundHelper.queryDisplayName(context, Uri.parse(uriStr))
            }
            soundId.startsWith("content://") || soundId.startsWith("file://") -> {
                DeviceSoundHelper.queryDisplayName(context, Uri.parse(soundId))
            }
            else -> {
                // Try legacy match
                val match = BuiltInMelodies.LIST.find {
                    it.id.removePrefix("builtin:") == soundId || it.id == soundId
                }
                match?.title ?: "Звук"
            }
        }
    }

    /**
     * Plays sound with looping and optional 10s volume ramping.
     */
    fun play(
        context: Context,
        soundId: String,
        loop: Boolean = true,
        rampVolume: Boolean = true
    ) {
        stop()
        isPlaying = true

        // 1. Device or Custom Content URI
        if (soundId.startsWith("device:") || soundId.startsWith("custom:") ||
            soundId.startsWith("content://") || soundId.startsWith("file://")
        ) {
            val rawUri = when {
                soundId.startsWith("device:") -> soundId.removePrefix("device:")
                soundId.startsWith("custom:") -> soundId.removePrefix("custom:")
                else -> soundId
            }

            // Verify URI accessibility
            if (!DeviceSoundHelper.isUriAccessible(context, rawUri)) {
                showFallbackNotification(context)
                // Fallback to default built-in sound
                playBuiltIn(context, "builtin:dawn", loop, rampVolume)
                return
            }

            playUri(context, Uri.parse(rawUri), loop, rampVolume)
            return
        }

        // 2. Built-in synthesized sound
        playBuiltIn(context, soundId, loop, rampVolume)
    }

    private fun playBuiltIn(
        context: Context,
        soundId: String,
        loop: Boolean,
        rampVolume: Boolean
    ) {
        playThread = thread(start = true, name = "NeoClockSoundSynth") {
            try {
                val buffer = BuiltInMelodies.generatePcm(soundId)
                val bufferSizeBytes = buffer.size * 2

                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val track = AudioTrack(
                    attributes,
                    format,
                    maxOf(bufferSizeBytes, minBufferSize * 2),
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                activeTrack = track
                track.play()

                val startTime = SystemClock.elapsedRealtime()

                while (isPlaying) {
                    if (rampVolume) {
                        val elapsedSec = (SystemClock.elapsedRealtime() - startTime) / 1000f
                        val volume = (elapsedSec / 10f).coerceIn(0.12f, 1.0f)
                        track.setVolume(volume)
                    } else {
                        track.setVolume(1.0f)
                    }

                    var written = 0
                    while (written < buffer.size && isPlaying) {
                        val chunk = minOf(1024, buffer.size - written)
                        val ret = track.write(buffer, written, chunk)
                        if (ret < 0) break
                        written += ret
                    }

                    if (!loop) break
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.e(TAG, "Audio synthesis playback failed", e)
            } finally {
                activeTrack = null
                isPlaying = false
            }
        }
    }

    private fun playUri(
        context: Context,
        uri: Uri,
        loop: Boolean,
        rampVolume: Boolean
    ) {
        playThread = thread(start = true, name = "NeoClockUriPlayer") {
            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(context, uri)
                    isLooping = loop
                    prepare()
                }
                activePlayer = player
                player.start()

                val startTime = SystemClock.elapsedRealtime()
                while (isPlaying && player.isPlaying) {
                    if (rampVolume) {
                        val elapsedSec = (SystemClock.elapsedRealtime() - startTime) / 1000f
                        val vol = (elapsedSec / 10f).coerceIn(0.12f, 1.0f)
                        player.setVolume(vol, vol)
                    } else {
                        player.setVolume(1.0f, 1.0f)
                    }
                    SystemClock.sleep(200)
                    if (!loop && !player.isPlaying) break
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play URI: $uri", e)
                showFallbackNotification(context)
                playBuiltIn(context, "builtin:dawn", loop, rampVolume)
            } finally {
                activePlayer?.release()
                activePlayer = null
                isPlaying = false
            }
        }
    }

    fun stop() {
        isPlaying = false
        try {
            activeTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioTrack", e)
        }
        activeTrack = null

        try {
            activePlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaPlayer", e)
        }
        activePlayer = null

        playThread?.interrupt()
        playThread = null
    }

    private fun showFallbackNotification(context: Context) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                context.applicationContext,
                "Мелодия недоступна, использована стандартная",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
