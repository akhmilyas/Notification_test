package center.basis.dion.call.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var callInteractor: CallInteractor

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleNow(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun handleNow(data: MutableMap<String, String>) {
        val name = data["name"]
        val id = data["id"]
        if (name != null && id != null) {
            callInteractor.onAction(CallAction.ShowIncomingNotification(IncomingCallInfo(name, id), this))
        }
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
        // TODO
    }

    companion object {
        private const val TAG = "FCMService"
    }
}