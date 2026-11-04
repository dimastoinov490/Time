package com.neo.clock.audio

import com.neo.clock.audio.ProceduralTimbres.NOTE_A3
import com.neo.clock.audio.ProceduralTimbres.NOTE_A4
import com.neo.clock.audio.ProceduralTimbres.NOTE_A5
import com.neo.clock.audio.ProceduralTimbres.NOTE_AS4
import com.neo.clock.audio.ProceduralTimbres.NOTE_B3
import com.neo.clock.audio.ProceduralTimbres.NOTE_B4
import com.neo.clock.audio.ProceduralTimbres.NOTE_B5
import com.neo.clock.audio.ProceduralTimbres.NOTE_C3
import com.neo.clock.audio.ProceduralTimbres.NOTE_C4
import com.neo.clock.audio.ProceduralTimbres.NOTE_C5
import com.neo.clock.audio.ProceduralTimbres.NOTE_C6
import com.neo.clock.audio.ProceduralTimbres.NOTE_CS3
import com.neo.clock.audio.ProceduralTimbres.NOTE_CS4
import com.neo.clock.audio.ProceduralTimbres.NOTE_CS5
import com.neo.clock.audio.ProceduralTimbres.NOTE_CS6
import com.neo.clock.audio.ProceduralTimbres.NOTE_D3
import com.neo.clock.audio.ProceduralTimbres.NOTE_D4
import com.neo.clock.audio.ProceduralTimbres.NOTE_D5
import com.neo.clock.audio.ProceduralTimbres.NOTE_D6
import com.neo.clock.audio.ProceduralTimbres.NOTE_DS4
import com.neo.clock.audio.ProceduralTimbres.NOTE_DS5
import com.neo.clock.audio.ProceduralTimbres.NOTE_E3
import com.neo.clock.audio.ProceduralTimbres.NOTE_E4
import com.neo.clock.audio.ProceduralTimbres.NOTE_E5
import com.neo.clock.audio.ProceduralTimbres.NOTE_E6
import com.neo.clock.audio.ProceduralTimbres.NOTE_F3
import com.neo.clock.audio.ProceduralTimbres.NOTE_F4
import com.neo.clock.audio.ProceduralTimbres.NOTE_F5
import com.neo.clock.audio.ProceduralTimbres.NOTE_FS3
import com.neo.clock.audio.ProceduralTimbres.NOTE_FS4
import com.neo.clock.audio.ProceduralTimbres.NOTE_FS5
import com.neo.clock.audio.ProceduralTimbres.NOTE_G3
import com.neo.clock.audio.ProceduralTimbres.NOTE_G4
import com.neo.clock.audio.ProceduralTimbres.NOTE_G5
import com.neo.clock.audio.ProceduralTimbres.NOTE_G6
import com.neo.clock.audio.ProceduralTimbres.NOTE_GS3
import com.neo.clock.audio.ProceduralTimbres.NOTE_GS4
import com.neo.clock.audio.ProceduralTimbres.NOTE_GS5

object BuiltInMelodies {

