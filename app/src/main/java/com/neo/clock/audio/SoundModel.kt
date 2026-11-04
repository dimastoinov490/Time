package com.neo.clock.audio

enum class SoundSource {
    BUILT_IN,
    DEVICE,
    CUSTOM
}

data class CustomSound(
    val uriString: String,
    val title: String,
    val addedAt: Long = System.currentTimeMillis()
)

data class SoundItem(
    val id: String,
    val title: String,
    val group: String,
    val source: SoundSource,
    val uriString: String? = null
)
