package com.example.simplepac.data

import com.example.simplepac.network.FlashcardApiService
import com.example.simplepac.network.FlashcardRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFlashcardApiService(): FlashcardApiService {
        return FlashcardApiService.create()
    }

    @Provides
    @Singleton
    fun provideFlashcardRepository(apiService: FlashcardApiService): FlashcardRepository {
        return FlashcardRepository(apiService)
    }
}