package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationEntry(
    val id: String,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MayaNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isListening.value = true
        instance = this
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isListening.value = false
        if (instance == this) instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return
        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val pkg = sbn.packageName ?: ""

        if (title.isNotBlank() || text.isNotBlank()) {
            val entry = NotificationEntry(
                id = "${sbn.id}_${sbn.postTime}",
                packageName = pkg,
                title = title,
                text = text,
                timestamp = sbn.postTime
            )
            val current = _recentNotifications.value.toMutableList()
            current.add(0, entry)
            _recentNotifications.value = current.take(30)
        }
    }

    companion object {
        var instance: MayaNotificationListenerService? = null
            private set

        private val _isListening = MutableStateFlow(false)
        val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

        private val _recentNotifications = MutableStateFlow<List<NotificationEntry>>(emptyList())
        val recentNotifications: StateFlow<List<NotificationEntry>> = _recentNotifications.asStateFlow()
    }
}
