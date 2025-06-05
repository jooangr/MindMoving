package com.example.mindmoving.neuroSkyService

interface NeuroSkyListener {
    fun onAttentionReceived(level: Int)
    fun onBlinkDetected(strength: Int)
    fun onMeditationReceived(level: Int)
    fun onSignalPoor(signal: Int)
    fun onStateChanged(state: Int)
}