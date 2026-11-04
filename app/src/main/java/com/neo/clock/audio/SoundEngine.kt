package com.neo.clock.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import java.util.Random
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

data class SoundInfo(
    val id: String,
    val name: String,
    val group: String
)

object SoundEngine {
    private const val TAG = "SoundEngine"
    private const val SAMPLE_RATE = 44100

    val ALL_SOUNDS = listOf(
        // iOS tones (iOS 26 / modern iOS alarm tones)
        SoundInfo("radar", "Радар (Radar)", "iOS"),
        SoundInfo("opening", "Вступление (Opening)", "iOS"),
        SoundInfo("chimes", "Перезвон (Chimes)", "iOS"),
        SoundInfo("reflection", "Отражение (Reflection)", "iOS"),
        SoundInfo("apex", "Апекс (Apex)", "iOS"),
        SoundInfo("beacon", "Маяк (Beacon)", "iOS"),
        SoundInfo("seaside", "У моря (By the Seaside)", "iOS"),
        SoundInfo("silk", "Шёлк (Silk)", "iOS"),
        SoundInfo("twinkle", "Мерцание (Twinkle)", "iOS"),
        SoundInfo("cosmic", "Космос (Cosmic)", "iOS"),
        // Nature
        SoundInfo("birds", "Утро в лесу", "Природа"),
        SoundInfo("rain", "Дождь", "Природа"),
        SoundInfo("ocean", "Океан", "Природа"),
        // Classical
        SoundInfo("elise", "Für Elise", "Классика"),
        SoundInfo("ode", "Ода к радости", "Классика"),
        // Modern
        SoundInfo("pulse", "Neon Pulse", "Современные"),
        SoundInfo("crystal", "Хрустальные колокола", "Современные"),
        SoundInfo("classic", "Классический сигнал", "Современные")
    )

    @Volatile
    private var isPlaying = false
    private var activeTrack: AudioTrack? = null
    private var activePlayer: MediaPlayer? = null
    private var playThread: Thread? = null

