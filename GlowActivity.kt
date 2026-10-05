package com.example.noled

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout

class GlowActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())
    private lateinit var row: LinearLayout

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setShowWhenLocked(true); setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply { screenBrightness = 0.05f }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        row = LinearLayout(this).apply { gravity = Gravity.CENTER }
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK); setOnClickListener { finish() } }
        root.addView(row, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        render()
    }

    override fun onNewIntent(i: Intent) { super.onNewIntent(i); render() }

    private fun render() {
        row.removeAllViews()
        val size = (72 * resources.displayMetrics.density).toInt()
        NotifService.pending.toList().forEach { pkg ->
            val icon = try { packageManager.getApplicationIcon(pkg) } catch (e: Exception) { return@forEach }
            row.addView(ImageView(this).apply {
                setImageDrawable(icon)
                setOnClickListener { open(pkg) }
            }, LinearLayout.LayoutParams(size, size).apply { setMargins(size / 3, 0, size / 3, 0) })
        }
        val dur = getSharedPreferences("p", 0).getInt("dur", 10)
        h.removeCallbacksAndMessages(null)
        h.postDelayed({ finish() }, dur * 1000L)
    }

    private fun open(pkg: String) {
        val launch = packageManager.getLaunchIntentForPackage(pkg) ?: return
        val go = { startActivity(launch); NotifService.pending.remove(pkg); finish() }
        val km = getSystemService(KeyguardManager::class.java)
        if (km.isKeyguardLocked) km.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
            override fun onDismissSucceeded() { go() }
        }) else go()
    }

    override fun onDestroy() { h.removeCallbacksAndMessages(null); super.onDestroy() }
}
