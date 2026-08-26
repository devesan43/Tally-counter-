package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator

object SoundHelper {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (_: Exception) {
            toneGen = null
        }
    }

    fun playClick() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        } catch (_: Exception) {
            // Ignore if audio is unavailable
        }
    }

    fun playMilestone() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
        } catch (_: Exception) {
            // Ignore if audio is unavailable
        }
    }
}