    fun play(context: Context, soundId: String, loop: Boolean = true, rampVolume: Boolean = true) {
        stop()
        isPlaying = true

        // If soundId is a content URI or file
        if (soundId.startsWith("content://") || soundId.startsWith("android.resource://") || soundId.startsWith("file://")) {
            playUri(context, Uri.parse(soundId), loop)
            return
        }

        // Check if raw resource exists (e.g. res/raw/<soundId>)
        val rawResId = context.resources.getIdentifier(soundId, "raw", context.packageName)
        if (rawResId != 0) {
            playRawResource(context, rawResId, loop)
            return
        }

        // Otherwise generate synthesis via AudioTrack
        playThread = thread(start = true, name = "NeoClockSoundSynth") {
            try {
                val buffer = generateSoundBuffer(soundId)
                val bufferSize = buffer.size * 2 // 16-bit PCM

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
                    maxOf(bufferSize, minBufferSize * 2),
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )
                activeTrack = track
                track.play()

                val startTime = SystemClock.elapsedRealtime()

                while (isPlaying) {
                    if (rampVolume) {
                        val elapsedSec = (SystemClock.elapsedRealtime() - startTime) / 1000f
                        val volume = (elapsedSec / 10f).coerceIn(0.15f, 1.0f)
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

    private fun playUri(context: Context, uri: Uri, loop: Boolean) {
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
                start()
            }
            activePlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play URI: $uri", e)
            play(context, "radar", loop)
        }
    }

    private fun playRawResource(context: Context, resId: Int, loop: Boolean) {
        try {
            val player = MediaPlayer.create(context, resId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = loop
                start()
            }
            activePlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play raw resId: $resId", e)
        }
    }

    private fun midiToFreq(midi: Int): Float {
        return (440.0 * 2.0.pow((midi - 69.0) / 12.0)).toFloat()
    }

    private fun generateSoundBuffer(soundId: String): ShortArray {
        return when (soundId) {
            // iOS alarm tones
            "radar" -> generateRadar()
            "opening" -> generateOpening()
            "chimes" -> generateChimes()
            "reflection" -> generateReflection()
            "apex" -> generateApex()
            "beacon" -> generateBeacon()
            "seaside" -> generateSeaside()
            "silk" -> generateSilk()
            "twinkle" -> generateTwinkle()
            "cosmic" -> generateCosmic()
            // Nature
            "birds" -> generateBirds()
            "rain" -> generateRain()
            "ocean" -> generateOcean()
            // Classical
            "elise" -> generateFürElise()
            "ode" -> generateOdeToJoy()
            // Modern
            "pulse" -> generateNeonPulse()
            "crystal" -> generateCrystalBells()
            else -> generateClassicBeep()
        }
    }

    // --- iOS 26 / Modern iOS Tones ---

    // 1. Radar (Радар) - Signature double staccato beep
    private fun generateRadar(): ShortArray {
        val totalSec = 2.4f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        // Pattern: [beep, gap, beep, long_gap], repeated twice
        val pattern = listOf(
            Triple(1568f, 0.08f, 0.04f), // G6 beep
            Triple(1865f, 0.09f, 0.35f), // Bb6 beep
            Triple(1568f, 0.08f, 0.04f),
            Triple(1865f, 0.09f, 0.65f)
        )

        var curSample = 0
        for ((freq, dur, pause) in pattern) {
            val beepLen = (dur * SAMPLE_RATE).toInt()
            for (n in 0 until beepLen) {
                val idx = curSample + n
                if (idx < numSamples) {
                    val t = n.toFloat() / SAMPLE_RATE
                    // Sharp attack, fast decay
                    val env = exp(-7.0f * (n.toFloat() / beepLen))
                    val tone = sin(2.0 * PI * freq * t) + 0.35 * sin(2.0 * PI * (freq * 1.5) * t)
                    val sample = (tone * env * 0.45 * Short.MAX_VALUE).toInt()
                    buffer[idx] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            curSample += beepLen + (pause * SAMPLE_RATE).toInt()
        }
        return buffer
    }

    // 2. Opening (Вступление) - Signature marimba arpeggio
    private fun generateOpening(): ShortArray {
        // C major marimba run: C5, E5, G5, C6, G5, E5, D5, G5, B5, D6, B5, G5, C5
        val notes = listOf(
            Pair(72, 0.18f), Pair(76, 0.18f), Pair(79, 0.18f), Pair(84, 0.35f),
            Pair(79, 0.18f), Pair(76, 0.18f), Pair(74, 0.18f), Pair(79, 0.18f),
            Pair(83, 0.18f), Pair(86, 0.35f), Pair(83, 0.18f), Pair(79, 0.18f),
            Pair(72, 0.60f)
        )
        return renderMarimba(notes)
    }

    // 3. Chimes (Перезвон) - Descending tubular chimes
    private fun generateChimes(): ShortArray {
        val notes = listOf(
            Pair(88, 0.45f), Pair(83, 0.45f), Pair(80, 0.45f), Pair(76, 0.55f),
            Pair(71, 0.45f), Pair(68, 0.80f)
        )
        return renderBell(notes)
    }

    // 4. Reflection (Отражение) - Vibraphone morning melody
    private fun generateReflection(): ShortArray {
        val notes = listOf(
            Pair(69, 0.28f), Pair(72, 0.28f), Pair(76, 0.28f), Pair(79, 0.42f),
            Pair(81, 0.55f), Pair(79, 0.28f), Pair(76, 0.28f), Pair(72, 0.28f),
            Pair(74, 0.28f), Pair(77, 0.28f), Pair(81, 0.42f), Pair(84, 0.70f)
        )
        return renderVibraphone(notes)
    }

    // 5. Apex (Апекс) - Modern punchy electronic synth sequence
    private fun generateApex(): ShortArray {
        val notes = listOf(
            Pair(74, 0.16f), Pair(78, 0.16f), Pair(81, 0.16f), Pair(86, 0.30f),
            Pair(83, 0.16f), Pair(79, 0.16f), Pair(76, 0.16f), Pair(74, 0.45f)
        )
        val totalSec = notes.sumOf { it.second.toDouble() }.toFloat() + 0.3f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        var curSample = 0
        for ((midi, dur) in notes) {
            val len = (dur * SAMPLE_RATE).toInt()
            val freq = midiToFreq(midi)
            for (n in 0 until len) {
                val idx = curSample + n
                if (idx < numSamples) {
                    val t = n.toFloat() / SAMPLE_RATE
                    // Sawtooth with sharp lowpass decay
                    val phase = (t * freq) % 1.0f
                    val saw = (2.0f * phase - 1.0f)
                    val sin = sin(2.0 * PI * freq * t).toFloat()
                    val env = exp(-5.0f * (n.toFloat() / len))
                    val sample = ((saw * 0.4f + sin * 0.6f) * env * 0.36f * Short.MAX_VALUE).toInt()
                    val cur = buffer[idx]
                    buffer[idx] = (cur + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            curSample += len
        }
        return buffer
    }

    // 6. Beacon (Маяк) - Sonar acoustic ping
    private fun generateBeacon(): ShortArray {
        val totalSec = 2.6f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        val pings = listOf(
            Pair(440f, 0.0f),
            Pair(880f, 0.8f)
        )

        for ((freq, startSec) in pings) {
            val startIdx = (startSec * SAMPLE_RATE).toInt()
            val pingLen = (1.4f * SAMPLE_RATE).toInt()
            for (n in 0 until pingLen) {
                val idx = startIdx + n
                if (idx < numSamples) {
                    val t = n.toFloat() / SAMPLE_RATE
                    val env = exp(-3.2f * t)
                    val tone = sin(2.0 * PI * freq * t) + 0.25 * sin(2.0 * PI * (freq * 2.0) * t)
                    val sample = (tone * env * 0.38 * Short.MAX_VALUE).toInt()
                    val cur = buffer[idx]
                    buffer[idx] = (cur + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return buffer
    }

    // 7. Seaside (У моря) - Cheerful tropical mallet phrase
    private fun generateSeaside(): ShortArray {
        val notes = listOf(
            Pair(79, 0.22f), Pair(81, 0.22f), Pair(83, 0.22f), Pair(86, 0.32f),
            Pair(88, 0.32f), Pair(86, 0.22f), Pair(83, 0.22f), Pair(81, 0.45f)
        )
        return renderMarimba(notes)
    }

    // 8. Silk (Шёлк) - Gentle warm flute/sine chords
    private fun generateSilk(): ShortArray {
        val notes = listOf(
            Pair(77, 0.50f), Pair(81, 0.50f), Pair(84, 0.65f),
            Pair(76, 0.50f), Pair(79, 0.50f), Pair(83, 0.80f)
        )
        val totalSec = notes.sumOf { it.second.toDouble() }.toFloat() + 0.5f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        var curSample = 0
        for ((midi, dur) in notes) {
            val len = (dur * SAMPLE_RATE).toInt()
            val freq = midiToFreq(midi)
            val ringLen = minOf(numSamples - curSample, (dur * 1.8f * SAMPLE_RATE).toInt())
            for (n in 0 until ringLen) {
                val idx = curSample + n
                if (idx < numSamples) {
                    val t = n.toFloat() / SAMPLE_RATE
                    // Soft attack, gentle exponential decay
                    val attack = (n.toFloat() / (0.05f * SAMPLE_RATE)).coerceAtMost(1f)
                    val env = attack * exp(-2.2f * (n.toFloat() / len))
                    val tone = sin(2.0 * PI * freq * t) + 0.15 * sin(2.0 * PI * freq * 2.0 * t)
                    val sample = (tone * env * 0.32 * Short.MAX_VALUE).toInt()
                    val cur = buffer[idx]
                    buffer[idx] = (cur + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            curSample += len
        }
        return buffer
    }

    // 9. Twinkle (Мерцание) - Crystalline glockenspiel
    private fun generateTwinkle(): ShortArray {
        val notes = listOf(
            Pair(84, 0.25f), Pair(88, 0.25f), Pair(91, 0.25f), Pair(96, 0.45f),
            Pair(95, 0.25f), Pair(91, 0.25f), Pair(88, 0.25f), Pair(84, 0.60f)
        )
        return renderBell(notes)
    }

    // 10. Cosmic (Космос) - Ethereal ambient pad
    private fun generateCosmic(): ShortArray {
        val totalSec = 3.6f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        val chords = listOf(
            listOf(60, 67, 72),
            listOf(62, 69, 74)
        )

        var curSample = 0
        val chordDur = 1.8f
        val chordLen = (chordDur * SAMPLE_RATE).toInt()

        for (chord in chords) {
            for (midi in chord) {
                val freq = midiToFreq(midi)
                for (n in 0 until chordLen) {
                    val idx = curSample + n
                    if (idx < numSamples) {
                        val t = n.toFloat() / SAMPLE_RATE
                        val env = sin(PI * (n.toFloat() / chordLen)).pow(1.5).toFloat()
                        val tone = sin(2.0 * PI * freq * t) + 0.2 * sin(2.0 * PI * (freq * 1.003) * t)
                        val sample = (tone * env * 0.12f * Short.MAX_VALUE).toInt()
                        val cur = buffer[idx]
                        buffer[idx] = (cur + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
            }
            curSample += chordLen
        }
        return buffer
    }

    // --- Acoustic Renderers ---

    // Marimba acoustic synthesizer: fundamental + wooden strike overtone
    private fun renderMarimba(notes: List<Pair<Int, Float>>): ShortArray {
        val totalSec = notes.sumOf { it.second.toDouble() }.toFloat() + 0.4f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        var curSample = 0
        for ((midi, dur) in notes) {
            val len = (dur * SAMPLE_RATE).toInt()
            val freq = midiToFreq(midi)
            val ringLen = minOf(numSamples - curSample, (dur * 1.6f * SAMPLE_RATE).toInt())
            for (n in 0 until ringLen) {
                val idx = curSample + n
                if (idx < numSamples) {
                    val t = n.toFloat() / SAMPLE_RATE
                    val env = exp(-9.0f * (n.toFloat() / len))
                    // 1st + 4th harmonic characteristic of marimba bars
                    val tone = sin(2.0 * PI * freq * t) + 0.25 * sin(2.0 * PI * freq * 3.98 * t)
                    val sample = (tone * env * 0.40 * Short.MAX_VALUE).toInt()
                    val cur = buffer[idx]
                    buffer[idx] = (cur + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            curSample += len
        }
        return buffer
    }

    // Bell / Glockenspiel synthesizer
    private fun renderBell(notes: List<Pair<Int, Float>>): ShortArray {
        val totalSec = notes.sumOf { it.second.toDouble() }.toFloat() + 0.6f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        var curSample = 0
        for ((midi, dur) in notes) {
            val len = (dur * SAMPLE_RATE).toInt()
            val freq = midiToFreq(midi)
            val decayLen = minOf(numSamples - curSample, (dur * 2.2f * SAMPLE_RATE).toInt())
            for (n in 0 until decayLen) {
                val idx = curSample + n
                if (idx < numSamples) {
                    val t = n.toFloat() / SAMPLE_RATE
                    val env = exp(-3.5f * (n.toFloat() / len))
                    val tone = sin(2.0 * PI * freq * t) * 0.65 + sin(2.0 * PI * freq * 2.76 * t) * 0.25 + sin(2.0 * PI * freq * 5.4 * t) * 0.1
                    val sample = (tone * env * 0.38 * Short.MAX_VALUE).toInt()
                    val cur = buffer[idx]
                    buffer[idx] = (cur + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            curSample += len
        }
        return buffer
    }

    // Vibraphone synthesizer with subtle tremolo
    private fun renderVibraphone(notes: List<Pair<Int, Float>>): ShortArray {
        val totalSec = notes.sumOf { it.second.toDouble() }.toFloat() + 0.5f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        var curSample = 0
        for ((midi, dur) in notes) {
            val len = (dur * SAMPLE_RATE).toInt()
            val freq = midiToFreq(midi)
            val ringLen = minOf(numSamples - curSample, (dur * 2.0f * SAMPLE_RATE).toInt())
            for (n in 0 until ringLen) {
                val idx = curSample + n
                if (idx < numSamples) {
                    val t = n.toFloat() / SAMPLE_RATE
                    val lfo = 1.0f + 0.15f * sin(2.0 * PI * 5.5 * t).toFloat()
                    val env = exp(-4.2f * (n.toFloat() / len)) * lfo
                    val tone = sin(2.0 * PI * freq * t) * 0.7 + sin(2.0 * PI * freq * 2.0 * t) * 0.2
                    val sample = (tone * env * 0.38 * Short.MAX_VALUE).toInt()
                    val cur = buffer[idx]
                    buffer[idx] = (cur + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            curSample += len
        }
        return buffer
    }

    // --- Nature & Classical Sounds ---

    private fun generateBirds(): ShortArray {
        val durationSec = 3.2f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val rand = Random(42)

        for (k in 0 until 5) {
            val chirpStart = ((0.35 * k + rand.nextFloat() * 0.2f) * SAMPLE_RATE).toInt()
            val baseFreq = 2600f + rand.nextFloat() * 1400f
            for (j in 0 until 3) {
                val subStart = chirpStart + (j * 0.09f * SAMPLE_RATE).toInt()
                val subLen = (0.09f * SAMPLE_RATE).toInt()
                for (n in 0 until subLen) {
                    val idx = subStart + n
                    if (idx in 0 until numSamples) {
                        val t = n.toFloat() / SAMPLE_RATE
                        val freq = baseFreq * (1f + 0.4f * (n.toFloat() / subLen))
                        val env = (sin(PI.toFloat() * (n.toFloat() / subLen))).pow(2f)
                        val sample = (sin(2.0 * PI * freq * t) * env * 0.4f * Short.MAX_VALUE).toInt()
                        val current = buffer[idx]
                        buffer[idx] = (current + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
            }
        }
        return buffer
    }

    private fun generateRain(): ShortArray {
        val durationSec = 4f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val rand = Random(123)

        var lastSample = 0f
        for (i in 0 until numSamples) {
            val white = (rand.nextFloat() * 2f - 1f)
            val hp = white - lastSample * 0.6f
            lastSample = white
            buffer[i] = (hp * 0.28f * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateOcean(): ShortArray {
        val durationSec = 5f
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val rand = Random(999)

        var smooth = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val lfo = 0.35f + 0.35f * sin(2.0 * PI * 0.15 * t).toFloat()
            val white = (rand.nextFloat() * 2f - 1f)
            smooth = smooth * 0.94f + white * 0.06f
            val sample = (smooth * lfo * 0.8f * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateFürElise(): ShortArray {
        val notes = listOf(
            Pair(76, 0.4f), Pair(75, 0.4f), Pair(76, 0.4f), Pair(75, 0.4f),
            Pair(76, 0.4f), Pair(71, 0.4f), Pair(74, 0.4f), Pair(72, 0.4f),
            Pair(69, 0.8f), Pair(0, 0.2f), Pair(60, 0.4f), Pair(64, 0.4f),
            Pair(69, 0.4f), Pair(71, 0.8f), Pair(0, 0.2f), Pair(64, 0.4f),
            Pair(68, 0.4f), Pair(71, 0.4f), Pair(72, 1.0f)
        )
        return renderPiano(notes)
    }

    private fun generateOdeToJoy(): ShortArray {
        val notes = listOf(
            Pair(64, 0.45f), Pair(64, 0.45f), Pair(65, 0.45f), Pair(67, 0.45f),
            Pair(67, 0.45f), Pair(65, 0.45f), Pair(64, 0.45f), Pair(62, 0.45f),
            Pair(60, 0.45f), Pair(60, 0.45f), Pair(62, 0.45f), Pair(64, 0.45f),
            Pair(64, 0.65f), Pair(62, 0.25f), Pair(62, 0.8f)
        )
        return renderPiano(notes)
    }

    private fun generateNeonPulse(): ShortArray {
        val midiNotes = listOf(57, 60, 64, 69, 64, 60, 57, 60, 59, 62, 67, 71, 67, 62, 59, 62)
        val noteDur = 0.18f
        val numSamples = (SAMPLE_RATE * noteDur * midiNotes.size).toInt()
        val buffer = ShortArray(numSamples)

        midiNotes.forEachIndexed { idx, midi ->
            val freq = midiToFreq(midi)
            val start = (idx * noteDur * SAMPLE_RATE).toInt()
            val len = (noteDur * SAMPLE_RATE).toInt()

            for (n in 0 until len) {
                val bufIdx = start + n
                if (bufIdx < numSamples) {
                    val t = n.toFloat() / SAMPLE_RATE
                    val phase = (t * freq) % 1.0f
                    val saw = (2.0f * phase - 1.0f)
                    val env = exp(-4.0f * (n.toFloat() / len))
                    val sample = (saw * env * 0.35f * Short.MAX_VALUE).toInt()
                    buffer[bufIdx] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return buffer
    }

    private fun generateCrystalBells(): ShortArray {
        val notes = listOf(
            Pair(72, 0.5f), Pair(76, 0.5f), Pair(79, 0.5f), Pair(84, 0.8f),
            Pair(79, 0.5f), Pair(76, 0.5f), Pair(72, 1.2f)
        )
        return renderBell(notes)
    }

    private fun generateClassicBeep(): ShortArray {
        val beeps = listOf(
            Pair(88, 0.15f), Pair(0, 0.08f),
            Pair(88, 0.15f), Pair(0, 0.08f),
            Pair(88, 0.15f), Pair(0, 0.08f),
            Pair(88, 0.25f), Pair(0, 0.5f)
        )
        val totalSec = beeps.sumOf { it.second.toDouble() }.toFloat()
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        var curSample = 0
        for ((midi, dur) in beeps) {
            val len = (dur * SAMPLE_RATE).toInt()
            if (midi > 0) {
                val freq = midiToFreq(midi)
                for (n in 0 until len) {
                    val bufIdx = curSample + n
                    if (bufIdx < numSamples) {
                        val t = n.toFloat() / SAMPLE_RATE
                        val square = if (sin(2.0 * PI * freq * t) >= 0) 1f else -1f
                        val sample = (square * 0.22f * Short.MAX_VALUE).toInt()
                        buffer[bufIdx] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
            }
            curSample += len
        }
        return buffer
    }

    private fun renderPiano(notes: List<Pair<Int, Float>>): ShortArray {
        val totalSec = notes.sumOf { it.second.toDouble() }.toFloat() + 0.5f
        val numSamples = (SAMPLE_RATE * totalSec).toInt()
        val buffer = ShortArray(numSamples)

        var curSample = 0
        for ((midi, dur) in notes) {
            val len = (dur * SAMPLE_RATE).toInt()
            if (midi > 0) {
                val freq = midiToFreq(midi)
                val decayLen = minOf(numSamples - curSample, (dur * 1.5f * SAMPLE_RATE).toInt())
                for (n in 0 until decayLen) {
                    val bufIdx = curSample + n
                    if (bufIdx < numSamples) {
                        val t = n.toFloat() / SAMPLE_RATE
                        val env = exp(-3.0f * (n.toFloat() / len))
                        val tone = (sin(2.0 * PI * freq * t) * 0.7 + sin(2.0 * PI * freq * 2.0 * t) * 0.15)
                        val sample = (tone * env * 0.35f * Short.MAX_VALUE).toInt()
                        val current = buffer[bufIdx]
                        buffer[bufIdx] = (current + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }
            }
            curSample += len
        }
        return buffer
    }
}
