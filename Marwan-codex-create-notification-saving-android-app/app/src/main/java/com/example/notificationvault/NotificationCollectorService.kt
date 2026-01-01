package com.example.notificationvault

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationCollectorService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
            ?: extras.getCharSequence("android.bigText")?.toString()
        val raw = extras.keySet().joinToString(
            prefix = "{",
            postfix = "}",
            separator = ", "
        ) { key -> "$key=${extras.get(key)}" }

        NotificationStore(applicationContext).insertNotification(
            packageName = sbn.packageName,
            title = title,
            text = text,
            postedAt = sbn.postTime,
            raw = raw
        )
    }
}
