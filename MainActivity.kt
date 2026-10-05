package com.example.noled

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("p", 0) }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 48, 32, 32) }
        setContentView(ScrollView(this).apply { addView(col) })

        col.addView(Button(this).apply {
            text = "1. Autoriser l'accès aux notifications"
            setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        })
        col.addView(Button(this).apply {
            text = "2. Autoriser l'affichage par-dessus les autres applis"
            setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }
        })

        val durLabel = TextView(this)
        fun setLabel(v: Int) { durLabel.text = "Durée d'affichage : $v s" }
        setLabel(prefs.getInt("dur", 10))
        col.addView(durLabel)
        col.addView(SeekBar(this).apply {
            max = 57; progress = prefs.getInt("dur", 10) - 3
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) { prefs.edit().putInt("dur", p + 3).apply(); setLabel(p + 3) }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        col.addView(TextView(this).apply { text = "\nApplications à surveiller :" })
        val selected = prefs.getStringSet("apps", emptySet())!!.toMutableSet()
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        packageManager.queryIntentActivities(launcher, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
            .forEach { ri ->
                val pkg = ri.activityInfo.packageName
                col.addView(CheckBox(this).apply {
                    text = ri.loadLabel(packageManager)
                    setCompoundDrawablesWithIntrinsicBounds(ri.loadIcon(packageManager).apply { setBounds(0, 0, 96, 96) }, null, null, null)
                    compoundDrawablePadding = 24
                    isChecked = pkg in selected
                    setOnCheckedChangeListener { _, c ->
                        if (c) selected.add(pkg) else selected.remove(pkg)
                        prefs.edit().putStringSet("apps", selected.toSet()).apply()
                    }
                })
            }
    }
}
