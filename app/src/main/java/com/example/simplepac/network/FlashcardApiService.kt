package com.example.simplepac.network

import com.example.simplepac.data.Flashcard
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface FlashcardApiService {
    @GET("server/api/card")
    suspend fun getFlashcards(): List<Flashcard>

    companion object {
        fun create(): FlashcardApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://simplepacmind-60036155049.development.catalystserverless.in/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build()
                )
                .build()
            return retrofit.create(FlashcardApiService::class.java)
        }
    }

}