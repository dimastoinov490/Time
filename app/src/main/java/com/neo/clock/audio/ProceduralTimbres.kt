package com.neo.clock.audio

import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

object ProceduralTimbres {
    const val SAMPLE_RATE = 44100

    // Standard note frequencies in Hz
    val NOTE_C3 = 130.81f
    val NOTE_CS3 = 138.59f
    val NOTE_D3 = 146.83f
    val NOTE_DS3 = 155.56f
    val NOTE_E3 = 164.81f
    val NOTE_F3 = 174.61f
    val NOTE_FS3 = 185.00f
    val NOTE_G3 = 196.00f
    val NOTE_GS3 = 207.65f
    val NOTE_A3 = 220.00f
    val NOTE_AS3 = 233.08f
    val NOTE_B3 = 246.94f

    val NOTE_C4 = 261.63f
    val NOTE_CS4 = 277.18f
    val NOTE_D4 = 293.66f
    val NOTE_DS4 = 311.13f
    val NOTE_E4 = 329.63f
    val NOTE_F4 = 349.23f
    val NOTE_FS4 = 369.99f
    val NOTE_G4 = 392.00f
    val NOTE_GS4 = 415.30f
    val NOTE_A4 = 440.00f
    val NOTE_AS4 = 466.16f
    val NOTE_B4 = 493.88f

    val NOTE_C5 = 523.25f
    val NOTE_CS5 = 554.37f
    val NOTE_D5 = 587.33f
    val NOTE_DS5 = 622.25f
    val NOTE_E5 = 659.25f
    val NOTE_F5 = 698.46f
    val NOTE_FS5 = 739.99f
    val NOTE_G5 = 783.99f
    val NOTE_GS5 = 830.61f
    val NOTE_A5 = 880.00f
    val NOTE_AS5 = 932.33f
    val NOTE_B5 = 987.77f

    val NOTE_C6 = 1046.50f
    val NOTE_CS6 = 1108.73f
    val NOTE_D6 = 1174.66f
    val NOTE_E6 = 1318.51f
    val NOTE_G6 = 1567.98f

    /**
     * Applies ADSR envelope to a sample given current time t and total note duration.
     */
    fun adsr(
        t: Float,
        duration: Float,
        attack: Float = 0.02f,
        decay: Float = 0.08f,
        sustain: Float = 0.65f,
        release: Float = 0.12f
    ): Float {
        if (t < 0f || t > duration) return 0f
        return when {
            t < attack -> t / attack
            t < attack + decay -> 1f - (1f - sustain) * ((t - attack) / decay)
            t < duration - release -> sustain
            else -> {
                val relProgress = (t - (duration - release)) / release
                sustain * (1f - relProgress).coerceIn(0f, 1f)
            }
        }
    }

    /**
     * Bell/Crystal timbre with multiple overtone ratios and exponential decay.
     */
    fun bellSample(f: Float, t: Float, duration: Float): Float {
        val env = exp(-3.8 * (t / duration)).toFloat()
        val s1 = sin(2.0 * PI * f * t).toFloat()
        val s2 = 0.55f * sin(2.0 * PI * f * 2.756 * t).toFloat()
        val s3 = 0.32f * sin(2.0 * PI * f * 5.404 * t).toFloat()
        val s4 = 0.18f * sin(2.0 * PI * f * 8.932 * t).toFloat()
        return (s1 + s2 + s3 + s4) * env
    }

    /**
     * Warm Marimba/Xylophone timbre: acoustic wooden resonance with soft strike attack.
     */
    fun marimbaSample(f: Float, t: Float, duration: Float): Float {
        val env = exp(-7.5 * (t / duration)).toFloat()
        val strikeEnv = exp(-45.0 * t).toFloat()
        val s1 = sin(2.0 * PI * f * t).toFloat()
        val s2 = 0.42f * sin(2.0 * PI * f * 3.97 * t).toFloat()
        val s3 = 0.15f * sin(2.0 * PI * f * 9.2 * t).toFloat()
        val strike = strikeEnv * 0.3f
        return ((s1 + s2 + s3) * env + strike).coerceIn(-1.5f, 1.5f)
    }

