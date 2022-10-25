package center.basis.dion.call.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var callInteractor: CallInteractor

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            DISMISS_ACTION -> callInteractor.onAction(CallAction.DismissCall)
        }
    }

    companion object {
        fun getDismissAction(context: Context) = NotificationCompat.Action.Builder(
            R.drawable.ic_phone_disabled,
            context.getString(R.string.text_dismiss_call_notification),
            PendingIntent.getBroadcast(
                context,
                13,
                getDismissIntent(context),
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()

        private fun getCommonIntent(context: Context) = Intent(context, CallBroadcastReceiver::class.java)

        private fun getDismissIntent(context: Context) = getCommonIntent(context).apply { action = DISMISS_ACTION }

        private const val DISMISS_ACTION = "center.basis.dion.call.receiver.dismiss"
    }
}