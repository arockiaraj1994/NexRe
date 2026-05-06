package com.mindshift.nexre.domain.usecase

import com.mindshift.nexre.data.remote.GeminiApiService
import javax.inject.Inject

class ValidateGeminiKeyUseCase @Inject constructor(private val geminiApiService: GeminiApiService) {

    suspend operator fun invoke(apiKey: String): Boolean = try {
        val response = geminiApiService.listModels(apiKey.trim())
        response.isSuccessful
    } catch (e: Exception) {
        false
    }
}
