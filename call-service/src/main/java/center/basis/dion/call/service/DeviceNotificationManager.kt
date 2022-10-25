package center.basis.dion.call.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject
import kotlin.random.Random

@ServiceScoped
class DeviceNotificationManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel(channelId: String, channelNameRes: Int) {
        val channel = NotificationChannel(
            channelId,
            appContext.getString(channelNameRes),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}