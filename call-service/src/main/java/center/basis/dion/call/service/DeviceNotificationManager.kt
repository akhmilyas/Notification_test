package center.basis.dion.call.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class DeviceNotificationManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val sharedPrefs = appContext.getSharedPreferences(CALL_PREFS, Context.MODE_PRIVATE)

    /**
     * @return ChannelId
     */
    fun getCallNotificationChannel(channelNameRes: Int): String {
        var id = sharedPrefs.getInt(CALL_CHANNEL, 300)
        val existedChannel = notificationManager.getNotificationChannel(id.toString())
        if (
            existedChannel != null && (existedChannel.importance < NotificationManager.IMPORTANCE_HIGH ||
                    existedChannel.shouldVibrate() ||
                    existedChannel.sound != null ||
                    existedChannel.vibrationPattern != null)
        ) {
            notificationManager.deleteNotificationChannel(id.toString())
            id++
            sharedPrefs.edit { putInt(CALL_CHANNEL, id) }
        }
        val channel = NotificationChannel(
            id.toString(),
            appContext.getString(channelNameRes),
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.setSound(null, null)
        channel.enableVibration(false)
        channel.enableLights(false)
        channel.setBypassDnd(true)
        notificationManager.createNotificationChannel(channel)
        return id.toString()
    }

    fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    companion object {
        private const val CALL_PREFS = "CALL_SERVICE_NOTIFICATION_CHANNEL"
        private const val CALL_CHANNEL = "call_channel"
    }
}