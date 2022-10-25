package center.basis.dion.call.service

import android.app.Service
import android.content.Context
import android.media.*
import android.net.Uri
import android.os.Vibrator
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class IncomingCallAudioManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var player: MediaPlayer? = null

    fun startRinging() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val needRing = am.ringerMode != AudioManager.RINGER_MODE_SILENT
        if (needRing) {
            val ringtonePlayer = MediaPlayer()
            player = ringtonePlayer
            ringtonePlayer.setOnPreparedListener { mediaPlayer: MediaPlayer ->
                try {
                    mediaPlayer.start()
                } catch (e: Throwable) {
                    // TODO ErrorHandler.handle
                }
            }
            ringtonePlayer.isLooping = true
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setLegacyStreamType(AudioManager.STREAM_RING)
                .build()
            ringtonePlayer.setAudioAttributes(audioAttributes)
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .build()
            am.requestAudioFocus(focusRequest)
            try {
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ringtonePlayer.setDataSource(context, notificationUri)
                ringtonePlayer.prepareAsync()
            } catch (e: Exception) {
                ringtonePlayer.release()
                player = null
            }
            if (am.ringerMode == AudioManager.RINGER_MODE_VIBRATE || am.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                val vibrator = context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(longArrayOf(0, 700, 500), 0)
            }
        }
    }

    fun stopRinging() {
        player.stopSafely()
        player = null
        (context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator).cancel()
    }

    private fun MediaPlayer?.stopSafely() {
        if (this != null) {
            try {
                this.stop()
                this.release()
            } catch (e: Exception) {
                // TODO handle
            }
        }
    }
}