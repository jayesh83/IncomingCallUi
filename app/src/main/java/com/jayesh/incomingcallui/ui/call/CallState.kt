package com.jayesh.incomingcallui.ui.call

import android.net.Uri

sealed class CallState {
    class NotPicked(
        val name: String,
        val number: String,
        val country: String,
        val avatarUrl: Uri
    ) : CallState()

    object Picked : CallState()

    object Rejected : CallState()
}
