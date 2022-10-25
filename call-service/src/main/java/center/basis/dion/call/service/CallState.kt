package center.basis.dion.call.service

sealed interface CallState {
    object Inactive: CallState
    data class Ringing(val info: IncomingCallInfo): CallState
    sealed interface ProcessInternal: CallState {
        object Accepted: ProcessInternal
        object Dismissed: ProcessInternal
    }
}