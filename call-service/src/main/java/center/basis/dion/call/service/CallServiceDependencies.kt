package center.basis.dion.call.service

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat

interface CallServiceDependencies {
    fun getIncomingCallPendingIntent(context: Context): PendingIntent
    fun getAcceptCallPendingIntent(context: Context): PendingIntent
}

fun CallServiceDependencies.getAcceptCallAction(context: Context) = NotificationCompat.Action.Builder(
    R.drawable.ic_phone,
    context.getString(R.string.text_accept_call_notification),
    getAcceptCallPendingIntent(context)
).build()