    val LIST = listOf(
        // Мягкие/плавные (нарастающие)
        SoundItem("builtin:dawn", "Рассвет", "Мягкие", SoundSource.BUILT_IN),
        SoundItem("builtin:gentle_breeze", "Лёгкий бриз", "Мягкие", SoundSource.BUILT_IN),
        SoundItem("builtin:quiet_morning", "Тихое утро", "Мягкие", SoundSource.BUILT_IN),
        SoundItem("builtin:mist", "Дымка", "Мягкие", SoundSource.BUILT_IN),
        SoundItem("builtin:glow", "Свечение", "Мягкие", SoundSource.BUILT_IN),
        SoundItem("builtin:soar", "Парение", "Мягкие", SoundSource.BUILT_IN),
        SoundItem("builtin:clouds", "Облака", "Мягкие", SoundSource.BUILT_IN),
        SoundItem("builtin:silk", "Шёлк", "Мягкие", SoundSource.BUILT_IN),

        // Мелодичные/музыкальные
        SoundItem("builtin:marimba", "Маримба", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:celeste", "Челеста", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:crystal", "Кристалл", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:shimmer", "Мерцание", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:city_lights", "Огни города", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:raindrops", "Капли", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:chimes", "Хрусталь", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:harp", "Арфа", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:music_box", "Музыкальная шкатулка", "Мелодичные", SoundSource.BUILT_IN),
        SoundItem("builtin:vibraphone", "Вибрафон", "Мелодичные", SoundSource.BUILT_IN),

        // Бодрые/современные
        SoundItem("builtin:radar", "Радар", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:pulse", "Пульс", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:wave", "Волна", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:neon", "Неон", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:arcade", "Аркада", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:synthwave", "Синтвейв", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:energy", "Энергия", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:start", "Старт", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:impulse", "Импульс", "Бодрые", SoundSource.BUILT_IN),
        SoundItem("builtin:rhythm", "Ритм", "Бодрые", SoundSource.BUILT_IN),

        // Природа
        SoundItem("builtin:forest_birds", "Утренний лес", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:rain", "Дождь", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:distant_thunder", "Гроза вдали", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:ocean", "Океан", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:stream", "Ручей", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:wind", "Ветер", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:campfire", "Костёр", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:cicadas", "Цикады", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:night_forest", "Ночной лес", "Природа", SoundSource.BUILT_IN),
        SoundItem("builtin:mountain_air", "Горный воздух", "Природа", SoundSource.BUILT_IN),

        // Классика (общественное достояние)
        SoundItem("builtin:fur_elise", "Für Elise", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:ode_to_joy", "Ода к радости", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:moonlight_sonata", "Лунная соната", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:pachelbel_canon", "Канон Пахельбеля", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:turkish_march", "Турецкий марш", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:gymnopedie_1", "Гимнопедия №1", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:morning_mood", "Утро (Григ)", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:swan_lake", "Лебединое озеро", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:spring_vivaldi", "Весна (Вивальди)", "Классика", SoundSource.BUILT_IN),
        SoundItem("builtin:eine_kleine", "Маленькая ночная серенада", "Классика", SoundSource.BUILT_IN),

        // Классические сигналы
        SoundItem("builtin:classic_alarm", "Классический будильник", "Сигналы", SoundSource.BUILT_IN),
        SoundItem("builtin:digital_beep", "Электронный писк", "Сигналы", SoundSource.BUILT_IN),
        SoundItem("builtin:bell_chime", "Колокольчик", "Сигналы", SoundSource.BUILT_IN),
        SoundItem("builtin:gong", "Гонг", "Сигналы", SoundSource.BUILT_IN)
    )

    fun getTitle(soundId: String): String {
        val cleanId = soundId.removePrefix("builtin:")
        val item = LIST.find { it.id == soundId || it.id.removePrefix("builtin:") == cleanId }
        return item?.title ?: "Звук"
    }

