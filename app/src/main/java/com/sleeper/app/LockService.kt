package com.sleeper.app

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class LockService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())

    private val lockRunnable = Runnable {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> handler.removeCallbacks(lockRunnable)
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> reschedule()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(receiver, filter)
        reschedule()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        reschedule()
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacks(lockRunnable)
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
        }
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun reschedule() {
        handler.removeCallbacks(lockRunnable)
        val prefs = getSharedPreferences("sleeper", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        val sec = prefs.getInt("seconds", 30).coerceAtLeast(1)
        handler.postDelayed(lockRunnable, sec * 1000L)
    }

    companion object {
        var instance: LockService? = null
    }
}
