package com.example.simplepac.network

import com.example.simplepac.data.Flashcard
import javax.inject.Inject

class FlashcardRepository @Inject constructor(
    private val apiService: FlashcardApiService
) {
    suspend fun getFlashcards(): Result<List<Flashcard>> {
        return try {
            val flashcards = apiService.getFlashcards()
            Result.success(flashcards)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}