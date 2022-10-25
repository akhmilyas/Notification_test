package center.basis.dion.call.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CallInteractor(
    private val appContext: Context
) {
    private var binder: CallService.CallBinder? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            Log.d("CallInteractor", "CallService connected")
            binder = service as CallService.CallBinder
            collectFlow()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("CallInteractor", "CallService disconnected")
            stopCollectFlow()
            binder = null
        }
    }

    private val _state = MutableSharedFlow<CallState>(replay = 1, extraBufferCapacity = 10)
    val state = _state.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var collectingJob: Job? = null

    fun onAction(action: CallAction) {
        scope.launch {
            when (action) {
                is CallAction.ShowIncomingNotification -> startService(action)
                CallAction.AcceptCall -> processActionWithBinder { acceptCall() }
                CallAction.DismissCall -> processActionWithBinder { dismissCall() }
            }
        }
    }

    private suspend fun processActionWithBinder(fn: CallService.CallBinder.() -> Unit) {
        val binder = this.binder
        if (binder != null) {
            fn(binder)
        } else {
            _state.emit(CallState.Inactive)
        }
    }

    private fun startService(action: CallAction.ShowIncomingNotification) {
        scope.launch {
//            delay(5000) //TODO для проверки ограничения на 5 секунд
            val info = action.info
            val context = action.context
            val serviceIntent = Intent(context, CallService::class.java)
            val startIntent = serviceIntent.apply {
                putExtra(CallService.CALL_INFO_KEY, info)
            }
            withContext(Dispatchers.Main) {
                context.startForegroundService(startIntent)
                appContext.bindService(
                    serviceIntent,
                    serviceConnection,
                    Context.BIND_ABOVE_CLIENT
                )
            }
        }
    }

    private fun collectFlow() {
        collectingJob = scope.launch {
            binder?.state?.collect {
                _state.emit(it)
            }
        }
    }

    private fun stopCollectFlow() {
        collectingJob?.cancel()
        collectingJob = null
    }
}

