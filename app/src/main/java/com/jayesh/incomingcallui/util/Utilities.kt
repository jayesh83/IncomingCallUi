package com.jayesh.incomingcallui.util

import android.content.Context
import android.os.Build
import android.os.Vibrator

object Utilities {
    fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(Vibrator::class.java)
        } else {
            context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}