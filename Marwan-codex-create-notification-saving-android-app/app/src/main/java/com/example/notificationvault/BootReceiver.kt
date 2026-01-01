package com.example.notificationvault

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val serviceIntent = Intent(context, KeepAliveService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
