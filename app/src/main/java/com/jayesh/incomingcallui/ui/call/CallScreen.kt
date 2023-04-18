package com.jayesh.incomingcallui.ui.call

import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jayesh.incomingcallui.util.Utilities

@Composable
fun CallScreen() {
    val context = LocalContext.current
    val vibrator = remember(key1 = context) {
        Utilities.getVibrator(context)
    }
    val vibrateOnCallAcceptReject = remember(key1 = vibrator) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(50, 1)
                vibrator.cancel()
                vibrator.vibrate(vibrationEffect)
            } else {
                TODO("handle older devices")
            }
        }
    }

    val incomingCallNotPickedState = remember {
        CallState.NotPicked(
            name = "Sophia",
            number = "+1 2210521849",
            country = "US",
            avatarUrl = Uri.EMPTY,
        )
    }

    var incomingCallState by remember {
        mutableStateOf<CallState>(incomingCallNotPickedState)
    }

    when (incomingCallState) {
        is CallState.NotPicked -> {
            IncomingCallScreen(
                name = (incomingCallState as CallState.NotPicked).name,
                number = (incomingCallState as CallState.NotPicked).number,
                country = (incomingCallState as CallState.NotPicked).country,
                avatarUrl = (incomingCallState as CallState.NotPicked).avatarUrl,
                onCallAccept = {
                    incomingCallState = CallState.Picked
                    vibrateOnCallAcceptReject()
                },
                onCallReject = {
                    incomingCallState = CallState.Rejected
                    vibrateOnCallAcceptReject()
                }
            )
        }

        CallState.Picked -> {
            BackHandler {
                incomingCallState = incomingCallNotPickedState
            }
            Surface(color = Color.Green) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Call accepted",
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }

        CallState.Rejected -> {
            BackHandler {
                incomingCallState = incomingCallNotPickedState
            }
            Surface(color = Color.Red) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Call rejected",
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }
    }
}