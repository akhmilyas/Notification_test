package center.basis.dion.call.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var notificationManager: DeviceNotificationManager
    @Inject
    lateinit var audioManager: IncomingCallAudioManager
    @Inject
    lateinit var callServiceDependencies: CallServiceDependencies

    private val binder by lazy { CallBinder() }

    private val _state = MutableStateFlow<CallState>(CallState.Inactive)

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val info: IncomingCallInfo? = intent?.getParcelableExtra(CALL_INFO_KEY)
        if (info != null) {
            showIncomingCall(info)
        }
        return START_NOT_STICKY
    }

    private fun showIncomingCall(info: IncomingCallInfo) {
        val channelId = notificationManager.getCallNotificationChannel(
            channelNameRes = R.string.incoming_call_notification_channel,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(info.name)
            .setContentText(getString(R.string.text_notification_content_title_conference_with_name, info.name))
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(callServiceDependencies.getIncomingCallPendingIntent(this))
            .addAction(CallBroadcastReceiver.getDismissAction(this))
            .addAction(callServiceDependencies.getAcceptCallAction(this))
            .setSmallIcon(R.drawable.ic_phone)
            .setFullScreenIntent(callServiceDependencies.getIncomingCallPendingIntent(this), true)
            .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
            .build()

        notificationManager.showNotification(
            INCOMING_CALL_NOTIFICATION_ID,
            notification
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                INCOMING_CALL_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(
                INCOMING_CALL_NOTIFICATION_ID,
                notification
            )
        }
        startAudioAndVibrate()
        _state.update {
            CallState.Ringing(info)
        }
    }

    private fun startAudioAndVibrate() {
        audioManager.startRinging()
    }

    private fun stop() {
        stopAudioAndVibrate()
        stopSelf()
    }

    private fun stopAudioAndVibrate() {
        audioManager.stopRinging()
    }

    private fun dismissCall() {
        _state.update { CallState.ProcessInternal.Dismissed }
        stop()
    }

    private fun acceptCall() {
        _state.update { CallState.ProcessInternal.Accepted }
        Log.d("CallService", "acceptCall, activity already started from pending intent")
        stop()
    }

    inner class CallBinder : Binder() {
        val state = _state.asStateFlow()

        fun acceptCall() {
            this@CallService.acceptCall()
        }

        fun dismissCall() {
            this@CallService.dismissCall()
        }
    }

    companion object {
        const val CALL_INFO_KEY = "call_info"
        const val INCOMING_CALL_NOTIFICATION_ID = 300
    }

}