package com.akhmilyas.test.notification

import android.app.Activity
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import center.basis.dion.call.service.CallAction
import center.basis.dion.call.service.CallInteractor
import center.basis.dion.call.service.CallServiceDependencies
import center.basis.dion.call.service.CallState
import com.akhmilyas.test.notification.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var callInteractor: CallInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processIntent(intent)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fcmKey.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Текст скопирован", binding.fcmKey.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Ключ скопирован", Toast.LENGTH_LONG).show()
        }

        binding.accept.setOnClickListener {
            callInteractor.onAction(CallAction.AcceptCall)
        }
        binding.dismiss.setOnClickListener {
            callInteractor.onAction(CallAction.DismissCall)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                callInteractor.state.collectLatest {
                    processState(it)
                }
            }
        }
        handleToken()
    }

    private fun handleToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, token)
            binding.fcmKey.text = token
        }
    }

    private fun processState(state: CallState) {
        val text: String
        var isButtonsVisible = false
        when (state) {
            is CallState.Ringing -> {
                text = "Идет звонок входящий звонок от ${state.info.name}"
                isButtonsVisible = true
            }
            CallState.ProcessInternal.Accepted -> {
                text = "Звонок принят, заходим в конференцию"
            }
            CallState.ProcessInternal.Dismissed -> {
                disableScreenOnAndKeyguardOff()
                text = "Звонок отменен"
            }
            CallState.Inactive -> {
                disableScreenOnAndKeyguardOff()
                text = "Ничего не происходит"
            }
        }
        changeButtons(isButtonsVisible)
        binding.mainLabelText.text = text
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent?) {
        when (intent?.action) {
            INCOMING_CALL_ACCEPT -> callInteractor.onAction(CallAction.AcceptCall)
            INCOMING_CALL_RINGING -> turnScreenOnAndKeyguardOff()
        }
    }

    private fun changeButtons(isVisible: Boolean) {
        binding.accept.isVisible = isVisible
        binding.dismiss.isVisible = isVisible
    }

    companion object {

        private const val INCOMING_CALL_RINGING = "center.basis.dion.incoming.call.action"
        private const val INCOMING_CALL_ACCEPT = "center.basis.dion.incoming.call.accept"
        private val TAG: String = "MainActivity"

        private fun getIntent(context: Context, action: String) = Intent(context, MainActivity::class.java).apply {
            this.action = action
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        fun getIncomingCallPendingIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context,
            12,
            getIntent(context, INCOMING_CALL_RINGING),
            PendingIntent.FLAG_IMMUTABLE
        )

        fun getAcceptCallIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context,
            13,
            getIntent(context, INCOMING_CALL_ACCEPT),
            PendingIntent.FLAG_IMMUTABLE,
        )

    }
}

class CallServiceDependenciesImpl : CallServiceDependencies {
    override fun getIncomingCallPendingIntent(context: Context): PendingIntent =
        MainActivity.getIncomingCallPendingIntent(context)

    override fun getAcceptCallPendingIntent(context: Context): PendingIntent =
        MainActivity.getAcceptCallIntent(context)
}

/**
 * @author https://medium.com/android-news/full-screen-intent-notifications-android-85ea2f5b5dc1
 * @see [link](https://medium.com/android-news/full-screen-intent-notifications-android-85ea2f5b5dc1)
 */
fun Activity.turnScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    } else {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }

    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
        requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
    }
}

fun Activity.disableScreenOnAndKeyguardOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(false)
        setTurnScreenOn(false)
    } else {
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }
}