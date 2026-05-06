package com.mindshift.nexre.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig = GenerationConfig(),
    @Json(name = "systemInstruction") val systemInstruction: SystemInstruction? = null,
)

@JsonClass(generateAdapter = true)
data class SystemInstruction(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String = "application/json",
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int = 1024,
)
