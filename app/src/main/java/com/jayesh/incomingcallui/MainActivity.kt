package com.jayesh.incomingcallui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jayesh.incomingcallui.ui.call.CallScreen
import com.jayesh.incomingcallui.ui.theme.IncomingCallUiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IncomingCallUiTheme {
                CallScreen()
            }
        }
    }
}