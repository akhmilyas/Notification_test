package center.basis.dion.call.service

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IncomingCallInfo(
    val name: String,
    val id: String,
) : Parcelable