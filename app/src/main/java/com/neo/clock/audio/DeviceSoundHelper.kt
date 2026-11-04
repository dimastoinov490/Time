package com.neo.clock.audio

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log

object DeviceSoundHelper {
    private const val TAG = "DeviceSoundHelper"

    /**
     * Queries device system sounds (alarms, ringtones, notifications)
     * e.g. Xiaomi HyperOS system tones, Samsung/Pixel system sounds.
     */
    fun getDeviceSounds(context: Context): List<SoundItem> {
        val sounds = mutableListOf<SoundItem>()
        val seenUris = mutableSetOf<String>()

        val types = listOf(
            RingtoneManager.TYPE_ALARM to "Будильники",
            RingtoneManager.TYPE_RINGTONE to "Рингтоны",
            RingtoneManager.TYPE_NOTIFICATION to "Уведомления"
        )

        for ((type, groupName) in types) {
            try {
                val manager = RingtoneManager(context).apply {
                    setType(type)
                }
                val cursor = manager.cursor
                while (cursor != null && cursor.moveToNext()) {
                    val uri = manager.getRingtoneUri(cursor.position)
                    if (uri != null) {
                        val uriStr = uri.toString()
                        if (!seenUris.contains(uriStr)) {
                            seenUris.add(uriStr)
                            val ringtone = RingtoneManager.getRingtone(context, uri)
                            val title = try {
                                ringtone?.getTitle(context) ?: "Системный звук"
                            } catch (e: Exception) {
                                "Системный звук"
                            }
                            sounds.add(
                                SoundItem(
                                    id = "device:$uriStr",
                                    title = title,
                                    group = groupName,
                                    source = SoundSource.DEVICE,
                                    uriString = uriStr
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error querying system sounds for type $type", e)
            }
        }
        return sounds
    }

    /**
     * Resolves human-readable file name from ContentResolver for a custom URI.
     */
    fun queryDisplayName(context: Context, uri: Uri): String {
        var name = "Моя мелодия"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val displayName = it.getString(nameIndex)
                        if (!displayName.isNullOrBlank()) {
                            name = displayName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not query display name for $uri", e)
        }
        return name
    }

    /**
     * Checks if a Content URI is accessible (not deleted or revoked).
     */
    fun isUriAccessible(context: Context, uriString: String): Boolean {
        return try {
            val uri = Uri.parse(uriString)
            val stream = context.contentResolver.openInputStream(uri)
            if (stream != null) {
                stream.close()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
