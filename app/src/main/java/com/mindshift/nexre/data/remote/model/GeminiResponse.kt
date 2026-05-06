package com.mindshift.nexre.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?,
    val error: GeminiError?,
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?,
    @Json(name = "finishReason") val finishReason: String?,
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String,
)

@JsonClass(generateAdapter = true)
data class GeminiSummaryResult(
    val summary: String,
    val tags: List<String>,
)
