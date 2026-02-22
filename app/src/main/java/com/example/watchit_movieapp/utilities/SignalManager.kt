package com.example.watchit_movieapp.utilities

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import java.lang.ref.WeakReference

/**
 * class for the toast managing
 */
class SignalManager private constructor(context: Context) {
    private val contextRef = WeakReference(context)

    enum class ToastLength(val length: Int) {
        SHORT(Toast.LENGTH_SHORT),
        LONG(Toast.LENGTH_LONG)
    }

    companion object {
        @Volatile
        private var instance: SignalManager? = null
        fun init(context: Context): SignalManager {
            return instance ?: synchronized(this) {
                instance
                    ?: SignalManager(context).also { instance = it }
            }
        }

        fun getInstance(): SignalManager {
            return instance ?: throw IllegalStateException(
                "SignalManager must be initialized by calling init(context) before use."
            )

        }
    }

    fun toast(text: String, duration: ToastLength) {
        contextRef.get()?.let { context ->
            Toast
                .makeText(
                    context,
                    text,
                    duration.ordinal
                )
                .show()
        }
    }

    fun toastFavoriteStatus(context: Context, itemName: String, isFavorite: Boolean) {
        val message = if (isFavorite) "$itemName added to favorites" else "$itemName removed"
        toast(message, ToastLength.SHORT)
    }

    fun vibrate() {
        contextRef.get()?.let { context ->
            val vibrator: Vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(
                            Context.VIBRATOR_MANAGER_SERVICE
                        ) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    context.getSystemService(
                        Context.VIBRATOR_SERVICE
                    ) as Vibrator
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val oneShotVibrationEffect =
                    VibrationEffect
                        .createOneShot(
                            50,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )

                vibrator.vibrate(oneShotVibrationEffect)}
            else{
                vibrator.vibrate(50)
            }
        }

    }


}