    /**
     * Celesta / Music Box timbre: bright, glistening with gentle shimmering vibrato.
     */
    fun celestaSample(f: Float, t: Float, duration: Float): Float {
        val env = exp(-4.2 * (t / duration)).toFloat()
        val vibrato = 1f + 0.003f * sin(2.0 * PI * 5.5 * t).toFloat()
        val s1 = sin(2.0 * PI * (f * vibrato) * t).toFloat()
        val s2 = 0.48f * sin(2.0 * PI * (f * 2.002 * vibrato) * t).toFloat()
        val s3 = 0.28f * sin(2.0 * PI * (f * 3.01 * vibrato) * t).toFloat()
        return (s1 + s2 + s3) * env
    }

    /**
     * Plucked Harp / Acoustic String timbre.
     */
    fun harpSample(f: Float, t: Float, duration: Float): Float {
        val env = exp(-3.2 * (t / duration)).toFloat()
        val s1 = sin(2.0 * PI * f * t).toFloat()
        val s2 = 0.45f * sin(2.0 * PI * f * 2.0 * t).toFloat() * exp(-4.5 * (t / duration)).toFloat()
        val s3 = 0.25f * sin(2.0 * PI * f * 3.0 * t).toFloat() * exp(-6.0 * (t / duration)).toFloat()
        val s4 = 0.12f * sin(2.0 * PI * f * 4.0 * t).toFloat() * exp(-8.0 * (t / duration)).toFloat()
        return (s1 + s2 + s3 + s4) * env
    }

    /**
     * Smooth Ambient Pad timbre with gentle rising attack and rich detuned chorus.
     */
    fun softPadSample(f: Float, t: Float, duration: Float): Float {
        val env = adsr(t, duration, attack = 0.15f * duration, decay = 0.1f * duration, sustain = 0.85f, release = 0.2f * duration)
        val s1 = sin(2.0 * PI * f * t).toFloat()
        val s2 = 0.5f * sin(2.0 * PI * (f * 1.004f) * t).toFloat()
        val s3 = 0.4f * sin(2.0 * PI * (f * 0.996f) * t).toFloat()
        val s4 = 0.25f * sin(2.0 * PI * (f * 2.001f) * t).toFloat()
        return (s1 + s2 + s3 + s4) * env * 0.5f
    }

    /**
     * Modern Synth timbre with energetic saturated harmonics.
     */
    fun modernSynthSample(f: Float, t: Float, duration: Float): Float {
        val env = adsr(t, duration, attack = 0.015f, decay = 0.09f, sustain = 0.7f, release = 0.08f)
        val phase = (f * t) % 1.0f
        val saw = (2.0f * phase - 1.0f)
        val sub = 0.4f * sin(2.0 * PI * (f * 0.5) * t).toFloat()
        val filtered = (saw * 0.6f + sub)
        return filtered * env
    }

    /**
     * Classic Signal: Twin bell ringing at 800 Hz / 1200 Hz with 16 Hz strike rate.
     */
    fun twinBellAlarm(t: Float): Float {
        val strike = sin(2.0 * PI * 16.0 * t).toFloat().coerceAtLeast(0f).pow(3f)
        val b1 = sin(2.0 * PI * 820.0 * t).toFloat()
        val b2 = 0.7f * sin(2.0 * PI * 1230.0 * t).toFloat()
        return (b1 + b2) * strike
    }

    /**
     * Electronic digital beep (beep-beep-pause).
     */
    fun digitalBeepAlarm(t: Float): Float {
        val cycle = t % 1.0f
        return if ((cycle in 0.0f..0.12f) || (cycle in 0.22f..0.34f)) {
            val f = 1950.0
            (sin(2.0 * PI * f * t) + 0.35 * sin(2.0 * PI * (f * 2) * t)).toFloat()
        } else {
            0f
        }
    }

    /**
     * Reverberation / Feedback Delay line.
     */
    fun applyFeedbackDelay(samples: FloatArray, delayMs: Int = 180, feedback: Float = 0.28f): FloatArray {
        val delaySamples = (SAMPLE_RATE * delayMs / 1000).coerceAtLeast(1)
        val out = FloatArray(samples.size)
        for (i in samples.indices) {
            val delayed = if (i >= delaySamples) out[i - delaySamples] else 0f
            out[i] = samples[i] + delayed * feedback
        }
        return out
    }

