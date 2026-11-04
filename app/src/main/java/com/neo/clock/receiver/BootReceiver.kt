package com.neo.clock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.neo.clock.data.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "Received broadcast: ${intent?.action}. Rescheduling all active alarms.")
        val repository = AlarmRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.rescheduleAllActive()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule alarms on boot", e)
            }
        }
    }
}
