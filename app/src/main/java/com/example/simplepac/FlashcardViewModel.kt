package com.example.simplepac

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplepac.data.Flashcard
import com.example.simplepac.network.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {
    private val _state = mutableStateOf(FlashcardState())
    val state: State<FlashcardState> = _state

    init {
        loadFlashcards()
    }

    fun loadFlashcards() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            repository.getFlashcards().fold(
                onSuccess = { flashcards ->
                    _state.value = _state.value.copy(
                        flashcards = flashcards,
                        isLoading = false,
                        currentCardIndex = if (flashcards.isNotEmpty()) 0 else -1
                    )
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load flashcards"
                    )
                }
            )
        }
    }

    fun nextCard() {
        val currentIndex = _state.value.currentCardIndex
        if (currentIndex < _state.value.flashcards.size - 1) {
            _state.value = _state.value.copy(
                currentCardIndex = currentIndex + 1,
                isCardFlipped = false
            )
        }
    }

    fun previousCard() {
        val currentIndex = _state.value.currentCardIndex
        if (currentIndex > 0) {
            _state.value = _state.value.copy(
                currentCardIndex = currentIndex - 1,
                isCardFlipped = false
            )
        }
    }

    fun toggleCardFlip() {
        _state.value = _state.value.copy(
            isCardFlipped = !_state.value.isCardFlipped
        )
    }

    data class FlashcardState(
        val flashcards: List<Flashcard> = emptyList(),
        val currentCardIndex: Int = -1,
        val isCardFlipped: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}