    /**
     * Peak normalization to prevent clipping and ensure consistent perceived loudness.
     */
    fun normalize(samples: FloatArray, targetPeak: Float = 0.85f): ShortArray {
        var maxVal = 0f
        for (s in samples) {
            val a = abs(s)
            if (a > maxVal) maxVal = a
        }
        val scale = if (maxVal > 0.0001f) (targetPeak / maxVal) * 32767f else 32767f
        val result = ShortArray(samples.size)
        for (i in samples.indices) {
            result[i] = (samples[i] * scale).coerceIn(-32768f, 32767f).toInt().toShort()
        }
        return result
    }

    /**
     * Synthesize a note sequence using the given timbre function.
     */
    fun renderMelody(
        notes: List<Pair<Float, Float>>, // (Frequency, duration in seconds)
        timbre: (Float, Float, Float) -> Float,
        delayMs: Int = 180,
        delayFeedback: Float = 0.25f,
        targetPeak: Float = 0.85f
    ): ShortArray {
        val totalSec = notes.sumOf { it.second.toDouble() }.toFloat()
        val totalSamples = (totalSec * SAMPLE_RATE).toInt()
        val buffer = FloatArray(totalSamples)

        var sampleOffset = 0
        for ((freq, dur) in notes) {
            val noteSamples = (dur * SAMPLE_RATE).toInt()
            if (freq > 20f) {
                for (i in 0 until noteSamples) {
                    val idx = sampleOffset + i
                    if (idx < buffer.size) {
                        val t = i.toFloat() / SAMPLE_RATE
                        buffer[idx] += timbre(freq, t, dur)
                    }
                }
            }
            sampleOffset += noteSamples
        }

        val delayed = applyFeedbackDelay(buffer, delayMs, delayFeedback)
        return normalize(delayed, targetPeak)
    }

    /**
     * Nature sound: Bird song with randomized sweeping chirps and wind noise.
     */
    fun renderForestBirds(durationSec: Float = 4.0f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        val random = Random(42)

        // Generate gentle background breeze
        var noiseState = 0f
        for (i in 0 until totalSamples) {
            val white = (random.nextFloat() * 2f - 1f)
            noiseState = noiseState * 0.95f + white * 0.05f
            val breezeMod = 0.5f + 0.5f * sin(2.0 * PI * 0.25 * (i.toDouble() / SAMPLE_RATE)).toFloat()
            out[i] = noiseState * 0.12f * breezeMod
        }

        // Add 5 natural bird chirps
        val chirpTimes = listOf(0.4f, 1.1f, 1.9f, 2.7f, 3.4f)
        for (startTime in chirpTimes) {
            val startIdx = (startTime * SAMPLE_RATE).toInt()
            val chirpDur = 0.16f
            val chirpSamples = (chirpDur * SAMPLE_RATE).toInt()
            val baseFreq = 2400f + random.nextInt(600)
            for (i in 0 until chirpSamples) {
                val idx = startIdx + i
                if (idx < totalSamples) {
                    val t = i.toFloat() / SAMPLE_RATE
                    val freq = baseFreq + 1200f * sin(PI * (t / chirpDur)).toFloat()
                    val env = sin(PI * (t / chirpDur)).toFloat()
                    out[idx] += 0.55f * sin(2.0 * PI * freq * t).toFloat() * env
                }
            }
        }

        return normalize(applyFeedbackDelay(out, 120, 0.2f))
    }

    /**
     * Nature sound: Soothing Rain with filtered white noise.
     */
    fun renderRain(durationSec: Float = 4.0f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        val random = Random(123)

        var lpf1 = 0f
        var lpf2 = 0f
        for (i in 0 until totalSamples) {
            val white = (random.nextFloat() * 2f - 1f)
            lpf1 += 0.08f * (white - lpf1)
            lpf2 += 0.08f * (lpf1 - lpf2)

            // Occasional droplet transient
            val drop = if (random.nextFloat() < 0.0008f) (random.nextFloat() * 0.6f) else 0f
            out[i] = lpf2 * 0.8f + drop
        }
        return normalize(out)
    }

