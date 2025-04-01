package com.example.simplepac.data

import com.google.gson.annotations.SerializedName

data class Flashcard(
    @SerializedName("image") val imageUrl: String?,
    @SerializedName("question") val question: String,
    @SerializedName("answer") val answer: String,
    @SerializedName("audio") val audioUrl: String?
)