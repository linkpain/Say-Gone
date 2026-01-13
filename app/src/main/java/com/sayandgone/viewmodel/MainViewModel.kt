package com.sayandgone.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    private val _isDestroyed = MutableStateFlow(false)
    val isDestroyed: StateFlow<Boolean> = _isDestroyed

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo

    private val _endingPhrase = MutableStateFlow("")
    val endingPhrase: StateFlow<String> = _endingPhrase

    private var savedText = ""

    fun updateText(text: String) {
        _inputText.value = text
    }

    fun saveText() {
        savedText = _inputText.value
    }

    fun destroy() {
        _isDestroyed.value = true
        _canUndo.value = true
    }

    fun undo() {
        _inputText.value = savedText
        _isDestroyed.value = false
        _canUndo.value = false
        savedText = ""
    }

    fun finalize() {
        _canUndo.value = false
        savedText = ""
    }

    fun setEndingPhrase(phrase: String) {
        _endingPhrase.value = phrase
    }

    fun reset() {
        _inputText.value = ""
        _isDestroyed.value = false
        _canUndo.value = false
        _endingPhrase.value = ""
        savedText = ""
    }
}