    /**
     * Nature sound: Distant Thunder with deep rumble swells.
     */
    fun renderDistantThunder(durationSec: Float = 4.5f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        val random = Random(789)

        var lp = 0f
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val white = (random.nextFloat() * 2f - 1f)
            lp += 0.012f * (white - lp) // Very deep low-pass (<80Hz)
            val swell = sin(PI * (t / durationSec)).toFloat().pow(1.8f)
            val rumble = sin(2.0 * PI * (48.0 + 12.0 * sin(2.0 * PI * 0.5 * t)) * t).toFloat() * 0.4f
            out[i] = (lp * 2.2f + rumble) * swell
        }
        return normalize(out)
    }

    /**
     * Nature sound: Ocean Waves with rhythmic swell.
     */
    fun renderOcean(durationSec: Float = 4.5f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        val random = Random(456)

        var brownNoise = 0f
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val white = (random.nextFloat() * 2f - 1f)
            brownNoise = (brownNoise + 0.02f * white).coerceIn(-1f, 1f)
            val waveSwell = 0.35f + 0.65f * (0.5f + 0.5f * sin(2.0 * PI * 0.3 * t)).toFloat().pow(2f)
            out[i] = brownNoise * waveSwell
        }
        return normalize(out)
    }

    /**
     * Nature sound: Flowing Stream / Brook with gentle bubbling.
     */
    fun renderStream(durationSec: Float = 4.0f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        val random = Random(321)

        var lp = 0f
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val white = (random.nextFloat() * 2f - 1f)
            lp += 0.06f * (white - lp)
            val bubble = if (random.nextFloat() < 0.002f) {
                sin(2.0 * PI * (600.0 + random.nextInt(800)) * t).toFloat() * 0.35f
            } else 0f
            out[i] = lp * 0.7f + bubble
        }
        return normalize(out)
    }

    /**
     * Nature sound: Wind breeze with harmonic resonance.
     */
    fun renderWind(durationSec: Float = 4.0f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        val random = Random(654)

        var lp = 0f
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val white = (random.nextFloat() * 2f - 1f)
            lp += 0.035f * (white - lp)
            val gust = 0.4f + 0.6f * sin(2.0 * PI * 0.4 * t).toFloat().pow(2f)
            val whistle = 0.15f * sin(2.0 * PI * (320.0 + 80.0 * sin(2.0 * PI * 0.25 * t)) * t).toFloat()
            out[i] = (lp * 1.5f + whistle) * gust
        }
        return normalize(out)
    }

    /**
     * Nature sound: Campfire with warm low hum and random crackling pops.
     */
    fun renderCampfire(durationSec: Float = 4.0f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        val random = Random(987)

        var lp = 0f
        for (i in 0 until totalSamples) {
            val white = (random.nextFloat() * 2f - 1f)
            lp += 0.02f * (white - lp)
            val crackle = if (random.nextFloat() < 0.0012f) {
                (random.nextFloat() * 1.8f - 0.9f)
            } else 0f
            out[i] = lp * 0.6f + crackle
        }
        return normalize(out)
    }

    /**
     * Nature sound: Evening Cicadas.
     */
    fun renderCicadas(durationSec: Float = 4.0f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val carrier = sin(2.0 * PI * 4600.0 * t).toFloat()
            val mod = (0.5f + 0.5f * sin(2.0 * PI * 18.0 * t).toFloat()).pow(2f)
            val slowSwell = 0.4f + 0.6f * sin(2.0 * PI * 0.5 * t).toFloat().pow(2f)
            out[i] = carrier * mod * slowSwell
        }
        return normalize(out)
    }

    /**
     * Nature sound: Night Forest with soft crickets and distant breeze.
     */
    fun renderNightForest(durationSec: Float = 4.0f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        val random = Random(852)
        var lp = 0f
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val white = (random.nextFloat() * 2f - 1f)
            lp += 0.025f * (white - lp)
            val cricket = 0.25f * sin(2.0 * PI * 4200.0 * t).toFloat() *
                    (0.5f + 0.5f * sin(2.0 * PI * 14.0 * t).toFloat()).pow(3f)
            out[i] = lp * 0.35f + cricket
        }
        return normalize(out)
    }

    /**
     * Nature sound: Mountain Air with deep spacious openness.
     */
    fun renderMountainAir(durationSec: Float = 4.0f): ShortArray {
        val totalSamples = (durationSec * SAMPLE_RATE).toInt()
        val out = FloatArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val tone1 = sin(2.0 * PI * 220.0 * t).toFloat() * 0.3f
            val tone2 = sin(2.0 * PI * 329.63 * t).toFloat() * 0.25f
            val tone3 = sin(2.0 * PI * 440.0 * t).toFloat() * 0.2f
            val breathe = (0.5f + 0.5f * sin(2.0 * PI * 0.2 * t).toFloat())
            out[i] = (tone1 + tone2 + tone3) * breathe
        }
        return normalize(applyFeedbackDelay(out, 240, 0.35f))
    }
}
