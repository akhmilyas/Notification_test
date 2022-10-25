package center.basis.dion.call.service

import android.content.Context

sealed interface CallAction {
    class ShowIncomingNotification(val info: IncomingCallInfo, val context: Context) : CallAction
    object AcceptCall : CallAction
    object DismissCall : CallAction
}