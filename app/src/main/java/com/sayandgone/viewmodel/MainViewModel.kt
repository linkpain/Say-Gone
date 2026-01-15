package com.sayandgone.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    private val _emotionName = MutableStateFlow("")
    val emotionName: StateFlow<String> = _emotionName

    private val _isDestroyed = MutableStateFlow(false)
    val isDestroyed: StateFlow<Boolean> = _isDestroyed

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo

    private val _endingPhrase = MutableStateFlow("")
    val endingPhrase: StateFlow<String> = _endingPhrase

    private val _isReleasing = MutableStateFlow(false)
    val isReleasing: StateFlow<Boolean> = _isReleasing

    private val _clickCount = MutableStateFlow(0)
    val clickCount: StateFlow<Int> = _clickCount

    private val _releaseIntensity = MutableStateFlow(ReleaseIntensity.LEVEL_1)
    val releaseIntensity: StateFlow<ReleaseIntensity> = _releaseIntensity

    private var savedText = ""
    private var savedEmotionName = ""

    enum class ReleaseIntensity {
        LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5
    }

    fun updateText(text: String) {
        _inputText.value = text
    }

    fun updateEmotionName(name: String) {
        _emotionName.value = name
    }

    fun saveText() {
        savedText = _inputText.value
        savedEmotionName = _emotionName.value
    }

    fun destroy() {
        _isDestroyed.value = true
        _canUndo.value = true
    }

    fun undo() {
        _inputText.value = savedText
        _emotionName.value = savedEmotionName
        _isDestroyed.value = false
        _canUndo.value = false
        savedText = ""
        savedEmotionName = ""
    }

    fun finalize() {
        _canUndo.value = false
        savedText = ""
        savedEmotionName = ""
    }

    fun setEndingPhrase(phrase: String) {
        _endingPhrase.value = phrase
    }

    fun startReleasing() {
        _isReleasing.value = true
        _clickCount.value = 0
        _releaseIntensity.value = ReleaseIntensity.LEVEL_1
    }

    fun incrementClick() {
        _clickCount.value = _clickCount.value + 1
        updateReleaseIntensity()
    }

    private fun updateReleaseIntensity() {
        val count = _clickCount.value
        _releaseIntensity.value = when {
            count <= 20 -> ReleaseIntensity.LEVEL_1
            count <= 50 -> ReleaseIntensity.LEVEL_2
            count <= 100 -> ReleaseIntensity.LEVEL_3
            count <= 150 -> ReleaseIntensity.LEVEL_4
            else -> ReleaseIntensity.LEVEL_5
        }
    }

    fun reset() {
        _inputText.value = ""
        _emotionName.value = ""
        _isDestroyed.value = false
        _canUndo.value = false
        _endingPhrase.value = ""
        _isReleasing.value = false
        _clickCount.value = 0
        _releaseIntensity.value = ReleaseIntensity.LEVEL_1
        savedText = ""
        savedEmotionName = ""
    }
}
