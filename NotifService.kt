package com.example.noled

import android.content.Intent
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifService : NotificationListenerService() {
    companion object { val pending = LinkedHashSet<String>() }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val apps = getSharedPreferences("p", 0).getStringSet("apps", emptySet())!!
        if (sbn.packageName !in apps || sbn.isOngoing) return
        pending.add(sbn.packageName)
        if (getSystemService(PowerManager::class.java).isInteractive) return // écran déjà allumé
        startActivity(Intent(this, GlowActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (activeNotifications.none { it.packageName == sbn.packageName }) pending.remove(sbn.packageName)
    }
}
