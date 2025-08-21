package com.viz.prodzen.ui.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusSessionViewModel @Inject constructor() : ViewModel() {

    private var timerJob: Job? = null

    private val _durationMinutes = MutableStateFlow(25f)
    val durationMinutes = _durationMinutes.asStateFlow()

    private val _timeRemaining = MutableStateFlow("25:00")
    val timeRemaining = _timeRemaining.asStateFlow()

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()

    private val _progress = MutableStateFlow(1f)
    val progress = _progress.asStateFlow()

    companion object {
        // FIXED: Changed from val to var to allow reassignment.
        var isSessionActive = MutableStateFlow(false)
    }

    fun setDuration(minutes: Float) {
        if (!_isSessionActive.value) {
            _durationMinutes.value = minutes
            _timeRemaining.value = String.format("%02d:00", minutes.toInt())
            _progress.value = 1f
        }
    }

    fun startSession() {
        _isSessionActive.value = true

//        isSessionActive.value = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val totalSeconds = _durationMinutes.value.toInt() * 60
            for (remainingSeconds in totalSeconds downTo 0) {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                _timeRemaining.value = String.format("%02d:%02d", minutes, seconds)
                _progress.value = remainingSeconds.toFloat() / totalSeconds.toFloat()
                delay(1000)
            }
            stopSession()
        }
    }

    fun stopSession() {
        timerJob?.cancel()
        _isSessionActive.value = false
//        isSessionActive.value = false
        setDuration(_durationMinutes.value)
    }

    override fun onCleared() {
        super.onCleared()
        stopSession()
    }
}