    fun generatePcm(soundId: String): ShortArray {
        val key = soundId.removePrefix("builtin:")
        return when (key) {
            // Мягкие/плавные
            "dawn" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_C4 to 0.4f, NOTE_E4 to 0.4f, NOTE_G4 to 0.4f, NOTE_B4 to 0.4f, NOTE_C5 to 0.8f, 0f to 0.3f),
                ProceduralTimbres::softPadSample, delayMs = 220, delayFeedback = 0.32f
            )
            "gentle_breeze" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_D4 to 0.35f, NOTE_FS4 to 0.35f, NOTE_A4 to 0.35f, NOTE_CS5 to 0.45f, NOTE_B4 to 0.7f, 0f to 0.3f),
                ProceduralTimbres::softPadSample, delayMs = 200, delayFeedback = 0.3f
            )
            "quiet_morning" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_E4 to 0.4f, NOTE_GS4 to 0.4f, NOTE_B4 to 0.5f, NOTE_E5 to 0.8f, 0f to 0.4f),
                ProceduralTimbres::softPadSample, delayMs = 250, delayFeedback = 0.35f
            )
            "mist" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_F4 to 0.45f, NOTE_A4 to 0.45f, NOTE_C5 to 0.45f, NOTE_E5 to 0.8f, 0f to 0.3f),
                ProceduralTimbres::softPadSample, delayMs = 240, delayFeedback = 0.32f
            )
            "glow" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_G4 to 0.35f, NOTE_B4 to 0.35f, NOTE_D5 to 0.4f, NOTE_FS5 to 0.45f, NOTE_G5 to 0.7f, 0f to 0.3f),
                ProceduralTimbres::softPadSample, delayMs = 210, delayFeedback = 0.3f
            )
            "soar" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_A4 to 0.3f, NOTE_CS5 to 0.3f, NOTE_E5 to 0.35f, NOTE_A5 to 0.75f, 0f to 0.35f),
                ProceduralTimbres::softPadSample, delayMs = 220, delayFeedback = 0.33f
            )
            "clouds" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_C4 to 0.45f, NOTE_F4 to 0.45f, NOTE_A4 to 0.5f, NOTE_C5 to 0.85f, 0f to 0.3f),
                ProceduralTimbres::softPadSample, delayMs = 260, delayFeedback = 0.35f
            )
            "silk" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_D4 to 0.35f, NOTE_G4 to 0.35f, NOTE_B4 to 0.4f, NOTE_D5 to 0.4f, NOTE_E5 to 0.75f, 0f to 0.3f),
                ProceduralTimbres::softPadSample, delayMs = 230, delayFeedback = 0.32f
            )

            // Мелодичные/музыкальные
            "marimba" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_G4 to 0.22f, NOTE_C5 to 0.22f, NOTE_E5 to 0.22f, NOTE_D5 to 0.22f, NOTE_G5 to 0.55f, 0f to 0.3f),
                ProceduralTimbres::marimbaSample, delayMs = 150, delayFeedback = 0.22f
            )
            "celeste" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_E5 to 0.25f, NOTE_G5 to 0.25f, NOTE_B5 to 0.25f, NOTE_C6 to 0.6f, 0f to 0.3f),
                ProceduralTimbres::celestaSample, delayMs = 180, delayFeedback = 0.28f
            )
            "crystal" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_FS5 to 0.25f, NOTE_A5 to 0.25f, NOTE_CS6 to 0.3f, NOTE_E6 to 0.65f, 0f to 0.35f),
                ProceduralTimbres::bellSample, delayMs = 200, delayFeedback = 0.3f
            )
            "shimmer" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_D5 to 0.2f, NOTE_FS5 to 0.2f, NOTE_A5 to 0.25f, NOTE_D6 to 0.6f, 0f to 0.3f),
                ProceduralTimbres::celestaSample, delayMs = 170, delayFeedback = 0.28f
            )
            "city_lights" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_C4 to 0.25f, NOTE_E4 to 0.25f, NOTE_G4 to 0.25f, NOTE_B4 to 0.35f, NOTE_A4 to 0.6f, 0f to 0.3f),
                ProceduralTimbres::marimbaSample, delayMs = 160, delayFeedback = 0.24f
            )
            "raindrops" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_G5 to 0.18f, NOTE_E5 to 0.18f, NOTE_C5 to 0.22f, NOTE_D5 to 0.22f, NOTE_G5 to 0.55f, 0f to 0.3f),
                ProceduralTimbres::celestaSample, delayMs = 140, delayFeedback = 0.25f
            )
            "chimes" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_B4 to 0.28f, NOTE_DS5 to 0.28f, NOTE_FS5 to 0.3f, NOTE_B5 to 0.65f, 0f to 0.35f),
                ProceduralTimbres::bellSample, delayMs = 220, delayFeedback = 0.32f
            )
            "harp" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_C4 to 0.18f, NOTE_D4 to 0.18f, NOTE_E4 to 0.18f, NOTE_G4 to 0.2f, NOTE_A4 to 0.22f, NOTE_C5 to 0.6f, 0f to 0.3f),
                ProceduralTimbres::harpSample, delayMs = 190, delayFeedback = 0.26f
            )
            "music_box" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_E5 to 0.22f, NOTE_D5 to 0.22f, NOTE_C5 to 0.22f, NOTE_D5 to 0.22f, NOTE_E5 to 0.22f, NOTE_E5 to 0.22f, NOTE_E5 to 0.5f, 0f to 0.3f),
                ProceduralTimbres::celestaSample, delayMs = 160, delayFeedback = 0.25f
            )
            "vibraphone" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_F4 to 0.3f, NOTE_A4 to 0.3f, NOTE_C5 to 0.3f, NOTE_E5 to 0.35f, NOTE_D5 to 0.65f, 0f to 0.3f),
                ProceduralTimbres::marimbaSample, delayMs = 180, delayFeedback = 0.28f
            )

            // Бодрые/современные
            "radar" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_E5 to 0.12f, NOTE_GS5 to 0.12f, NOTE_B5 to 0.25f, 0f to 0.2f, NOTE_E5 to 0.12f, NOTE_GS5 to 0.12f, NOTE_B5 to 0.35f, 0f to 0.4f),
                ProceduralTimbres::modernSynthSample, delayMs = 120, delayFeedback = 0.2f
            )
            "pulse" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_C5 to 0.14f, NOTE_C5 to 0.14f, NOTE_E5 to 0.14f, NOTE_G5 to 0.28f, 0f to 0.15f, NOTE_G5 to 0.14f, NOTE_C6 to 0.35f, 0f to 0.35f),
                ProceduralTimbres::modernSynthSample, delayMs = 110, delayFeedback = 0.18f
            )
            "wave" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_A4 to 0.18f, NOTE_E5 to 0.18f, NOTE_G5 to 0.22f, NOTE_A5 to 0.55f, 0f to 0.3f),
                ProceduralTimbres::modernSynthSample, delayMs = 140, delayFeedback = 0.22f
            )
            "neon" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_D4 to 0.15f, NOTE_F4 to 0.15f, NOTE_A4 to 0.15f, NOTE_D5 to 0.2f, NOTE_F5 to 0.45f, 0f to 0.3f),
                ProceduralTimbres::modernSynthSample, delayMs = 130, delayFeedback = 0.24f
            )
            "arcade" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_C5 to 0.12f, NOTE_E5 to 0.12f, NOTE_G5 to 0.12f, NOTE_C6 to 0.2f, NOTE_G5 to 0.15f, NOTE_E5 to 0.35f, 0f to 0.3f),
                ProceduralTimbres::modernSynthSample, delayMs = 100, delayFeedback = 0.16f
            )
            "synthwave" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_F4 to 0.2f, NOTE_C5 to 0.2f, NOTE_A4 to 0.2f, NOTE_E5 to 0.5f, 0f to 0.3f),
                ProceduralTimbres::modernSynthSample, delayMs = 150, delayFeedback = 0.25f
            )
            "energy" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_G4 to 0.16f, NOTE_B4 to 0.16f, NOTE_D5 to 0.18f, NOTE_G5 to 0.22f, NOTE_E5 to 0.45f, 0f to 0.3f),
                ProceduralTimbres::modernSynthSample, delayMs = 120, delayFeedback = 0.2f
            )
            "start" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_C5 to 0.15f, NOTE_E5 to 0.15f, NOTE_G5 to 0.18f, NOTE_C6 to 0.6f, 0f to 0.35f),
                ProceduralTimbres::modernSynthSample, delayMs = 130, delayFeedback = 0.22f
            )
            "impulse" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_E5 to 0.12f, NOTE_B5 to 0.15f, NOTE_E5 to 0.12f, NOTE_GS5 to 0.45f, 0f to 0.3f),
                ProceduralTimbres::modernSynthSample, delayMs = 110, delayFeedback = 0.18f
            )
            "rhythm" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_A4 to 0.15f, NOTE_C5 to 0.15f, NOTE_D5 to 0.18f, NOTE_E5 to 0.18f, NOTE_G5 to 0.45f, 0f to 0.3f),
                ProceduralTimbres::modernSynthSample, delayMs = 125, delayFeedback = 0.2f
            )

            // Природа
            "forest_birds" -> ProceduralTimbres.renderForestBirds()
            "rain" -> ProceduralTimbres.renderRain()
            "distant_thunder" -> ProceduralTimbres.renderDistantThunder()
            "ocean" -> ProceduralTimbres.renderOcean()
            "stream" -> ProceduralTimbres.renderStream()
            "wind" -> ProceduralTimbres.renderWind()
            "campfire" -> ProceduralTimbres.renderCampfire()
            "cicadas" -> ProceduralTimbres.renderCicadas()
            "night_forest" -> ProceduralTimbres.renderNightForest()
            "mountain_air" -> ProceduralTimbres.renderMountainAir()

            // Классика (общественное достояние)
            "fur_elise" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_E5 to 0.24f, NOTE_DS5 to 0.24f, NOTE_E5 to 0.24f, NOTE_DS5 to 0.24f,
                    NOTE_E5 to 0.24f, NOTE_B4 to 0.24f, NOTE_D5 to 0.24f, NOTE_C5 to 0.24f,
                    NOTE_A4 to 0.7f, 0f to 0.35f
                ),
                ProceduralTimbres::celestaSample, delayMs = 160, delayFeedback = 0.26f
            )
            "ode_to_joy" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_E4 to 0.28f, NOTE_E4 to 0.28f, NOTE_F4 to 0.28f, NOTE_G4 to 0.28f,
                    NOTE_G4 to 0.28f, NOTE_F4 to 0.28f, NOTE_E4 to 0.28f, NOTE_D4 to 0.28f,
                    NOTE_C4 to 0.28f, NOTE_C4 to 0.28f, NOTE_D4 to 0.28f, NOTE_E4 to 0.42f,
                    NOTE_D4 to 0.2f, NOTE_D4 to 0.5f, 0f to 0.3f
                ),
                ProceduralTimbres::celestaSample, delayMs = 170, delayFeedback = 0.24f
            )
            "moonlight_sonata" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_GS3 to 0.28f, NOTE_CS4 to 0.28f, NOTE_E4 to 0.28f,
                    NOTE_GS3 to 0.28f, NOTE_CS4 to 0.28f, NOTE_E4 to 0.28f,
                    NOTE_A3 to 0.28f, NOTE_CS4 to 0.28f, NOTE_E4 to 0.28f,
                    NOTE_GS3 to 0.28f, NOTE_CS4 to 0.28f, NOTE_DS4 to 0.7f, 0f to 0.35f
                ),
                ProceduralTimbres::harpSample, delayMs = 210, delayFeedback = 0.32f
            )
            "pachelbel_canon" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_FS4 to 0.35f, NOTE_E4 to 0.35f, NOTE_D4 to 0.35f, NOTE_CS4 to 0.35f,
                    NOTE_B3 to 0.35f, NOTE_A3 to 0.35f, NOTE_B3 to 0.35f, NOTE_CS4 to 0.65f, 0f to 0.35f
                ),
                ProceduralTimbres::harpSample, delayMs = 200, delayFeedback = 0.28f
            )
            "turkish_march" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_B4 to 0.18f, NOTE_A4 to 0.18f, NOTE_GS4 to 0.18f, NOTE_A4 to 0.18f,
                    NOTE_C5 to 0.35f, NOTE_D5 to 0.18f, NOTE_C5 to 0.18f, NOTE_B4 to 0.18f,
                    NOTE_C5 to 0.18f, NOTE_E5 to 0.6f, 0f to 0.3f
                ),
                ProceduralTimbres::celestaSample, delayMs = 140, delayFeedback = 0.22f
            )
            "gymnopedie_1" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_G4 to 0.45f, NOTE_FS4 to 0.45f, NOTE_G4 to 0.45f,
                    NOTE_A4 to 0.65f, NOTE_FS4 to 0.45f, NOTE_D4 to 0.85f, 0f to 0.4f
                ),
                ProceduralTimbres::softPadSample, delayMs = 240, delayFeedback = 0.35f
            )
            "morning_mood" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_G4 to 0.25f, NOTE_E4 to 0.25f, NOTE_D4 to 0.25f, NOTE_C4 to 0.25f,
                    NOTE_D4 to 0.25f, NOTE_E4 to 0.35f, NOTE_G4 to 0.25f, NOTE_E4 to 0.25f,
                    NOTE_D4 to 0.25f, NOTE_C4 to 0.25f, NOTE_D4 to 0.25f, NOTE_E4 to 0.65f, 0f to 0.35f
                ),
                ProceduralTimbres::marimbaSample, delayMs = 180, delayFeedback = 0.28f
            )
            "swan_lake" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_B4 to 0.35f, NOTE_E5 to 0.35f, NOTE_FS5 to 0.35f, NOTE_G5 to 0.35f,
                    NOTE_A5 to 0.35f, NOTE_B5 to 0.6f, NOTE_G5 to 0.35f, NOTE_E5 to 0.75f, 0f to 0.35f
                ),
                ProceduralTimbres::harpSample, delayMs = 210, delayFeedback = 0.3f
            )
            "spring_vivaldi" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_E5 to 0.3f, NOTE_GS5 to 0.18f, NOTE_GS5 to 0.18f, NOTE_GS5 to 0.18f,
                    NOTE_FS5 to 0.18f, NOTE_E5 to 0.28f, NOTE_B4 to 0.6f, 0f to 0.3f
                ),
                ProceduralTimbres::celestaSample, delayMs = 160, delayFeedback = 0.25f
            )
            "eine_kleine" -> ProceduralTimbres.renderMelody(
                listOf(
                    NOTE_G4 to 0.3f, NOTE_D4 to 0.2f, NOTE_G4 to 0.3f, NOTE_D4 to 0.2f,
                    NOTE_G4 to 0.2f, NOTE_B4 to 0.2f, NOTE_D5 to 0.65f, 0f to 0.3f
                ),
                ProceduralTimbres::celestaSample, delayMs = 150, delayFeedback = 0.24f
            )

            // Классические сигналы
            "classic_alarm" -> {
                val durSec = 3.5f
                val totalSamples = (durSec * ProceduralTimbres.SAMPLE_RATE).toInt()
                val out = FloatArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / ProceduralTimbres.SAMPLE_RATE
                    out[i] = ProceduralTimbres.twinBellAlarm(t)
                }
                ProceduralTimbres.normalize(out)
            }
            "digital_beep" -> {
                val durSec = 3.0f
                val totalSamples = (durSec * ProceduralTimbres.SAMPLE_RATE).toInt()
                val out = FloatArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / ProceduralTimbres.SAMPLE_RATE
                    out[i] = ProceduralTimbres.digitalBeepAlarm(t)
                }
                ProceduralTimbres.normalize(out)
            }
            "bell_chime" -> ProceduralTimbres.renderMelody(
                listOf(NOTE_E4 to 0.6f, NOTE_GS4 to 0.6f, NOTE_FS4 to 0.6f, NOTE_B3 to 1.1f, 0f to 0.5f),
                ProceduralTimbres::bellSample, delayMs = 260, delayFeedback = 0.35f
            )
            "gong" -> {
                val durSec = 4.0f
                val totalSamples = (durSec * ProceduralTimbres.SAMPLE_RATE).toInt()
                val out = FloatArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / ProceduralTimbres.SAMPLE_RATE
                    val env = kotlin.math.exp(-1.4 * t).toFloat()
                    val f = 110.0
                    val s1 = kotlin.math.sin(2.0 * kotlin.math.PI * f * t).toFloat()
                    val s2 = 0.7f * kotlin.math.sin(2.0 * kotlin.math.PI * (f * 1.58) * t).toFloat()
                    val s3 = 0.45f * kotlin.math.sin(2.0 * kotlin.math.PI * (f * 2.32) * t).toFloat()
                    val s4 = 0.3f * kotlin.math.sin(2.0 * kotlin.math.PI * (f * 3.41) * t).toFloat()
                    out[i] = (s1 + s2 + s3 + s4) * env
                }
                ProceduralTimbres.normalize(ProceduralTimbres.applyFeedbackDelay(out, 280, 0.38f))
            }

            // Fallback
            else -> ProceduralTimbres.renderMelody(
                listOf(NOTE_C5 to 0.3f, NOTE_E5 to 0.3f, NOTE_G5 to 0.6f, 0f to 0.3f),
                ProceduralTimbres::bellSample
            )
        }
    }
}
